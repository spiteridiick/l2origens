/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.others;

import org.l2j.gameserver.enums.ChatType;
import org.l2j.gameserver.model.actor.L2Npc;
import org.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.model.holders.MinionHolder;
import org.l2j.gameserver.network.NpcStringId;

import ai.AbstractNpcAI;

/**
 * Timak Orc Troop Leader AI.
 * @author Mobius
 */
public final class TimakOrcTroopLeader extends AbstractNpcAI
{
	private static final int TIMAK_ORC_TROOP_LEADER = 20767;
	private static final NpcStringId[] ON_ATTACK_MSG =
	{
		NpcStringId.COME_OUT_YOU_CHILDREN_OF_DARKNESS,
		NpcStringId.SHOW_YOURSELVES,
		NpcStringId.DESTROY_THE_ENEMY_MY_BROTHERS,
		NpcStringId.FORCES_OF_DARKNESS_FOLLOW_ME
	};
	
	private TimakOrcTroopLeader()
	{
		addAttackId(TIMAK_ORC_TROOP_LEADER);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (npc.isMonster())
		{
			final L2MonsterInstance monster = (L2MonsterInstance) npc;
			if (!monster.isTeleporting())
			{
				if (getRandom(1, 100) <= npc.getParameters().getInt("SummonPrivateRate", 0))
				{
					for (MinionHolder is : npc.getParameters().getMinionList("Privates"))
					{
						addMinion((L2MonsterInstance) npc, is.getId());
					}
					npc.broadcastSay(ChatType.NPC_GENERAL, ON_ATTACK_MSG[getRandom(ON_ATTACK_MSG.length)]);
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	public static AbstractNpcAI provider()
	{
		return new TimakOrcTroopLeader();
	}
}