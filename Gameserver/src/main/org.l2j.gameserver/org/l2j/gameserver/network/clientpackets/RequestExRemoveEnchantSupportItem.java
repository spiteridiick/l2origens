package org.l2j.gameserver.network.clientpackets;

import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.model.actor.request.EnchantItemRequest;
import org.l2j.gameserver.model.items.instance.L2ItemInstance;
import org.l2j.gameserver.network.serverpackets.ExRemoveEnchantSupportItemResult;

/**
 * @author Sdw
 */
public class RequestExRemoveEnchantSupportItem extends ClientPacket {
    @Override
    public void readImpl() {

    }

    @Override
    public void runImpl() {
        final L2PcInstance activeChar = client.getActiveChar();
        if (activeChar == null) {
            return;
        }

        final EnchantItemRequest request = activeChar.getRequest(EnchantItemRequest.class);
        if ((request == null) || request.isProcessing()) {
            return;
        }

        final L2ItemInstance supportItem = request.getSupportItem();
        if ((supportItem == null) || (supportItem.getCount() < 1)) {
            request.setSupportItem(L2PcInstance.ID_NONE);
        }

        request.setTimestamp(System.currentTimeMillis());
        activeChar.sendPacket(ExRemoveEnchantSupportItemResult.STATIC_PACKET);
    }
}
