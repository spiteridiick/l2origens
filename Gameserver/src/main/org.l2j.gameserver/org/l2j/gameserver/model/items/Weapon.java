package org.l2j.gameserver.model.items;

import org.l2j.commons.util.Rnd;
import org.l2j.commons.util.Util;
import org.l2j.gameserver.enums.ItemSkillType;
import org.l2j.gameserver.model.StatsSet;
import org.l2j.gameserver.world.World;
import org.l2j.gameserver.model.actor.Creature;
import org.l2j.gameserver.model.actor.Npc;
import org.l2j.gameserver.model.events.EventDispatcher;
import org.l2j.gameserver.model.events.impl.character.npc.OnNpcSkillSee;
import org.l2j.gameserver.model.items.type.WeaponType;
import org.l2j.gameserver.model.skills.Skill;
import org.l2j.gameserver.model.stats.Formulas;
import org.l2j.gameserver.network.SystemMessageId;
import org.l2j.gameserver.network.serverpackets.SystemMessage;

import static org.l2j.gameserver.util.GameUtils.isPlayer;

/**
 * This class is dedicated to the management of weapons.
 */
public final class Weapon extends ItemTemplate {
    private WeaponType _type;
    private boolean _isMagicWeapon;
    private int _soulShotCount;
    private int _spiritShotCount;
    private int _mpConsume;
    private int _baseAttackRadius;
    private int _baseAttackAngle;
    private int _changeWeaponId;

    private int _reducedSoulshot;
    private int _reducedSoulshotChance;

    private int _reducedMpConsume;
    private int _reducedMpConsumeChance;

    private boolean _isForceEquip;
    private boolean _isAttackWeapon;
    private boolean _useWeaponSkillsOnly;

    /**
     * Constructor for Weapon.
     *
     * @param set the StatsSet designating the set of couples (key,value) characterizing the weapon.
     */
    public Weapon(StatsSet set) {
        super(set);
    }

    @Override
    public void set(StatsSet set) {
        super.set(set);
        _type = WeaponType.valueOf(set.getString("weapon_type", "none").toUpperCase());
        _type1 = ItemTemplate.TYPE1_WEAPON_RING_EARRING_NECKLACE;
        _type2 = ItemTemplate.TYPE2_WEAPON;
        _isMagicWeapon = set.getBoolean("is_magic_weapon", false);
        _soulShotCount = set.getInt("soulshots", 0);
        _spiritShotCount = set.getInt("spiritshots", 0);
        _mpConsume = set.getInt("mp_consume", 0);
        final String[] damageRange = set.getString("damage_range", "").split(";"); // 0?;0?;fan sector;base attack angle
        if ((damageRange.length > 1) && Util.isInteger(damageRange[2]) && Util.isInteger(damageRange[3])) {
            _baseAttackRadius = Integer.parseInt(damageRange[2]);
            _baseAttackAngle = 360 - Integer.parseInt(damageRange[3]);
        } else {
            _baseAttackRadius = 40;
            _baseAttackAngle = 240; // 360 - 120
        }

        final String[] reduced_soulshots = set.getString("reduced_soulshot", "").split(",");
        _reducedSoulshotChance = (reduced_soulshots.length == 2) ? Integer.parseInt(reduced_soulshots[0]) : 0;
        _reducedSoulshot = (reduced_soulshots.length == 2) ? Integer.parseInt(reduced_soulshots[1]) : 0;

        final String[] reduced_mpconsume = set.getString("reduced_mp_consume", "").split(",");
        _reducedMpConsumeChance = (reduced_mpconsume.length == 2) ? Integer.parseInt(reduced_mpconsume[0]) : 0;
        _reducedMpConsume = (reduced_mpconsume.length == 2) ? Integer.parseInt(reduced_mpconsume[1]) : 0;

        _changeWeaponId = set.getInt("change_weaponId", 0);
        _isForceEquip = set.getBoolean("isForceEquip", false);
        _isAttackWeapon = set.getBoolean("isAttackWeapon", true);
        _useWeaponSkillsOnly = set.getBoolean("useWeaponSkillsOnly", false);
    }

