package com.spacefarm.world;

// локації для зачистки
public class ScavengingLocation {
    public enum LocationType {
        CRYSTAL,      // звичайна локація зачистки
        SEED_WHEEL    // колесо фортуни з насінням
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
    private boolean isGreened = false;

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
    
    // Перевіряє чи знаходиться координата всередині цієї локації
    public boolean contains(TileCoord coord) {
        return contains(coord.x(), coord.y());
    }
    
    public boolean contains(int x, int y) {
        return x >= topLeft.x() && x < topLeft.x() + width &&
               y >= topLeft.y() && y < topLeft.y() + height;
    }
    
    // перевіряє чи зараз откат зачистки
    public boolean isInCooldown() {
        if (lastClearedTime == 0) return false;
        long elapsedTime = System.currentTimeMillis() - lastClearedTime;
        long cooldownMillis = (locationType == LocationType.SEED_WHEEL) ? SeedWheelConstants.SPIN_COOLDOWN_MILLIS : OutdoorConstants.SCAVENGING_COOLDOWN_MILLIS;
        return elapsedTime < cooldownMillis;
    }

    public long getRemainingCooldownTime() {
        if (lastClearedTime == 0) return 0;
        long elapsedTime = System.currentTimeMillis() - lastClearedTime;
        long cooldownMillis = (locationType == LocationType.SEED_WHEEL) ? SeedWheelConstants.SPIN_COOLDOWN_MILLIS : OutdoorConstants.SCAVENGING_COOLDOWN_MILLIS;
        long remaining = cooldownMillis - elapsedTime;
        return Math.max(0, remaining);
    }
    
    // початок зачистки локації
    public void startScavenging() {
        if (!isScavenging && !isInCooldown()) {
            isCleared = false;
            isScavenging = true;
            scavengingStartTime = System.currentTimeMillis();
        }
    }

    public boolean isScavengingComplete(long durationMillis) {
        if (!isScavenging) {
            return false;
        }
        long elapsedTime = System.currentTimeMillis() - scavengingStartTime;
        return elapsedTime >= durationMillis;
    }
    // завершення зачистки локації
    public void completeScavenging() {
        isScavenging = false;
        isCleared = true;
        lastClearedTime = System.currentTimeMillis();
    }
    // отримує прогрес зачистки у відсотках
    public float getScavengingProgress(long durationMillis) {
        if (!isScavenging) {
            return 0f;
        }
        long elapsedTime = System.currentTimeMillis() - scavengingStartTime;
        return Math.min(100f, (elapsedTime * 100f) / (float)durationMillis);
    }

    public void loadState(boolean isCleared, long lastClearedTime, boolean isScavenging, long scavengingStartTime) {
        this.isCleared = isCleared;
        this.lastClearedTime = lastClearedTime;
        this.isScavenging = isScavenging;
        this.scavengingStartTime = scavengingStartTime;
    }

    public TileCoord getTopLeft() { return topLeft; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getColor() { return color; }
    public boolean isCleared() { return isCleared; }
    public boolean isScavenging() { return isScavenging; }
    public long getScavengingStartTime() { return scavengingStartTime; }
    public long getLastClearedTime() { return lastClearedTime; }
    public LocationType getLocationType() { return locationType; }
    public boolean isGreened()               { return isGreened; }
    public void setGreened(boolean greened)  { this.isGreened = greened; }
}

