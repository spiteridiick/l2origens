package org.l2j.gameserver.network.clientpackets;

import org.l2j.gameserver.model.L2World;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author -Wooden-
 */
public final class SnoopQuit extends ClientPacket {
    private int _snoopID;

    @Override
    public void readImpl() {
        _snoopID = readInt();
    }

    @Override
    public void runImpl() {
        final L2PcInstance player = L2World.getInstance().getPlayer(_snoopID);
        if (player == null) {
            return;
        }

        final L2PcInstance activeChar = client.getActiveChar();
        if (activeChar == null) {
            return;
        }

        player.removeSnooper(activeChar);
        activeChar.removeSnooped(player);

    }
}
