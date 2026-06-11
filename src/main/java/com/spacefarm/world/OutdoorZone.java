package com.spacefarm.world;

import java.util.*;

/**
 * Represents the outdoor zone with scavenging locations arranged around a border.
 */
public class OutdoorZone {
    private List<ScavengingLocation> locations;
    private int baseX, baseY, baseWidth, baseHeight;
    private int borderX, borderY, borderWidth, borderHeight;
    
    public OutdoorZone(BaseZone baseZone, int mapWidth, int mapHeight) {
        locations = new ArrayList<>();
        this.baseX = baseZone.getBaseX();
        this.baseY = baseZone.getBaseY();
        this.baseWidth = baseZone.getBaseWidth();
        this.baseHeight = baseZone.getBaseHeight();
        
        initializeBorder();
        initializeLocations();
    }
    
    /**
     * Initialize the grey border around the base.
     */
    private void initializeBorder() {
        int borderWidth = OutdoorConstants.BORDER_WIDTH;
        this.borderX = baseX - borderWidth;
        this.borderY = baseY - borderWidth;
        this.borderWidth = baseWidth + 2 * borderWidth;
        this.borderHeight = baseHeight + 2 * borderWidth;
    }
    
    /**
     * Initialize 6 scavenging locations scattered around the border.
     */
    private void initializeLocations() {
        // Regular scavenging locations
        int width = OutdoorConstants.OUTDOOR_LOCATION_WIDTH;
        int height = OutdoorConstants.OUTDOOR_LOCATION_HEIGHT;
        
        // Define 5 positions around the border (top, bottom, left, right, corners)
        int[][] positions = {
            // Top edge - center
            {baseX + baseWidth / 2 - width / 2, borderY + 5},
            // Bottom edge - center
            {baseX + baseWidth / 2 - width / 2, borderY + borderHeight - height - 5},
            // Left edge - center
            {borderX + 5, baseY + baseHeight / 2 - height / 2},
            // Right edge - center
            {borderX + borderWidth - width - 5, baseY + baseHeight / 2 - height / 2},
            // Top-left corner
            {borderX + 10, borderY + 10}
        };
        
        for (int i = 0; i < OutdoorConstants.NUM_LOCATIONS; i++) {
            int x = positions[i][0];
            int y = positions[i][1];
            int color = OutdoorConstants.LOCATION_COLORS[i];
            
            ScavengingLocation location = new ScavengingLocation(x, y, width, height, color);
            locations.add(location);
        }
        
        // Add seed wheel location at bottom-right corner
        int seedWheelWidth = SeedWheelConstants.SEED_WHEEL_WIDTH;
        int seedWheelHeight = SeedWheelConstants.SEED_WHEEL_HEIGHT;
        int seedWheelX = borderX + borderWidth - seedWheelWidth - 5;
        int seedWheelY = borderY + borderHeight - seedWheelHeight - 5;
        int seedWheelColor = 0x8b7355;  // Brown color for seed wheel area
        
        ScavengingLocation seedWheelLocation = new ScavengingLocation(
            seedWheelX, seedWheelY, seedWheelWidth, seedWheelHeight, 
            seedWheelColor, ScavengingLocation.LocationType.SEED_WHEEL
        );
        locations.add(seedWheelLocation);
    }
    
    /**
     * Check if a tile coordinate is within the border zone.
     */
    public boolean isInBorder(TileCoord coord) {
        return isInBorder(coord.x(), coord.y());
    }
    
    public boolean isInBorder(int x, int y) {
        return x >= borderX && x < borderX + borderWidth &&
               y >= borderY && y < borderY + borderHeight &&
               !(x >= baseX && x < baseX + baseWidth &&
                 y >= baseY && y < baseY + baseHeight);  // Exclude base itself
    }
    
    /**
     * Check if a tile coordinate is within any outdoor location.
     */
    public boolean isInOutdoor(TileCoord coord) {
        return isInOutdoor(coord.x(), coord.y());
    }
    
    public boolean isInOutdoor(int x, int y) {
        for (ScavengingLocation location : locations) {
            if (location.contains(x, y)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get the location containing the given tile coordinate.
     */
    public ScavengingLocation getLocationAt(TileCoord coord) {
        return getLocationAt(coord.x(), coord.y());
    }
    
    public ScavengingLocation getLocationAt(int x, int y) {
        for (ScavengingLocation location : locations) {
            if (location.contains(x, y)) {
                return location;
            }
        }
        return null;
    }
    
    /**
     * Get all scavenging locations.
     */
    public List<ScavengingLocation> getLocations() {
        return new ArrayList<>(locations);
    }
    
    /**
     * Get all currently scavenging locations.
     */
    public List<ScavengingLocation> getScavengingLocations() {
        List<ScavengingLocation> scavenging = new ArrayList<>();
        for (ScavengingLocation location : locations) {
            if (location.isScavenging()) {
                scavenging.add(location);
            }
        }
        return scavenging;
    }
    /**
     * Marks a scavenging location as greened (nature reclaims the area).
     * Call this when a tree phase is confirmed.
     * Phase index 0-4 maps to regular locations; index 5 is the seed wheel location.
     */
    public void greenLocation(int locationIndex) {
        if (locationIndex >= 0 && locationIndex < locations.size()) {
            locations.get(locationIndex).setGreened(true);
        }
    }

    /**
     * Returns how many locations are currently greened.
     */
    public int getGreenedLocationCount() {
        int count = 0;
        for (ScavengingLocation loc : locations) {
            if (loc.isGreened()) count++;
        }
        return count;
    }
    
    // Getters
    public int getBorderX() { return borderX; }
    public int getBorderY() { return borderY; }
    public int getBorderWidth() { return borderWidth; }
    public int getBorderHeight() { return borderHeight; }
    public int getBaseWidth() { return baseWidth; }
    public int getBaseHeight() { return baseHeight; }
}

