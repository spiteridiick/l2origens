package org.l2j.gameserver.network.clientpackets;

import org.l2j.gameserver.Config;
import org.l2j.gameserver.enums.ItemLocation;
import org.l2j.gameserver.enums.PrivateStoreType;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.model.actor.request.EnchantItemAttributeRequest;
import org.l2j.gameserver.model.actor.request.EnchantItemRequest;
import org.l2j.gameserver.model.items.L2Armor;
import org.l2j.gameserver.model.items.L2Weapon;
import org.l2j.gameserver.model.items.instance.L2ItemInstance;
import org.l2j.gameserver.model.options.VariationFee;
import org.l2j.gameserver.model.skills.AbnormalType;
import org.l2j.gameserver.network.SystemMessageId;

import java.util.Arrays;

public abstract class AbstractRefinePacket extends ClientPacket {
    /**
     * Checks player, source item, lifestone and gemstone validity for augmentation process
     *
     * @param player
     * @param item
     * @param mineralItem
     * @param feeItem
     * @param fee
     * @return
     */
    protected static boolean isValid(L2PcInstance player, L2ItemInstance item, L2ItemInstance mineralItem, L2ItemInstance feeItem, VariationFee fee) {
        if (fee == null) {
            return false;
        }

        if (!isValid(player, item, mineralItem)) {
            return false;
        }

        // GemStones must belong to owner
        if (feeItem.getOwnerId() != player.getObjectId()) {
            return false;
        }
        // .. and located in inventory
        if (feeItem.getItemLocation() != ItemLocation.INVENTORY) {
            return false;
        }

        // Check for item id
        if (fee.getItemId() != feeItem.getId()) {
            return false;
        }
        // Count must be greater or equal of required number
        if (fee.getItemCount() > feeItem.getCount()) {
            return false;
        }

        return true;
    }

    /**
     * Checks player, source item and lifestone validity for augmentation process
     *
     * @param player
     * @param item
     * @param mineralItem
     * @return
     */
    protected static boolean isValid(L2PcInstance player, L2ItemInstance item, L2ItemInstance mineralItem) {
        if (!isValid(player, item)) {
            return false;
        }

        // Item must belong to owner
        if (mineralItem.getOwnerId() != player.getObjectId()) {
            return false;
        }
        // Lifestone must be located in inventory
        if (mineralItem.getItemLocation() != ItemLocation.INVENTORY) {
            return false;
        }

        return true;
    }

    /**
     * Check both player and source item conditions for augmentation process
     *
     * @param player
     * @param item
     * @return
     */
    protected static boolean isValid(L2PcInstance player, L2ItemInstance item) {
        if (!isValid(player)) {
            return false;
        }

        // Item must belong to owner
        if (item.getOwnerId() != player.getObjectId()) {
            return false;
        }
        if (item.isAugmented()) {
            return false;
        }
        if (item.isHeroItem()) {
            return false;
        }
        if (item.isShadowItem()) {
            return false;
        }
        if (item.isCommonItem()) {
            return false;
        }
        if (item.isEtcItem()) {
            return false;
        }
        if (item.isTimeLimitedItem()) {
            return false;
        }
        if (item.isPvp() && !Config.ALT_ALLOW_AUGMENT_PVP_ITEMS) {
            return false;
        }

        // Source item can be equipped or in inventory
        switch (item.getItemLocation()) {
            case INVENTORY:
            case PAPERDOLL: {
                break;
            }
            default: {
                return false;
            }
        }

        if (!(item.getItem() instanceof L2Weapon) && !(item.getItem() instanceof L2Armor)) {
            return false; // neither weapon nor armor ?
        }

        // blacklist check
        if (Arrays.binarySearch(Config.AUGMENTATION_BLACKLIST, item.getId()) >= 0) {
            return false;
        }

        return true;
    }

    /**
     * Check if player's conditions valid for augmentation process
     *
     * @param player
     * @return
     */
    protected static boolean isValid(L2PcInstance player) {
        if (player.getPrivateStoreType() != PrivateStoreType.NONE) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION);
            return false;
        }
        if (player.getActiveTradeList() != null) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_ENGAGED_IN_TRADE_ACTIVITIES);
            return false;
        }
        if (player.isDead()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD);
            return false;
        }
        if (player.hasBlockActions() && player.hasAbnormalType(AbnormalType.PARALYZE)) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED);
            return false;
        }
        if (player.isFishing()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING);
            return false;
        }
        if (player.isSitting()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN);
            return false;
        }
        if (player.isCursedWeaponEquipped()) {
            return false;
        }
        if (player.hasRequest(EnchantItemRequest.class, EnchantItemAttributeRequest.class) || player.isProcessingTransaction()) {
            return false;
        }

        return true;
    }
}
