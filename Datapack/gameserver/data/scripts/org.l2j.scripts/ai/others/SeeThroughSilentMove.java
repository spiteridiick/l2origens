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

import org.l2j.gameserver.model.actor.L2Attackable;
import org.l2j.gameserver.model.actor.L2Npc;

import ai.AbstractNpcAI;

/**
 * See Through Silent Move AI.
 * @author Gigiikun
 */
public class SeeThroughSilentMove extends AbstractNpcAI
{
	//@formatter:off
	private static final int[] MONSTERS =
	{
		20142, 18002, 29009, 29010, 29011, 29012, 29013
	};
	//@formatter:on
	
	private SeeThroughSilentMove()
	{
		addSpawnId(MONSTERS);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.isAttackable())
		{
			((L2Attackable) npc).setSeeThroughSilentMove(true);
		}
		return super.onSpawn(npc);
	}
	
	public static AbstractNpcAI provider()
	{
		return new SeeThroughSilentMove();
	}
}
