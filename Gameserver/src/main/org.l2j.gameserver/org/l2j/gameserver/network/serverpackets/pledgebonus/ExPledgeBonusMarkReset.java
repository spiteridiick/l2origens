package org.l2j.gameserver.network.serverpackets.pledgebonus;

import io.github.joealisson.mmocore.StaticPacket;
import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.ServerPacketId;
import org.l2j.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
@StaticPacket
public class ExPledgeBonusMarkReset extends ServerPacket {
    public static ExPledgeBonusMarkReset STATIC_PACKET = new ExPledgeBonusMarkReset();

    private ExPledgeBonusMarkReset() {
    }

    @Override
    public void writeImpl(L2GameClient client) {
        writeId(ServerPacketId.EX_PLEDGE_BONUS_MARK_RESET);
    }

}
