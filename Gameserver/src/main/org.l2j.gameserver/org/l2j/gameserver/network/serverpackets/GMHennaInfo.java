package org.l2j.gameserver.network.serverpackets;

import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.model.items.L2Henna;
import org.l2j.gameserver.model.stats.BaseStats;
import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.ServerPacketId;

import java.util.ArrayList;
import java.util.List;

/**
 * This server packet sends the player's henna information using the Game Master's UI.
 *
 * @author KenM, Zoey76
 */
public final class GMHennaInfo extends ServerPacket {
    private final L2PcInstance _activeChar;
    private final List<L2Henna> _hennas = new ArrayList<>();

    public GMHennaInfo(L2PcInstance player) {
        _activeChar = player;
        for (L2Henna henna : _activeChar.getHennaList()) {
            if (henna != null) {
                _hennas.add(henna);
            }
        }
    }

    @Override
    public void writeImpl(L2GameClient client) {
        writeId(ServerPacketId.GMHENNA_INFO);

        writeShort((short) _activeChar.getHennaValue(BaseStats.INT)); // equip INT
        writeShort((short) _activeChar.getHennaValue(BaseStats.STR)); // equip STR
        writeShort((short) _activeChar.getHennaValue(BaseStats.CON)); // equip CON
        writeShort((short) _activeChar.getHennaValue(BaseStats.MEN)); // equip MEN
        writeShort((short) _activeChar.getHennaValue(BaseStats.DEX)); // equip DEX
        writeShort((short) _activeChar.getHennaValue(BaseStats.WIT)); // equip WIT
        writeShort((short) 0x00); // equip LUC
        writeShort((short) 0x00); // equip CHA
        writeInt(3); // Slots
        writeInt(_hennas.size()); // Size
        for (L2Henna henna : _hennas) {
            writeInt(henna.getDyeId());
            writeInt(0x01);
        }
        writeInt(0x00);
        writeInt(0x00);
        writeInt(0x00);
    }

}
