package org.l2j.gameserver.network.clientpackets;

import org.l2j.commons.util.Rnd;
import org.l2j.gameserver.Config;
import org.l2j.gameserver.data.xml.impl.EnchantItemData;
import org.l2j.gameserver.enums.ItemSkillType;
import org.l2j.gameserver.enums.UserInfoType;
import org.l2j.gameserver.model.L2World;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.model.actor.request.EnchantItemRequest;
import org.l2j.gameserver.model.items.L2Item;
import org.l2j.gameserver.model.items.enchant.EnchantResultType;
import org.l2j.gameserver.model.items.enchant.EnchantScroll;
import org.l2j.gameserver.model.items.enchant.EnchantSupportItem;
import org.l2j.gameserver.model.items.instance.L2ItemInstance;
import org.l2j.gameserver.model.skills.CommonSkill;
import org.l2j.gameserver.model.skills.Skill;
import org.l2j.gameserver.network.SystemMessageId;
import org.l2j.gameserver.network.serverpackets.EnchantResult;
import org.l2j.gameserver.network.serverpackets.InventoryUpdate;
import org.l2j.gameserver.network.serverpackets.MagicSkillUse;
import org.l2j.gameserver.network.serverpackets.SystemMessage;
import org.l2j.gameserver.util.GameUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class RequestEnchantItem extends ClientPacket {
    protected static final Logger LOGGER_ENCHANT = LoggerFactory.getLogger("enchant.items");

    private int _objectId;
    private int _supportId;

    @Override
    public void readImpl() {
        _objectId = readInt();
        _supportId = readInt();
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

        request.setEnchantingItem(_objectId);
        request.setProcessing(true);

        if (!activeChar.isOnline() || client.isDetached()) {
            activeChar.removeRequest(request.getClass());
            return;
        }

        if (activeChar.isProcessingTransaction() || activeChar.isInStoreMode()) {
            client.sendPacket(SystemMessageId.YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
            activeChar.removeRequest(request.getClass());
            return;
        }

        final L2ItemInstance item = request.getEnchantingItem();
        final L2ItemInstance scroll = request.getEnchantingScroll();
        final L2ItemInstance support = request.getSupportItem();
        if ((item == null) || (scroll == null)) {
            activeChar.removeRequest(request.getClass());
            return;
        }

        // template for scroll
        final EnchantScroll scrollTemplate = EnchantItemData.getInstance().getEnchantScroll(scroll);
        if (scrollTemplate == null) {
            return;
        }

        // template for support item, if exist
        EnchantSupportItem supportTemplate = null;
        if (support != null) {
            if (support.getObjectId() != _supportId) {
                activeChar.removeRequest(request.getClass());
                return;
            }
            supportTemplate = EnchantItemData.getInstance().getSupportItem(support);
        }

        // first validation check - also over enchant check
        if (!scrollTemplate.isValid(item, supportTemplate) || (Config.DISABLE_OVER_ENCHANTING && (item.getEnchantLevel() == scrollTemplate.getMaxEnchantLevel()))) {
            client.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
            activeChar.removeRequest(request.getClass());
            client.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
            return;
        }

        // fast auto-enchant cheat check
        if ((request.getTimestamp() == 0) || ((System.currentTimeMillis() - request.getTimestamp()) < 2000)) {
            GameUtils.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " use autoenchant program ", Config.DEFAULT_PUNISH);
            activeChar.removeRequest(request.getClass());
            client.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
            return;
        }

        // attempting to destroy scroll
        if (activeChar.getInventory().destroyItem("Enchant", scroll.getObjectId(), 1, activeChar, item) == null) {
            client.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
            GameUtils.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to enchant with a scroll he doesn't have", Config.DEFAULT_PUNISH);
            activeChar.removeRequest(request.getClass());
            client.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
            return;
        }

        // attempting to destroy support if exist
        if (support != null) {
            if (activeChar.getInventory().destroyItem("Enchant", support.getObjectId(), 1, activeChar, item) == null) {
                client.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
                GameUtils.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to enchant with a support item he doesn't have", Config.DEFAULT_PUNISH);
                activeChar.removeRequest(request.getClass());
                client.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
                return;
            }
        }

        final InventoryUpdate iu = new InventoryUpdate();
        synchronized (item) {
            // last validation check
            if ((item.getOwnerId() != activeChar.getObjectId()) || (item.isEnchantable() == 0)) {
                client.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
                activeChar.removeRequest(request.getClass());
                client.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
                return;
            }

            final EnchantResultType resultType = scrollTemplate.calculateSuccess(activeChar, item, supportTemplate);
            switch (resultType) {
                case ERROR: {
                    client.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
                    activeChar.removeRequest(request.getClass());
                    client.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
                    break;
                }
                case SUCCESS: {
                    final L2Item it = item.getItem();
                    // Increase enchant level only if scroll's base template has chance, some armors can success over +20 but they shouldn't have increased.
                    if (scrollTemplate.getChance(activeChar, item) > 0) {
                        if (scrollTemplate.isGiant()) {
                            if (((supportTemplate != null) && (supportTemplate.getId() == 23785)) || ((supportTemplate != null) && (supportTemplate.getId() == 23786))) // Lesser Giant's Lucky Enchant Stones. Increase from +2 to +4
                            {
                                item.setEnchantLevel(Math.min(item.getEnchantLevel() + 2 + Rnd.get(3), scrollTemplate.getMaxEnchantLevel()));
                            } else {
                                item.setEnchantLevel(Math.min(item.getEnchantLevel() + 1 + Rnd.get(3), scrollTemplate.getMaxEnchantLevel()));
                            }
                        } else {
                            item.setEnchantLevel(item.getEnchantLevel() + 1);
                        }
                        item.updateDatabase();
                    }
                    client.sendPacket(new EnchantResult(EnchantResult.SUCCESS, item));

                    if (Config.LOG_ITEM_ENCHANTS) {
                        if (item.getEnchantLevel() > 0) {
                            if (support == null) {
                                LOGGER_ENCHANT.info("Success, Character:" + activeChar.getName() + " [" + activeChar.getObjectId() + "] Account:" + activeChar.getAccountName() + " IP:" + activeChar.getIPAddress() + ", +" + item.getEnchantLevel() + " " + item.getName() + "(" + item.getCount() + ") [" + item.getObjectId() + "], " + scroll.getName() + "(" + scroll.getCount() + ") [" + scroll.getObjectId() + "]");
                            } else {
                                LOGGER_ENCHANT.info("Success, Character:" + activeChar.getName() + " [" + activeChar.getObjectId() + "] Account:" + activeChar.getAccountName() + " IP:" + activeChar.getIPAddress() + ", +" + item.getEnchantLevel() + " " + item.getName() + "(" + item.getCount() + ") [" + item.getObjectId() + "], " + scroll.getName() + "(" + scroll.getCount() + ") [" + scroll.getObjectId() + "], " + support.getName() + "(" + support.getCount() + ") [" + support.getObjectId() + "]");
                            }
                        } else if (support == null) {
                            LOGGER_ENCHANT.info("Success, Character:" + activeChar.getName() + " [" + activeChar.getObjectId() + "] Account:" + activeChar.getAccountName() + " IP:" + activeChar.getIPAddress() + ", " + item.getName() + "(" + item.getCount() + ") [" + item.getObjectId() + "], " + scroll.getName() + "(" + scroll.getCount() + ") [" + scroll.getObjectId() + "]");
                        } else {
                            LOGGER_ENCHANT.info("Success, Character:" + activeChar.getName() + " [" + activeChar.getObjectId() + "] Account:" + activeChar.getAccountName() + " IP:" + activeChar.getIPAddress() + ", " + item.getName() + "(" + item.getCount() + ") [" + item.getObjectId() + "], " + scroll.getName() + "(" + scroll.getCount() + ") [" + scroll.getObjectId() + "], " + support.getName() + "(" + support.getCount() + ") [" + support.getObjectId() + "]");
                        }
                    }

                    // announce the success
                    final int minEnchantAnnounce = item.isArmor() ? 6 : 7;
                    final int maxEnchantAnnounce = item.isArmor() ? 0 : 15;
                    if ((item.getEnchantLevel() == minEnchantAnnounce) || (item.getEnchantLevel() == maxEnchantAnnounce)) {
                        final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_SUCCESSFULLY_ENCHANTED_A_S2_S3);
                        sm.addString(activeChar.getName());
                        sm.addInt(item.getEnchantLevel());
                        sm.addItemName(item);
                        activeChar.broadcastPacket(sm);

                        final Skill skill = CommonSkill.FIREWORK.getSkill();
                        if (skill != null) {
                            activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
                        }
                    }

                    if (item.isEquipped()) {
                        if (item.isArmor()) {
                            it.forEachSkill(ItemSkillType.ON_ENCHANT, holder ->
                            {
                                // add skills bestowed from +4 armor
                                if (item.getEnchantLevel() >= holder.getValue()) {
                                    activeChar.addSkill(holder.getSkill(), false);
                                    activeChar.sendSkillList();
                                }
                            });
                        }
                        activeChar.broadcastUserInfo(); // update user info
                    }
                    break;
                }
                case FAILURE: {
                    if (scrollTemplate.isSafe()) {
                        // safe enchant - remain old value
                        client.sendPacket(SystemMessageId.ENCHANT_FAILED_THE_ENCHANT_SKILL_FOR_THE_CORRESPONDING_ITEM_WILL_BE_EXACTLY_RETAINED);
                        client.sendPacket(new EnchantResult(EnchantResult.SAFE_FAIL, item));

                        if (Config.LOG_ITEM_ENCHANTS) {
                            if (item.getEnchantLevel() > 0) {
                                if (support == null) {
                                    LOGGER_ENCHANT.info("Safe Fail, Character:" + activeChar.getName() + " [" + activeChar.getObjectId() + "] Account:" + activeChar.getAccountName() + " IP:" + activeChar.getIPAddress() + ", +" + item.getEnchantLevel() + " " + item.getName() + "(" + item.getCount() + ") [" + item.getObjectId() + "], " + scroll.getName() + "(" + scroll.getCount() + ") [" + scroll.getObjectId() + "]");
                                } else {
                                    LOGGER_ENCHANT.info("Safe Fail, Character:" + activeChar.getName() + " [" + activeChar.getObjectId() + "] Account:" + activeChar.getAccountName() + " IP:" + activeChar.getIPAddress() + ", +" + item.getEnchantLevel() + " " + item.getName() + "(" + item.getCount() + ") [" + item.getObjectId() + "], " + scroll.getName() + "(" + scroll.getCount() + ") [" + scroll.getObjectId() + "], " + support.getName() + "(" + support.getCount() + ") [" + support.getObjectId() + "]");
                                }
                            } else if (support == null) {
                                LOGGER_ENCHANT.info("Safe Fail, Character:" + activeChar.getName() + " [" + activeChar.getObjectId() + "] Account:" + activeChar.getAccountName() + " IP:" + activeChar.getIPAddress() + ", " + item.getName() + "(" + item.getCount() + ") [" + item.getObjectId() + "], " + scroll.getName() + "(" + scroll.getCount() + ") [" + scroll.getObjectId() + "]");
                            } else {
                                LOGGER_ENCHANT.info("Safe Fail, Character:" + activeChar.getName() + " [" + activeChar.getObjectId() + "] Account:" + activeChar.getAccountName() + " IP:" + activeChar.getIPAddress() + ", " + item.getName() + "(" + item.getCount() + ") [" + item.getObjectId() + "], " + scroll.getName() + "(" + scroll.getCount() + ") [" + scroll.getObjectId() + "], " + support.getName() + "(" + support.getCount() + ") [" + support.getObjectId() + "]");
                            }
                        }
                    } else {
                        // unequip item on enchant failure to avoid item skills stack
                        if (item.isEquipped()) {
                            if (item.getEnchantLevel() > 0) {
                                final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_EQUIPMENT_S1_S2_HAS_BEEN_REMOVED);
                                sm.addInt(item.getEnchantLevel());
                                sm.addItemName(item);
                                client.sendPacket(sm);
                            } else {
                                final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_UNEQUIPPED);
                                sm.addItemName(item);
                                client.sendPacket(sm);
                            }

                            final L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
                            for (L2ItemInstance itm : unequiped) {
                                iu.addModifiedItem(itm);
                            }

                            activeChar.sendInventoryUpdate(iu);
                            activeChar.broadcastUserInfo();
                        }

                        if (scrollTemplate.isBlessed() || ((supportTemplate != null) && supportTemplate.isBlessed())) {
                            // blessed enchant - clear enchant value
                            client.sendPacket(SystemMessageId.THE_BLESSED_ENCHANT_FAILED_THE_ENCHANT_VALUE_OF_THE_ITEM_BECAME_0);

                            item.setEnchantLevel(0);
                            item.updateDatabase();
                            client.sendPacket(new EnchantResult(EnchantResult.BLESSED_FAIL, 0, 0));

                            if (Config.LOG_ITEM_ENCHANTS) {
                                if (item.getEnchantLevel() > 0) {
                                    if (support == null) {
                                        LOGGER_ENCHANT.info("Blessed Fail, Character:" + activeChar.getName() + " [" + activeChar.getObjectId() + "] Account:" + activeChar.getAccountName() + " IP:" + activeChar.getIPAddress() + ", +" + item.getEnchantLevel() + " " + item.getName() + "(" + item.getCount() + ") [" + item.getObjectId() + "], " + scroll.getName() + "(" + scroll.getCount() + ") [" + scroll.getObjectId() + "]");
                                    } else {
                                        LOGGER_ENCHANT.info("Blessed Fail, Character:" + activeChar.getName() + " [" + activeChar.getObjectId() + "] Account:" + activeChar.getAccountName() + " IP:" + activeChar.getIPAddress() + ", +" + item.getEnchantLevel() + " " + item.getName() + "(" + item.getCount() + ") [" + item.getObjectId() + "], " + scroll.getName() + "(" + scroll.getCount() + ") [" + scroll.getObjectId() + "], " + support.getName() + "(" + support.getCount() + ") [" + support.getObjectId() + "]");
                                    }
                                } else if (support == null) {
                                    LOGGER_ENCHANT.info("Blessed Fail, Character:" + activeChar.getName() + " [" + activeChar.getObjectId() + "] Account:" + activeChar.getAccountName() + " IP:" + activeChar.getIPAddress() + ", " + item.getName() + "(" + item.getCount() + ") [" + item.getObjectId() + "], " + scroll.getName() + "(" + scroll.getCount() + ") [" + scroll.getObjectId() + "]");
                                } else {
                                    LOGGER_ENCHANT.info("Blessed Fail, Character:" + activeChar.getName() + " [" + activeChar.getObjectId() + "] Account:" + activeChar.getAccountName() + " IP:" + activeChar.getIPAddress() + ", " + item.getName() + "(" + item.getCount() + ") [" + item.getObjectId() + "], " + scroll.getName() + "(" + scroll.getCount() + ") [" + scroll.getObjectId() + "], " + support.getName() + "(" + support.getCount() + ") [" + support.getObjectId() + "]");
                                }
                            }
                        } else {
                            // enchant failed, destroy item
                            final int crystalId = item.getItem().getCrystalItemId();
                            int count = item.getCrystalCount() - ((item.getItem().getCrystalCount() + 1) / 2);
                            if (count < 1) {
                                count = 1;
                            }

                            if (activeChar.getInventory().destroyItem("Enchant", item, activeChar, null) == null) {
                                // unable to destroy item, cheater ?
                                GameUtils.handleIllegalPlayerAction(activeChar, "Unable to delete item on enchant failure from player " + activeChar.getName() + ", possible cheater !", Config.DEFAULT_PUNISH);
                                activeChar.removeRequest(request.getClass());
                                client.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));

                                if (Config.LOG_ITEM_ENCHANTS) {
                                    if (item.getEnchantLevel() > 0) {
                                        if (support == null) {
                                            LOGGER_ENCHANT.info("Unable to destroy, Character:" + activeChar.getName() + " [" + activeChar.getObjectId() + "] Account:" + activeChar.getAccountName() + " IP:" + activeChar.getIPAddress() + ", +" + item.getEnchantLevel() + " " + item.getName() + "(" + item.getCount() + ") [" + item.getObjectId() + "], " + scroll.getName() + "(" + scroll.getCount() + ") [" + scroll.getObjectId() + "]");
                                        } else {
                                            LOGGER_ENCHANT.info("Unable to destroy, Character:" + activeChar.getName() + " [" + activeChar.getObjectId() + "] Account:" + activeChar.getAccountName() + " IP:" + activeChar.getIPAddress() + ", +" + item.getEnchantLevel() + " " + item.getName() + "(" + item.getCount() + ") [" + item.getObjectId() + "], " + scroll.getName() + "(" + scroll.getCount() + ") [" + scroll.getObjectId() + "], " + support.getName() + "(" + support.getCount() + ") [" + support.getObjectId() + "]");
                                        }
                                    } else if (support == null) {
                                        LOGGER_ENCHANT.info("Unable to destroy, Character:" + activeChar.getName() + " [" + activeChar.getObjectId() + "] Account:" + activeChar.getAccountName() + " IP:" + activeChar.getIPAddress() + ", " + item.getName() + "(" + item.getCount() + ") [" + item.getObjectId() + "], " + scroll.getName() + "(" + scroll.getCount() + ") [" + scroll.getObjectId() + "]");
                                    } else {
                                        LOGGER_ENCHANT.info("Unable to destroy, Character:" + activeChar.getName() + " [" + activeChar.getObjectId() + "] Account:" + activeChar.getAccountName() + " IP:" + activeChar.getIPAddress() + ", " + item.getName() + "(" + item.getCount() + ") [" + item.getObjectId() + "], " + scroll.getName() + "(" + scroll.getCount() + ") [" + scroll.getObjectId() + "], " + support.getName() + "(" + support.getCount() + ") [" + support.getObjectId() + "]");
                                    }
                                }
                                return;
                            }

                            L2World.getInstance().removeObject(item);
                            L2ItemInstance crystals = null;
                            if (crystalId != 0) {
                                crystals = activeChar.getInventory().addItem("Enchant", crystalId, count, activeChar, item);

                                final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
                                sm.addItemName(crystals);
                                sm.addLong(count);
                                client.sendPacket(sm);
                            }

                            if (!Config.FORCE_INVENTORY_UPDATE) {
                                if (crystals != null) {
                                    iu.addItem(crystals);
                                }
                            }

                            if (crystalId == 0) {
                                client.sendPacket(new EnchantResult(EnchantResult.NO_CRYSTAL, 0, 0));
                            } else {
                                client.sendPacket(new EnchantResult(EnchantResult.FAIL, crystalId, count));
                            }

                            if (Config.LOG_ITEM_ENCHANTS) {
                                if (item.getEnchantLevel() > 0) {
                                    if (support == null) {
                                        LOGGER_ENCHANT.info("Fail, Character:" + activeChar.getName() + " [" + activeChar.getObjectId() + "] Account:" + activeChar.getAccountName() + " IP:" + activeChar.getIPAddress() + ", +" + item.getEnchantLevel() + " " + item.getName() + "(" + item.getCount() + ") [" + item.getObjectId() + "], " + scroll.getName() + "(" + scroll.getCount() + ") [" + scroll.getObjectId() + "]");
                                    } else {
                                        LOGGER_ENCHANT.info("Fail, Character:" + activeChar.getName() + " [" + activeChar.getObjectId() + "] Account:" + activeChar.getAccountName() + " IP:" + activeChar.getIPAddress() + ", +" + item.getEnchantLevel() + " " + item.getName() + "(" + item.getCount() + ") [" + item.getObjectId() + "], " + scroll.getName() + "(" + scroll.getCount() + ") [" + scroll.getObjectId() + "], " + support.getName() + "(" + support.getCount() + ") [" + support.getObjectId() + "]");
                                    }
                                } else if (support == null) {
                                    LOGGER_ENCHANT.info("Fail, Character:" + activeChar.getName() + " [" + activeChar.getObjectId() + "] Account:" + activeChar.getAccountName() + " IP:" + activeChar.getIPAddress() + ", " + item.getName() + "(" + item.getCount() + ") [" + item.getObjectId() + "], " + scroll.getName() + "(" + scroll.getCount() + ") [" + scroll.getObjectId() + "]");
                                } else {
                                    LOGGER_ENCHANT.info("Fail, Character:" + activeChar.getName() + " [" + activeChar.getObjectId() + "] Account:" + activeChar.getAccountName() + " IP:" + activeChar.getIPAddress() + ", " + item.getName() + "(" + item.getCount() + ") [" + item.getObjectId() + "], " + scroll.getName() + "(" + scroll.getCount() + ") [" + scroll.getObjectId() + "], " + support.getName() + "(" + support.getCount() + ") [" + support.getObjectId() + "]");
                                }
                            }
                        }
                    }
                    break;
                }
            }

            if (!Config.FORCE_INVENTORY_UPDATE) {
                if (scroll.getCount() == 0) {
                    iu.addRemovedItem(scroll);
                } else {
                    iu.addModifiedItem(scroll);
                }

                if (item.getCount() == 0) {
                    iu.addRemovedItem(item);
                } else {
                    iu.addModifiedItem(item);
                }

                if (support != null) {
                    if (support.getCount() == 0) {
                        iu.addRemovedItem(support);
                    } else {
                        iu.addModifiedItem(support);
                    }
                }

                activeChar.sendInventoryUpdate(iu);
            } else {
                activeChar.sendItemList();
            }

            request.setProcessing(false);
            activeChar.broadcastUserInfo(UserInfoType.ENCHANTLEVEL);
        }
    }
}
