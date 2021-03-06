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
import org.l2j.gameserver.model.effects.L2EffectType;
import org.l2j.gameserver.model.items.instance.L2ItemInstance;
import org.l2j.gameserver.model.skills.Skill;
import org.l2j.gameserver.model.stats.Stats;
import org.l2j.gameserver.network.SystemMessageId;
import org.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * Heal Percent effect implementation.
 * @author UnAfraid
 */
public final class HealPercent extends AbstractEffect
{
	private final int _power;
	
	public HealPercent(StatsSet params)
	{
		_power = params.getInt("power", 0);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.HEAL;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(L2Character effector, L2Character effected, Skill skill, L2ItemInstance item)
	{
		if (effected.isDead() || effected.isDoor() || effected.isHpBlocked())
		{
			return;
		}
		
		double amount = 0;
		final double power = _power;
		final boolean full = (power == 100.0);
		
		amount = full ? effected.getMaxHp() : (effected.getMaxHp() * power) / 100.0;
		if ((item != null) && (item.isPotion() || item.isElixir()))
		{
			amount += effected.getStat().getValue(Stats.ADDITIONAL_POTION_HP, 0);
		}
		// Prevents overheal
		amount = Math.min(amount, effected.getMaxRecoverableHp() - effected.getCurrentHp());
		if (amount >= 0)
		{
			if (amount != 0)
			{
				final double newHp = amount + effected.getCurrentHp();
				effected.setCurrentHp(newHp, false);
				effected.broadcastStatusUpdate(effector);
			}
			
			SystemMessage sm;
			if (effector.getObjectId() != effected.getObjectId())
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HP_HAS_BEEN_RESTORED_BY_C1);
				sm.addString(effector.getName());
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_HAS_BEEN_RESTORED);
			}
			sm.addInt((int) amount);
			effected.sendPacket(sm);
		}
		else
		{
			final double damage = -amount;
			effected.reduceCurrentHp(damage, effector, skill, false, false, false, false);
			effector.sendDamageMessage(effected, skill, (int) damage, 0, false, false);
		}
	}
}
