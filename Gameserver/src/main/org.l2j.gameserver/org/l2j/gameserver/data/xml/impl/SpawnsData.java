package org.l2j.gameserver.data.xml.impl;

import org.l2j.commons.xml.XmlReader;
import org.l2j.gameserver.Config;
import org.l2j.gameserver.model.ChanceLocation;
import org.l2j.gameserver.model.StatsSet;
import org.l2j.gameserver.model.actor.templates.NpcTemplate;
import org.l2j.gameserver.model.holders.MinionHolder;
import org.l2j.gameserver.model.interfaces.IParameterized;
import org.l2j.gameserver.model.interfaces.ITerritorized;
import org.l2j.gameserver.model.spawns.NpcSpawnTemplate;
import org.l2j.gameserver.model.spawns.SpawnGroup;
import org.l2j.gameserver.model.spawns.SpawnTemplate;
import org.l2j.gameserver.world.zone.form.ZoneNPoly;
import org.l2j.gameserver.world.zone.type.BannedSpawnTerritory;
import org.l2j.gameserver.world.zone.type.SpawnTerritory;
import org.l2j.gameserver.settings.ServerSettings;
import org.l2j.gameserver.util.GameXmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.l2j.commons.configuration.Configurator.getSettings;

/**
 * @author UnAfraid
 */
public class SpawnsData extends GameXmlReader {
    protected static final Logger LOGGER = LoggerFactory.getLogger(SpawnsData.class);

    private final List<SpawnTemplate> spawns = new LinkedList<>();

    private SpawnsData() {
        load();
    }

    @Override
    protected Path getSchemaFilePath() {
        return getSettings(ServerSettings.class).dataPackDirectory().resolve("data/xsd/spawns.xsd");
    }

    public void init() {
        if (Config.ALT_DEV_NO_SPAWNS) {
            return;
        }

        LOGGER.info("Initializing spawns...");
        spawns.parallelStream().filter(SpawnTemplate::isSpawningByDefault).forEach(template -> {
            template.spawnAll(null);
            template.notifyActivate();
        });
        LOGGER.info("All spawns has been initialized!");
    }

    @Override
    public void load() {
        parseDatapackDirectory("data/spawns", true);
        LOGGER.info("Loaded spawns");
    }

    @Override
    public void parseDocument(Document doc, File f) {
        forEach(doc, "list", listNode -> forEach(listNode, "spawn", spawnNode ->
        {
            try {
                parseSpawn(spawnNode, f, spawns);
            } catch (Exception e) {
                LOGGER.warn("Error while processing spawn in file " + f.getAbsolutePath(), e);
            }
        }));
    }

    public List<SpawnTemplate> getSpawns() {
        return spawns;
    }

    public List<SpawnTemplate> getSpawns(Predicate<SpawnTemplate> condition) {
        return spawns.stream().filter(condition).collect(Collectors.toList());
    }

    public List<NpcSpawnTemplate> getNpcSpawns(Predicate<NpcSpawnTemplate> condition) {
        return spawns.stream().flatMap(template -> template.getGroups().stream()).flatMap(group -> group.getSpawns().stream()).filter(condition).collect(Collectors.toList());
    }

    public void parseSpawn(Node spawnsNode, File file, List<SpawnTemplate> spawns) {
        final SpawnTemplate spawnTemplate = new SpawnTemplate(new StatsSet(parseAttributes(spawnsNode)), file.getAbsolutePath());
        SpawnGroup defaultGroup = null;

        for (Node innerNode = spawnsNode.getFirstChild(); innerNode != null; innerNode = innerNode.getNextSibling()) {
            if ("territories".equalsIgnoreCase(innerNode.getNodeName())) {
                parseTerritories(innerNode, file.getName(), spawnTemplate);
            } else if ("group".equalsIgnoreCase(innerNode.getNodeName())) {
                parseGroup(innerNode, spawnTemplate);
            } else if ("npc".equalsIgnoreCase(innerNode.getNodeName())) {
                if (isNull(defaultGroup)) {
                    defaultGroup = new SpawnGroup(StatsSet.EMPTY_STATSET);
                }
                parseNpc(innerNode, spawnTemplate, defaultGroup);
            } else if ("parameters".equalsIgnoreCase(innerNode.getNodeName())) {
                parseParameters(spawnsNode, spawnTemplate);
            }
        }

        // One static group for all npcs outside group scope
        if (defaultGroup != null) {
            spawnTemplate.addGroup(defaultGroup);
        }

        spawns.add(spawnTemplate);
    }

