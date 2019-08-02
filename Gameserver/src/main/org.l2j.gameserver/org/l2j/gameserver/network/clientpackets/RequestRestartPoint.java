package org.l2j.gameserver.network.clientpackets;

import org.l2j.commons.threading.ThreadPoolManager;
import org.l2j.gameserver.data.xml.impl.ClanHallData;
import org.l2j.gameserver.instancemanager.CastleManager;
import org.l2j.gameserver.instancemanager.FortDataManager;
import org.l2j.gameserver.world.MapRegionManager;
import org.l2j.gameserver.model.Clan;
import org.l2j.gameserver.model.SiegeClan;
import org.l2j.gameserver.model.Location;
import org.l2j.gameserver.model.TeleportWhereType;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.model.entity.Castle;
import org.l2j.gameserver.model.entity.ClanHall;
import org.l2j.gameserver.model.entity.Fort;
import org.l2j.gameserver.model.events.EventType;
import org.l2j.gameserver.model.events.listeners.AbstractEventListener;
import org.l2j.gameserver.model.instancezone.Instance;
import org.l2j.gameserver.model.quest.Event;
import org.l2j.gameserver.model.residences.ResidenceFunctionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class ...
 *
 * @version $Revision: 1.7.2.3.2.6 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestRestartPoint extends ClientPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestRestartPoint.class);
    protected int _requestedPointType;
    protected boolean _continuation;

    @Override
    public void readImpl() {
        _requestedPointType = readInt();
    }

    @Override
    public void runImpl() {
        final Player activeChar = client.getPlayer();

        if (activeChar == null) {
            return;
        }

        if (!activeChar.canRevive()) {
            return;
        }

        if (activeChar.isFakeDeath()) {
            activeChar.stopFakeDeath(true);
            return;
        } else if (!activeChar.isDead()) {
            LOGGER.warn("Living player [" + activeChar.getName() + "] called RestartPointPacket! Ban this player!");
            return;
        }

        // Custom event resurrection management.
        if (activeChar.isOnCustomEvent()) {
            for (AbstractEventListener listener : activeChar.getListeners(EventType.ON_CREATURE_DEATH)) {
                if (listener.getOwner() instanceof Event) {
                    ((Event) listener.getOwner()).notifyEvent("ResurrectPlayer", null, activeChar);
                    return;
                }
            }
        }

        final Castle castle = CastleManager.getInstance().getCastle(activeChar.getX(), activeChar.getY(), activeChar.getZ());
        if ((castle != null) && castle.getSiege().isInProgress()) {
            if ((activeChar.getClan() != null) && castle.getSiege().checkIsAttacker(activeChar.getClan())) {
                // Schedule respawn delay for attacker
                ThreadPoolManager.getInstance().schedule(new DeathTask(activeChar), castle.getSiege().getAttackerRespawnDelay());
                if (castle.getSiege().getAttackerRespawnDelay() > 0) {
                    activeChar.sendMessage("You will be re-spawned in " + (castle.getSiege().getAttackerRespawnDelay() / 1000) + " seconds");
                }
                return;
            }
        }

        portPlayer(activeChar);
    }

    protected final void portPlayer(Player player) {
        Location loc = null;
        Instance instance = null;

        // force jail
        if (player.isJailed()) {
            _requestedPointType = 27;
        }

        switch (_requestedPointType) {
            case 1: // to clanhall
            {
                if ((player.getClan() == null) || (player.getClan().getHideoutId() == 0)) {
                    LOGGER.warn("Player [" + player.getName() + "] called RestartPointPacket - To Clanhall and he doesn't have Clanhall!");
                    return;
                }
                loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.CLANHALL);
                final ClanHall residense = ClanHallData.getInstance().getClanHallByClan(player.getClan());

                if ((residense != null) && (residense.hasFunction(ResidenceFunctionType.EXP_RESTORE))) {
                    player.restoreExp(residense.getFunction(ResidenceFunctionType.EXP_RESTORE).getValue());
                }
                break;
            }
            case 2: // to castle
            {
                final Clan clan = player.getClan();
                Castle castle = CastleManager.getInstance().getCastle(player);
                if ((castle != null) && castle.getSiege().isInProgress()) {
                    // Siege in progress
                    if (castle.getSiege().checkIsDefender(clan)) {
                        loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.CASTLE);
                    } else if (castle.getSiege().checkIsAttacker(clan)) {
                        loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.TOWN);
                    } else {
                        LOGGER.warn("Player [" + player.getName() + "] called RestartPointPacket - To Castle and he doesn't have Castle!");
                        return;
                    }
                } else {
                    if ((clan == null) || (clan.getCastleId() == 0)) {
                        return;
                    }
                    loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.CASTLE);
                }

                if (clan != null) {
                    castle = CastleManager.getInstance().getCastleByOwner(clan);
                    if (castle != null) {
                        final Castle.CastleFunction castleFunction = castle.getCastleFunction(Castle.FUNC_RESTORE_EXP);
                        if (castleFunction != null) {
                            player.restoreExp(castleFunction.getLvl());
                        }
                    }
                }
                break;
            }
            case 3: // to fortress
            {
                final Clan clan = player.getClan();
                if ((clan == null) || (clan.getFortId() == 0)) {
                    LOGGER.warn("Player [" + player.getName() + "] called RestartPointPacket - To Fortress and he doesn't have Fortress!");
                    return;
                }
                loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.FORTRESS);

                final Fort fort = FortDataManager.getInstance().getFortByOwner(clan);
                if (fort != null) {
                    final Fort.FortFunction fortFunction = fort.getFortFunction(Fort.FUNC_RESTORE_EXP);
                    if (fortFunction != null) {
                        player.restoreExp(fortFunction.getLvl());
                    }
                }
                break;
            }
            case 4: // to siege HQ
            {
                SiegeClan siegeClan = null;
                final Castle castle = CastleManager.getInstance().getCastle(player);
                final Fort fort = FortDataManager.getInstance().getFort(player);

                if ((castle != null) && castle.getSiege().isInProgress()) {
                    siegeClan = castle.getSiege().getAttackerClan(player.getClan());
                } else if ((fort != null) && fort.getSiege().isInProgress()) {
                    siegeClan = fort.getSiege().getAttackerClan(player.getClan());
                }

                if (((siegeClan == null) || siegeClan.getFlag().isEmpty())) {
                    LOGGER.warn("Player [" + player.getName() + "] called RestartPointPacket - To Siege HQ and he doesn't have Siege HQ!");
                    return;
                }
                loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.SIEGEFLAG);
                break;
            }
            case 5: // Fixed or Player is a festival participant
            {
                if (!player.isGM() && !player.getInventory().haveItemForSelfResurrection()) {
                    LOGGER.warn("Player [" + player.getName() + "] called RestartPointPacket - Fixed and he isn't festival participant!");
                    return;
                }
                if (player.isGM() || player.destroyItemByItemId("Feather", 10649, 1, player, false) || player.destroyItemByItemId("Feather", 13300, 1, player, false) || player.destroyItemByItemId("Feather", 13128, 1, player, false)) {
                    player.doRevive(100.00);
                } else {
                    instance = player.getInstanceWorld();
                    loc = new Location(player);
                }
                break;
            }
            case 6: // TODO: Agathion resurrection
            {
                break;
            }
            case 7: // TODO: Adventurer's Song
            {
                break;
            }
            case 27: // to jail
            {
                if (!player.isJailed()) {
                    return;
                }
                loc = new Location(-114356, -249645, -2984);
                break;
            }
            default: {
                loc = MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.TOWN);
                break;
            }
        }

        // Teleport and revive
        if (loc != null) {
            player.setIsPendingRevive(true);
            player.teleToLocation(loc, true, instance);
        }
    }

    class DeathTask implements Runnable {
        final Player activeChar;

        DeathTask(Player _activeChar) {
            activeChar = _activeChar;
        }

        @Override
        public void run() {
            portPlayer(activeChar);
        }
    }

}
