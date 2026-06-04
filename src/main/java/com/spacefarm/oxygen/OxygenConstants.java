package com.spacefarm.oxygen;

/**
 * Constants for oxygen system management.
 */
public class OxygenConstants {
    // Oxygen decrease rates (outside base)
    public static float OXYGEN_DECREASE_PER_SECOND = 0.2f;  // 2% per 10 seconds = 0.2% per second
    public static float OXYGEN_DECREASE_INTERVAL = 10f;      // Check every 10 seconds
    public static float OXYGEN_DECREASE_AMOUNT = 4f;         // Decrease by 4% per interval

    // Oxygen increase from food
    public static float OXYGEN_INCREASE_FROM_FOOD = 10f;     // 10% per plant food

    // Oxygen limits
    public static float MAX_OXYGEN = 100f;
    public static float MIN_OXYGEN = 0f;

    // Base detection radius (in tiles)
    public static float BASE_DETECTION_RADIUS = 5f;

    // Default starting oxygen
    public static float STARTING_OXYGEN = 100f;
}

