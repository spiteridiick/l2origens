package org.l2j.gameserver.network.serverpackets.ensoul;

import org.l2j.gameserver.model.ensoul.EnsoulOption;
import org.l2j.gameserver.model.items.instance.L2ItemInstance;
import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.ServerPacketId;
import org.l2j.gameserver.network.serverpackets.ServerPacket;

/**
 * @author UnAfraid
 */
public class ExEnsoulResult extends ServerPacket {
    private final int _success;
    private final L2ItemInstance _item;

    public ExEnsoulResult(int success, L2ItemInstance item) {
        _success = success;
        _item = item;
    }

    @Override
    public void writeImpl(L2GameClient client) {
        writeId(ServerPacketId.EX_ENSOUL_RESULT);
        writeByte((byte) _success); // success / failure
        writeByte((byte) _item.getSpecialAbilities().size());
        for (EnsoulOption option : _item.getSpecialAbilities()) {
            writeInt(option.getId());
        }
        writeByte((byte) _item.getAdditionalSpecialAbilities().size());
        for (EnsoulOption option : _item.getAdditionalSpecialAbilities()) {
            writeInt(option.getId());
        }
    }

}
