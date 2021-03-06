package org.l2j.gameserver.network.clientpackets;

import org.l2j.gameserver.enums.MatchingRoomType;
import org.l2j.gameserver.model.L2Party;
import org.l2j.gameserver.model.L2World;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.model.matching.MatchingRoom;
import org.l2j.gameserver.network.SystemMessageId;

/**
 * format (ch) d
 *
 * @author -Wooden-
 */
public final class RequestOustFromPartyRoom extends ClientPacket {
    private int _charObjId;

    @Override
    public void readImpl() {
        _charObjId = readInt();
    }

    @Override
    public void runImpl() {
        final L2PcInstance player = client.getActiveChar();
        if (player == null) {
            return;
        }

        final L2PcInstance member = L2World.getInstance().getPlayer(_charObjId);
        if (member == null) {
            return;
        }

        final MatchingRoom room = player.getMatchingRoom();
        if ((room == null) || (room.getRoomType() != MatchingRoomType.PARTY) || (room.getLeader() != player) || (player == member)) {
            return;
        }

        final L2Party playerParty = player.getParty();
        final L2Party memberParty = member.getParty();

        if ((playerParty != null) && (memberParty != null) && (playerParty.getLeaderObjectId() == memberParty.getLeaderObjectId())) {
            client.sendPacket(SystemMessageId.YOU_CANNOT_DISMISS_A_PARTY_MEMBER_BY_FORCE);
        } else {
            room.deleteMember(member, true);
        }
    }
}
