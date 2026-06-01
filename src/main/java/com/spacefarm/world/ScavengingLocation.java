package com.spacefarm.world;

/**
 * Represents a scavenging location in the outdoor zone.
 */
public class ScavengingLocation {
    public enum LocationType {
        CRYSTAL,      // Regular scavenging location - drops crystals
        SEED_WHEEL    // Seed wheel - fortune wheel with different seed types
    }

    private TileCoord topLeft;
    private int width;
    private int height;
    private int color;
    private boolean isCleared;
    private long scavengingStartTime;
    private boolean isScavenging;
    private long lastClearedTime;
    private LocationType locationType;

    // Constructors
    public ScavengingLocation(int x, int y, int width, int height, int color) {
        this(x, y, width, height, color, LocationType.CRYSTAL);
    }

    public ScavengingLocation(int x, int y, int width, int height, int color, LocationType locationType) {
        this.topLeft = new TileCoord(x, y);
        this.width = width;
        this.height = height;
        this.color = color;
        this.locationType = locationType;
        this.isCleared = false;
        this.isScavenging = false;
        this.scavengingStartTime = 0;
        this.lastClearedTime = 0;
    }
    
    /**
     * Check if a tile coordinate is within this location.
     */
    public boolean contains(TileCoord coord) {
        return contains(coord.x(), coord.y());
    }
    
    public boolean contains(int x, int y) {
        return x >= topLeft.x() && x < topLeft.x() + width &&
               y >= topLeft.y() && y < topLeft.y() + height;
    }
    
    /**
     * Check if this location is in cooldown (can't restart scavenging yet).
     */
    public boolean isInCooldown() {
        if (lastClearedTime == 0) return false;
        long elapsedTime = System.currentTimeMillis() - lastClearedTime;
        long cooldownMillis = (locationType == LocationType.SEED_WHEEL)
            ? SeedWheelConstants.SPIN_COOLDOWN_MILLIS
            : OutdoorConstants.SCAVENGING_COOLDOWN_MILLIS;
        return elapsedTime < cooldownMillis;
    }

    /**
     * Get remaining cooldown time in milliseconds.
     */
    public long getRemainingCooldownTime() {
        if (lastClearedTime == 0) return 0;
        long elapsedTime = System.currentTimeMillis() - lastClearedTime;
        long cooldownMillis = (locationType == LocationType.SEED_WHEEL)
            ? SeedWheelConstants.SPIN_COOLDOWN_MILLIS
            : OutdoorConstants.SCAVENGING_COOLDOWN_MILLIS;
        long remaining = cooldownMillis - elapsedTime;
        return Math.max(0, remaining);
    }
    
    /**
     * Start scavenging this location.
     */
    public void startScavenging() {
        if (!isCleared && !isScavenging && !isInCooldown()) {
            isScavenging = true;
            scavengingStartTime = System.currentTimeMillis();
        }
    }
    
    /**
     * Check if scavenging is complete.
     */
    public boolean isScavengingComplete() {
        if (!isScavenging) {
            return false;
        }
        long elapsedTime = System.currentTimeMillis() - scavengingStartTime;
        return elapsedTime >= OutdoorConstants.SCAVENGING_DURATION_MILLIS;
    }
    
    /**
     * Complete the scavenging and clear this location.
     */
    public void completeScavenging() {
        isScavenging = false;
        isCleared = true;
        lastClearedTime = System.currentTimeMillis();
    }
    
    /**
     * Get the percentage of scavenging progress (0-100).
     */
    public float getScavengingProgress() {
        if (!isScavenging) {
            return 0f;
        }
        long elapsedTime = System.currentTimeMillis() - scavengingStartTime;
        return Math.min(100f, (elapsedTime * 100f) / OutdoorConstants.SCAVENGING_DURATION_MILLIS);
    }
    
    // Getters
    public TileCoord getTopLeft() { return topLeft; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getColor() { return color; }
    public boolean isCleared() { return isCleared; }
    public boolean isScavenging() { return isScavenging; }
    public long getScavengingStartTime() { return scavengingStartTime; }
    public LocationType getLocationType() { return locationType; }
}

