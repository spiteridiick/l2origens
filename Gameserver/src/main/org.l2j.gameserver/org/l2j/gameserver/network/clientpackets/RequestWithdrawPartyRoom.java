package org.l2j.gameserver.network.clientpackets;

import org.l2j.gameserver.enums.MatchingRoomType;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.model.matching.MatchingRoom;

/**
 * @author Gnacik
 */
public final class RequestWithdrawPartyRoom extends ClientPacket {
    private int _roomId;

    @Override
    public void readImpl() {
        _roomId = readInt();
    }

    @Override
    public void runImpl() {
        final L2PcInstance activeChar = client.getActiveChar();
        if (activeChar == null) {
            return;
        }

        final MatchingRoom room = activeChar.getMatchingRoom();
        if (room == null) {
            return;
        }

        if ((room.getId() != _roomId) || (room.getRoomType() != MatchingRoomType.PARTY)) {
            return;
        }

        room.deleteMember(activeChar, false);
    }
}
