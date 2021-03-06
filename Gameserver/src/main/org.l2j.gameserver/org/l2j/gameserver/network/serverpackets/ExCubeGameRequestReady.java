package org.l2j.gameserver.network.serverpackets;

import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.ServerPacketId;

/**
 * Show Confirm Dialog for 10 seconds
 *
 * @author mrTJO
 */
public class ExCubeGameRequestReady extends ServerPacket {
    public static final ExCubeGameRequestReady STATIC_PACKET = new ExCubeGameRequestReady();

    private ExCubeGameRequestReady() {

    }

    @Override
    public void writeImpl(L2GameClient client) {
        writeId(ServerPacketId.EX_BLOCK_UP_SET_LIST);

        writeInt(0x04);
    }

}
