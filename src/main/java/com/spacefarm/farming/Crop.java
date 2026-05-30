package com.spacefarm.farming;

import com.spacefarm.farming.FarmingConstants.GrowthStage;
import com.spacefarm.farming.FarmingConstants.WaterState;

/**
 * Represents a single crop on a tile.
 */
public class Crop {
    private GrowthStage growthStage;
    private float growthTimer;
    private float timeSinceWatered;
    private WaterState waterState;

    public Crop() {
        this.growthStage = GrowthStage.SEED;
        this.growthTimer = 0f;
        this.timeSinceWatered = 0f;
        this.waterState = WaterState.NORMAL;
    }

    /**
     * Update the crop's growth and water state.
     */
    public void update(float deltaTime) {
        // Update growth stage
        updateGrowthStage(deltaTime);

        // Update water state
        updateWaterState(deltaTime);
    }

    private void updateGrowthStage(float deltaTime) {
        growthTimer += deltaTime;

        float stageDuration = getStageDuration(growthStage);
        if (stageDuration > 0 && growthTimer >= stageDuration) {
            advanceGrowthStage();
        }
    }

    private void updateWaterState(float deltaTime) {
        timeSinceWatered += deltaTime;

        // Update water state based on time since watering
        if (timeSinceWatered <= FarmingConstants.WATERING_DURATION) {
            waterState = WaterState.WELL_WATERED;
        } else if (timeSinceWatered <= FarmingConstants.WATERING_DURATION * 1.5f) {
            waterState = WaterState.NORMAL;
        } else if (timeSinceWatered <= FarmingConstants.DRYING_DURATION) {
            waterState = WaterState.THIRSTY;
        } else {
            waterState = WaterState.DYING;
        }
    }

    private float getStageDuration(GrowthStage stage) {
        switch (stage) {
            case SEED:
                return FarmingConstants.STAGE_1_DURATION;
            case SPROUT:
                return FarmingConstants.STAGE_2_DURATION;
            case YOUNG:
                return FarmingConstants.STAGE_3_DURATION;
            case MATURE:
                return -1f; // Mature plants don't progress further
            default:
                return -1f;
        }
    }

    private void advanceGrowthStage() {
        switch (growthStage) {
            case SEED:
                growthStage = GrowthStage.SPROUT;
                break;
            case SPROUT:
                growthStage = GrowthStage.YOUNG;
                break;
            case YOUNG:
                growthStage = GrowthStage.MATURE;
                break;
            case MATURE:
                growthStage = GrowthStage.MATURE;
                break;
        }
        growthTimer = 0f;
    }

    /**
     * Water the crop, resetting the water timer.
     */
    public void water() {
        timeSinceWatered = 0f;
        waterState = WaterState.WELL_WATERED;
    }

    /**
     * Get the current growth progress (0.0 to 1.0) within the current stage.
     */
    public float getGrowthProgress() {
        float stageDuration = getStageDuration(growthStage);
        if (stageDuration <= 0) {
            return 1.0f; // Mature stage
        }
        return Math.min(1.0f, growthTimer / stageDuration);
    }

    /**
     * Get the current water progress (0.0 to 1.0).
     * 0.0 = dying, 1.0 = well watered
     */
    public float getWaterProgress() {
        float timeSinceDrying = Math.max(0, timeSinceWatered - FarmingConstants.WATERING_DURATION);
        float dryingTime = FarmingConstants.DRYING_DURATION - FarmingConstants.WATERING_DURATION;
        if (dryingTime <= 0) {
            return 1.0f;
        }
        return Math.max(0.0f, 1.0f - (timeSinceDrying / dryingTime));
    }

    // Getters
    public GrowthStage getGrowthStage() {
        return growthStage;
    }

    public WaterState getWaterState() {
        return waterState;
    }

    public float getTimeSinceWatered() {
        return timeSinceWatered;
    }

    public float getGrowthTimer() {
        return growthTimer;
    }

    public boolean isDead() {
        return waterState == WaterState.DYING && timeSinceWatered > FarmingConstants.DRYING_DURATION * 1.5f;
    }
}


