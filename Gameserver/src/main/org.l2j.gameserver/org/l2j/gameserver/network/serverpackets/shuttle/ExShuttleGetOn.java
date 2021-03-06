package org.l2j.gameserver.network.serverpackets.shuttle;

import org.l2j.gameserver.model.Location;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.model.actor.instance.L2ShuttleInstance;
import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.ServerPacketId;
import org.l2j.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExShuttleGetOn extends ServerPacket {
    private final int _playerObjectId;
    private final int _shuttleObjectId;
    private final Location _pos;

    public ExShuttleGetOn(L2PcInstance player, L2ShuttleInstance shuttle) {
        _playerObjectId = player.getObjectId();
        _shuttleObjectId = shuttle.getObjectId();
        _pos = player.getInVehiclePosition();
    }

    @Override
    public void writeImpl(L2GameClient client) {
        writeId(ServerPacketId.EX_SUTTLE_GET_ON);

        writeInt(_playerObjectId);
        writeInt(_shuttleObjectId);
        writeInt(_pos.getX());
        writeInt(_pos.getY());
        writeInt(_pos.getZ());
    }

}
