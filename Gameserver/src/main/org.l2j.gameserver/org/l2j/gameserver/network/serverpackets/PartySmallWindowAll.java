package org.l2j.gameserver.network.serverpackets;

import org.l2j.gameserver.model.L2Party;
import org.l2j.gameserver.model.actor.L2Summon;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.ServerPacketId;

public final class PartySmallWindowAll extends ServerPacket {
    private final L2Party _party;
    private final L2PcInstance _exclude;

    public PartySmallWindowAll(L2PcInstance exclude, L2Party party) {
        _exclude = exclude;
        _party = party;
    }

    @Override
    public void writeImpl(L2GameClient client) {
        writeId(ServerPacketId.PARTY_SMALL_WINDOW_ALL);

        writeInt(_party.getLeaderObjectId());
        writeByte((byte) _party.getDistributionType().getId());
        writeByte((byte) (_party.getMemberCount() - 1));

        for (L2PcInstance member : _party.getMembers()) {
            if ((member != null) && (member != _exclude)) {
                writeInt(member.getObjectId());
                writeString(member.getName());

                writeInt((int) member.getCurrentCp()); // c4
                writeInt(member.getMaxCp()); // c4

                writeInt((int) member.getCurrentHp());
                writeInt(member.getMaxHp());
                writeInt((int) member.getCurrentMp());
                writeInt(member.getMaxMp());
                writeInt(member.getVitalityPoints());
                writeByte((byte) member.getLevel());
                writeShort((short) member.getClassId().getId());
                writeByte((byte) 0x01); // Unk
                writeShort((short) member.getRace().ordinal());
                final L2Summon pet = member.getPet();
                writeInt(member.getServitors().size() + (pet != null ? 1 : 0)); // Summon size, one only atm
                if (pet != null) {
                    writeInt(pet.getObjectId());
                    writeInt(pet.getId() + 1000000);
                    writeByte((byte) pet.getSummonType());
                    writeString(pet.getName());
                    writeInt((int) pet.getCurrentHp());
                    writeInt(pet.getMaxHp());
                    writeInt((int) pet.getCurrentMp());
                    writeInt(pet.getMaxMp());
                    writeByte((byte) pet.getLevel());
                }
                member.getServitors().values().forEach(s ->
                {
                    writeInt(s.getObjectId());
                    writeInt(s.getId() + 1000000);
                    writeByte((byte) s.getSummonType());
                    writeString(s.getName());
                    writeInt((int) s.getCurrentHp());
                    writeInt(s.getMaxHp());
                    writeInt((int) s.getCurrentMp());
                    writeInt(s.getMaxMp());
                    writeByte((byte) s.getLevel());
                });
            }
        }
    }

}
