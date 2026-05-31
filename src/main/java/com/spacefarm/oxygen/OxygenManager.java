package com.spacefarm.oxygen;

import com.spacefarm.world.BaseZone;
import com.spacefarm.world.TileCoord;

/**
 * Manages player oxygen level with base/outside modes.
 */
public class OxygenManager {
    private float currentOxygen;
    private float oxygenTimer;
    private boolean isAtBase;
    private BaseZone baseZone;
    private TileCoord lastKnownPosition;
    private float scavengingTimer;  // Таймер для stableної витрати кислю

    public OxygenManager() {
        this.currentOxygen = OxygenConstants.STARTING_OXYGEN;
        this.oxygenTimer = 0f;
        this.scavengingTimer = 0f;
        this.isAtBase = true;  // Start at base
        this.baseZone = null;
    }

    /**
     * Update oxygen level based on location and time.
     * NOTE: Oxygen consumption happens in GameApp.updateScavenging() during scavenging activity
     */
    public void update(float deltaTime) {
        // Oxygen only decreases during scavenging (handled in GameApp.updateScavenging)
        // At base, oxygen stays the same
        // Outside base but not scavenging, oxygen stays the same
    }

    /**
     * Increase oxygen from food.
     */
    public void consumeFood() {
        currentOxygen += OxygenConstants.OXYGEN_INCREASE_FROM_FOOD;
        if (currentOxygen > OxygenConstants.MAX_OXYGEN) {
            currentOxygen = OxygenConstants.MAX_OXYGEN;
        }
    }

    /**
     * Consume oxygen during scavenging (accumulate time for stable consumption).
     */
    public void consumeOxygenDuringScavenging(float deltaTime) {
        if (isAtBase) return;  // No consumption at base

        scavengingTimer += deltaTime;

        // Consume exactly 2% every 10 seconds
        if (scavengingTimer >= OxygenConstants.OXYGEN_DECREASE_INTERVAL) {
            currentOxygen -= OxygenConstants.OXYGEN_DECREASE_AMOUNT;
            scavengingTimer = 0f;

            // Clamp oxygen
            if (currentOxygen < OxygenConstants.MIN_OXYGEN) {
                currentOxygen = OxygenConstants.MIN_OXYGEN;
            }
        }
    }

    /**
     * Consume oxygen directly.
     */
    public void consumeOxygen(float amount) {
        currentOxygen -= amount;
        if (currentOxygen < OxygenConstants.MIN_OXYGEN) {
            currentOxygen = OxygenConstants.MIN_OXYGEN;
        }
    }

    /**
     * Directly set oxygen level.
     */
    public void setOxygen(float level) {
        this.currentOxygen = Math.max(OxygenConstants.MIN_OXYGEN,
                                      Math.min(OxygenConstants.MAX_OXYGEN, level));
    }

    /**
     * Set location mode.
     */
    public void setAtBase(boolean atBase) {
        this.isAtBase = atBase;
    }

    /**
     * Set the base zone for automatic position detection.
     */
    public void setBaseZone(BaseZone baseZone) {
        this.baseZone = baseZone;
    }

    /**
     * Update oxygen based on current tile position.
     */
    public void updatePositionTile(TileCoord coord) {
        this.lastKnownPosition = coord;
        if (baseZone != null) {
            this.isAtBase = baseZone.isInBaseZone(coord);
        }
    }

    /**
     * Get current oxygen level (0-100).
     */
    public float getOxygen() {
        return currentOxygen;
    }

    /**
     * Get oxygen as percentage (0-1).
     */
    public float getOxygenPercent() {
        return currentOxygen / OxygenConstants.MAX_OXYGEN;
    }

    /**
     * Check if player is at base.
     */
    public boolean isAtBase() {
        return isAtBase;
    }

    /**
     * Check if oxygen is critically low (below 20%).
     */
    public boolean isCritical() {
        return currentOxygen < 20f;
    }
}

