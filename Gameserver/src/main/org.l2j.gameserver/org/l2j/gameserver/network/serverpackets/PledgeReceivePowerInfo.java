package org.l2j.gameserver.network.serverpackets;

import org.l2j.gameserver.model.L2ClanMember;
import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.ServerPacketId;

/**
 * @author -Wooden-
 */
public class PledgeReceivePowerInfo extends ServerPacket {
    private final L2ClanMember _member;

    public PledgeReceivePowerInfo(L2ClanMember member) {
        _member = member;
    }

    @Override
    public void writeImpl(L2GameClient client) {
        writeId(ServerPacketId.PLEDGE_RECEIVE_POWER_INFO);

        writeInt(_member.getPowerGrade()); // power grade
        writeString(_member.getName());
        writeInt(_member.getClan().getRankPrivs(_member.getPowerGrade()).getBitmask()); // privileges
    }

}
