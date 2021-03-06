package org.l2j.gameserver.network.clientpackets;

import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.network.serverpackets.ExGetBookMarkInfoPacket;

/**
 * @author ShanSoft Packets Structure: chddd
 */
public final class RequestBookMarkSlotInfo extends ClientPacket {

    @Override
    public void readImpl() {
    }

    @Override
    public void runImpl() {
        final L2PcInstance player = client.getActiveChar();
        if (player != null) {
            player.sendPacket(new ExGetBookMarkInfoPacket(player));
        }
    }
}
