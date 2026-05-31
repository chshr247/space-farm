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

    public OxygenManager() {
        this.currentOxygen = OxygenConstants.STARTING_OXYGEN;
        this.oxygenTimer = 0f;
        this.isAtBase = true;  // Start at base
        this.baseZone = null;
    }

    /**
     * Update oxygen level based on location and time.
     */
    public void update(float deltaTime) {
        if (!isAtBase) {
            // Decrease oxygen outside base
            oxygenTimer += deltaTime;

            if (oxygenTimer >= OxygenConstants.OXYGEN_DECREASE_INTERVAL) {
                currentOxygen -= OxygenConstants.OXYGEN_DECREASE_AMOUNT;
                oxygenTimer = 0f;
            }

            // Clamp oxygen
            if (currentOxygen < OxygenConstants.MIN_OXYGEN) {
                currentOxygen = OxygenConstants.MIN_OXYGEN;
            }
        } else {
            // At base - oxygen stays at max
            currentOxygen = OxygenConstants.MAX_OXYGEN;
            oxygenTimer = 0f;
        }
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

