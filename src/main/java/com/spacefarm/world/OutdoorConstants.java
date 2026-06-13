package com.spacefarm.world;


public class OutdoorConstants {
    // Outdoor zone dimensions
    public static final int OUTDOOR_LOCATION_WIDTH = 10;
    public static final int OUTDOOR_LOCATION_HEIGHT = 10;
    public static final int NUM_LOCATIONS = 5;

    // Border zone
    public static final int BORDER_WIDTH_X = 14;                     // Horizontal border: (60-32)/2 = 14
    public static final int BORDER_WIDTH_Y = 9;                      // Vertical border: (50-32)/2 = 9
    public static final int BORDER_COLOR = 0x4a4a4a;                 // Dark grey color

    // Scavenging requirements and costs
    public static final long SCAVENGING_DURATION_MILLIS = 10000;     // 10 seconds for scavenging
    public static final long SCAVENGING_COOLDOWN_MILLIS = 10000;     // 10 seconds cooldown before re-scavenge
    public static final float OXYGEN_DECREASE_INTERVAL = 10f;        // Decrease oxygen every 10 seconds
    public static float OXYGEN_DECREASE_AMOUNT = 4f;           // 4% per interval

    // Rewards
    public static final int CRYSTALS_PER_LOCATION = 1;               // Crystals reward per location clear

    // Різні кольори допоки гейм дизайнер не додасть спрайти
    public static final int[] LOCATION_COLORS = {
        0x3d5a2d,  // Dark green
        0x4a6d3f,  // Medium green
        0x5a7d4f,  // Light green
        0x2d5a42,  // Teal green
        0x4a7d5a   // Forest green
    };
}
