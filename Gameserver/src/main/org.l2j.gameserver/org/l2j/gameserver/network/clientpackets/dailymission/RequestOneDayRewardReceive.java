package org.l2j.gameserver.network.clientpackets.dailymission;

import org.l2j.commons.util.Util;
import org.l2j.gameserver.data.database.data.DailyMissionPlayerData;
import org.l2j.gameserver.data.xml.impl.DailyMissionData;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.model.dailymission.DailyMissionDataHolder;
import org.l2j.gameserver.network.clientpackets.ClientPacket;
import org.l2j.gameserver.network.serverpackets.dailymission.ExConnectedTimeAndGettableReward;
import org.l2j.gameserver.network.serverpackets.dailymission.ExOneDayReceiveRewardList;

import java.util.Collection;

/**
 * @author Sdw
 */
public class RequestOneDayRewardReceive extends ClientPacket {
    private int _id;

    @Override
    public void readImpl() {
        _id = readShort();
    }

    @Override
    public void runImpl() {
        final L2PcInstance player = client.getActiveChar();
        if (player == null) {
            return;
        }

        var dailyMissionData = DailyMissionData.getInstance();

        final Collection<DailyMissionDataHolder> missions = dailyMissionData.getDailyMissions(_id);
        if (Util.isNullOrEmpty(missions)) { return;
        }

        missions.stream().filter(o -> o.isDisplayable(player)).forEach(r -> r.requestReward(player));

        player.sendPacket(new ExOneDayReceiveRewardList(player, true));
        player.sendPacket(new ExConnectedTimeAndGettableReward((int) dailyMissionData.getStoredDailyMissionData(player).values().stream().filter(DailyMissionPlayerData::isAvailable).count()));
    }
}