    private void parseTerritories(Node innerNode, String fileName, ITerritorized spawnTemplate) {
        forEach(innerNode, XmlReader::isNode, territoryNode ->
        {
            final String name = parseString(territoryNode.getAttributes(), "name", fileName + "_" + (spawnTemplate.getTerritories().size() + 1));
            final int minZ = parseInteger(territoryNode.getAttributes(), "minZ");
            final int maxZ = parseInteger(territoryNode.getAttributes(), "maxZ");

            final List<Integer> xNodes = new ArrayList<>();
            final List<Integer> yNodes = new ArrayList<>();
            forEach(territoryNode, "node", node ->
            {
                xNodes.add(parseInteger(node.getAttributes(), "x"));
                yNodes.add(parseInteger(node.getAttributes(), "y"));
            });
            final int[] x = xNodes.stream().mapToInt(Integer::valueOf).toArray();
            final int[] y = yNodes.stream().mapToInt(Integer::valueOf).toArray();

            switch (territoryNode.getNodeName()) {
                case "territory" -> spawnTemplate.addTerritory(new SpawnTerritory(name, new ZoneNPoly(x, y, minZ, maxZ)));
                case "banned_territory" -> spawnTemplate.addBannedTerritory(new BannedSpawnTerritory(name, new ZoneNPoly(x, y, minZ, maxZ)));
            }
        });
    }

    private void parseGroup(Node n, SpawnTemplate spawnTemplate) {
        final SpawnGroup group = new SpawnGroup(new StatsSet(parseAttributes(n)));
        forEach(n, XmlReader::isNode, npcNode ->
        {
            switch (npcNode.getNodeName()) {
                case "territories" -> parseTerritories(npcNode, spawnTemplate.getFilePath(), group);
                case "npc" -> parseNpc(npcNode, spawnTemplate, group);
            }
        });
        spawnTemplate.addGroup(group);
    }

    private void parseNpc(Node n, SpawnTemplate spawnTemplate, SpawnGroup group) {
        final NpcSpawnTemplate npcTemplate = new NpcSpawnTemplate(spawnTemplate, group, new StatsSet(parseAttributes(n)));
        final NpcTemplate template = NpcData.getInstance().getTemplate(npcTemplate.getId());

        if (isNull(template)) {
            LOGGER.warn("Requested spawn for non existing npc: {} in file: {}", npcTemplate.getId(), spawnTemplate.getFilePath());
            return;
        }

        if (template.isType("L2Servitor") || template.isType("L2Pet")) {
            LOGGER.warn("Requested spawn for {} {} ({}) file: {}", template.getType(), template.getName(), template.getId(), spawnTemplate.getFilePath());
            return;
        }

        for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
            if ("parameters".equalsIgnoreCase(d.getNodeName())) {
                parseParameters(d, npcTemplate);
            } else if ("minions".equalsIgnoreCase(d.getNodeName())) {
                parseMinions(d, npcTemplate);
            } else if ("locations".equalsIgnoreCase(d.getNodeName())) {
                parseLocations(d, npcTemplate);
            }
        }
        group.addSpawn(npcTemplate);
    }

    private void parseLocations(Node n, NpcSpawnTemplate npcTemplate) {
        for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
            if ("location".equalsIgnoreCase(d.getNodeName())) {
                final int x = parseInteger(d.getAttributes(), "x");
                final int y = parseInteger(d.getAttributes(), "y");
                final int z = parseInteger(d.getAttributes(), "z");
                final int heading = parseInteger(d.getAttributes(), "heading", 0);
                final double chance = parseDouble(d.getAttributes(), "chance");
                npcTemplate.addSpawnLocation(new ChanceLocation(x, y, z, heading, chance));
            }
        }
    }

    private void parseParameters(Node n, IParameterized<StatsSet> npcTemplate) {
        final Map<String, Object> params = parseParameters(n);
        npcTemplate.setParameters(!params.isEmpty() ? new StatsSet(Collections.unmodifiableMap(params)) : StatsSet.EMPTY_STATSET);
    }

    private void parseMinions(Node n, NpcSpawnTemplate npcTemplate) {
        forEach(n, "minion", minionNode ->
        {
            npcTemplate.addMinion(new MinionHolder(new StatsSet(parseAttributes(minionNode))));
        });
    }

    public static SpawnsData getInstance() {
        return Singleton.INSTANCE;
    }

    private static class Singleton {
        private static final SpawnsData INSTANCE = new SpawnsData();
    }
}
