package org.l2j.gameserver.network.clientpackets;

import org.l2j.gameserver.instancemanager.QuestManager;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.model.quest.Quest;

/**
 * @author Sdw
 */
public class RequestAddExpandQuestAlarm extends ClientPacket {
    private int _questId;

    @Override
    public void readImpl() {
        _questId = readInt();
    }

    @Override
    public void runImpl() {
        final L2PcInstance activeChar = client.getActiveChar();
        if (activeChar == null) {
            return;
        }

        final Quest quest = QuestManager.getInstance().getQuest(_questId);
        if (quest != null) {
            quest.sendNpcLogList(activeChar);
        }
    }
}
