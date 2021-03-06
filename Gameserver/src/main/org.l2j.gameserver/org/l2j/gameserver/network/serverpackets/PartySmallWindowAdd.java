package org.l2j.gameserver.network.serverpackets;

import org.l2j.gameserver.model.L2Party;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.ServerPacketId;

public final class PartySmallWindowAdd extends ServerPacket {
    private final L2PcInstance _member;
    private final L2Party _party;

    public PartySmallWindowAdd(L2PcInstance member, L2Party party) {
        _member = member;
        _party = party;
    }

    @Override
    public void writeImpl(L2GameClient client) {
        writeId(ServerPacketId.PARTY_SMALL_WINDOW_ADD);

        writeInt(_party.getLeaderObjectId()); // c3
        writeInt(_party.getDistributionType().getId()); // c3
        writeInt(_member.getObjectId());
        writeString(_member.getName());

        writeInt((int) _member.getCurrentCp()); // c4
        writeInt(_member.getMaxCp()); // c4
        writeInt((int) _member.getCurrentHp());
        writeInt(_member.getMaxHp());
        writeInt((int) _member.getCurrentMp());
        writeInt(_member.getMaxMp());
        writeInt(_member.getVitalityPoints());
        writeByte((byte) _member.getLevel());
        writeShort((short) _member.getClassId().getId());
        writeByte((byte) 0x00);
        writeShort((short) _member.getRace().ordinal());
    }

}
