package org.l2j.gameserver.network.serverpackets;

import org.l2j.gameserver.model.actor.instance.L2BoatInstance;
import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.ServerPacketId;

/**
 * @author Maktakien
 */
public class VehicleInfo extends ServerPacket {
    private final int _objId;
    private final int _x;
    private final int _y;
    private final int _z;
    private final int _heading;

    public VehicleInfo(L2BoatInstance boat) {
        _objId = boat.getObjectId();
        _x = boat.getX();
        _y = boat.getY();
        _z = boat.getZ();
        _heading = boat.getHeading();
    }

    @Override
    public void writeImpl(L2GameClient client) {
        writeId(ServerPacketId.VEHICLE_INFO);

        writeInt(_objId);
        writeInt(_x);
        writeInt(_y);
        writeInt(_z);
        writeInt(_heading);
    }

}
