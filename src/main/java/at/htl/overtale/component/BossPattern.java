package at.htl.overtale.component;

public enum BossPattern {
    /** Random side, bullet aimed at heart. */
    RANDOM_TARGET,
    /** Multiple bullets fall straight down from the top. */
    RAIN,
    /** 4 bullets from all sides simultaneously, all aimed at heart. */
    CROSSFIRE,
    /** Gravity jump-and-run: heart falls, player jumps over scrolling obstacles. */
    GRAVITY_JUMP,
    /** Laser dodge: warning flash then active laser beams deal damage. */
    LASER,
    /** Concentric burst: bullets fire outward in a circle from the center, rotating each wave. */
    SPIRAL
}
