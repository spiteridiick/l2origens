package org.l2j.gameserver.network.serverpackets.fishing;

import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.ServerPacketId;
import org.l2j.gameserver.network.serverpackets.ServerPacket;

/**
 * @author bit
 */
public class ExAutoFishAvailable extends ServerPacket {
    public static ExAutoFishAvailable YES = new ExAutoFishAvailable(true);
    public static ExAutoFishAvailable NO = new ExAutoFishAvailable(false);

    private final boolean _available;

    private ExAutoFishAvailable(boolean available) {
        _available = available;
    }

    @Override
    public void writeImpl(L2GameClient client) {
        writeId(ServerPacketId.EX_AUTO_FISH_AVAILABLE);
        writeByte((byte) (_available ? 1 : 0));
    }

}
