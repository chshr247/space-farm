package com.spacefarm.world;

/**
 * Constants for outdoor zone scavenging system.
 */
public class OutdoorConstants {
    // Outdoor zone dimensions
    public static final int OUTDOOR_LOCATION_WIDTH = 10;
    public static final int OUTDOOR_LOCATION_HEIGHT = 10;
    public static final int NUM_LOCATIONS = 5;

    // Border zone (grey area around base)
    public static final int BORDER_WIDTH = 40;                       // Grey border width in tiles
    public static final int BORDER_COLOR = 0x4a4a4a;                 // Dark grey color

    // Scavenging requirements and costs
    public static final long SCAVENGING_DURATION_MILLIS = 180000;    // 3 minutes for scavenging
    public static final long SCAVENGING_COOLDOWN_MILLIS = 180000;    // 3 minutes cooldown before re-scavenge
    public static final float OXYGEN_DECREASE_INTERVAL = 10f;        // Decrease oxygen every 10 seconds
    public static final float OXYGEN_DECREASE_AMOUNT = 2f;           // 2% per interval
    public static final int CROPS_REQUIRED_PER_SCAVENGE = 1;         // Crops needed to start scavenging (not implemented yet)

    // Rewards
    public static final int CRYSTALS_PER_LOCATION = 1;               // Crystals reward per location clear

    // Location colors (RGB) for different locations
    public static final int[] LOCATION_COLORS = {
        0x3d5a2d,  // Dark green
        0x4a6d3f,  // Medium green
        0x5a7d4f,  // Light green
        0x2d5a42,  // Teal green
        0x4a7d5a   // Forest green
    };
}

