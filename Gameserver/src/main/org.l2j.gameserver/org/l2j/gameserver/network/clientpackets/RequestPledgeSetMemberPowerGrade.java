package org.l2j.gameserver.network.clientpackets;

import org.l2j.gameserver.model.ClanPrivilege;
import org.l2j.gameserver.model.L2Clan;
import org.l2j.gameserver.model.L2ClanMember;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.network.SystemMessageId;
import org.l2j.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import org.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * Format: (ch) Sd
 *
 * @author -Wooden-
 */
public final class RequestPledgeSetMemberPowerGrade extends ClientPacket {
    private String _member;
    private int _powerGrade;

    @Override
    public void readImpl() {
        _member = readString();
        _powerGrade = readInt();
    }

    @Override
    public void runImpl() {
        final L2PcInstance activeChar = client.getActiveChar();
        if (activeChar == null) {
            return;
        }

        final L2Clan clan = activeChar.getClan();
        if (clan == null) {
            return;
        }

        if (!activeChar.hasClanPrivilege(ClanPrivilege.CL_MANAGE_RANKS)) {
            return;
        }

        final L2ClanMember member = clan.getClanMember(_member);
        if (member == null) {
            return;
        }

        if (member.getObjectId() == clan.getLeaderId()) {
            return;
        }

        if (member.getPledgeType() == L2Clan.SUBUNIT_ACADEMY) {
            // also checked from client side
            activeChar.sendPacket(SystemMessageId.THAT_PRIVILEGE_CANNOT_BE_GRANTED_TO_A_CLAN_ACADEMY_MEMBER);
            return;
        }

        member.setPowerGrade(_powerGrade);
        clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(member));
        clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_C1_S_PRIVILEGE_LEVEL_HAS_BEEN_CHANGED_TO_S2).addString(member.getName()).addInt(_powerGrade));
    }

}