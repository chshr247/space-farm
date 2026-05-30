package com.spacefarm.oxygen;

/**
 * Manages player oxygen level with base/outside modes.
 */
public class OxygenManager {
    private float currentOxygen;
    private float oxygenTimer;
    private boolean isAtBase;

    public OxygenManager() {
        this.currentOxygen = OxygenConstants.STARTING_OXYGEN;
        this.oxygenTimer = 0f;
        this.isAtBase = true;  // Start at base
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

