package org.l2j.gameserver.network.clientpackets;

import org.l2j.gameserver.instancemanager.ClanEntryManager;
import org.l2j.gameserver.model.ClanPrivilege;
import org.l2j.gameserver.model.L2Clan;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.model.clan.entry.PledgeRecruitInfo;
import org.l2j.gameserver.network.SystemMessageId;
import org.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Sdw
 */
public class RequestPledgeRecruitBoardAccess extends ClientPacket {
    private int _applyType;
    private int _karma;
    private String _information;
    private String _datailedInformation;
    private int _applicationType;
    private int _recruitingType;

    @Override
    public void readImpl() {
        _applyType = readInt();
        _karma = readInt();
        _information = readString();
        _datailedInformation = readString();
        _applicationType = readInt(); // 0 - Allow, 1 - Public
        _recruitingType = readInt(); // 0 - Main clan
    }

    @Override
    public void runImpl() {
        final L2PcInstance activeChar = client.getActiveChar();

        if (activeChar == null) {
            return;
        }

        final L2Clan clan = activeChar.getClan();

        if (clan == null) {
            activeChar.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_OR_SOMEONE_WITH_RANK_MANAGEMENT_AUTHORITY_MAY_REGISTER_THE_CLAN);
            return;
        }

        if (!activeChar.hasClanPrivilege(ClanPrivilege.CL_MANAGE_RANKS)) {
            activeChar.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_OR_SOMEONE_WITH_RANK_MANAGEMENT_AUTHORITY_MAY_REGISTER_THE_CLAN);
            return;
        }

        final PledgeRecruitInfo pledgeRecruitInfo = new PledgeRecruitInfo(clan.getId(), _karma, _information, _datailedInformation, _applicationType, _recruitingType);

        switch (_applyType) {
            case 0: // remove
            {
                ClanEntryManager.getInstance().removeFromClanList(clan.getId());
                break;
            }
            case 1: // add
            {
                if (ClanEntryManager.getInstance().addToClanList(clan.getId(), pledgeRecruitInfo)) {
                    activeChar.sendPacket(SystemMessageId.ENTRY_APPLICATION_COMPLETE_USE_MY_APPLICATION_TO_CHECK_OR_CANCEL_YOUR_APPLICATION_APPLICATION_IS_AUTOMATICALLY_CANCELLED_AFTER_30_DAYS_IF_YOU_CANCEL_APPLICATION_YOU_CANNOT_APPLY_AGAIN_FOR_5_MINUTES);
                } else {
                    final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_MAY_APPLY_FOR_ENTRY_AFTER_S1_MINUTE_S_DUE_TO_CANCELLING_YOUR_APPLICATION);
                    sm.addLong(ClanEntryManager.getInstance().getClanLockTime(clan.getId()));
                    activeChar.sendPacket(sm);
                }
                break;
            }
            case 2: // update
            {
                if (ClanEntryManager.getInstance().updateClanList(clan.getId(), pledgeRecruitInfo)) {
                    activeChar.sendPacket(SystemMessageId.ENTRY_APPLICATION_COMPLETE_USE_MY_APPLICATION_TO_CHECK_OR_CANCEL_YOUR_APPLICATION_APPLICATION_IS_AUTOMATICALLY_CANCELLED_AFTER_30_DAYS_IF_YOU_CANCEL_APPLICATION_YOU_CANNOT_APPLY_AGAIN_FOR_5_MINUTES);
                } else {
                    final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_MAY_APPLY_FOR_ENTRY_AFTER_S1_MINUTE_S_DUE_TO_CANCELLING_YOUR_APPLICATION);
                    sm.addLong(ClanEntryManager.getInstance().getClanLockTime(clan.getId()));
                    activeChar.sendPacket(sm);
                }
                break;
            }
        }
    }

}
