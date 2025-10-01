package org.cloudburstmc.protocol.bedrock.data;

public enum Ability {
    BUILD,
    MINE,
    DOORS_AND_SWITCHES,
    OPEN_CONTAINERS,
    ATTACK_PLAYERS,
    ATTACK_MOBS,
    OPERATOR_COMMANDS,
    TELEPORT,
    INVULNERABLE,
    FLYING,
    MAY_FLY,
    INSTABUILD,
    LIGHTNING,
    FLY_SPEED,
    WALK_SPEED,
    MUTED,
    WORLD_BUILDER,
    NO_CLIP,
    /**
     * @since v575
     */
    PRIVILEGED_BUILDER,
    /**
     * @since v776
     */
    VERTICAL_FLY_SPEED;

    public enum Type {
        NONE,
        BOOLEAN,
        FLOAT
    }
}