    /**
     * @return the type of Weapon
     */
    @Override
    public WeaponType getItemType() {
        return _type;
    }

    /**
     * @return the ID of the Etc item after applying the mask.
     */
    @Override
    public int getItemMask() {
        return _type.mask();
    }

    /**
     * @return {@code true} if the weapon is magic, {@code false} otherwise.
     */
    @Override
    public boolean isMagicWeapon() {
        return _isMagicWeapon;
    }

    /**
     * @return the quantity of SoulShot used.
     */
    public int getSoulShotCount() {
        return _soulShotCount;
    }

    /**
     * @return the quantity of SpiritShot used.
     */
    public int getSpiritShotCount() {
        return _spiritShotCount;
    }

    /**
     * @return the reduced quantity of SoultShot used.
     */
    public int getReducedSoulShot() {
        return _reducedSoulshot;
    }

    /**
     * @return the chance to use Reduced SoultShot.
     */
    public int getReducedSoulShotChance() {
        return _reducedSoulshotChance;
    }

    /**
     * @return the MP consumption with the weapon.
     */
    public int getMpConsume() {
        return _mpConsume;
    }

    public int getBaseAttackRadius() {
        return _baseAttackRadius;
    }

    public int getBaseAttackAngle() {
        return _baseAttackAngle;
    }

    /**
     * @return the reduced MP consumption with the weapon.
     */
    public int getReducedMpConsume() {
        return _reducedMpConsume;
    }

    /**
     * @return the chance to use getReducedMpConsume()
     */
    public int getReducedMpConsumeChance() {
        return _reducedMpConsumeChance;
    }

    /**
     * @return the Id in which weapon this weapon can be changed.
     */
    public int getChangeWeaponId() {
        return _changeWeaponId;
    }

    /**
     * @return {@code true} if the weapon is force equip, {@code false} otherwise.
     */
    public boolean isForceEquip() {
        return _isForceEquip;
    }

    /**
     * @return {@code true} if the weapon is attack weapon, {@code false} otherwise.
     */
    public boolean isAttackWeapon() {
        return _isAttackWeapon;
    }

    /**
     * @return {@code true} if the weapon is skills only, {@code false} otherwise.
     */
    public boolean useWeaponSkillsOnly() {
        return _useWeaponSkillsOnly;
    }

    /**
     * @param caster  the Creature pointing out the caster
     * @param target  the Creature pointing out the target
     * @param trigger trigger skill
     * @param type type of skill
     */
    public void applyConditionalSkills(Creature caster, Creature target, Skill trigger, ItemSkillType type) {
        forEachSkill(type, holder ->
        {
            final Skill skill = holder.getSkill();
            if (Rnd.get(100) >= holder.getChance()) {
                return;
            }

            if (type == ItemSkillType.ON_MAGIC_SKILL) {
                // Trigger only if both are good or bad magic.
                if (trigger.isBad() != skill.isBad()) {
                    return;
                }

                // No Trigger if not Magic Skill or is toggle
                if (trigger.isMagic() != skill.isMagic()) {
                    return;
                }

                // No Trigger if skill is toggle
                if (trigger.isToggle()) {
                    return;
                }

                if (skill.isBad() && (Formulas.calcShldUse(caster, target) == Formulas.SHIELD_DEFENSE_PERFECT_BLOCK)) {
                    return;
                }
            }

            // Skill condition not met
            if (!skill.checkCondition(caster, target)) {
                return;
            }

            skill.activateSkill(caster, target);

            // TODO: Verify if this applies ONLY to ON_MAGIC_SKILL!
            if (type == ItemSkillType.ON_MAGIC_SKILL) {
                // notify quests of a skill use
                if (isPlayer(caster)) {
                    World.getInstance().forEachVisibleObjectInRange(caster, Npc.class, 1000, npc -> EventDispatcher.getInstance().notifyEventAsync(new OnNpcSkillSee(npc, caster.getActingPlayer(), skill, false, target), npc));
                }
                if (isPlayer(caster)) {
                    final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ACTIVATED);
                    sm.addSkillName(skill);
                    caster.sendPacket(sm);
                }
            }
        });
    }
}
