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
package handlers.conditions;

import org.l2j.gameserver.model.L2Object;
import org.l2j.gameserver.model.StatsSet;
import org.l2j.gameserver.model.actor.L2Character;
import org.l2j.gameserver.model.conditions.ICondition;

/**
 * @author Sdw
 */
public class NpcLevelCondition implements ICondition
{
	private final int _minLevel;
	private final int _maxLevel;
	
	public NpcLevelCondition(StatsSet params)
	{
		_minLevel = params.getInt("minLevel");
		_maxLevel = params.getInt("maxLevel");
	}
	
	@Override
	public boolean test(L2Character creature, L2Object object)
	{
		return object.isNpc() && (((L2Character) object).getLevel() >= _minLevel) && (((L2Character) object).getLevel() < _maxLevel);
	}
	
}
