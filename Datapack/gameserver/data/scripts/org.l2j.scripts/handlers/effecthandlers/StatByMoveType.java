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

import org.l2j.gameserver.model.StatsSet;
import org.l2j.gameserver.model.actor.L2Character;
import org.l2j.gameserver.model.effects.AbstractEffect;
import org.l2j.gameserver.model.items.instance.L2ItemInstance;
import org.l2j.gameserver.model.skills.Skill;
import org.l2j.gameserver.model.stats.MoveType;
import org.l2j.gameserver.model.stats.Stats;

/**
 * StatByMoveType effect implementation.
 * @author UnAfraid
 */
public class StatByMoveType extends AbstractEffect
{
	private final Stats _stat;
	private final MoveType _type;
	private final double _value;
	
	public StatByMoveType(StatsSet params)
	{
		_stat = params.getEnum("stat", Stats.class);
		_type = params.getEnum("type", MoveType.class);
		_value = params.getDouble("value");
	}
	
	@Override
	public void onStart(L2Character effector, L2Character effected, Skill skill, L2ItemInstance item)
	{
		effected.getStat().mergeMoveTypeValue(_stat, _type, _value);
	}
	
	@Override
	public void onExit(L2Character effector, L2Character effected, Skill skill)
	{
		effected.getStat().mergeMoveTypeValue(_stat, _type, -_value);
	}
	
	@Override
	public boolean onActionTime(L2Character effector, L2Character effected, Skill skill, L2ItemInstance item)
	{
		return skill.isPassive() || skill.isToggle();
	}
}
