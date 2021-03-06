package org.l2j.gameserver.network.serverpackets;

import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.ServerPacketId;

/**
 * @author Sdw
 */
public class ExUserInfoInvenWeight extends ServerPacket {
    private final L2PcInstance _activeChar;

    public ExUserInfoInvenWeight(L2PcInstance cha) {
        _activeChar = cha;
    }

    @Override
    public void writeImpl(L2GameClient client) {
        writeId(ServerPacketId.EX_USER_INFO_INVEN_WEIGHT);

        writeInt(_activeChar.getObjectId());
        writeInt(_activeChar.getCurrentLoad());
        writeInt(_activeChar.getMaxLoad());
    }

}