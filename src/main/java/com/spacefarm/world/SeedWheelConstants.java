package com.spacefarm.world;

/**
 * Constants for the seed wheel fortune location.
 */
public class SeedWheelConstants {
    // Location position and size
    public static final int SEED_WHEEL_WIDTH = 20;
    public static final int SEED_WHEEL_HEIGHT = 10;

    // Wheel spin parameters
    public static final float SPIN_DURATION_SECONDS = 8f;
    public static final float MIN_ROTATIONS = 3f;  // Minimum rotations before stopping
    public static final float MAX_ROTATION_SPEED = 360f * 6;  // Degrees per second max

    // Seed distribution (percentages)
    public static final float LEGENDARY_SEED_CHANCE = 10f;   // 10%
    public static final float RARE_SEED_CHANCE = 30f;        // 30%
    public static final float COMMON_SEED_CHANCE = 60f;      // 60%

    // Seed rewards (quantities)
    public static final int LEGENDARY_SEED_REWARD = 5;
    public static final int RARE_SEED_REWARD = 10;
    public static final int COMMON_SEED_REWARD = 20;

    // Oxygen restoration (percentage of max oxygen)
    public static final float RARE_SEED_OXYGEN_RESTORE = 20f;
    public static final float LEGENDARY_SEED_OXYGEN_RESTORE = 50f;

    // Common seed info
    public static final float COMMON_SEED_OXYGEN_RESTORE = 0f;  // Common seeds don't restore oxygen

    // Cooldown before next spin
    public static final long SPIN_COOLDOWN_MILLIS = 30000;  // 30 seconds between spins

    // UI colors
    public static final int LEGENDARY_ZONE_COLOR = 0xffaa00;  // Orange/gold
    public static final int RARE_ZONE_COLOR = 0xff00ff;       // Purple/magenta
    public static final int COMMON_ZONE_COLOR = 0x00aa00;     // Green
    public static final int WHEEL_BACKGROUND_COLOR = 0x222222; // Dark grey
}

