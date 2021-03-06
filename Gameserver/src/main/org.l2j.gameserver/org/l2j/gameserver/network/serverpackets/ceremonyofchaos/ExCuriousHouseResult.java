package org.l2j.gameserver.network.serverpackets.ceremonyofchaos;

import org.l2j.gameserver.enums.CeremonyOfChaosResult;
import org.l2j.gameserver.instancemanager.CeremonyOfChaosManager;
import org.l2j.gameserver.model.ceremonyofchaos.CeremonyOfChaosEvent;
import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.ServerPacketId;
import org.l2j.gameserver.network.serverpackets.ServerPacket;

/**
 * @author Sdw
 */
public class ExCuriousHouseResult extends ServerPacket {
    private final CeremonyOfChaosResult _result;
    private final CeremonyOfChaosEvent _event;

    public ExCuriousHouseResult(CeremonyOfChaosResult result, CeremonyOfChaosEvent event) {
        _result = result;
        _event = event;
    }

    @Override
    public void writeImpl(L2GameClient client) {
        writeId(ServerPacketId.EX_CURIOUS_HOUSE_RESULT);
        writeInt(_event.getId());
        writeShort((short) _result.ordinal());
        writeInt(CeremonyOfChaosManager.getInstance().getMaxPlayersInArena());
        writeInt(_event.getMembers().size());
        _event.getMembers().values().forEach(m ->
        {
            writeInt(m.getObjectId());
            writeInt(m.getPosition());
            writeInt(m.getClassId());
            writeInt(m.getLifeTime());
            writeInt(m.getScore());
        });
    }

}
