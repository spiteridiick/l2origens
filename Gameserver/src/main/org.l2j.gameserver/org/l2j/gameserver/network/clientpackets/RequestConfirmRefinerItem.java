package org.l2j.gameserver.network.clientpackets;

import org.l2j.gameserver.data.xml.impl.VariationData;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.model.items.instance.L2ItemInstance;
import org.l2j.gameserver.model.options.VariationFee;
import org.l2j.gameserver.network.SystemMessageId;
import org.l2j.gameserver.network.serverpackets.ExPutIntensiveResultForVariationMake;

import java.nio.ByteBuffer;

/**
 * Fromat(ch) dd
 *
 * @author -Wooden-
 */
public class RequestConfirmRefinerItem extends AbstractRefinePacket {
    private int _targetItemObjId;
    private int _refinerItemObjId;

    @Override
    public void readImpl() {
        _targetItemObjId = readInt();
        _refinerItemObjId = readInt();
    }

    @Override
    public void runImpl() {
        final L2PcInstance activeChar = client.getActiveChar();
        if (activeChar == null) {
            return;
        }

        final L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
        if (targetItem == null) {
            return;
        }

        final L2ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
        if (refinerItem == null) {
            return;
        }

        final VariationFee fee = VariationData.getInstance().getFee(targetItem.getId(), refinerItem.getId());
        if ((fee == null) || !isValid(activeChar, targetItem, refinerItem)) {
            activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
            return;
        }

        activeChar.sendPacket(new ExPutIntensiveResultForVariationMake(_refinerItemObjId, refinerItem.getId(), fee.getItemId(), fee.getItemCount()));
    }
}
