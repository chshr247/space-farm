package com.spacefarm.oxygen;


public class OxygenConstants {
    // Oxygen decrease rates (outside base)
    public static float OXYGEN_DECREASE_PER_SECOND = 0.4f;  // 4% per 10 seconds = 0.4% per second
    public static float OXYGEN_DECREASE_INTERVAL = 10f;      // Check every 10 seconds
    public static float OXYGEN_DECREASE_AMOUNT = 4f;         // Decrease by 4% per interval

    // Oxygen increase from food
    public static float OXYGEN_INCREASE_FROM_FOOD = 10f;     // 10% per default plant food

    // Oxygen limits
    public static float MAX_OXYGEN = 100f;
    public static float MIN_OXYGEN = 0f;

    // Default starting oxygen
    public static float STARTING_OXYGEN = 100f;
}

