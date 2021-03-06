package org.l2j.gameserver.data.elemental;

import org.l2j.gameserver.model.stats.Stats;

public enum ElementalType {
    NONE,
    FIRE,
    WATER,
    WIND,
    EARTH;

    public byte getId() {
        return (byte) (ordinal());
    }

    public static ElementalType of(byte elementId) {
        return values()[elementId];
    }

    public ElementalType getDominating() {
        return dominating(this);
    }

    public ElementalType dominating(ElementalType elementalType) {
        return switch (elementalType) {
            case FIRE -> WATER;
            case WATER -> EARTH;
            case WIND ->  FIRE;
            case EARTH -> WIND;
            default -> NONE;
        };
    }

    public Stats getAttackStat() {
        return switch (this) {
            case EARTH -> Stats.ELEMENTAL_SPIRIT_EARTH_ATTACK;
            case WIND -> Stats.ELEMENTAL_SPIRIT_WIND_ATTACK;
            case FIRE -> Stats.ELEMENTAL_SPIRIT_FIRE_ATTACK;
            case WATER -> Stats.ELEMENTAL_SPIRIT_WATER_ATTACK;
            default -> null;
        };
    }

    public Stats getDefenseStat() {
        return switch (this) {
            case EARTH -> Stats.ELEMENTAL_SPIRIT_EARTH_DEFENSE;
            case WIND -> Stats.ELEMENTAL_SPIRIT_WIND_DEFENSE;
            case FIRE -> Stats.ELEMENTAL_SPIRIT_FIRE_DEFENSE;
            case WATER -> Stats.ELEMENTAL_SPIRIT_WATER_DEFENSE;
            default -> null;
        };
    }
}
