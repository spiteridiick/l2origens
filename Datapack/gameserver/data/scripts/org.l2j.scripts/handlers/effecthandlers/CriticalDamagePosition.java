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
package handlers.effecthandlers;

import org.l2j.gameserver.enums.Position;
import org.l2j.gameserver.model.StatsSet;
import org.l2j.gameserver.model.actor.L2Character;
import org.l2j.gameserver.model.effects.AbstractEffect;
import org.l2j.gameserver.model.items.instance.L2ItemInstance;
import org.l2j.gameserver.model.skills.Skill;
import org.l2j.gameserver.model.stats.Stats;
import org.l2j.gameserver.util.MathUtil;

/**
 * @author Sdw
 */
public class CriticalDamagePosition extends AbstractEffect
{
	private final double _amount;
	private final Position _position;
	
	public CriticalDamagePosition(StatsSet params)
	{
		_amount = params.getDouble("amount", 0);
		_position = params.getEnum("position", Position.class, Position.FRONT);
	}
	
	@Override
	public void onStart(L2Character effector, L2Character effected, Skill skill, L2ItemInstance item)
	{
		effected.getStat().mergePositionTypeValue(Stats.CRITICAL_DAMAGE, _position, (_amount / 100) + 1, MathUtil::mul);
	}
	
	@Override
	public void onExit(L2Character effector, L2Character effected, Skill skill)
	{
		effected.getStat().mergePositionTypeValue(Stats.CRITICAL_DAMAGE, _position, (_amount / 100) + 1, MathUtil::div);
	}
}
