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

import java.util.ArrayList;
import java.util.List;

import org.l2j.commons.util.Rnd;
import org.l2j.gameserver.mobius.gameserver.ai.CtrlEvent;
import org.l2j.gameserver.mobius.gameserver.ai.CtrlIntention;
import org.l2j.gameserver.mobius.gameserver.model.L2World;
import org.l2j.gameserver.mobius.gameserver.model.StatsSet;
import org.l2j.gameserver.mobius.gameserver.model.actor.L2Character;
import org.l2j.gameserver.mobius.gameserver.model.effects.AbstractEffect;
import org.l2j.gameserver.mobius.gameserver.model.effects.EffectFlag;
import org.l2j.gameserver.mobius.gameserver.model.items.instance.L2ItemInstance;
import org.l2j.gameserver.mobius.gameserver.model.skills.Skill;
import org.l2j.gameserver.mobius.gameserver.model.stats.Formulas;

/**
 * Confuse effect implementation.
 * @author littlecrow
 */
public final class Confuse extends AbstractEffect
{
	private final int _chance;
	
	public Confuse(StatsSet params)
	{
		_chance = params.getInt("chance", 100);
	}
	
	@Override
	public boolean calcSuccess(L2Character effector, L2Character effected, Skill skill)
	{
		return Formulas.calcProbability(_chance, effector, effected, skill);
	}
	
	@Override
	public long getEffectFlags()
	{
		return EffectFlag.CONFUSED.getMask();
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(L2Character effector, L2Character effected, Skill skill, L2ItemInstance item)
	{
		effected.getAI().notifyEvent(CtrlEvent.EVT_CONFUSED);
		
		final List<L2Character> targetList = new ArrayList<>();
		// Getting the possible targets
		
		L2World.getInstance().forEachVisibleObject(effected, L2Character.class, targetList::add);
		
		// if there is no target, exit function
		if (!targetList.isEmpty())
		{
			// Choosing randomly a new target
			final L2Character target = targetList.get(Rnd.get(targetList.size()));
			// Attacking the target
			effected.setTarget(target);
			effected.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
	}
}