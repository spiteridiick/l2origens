package org.l2j.gameserver.network.clientpackets.training;

import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.model.holders.TrainingHolder;
import org.l2j.gameserver.network.clientpackets.ClientPacket;
import org.l2j.gameserver.network.serverpackets.training.ExTrainingZone_Leaving;

/**
 * @author Sdw
 */
public class NotifyTrainingRoomEnd extends ClientPacket {
    @Override
    public void readImpl() {
        // Nothing to read
    }

    @Override
    public void runImpl() {
        final L2PcInstance activeChar = client.getActiveChar();
        if (activeChar == null) {
            return;
        }

        final TrainingHolder holder = activeChar.getTraingCampInfo();
        if (holder == null) {
            return;
        }

        if (holder.isTraining()) {
            holder.setEndTime(System.currentTimeMillis());
            activeChar.setTraingCampInfo(holder);
            activeChar.enableAllSkills();
            activeChar.setIsInvul(false);
            activeChar.setInvisible(false);
            activeChar.setIsImmobilized(false);
            activeChar.teleToLocation(activeChar.getLastLocation());
            activeChar.sendPacket(ExTrainingZone_Leaving.STATIC_PACKET);
            holder.setEndTime(System.currentTimeMillis());
            activeChar.setTraingCampInfo(holder);
        }
    }
}
