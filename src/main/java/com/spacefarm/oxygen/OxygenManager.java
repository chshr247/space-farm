package com.spacefarm.oxygen;

import com.spacefarm.world.BaseZone;
import com.spacefarm.world.TileCoord;


public class OxygenManager {
    private float currentOxygen;
    private float oxygenTimer;
    private boolean isAtBase;
    private BaseZone baseZone;
    private TileCoord lastKnownPosition;
    private float scavengingTimer;  // Таймер для витрати кислю

    public OxygenManager() {
        this.currentOxygen = OxygenConstants.STARTING_OXYGEN;
        this.oxygenTimer = 0f;
        this.scavengingTimer = 0f;
        this.isAtBase = true;  // Start at base
        this.baseZone = null;
    }

     // Oxygen consumption happens in GameApp.updateScavenging() during scavenging activity
    public void update(float deltaTime) {

    }
    // їжа від плодів
    public void consumeFood() {
        currentOxygen += OxygenConstants.OXYGEN_INCREASE_FROM_FOOD;
        if (currentOxygen > OxygenConstants.MAX_OXYGEN) {
            currentOxygen = OxygenConstants.MAX_OXYGEN;
        }
    }
    // Споживання кисню під час зачистки (залежить від часу та наявності зеленості зони)
    public void consumeOxygenDuringScavenging(float deltaTime, boolean locationIsGreened) {
        scavengingTimer += deltaTime;

        if (scavengingTimer >= OxygenConstants.OXYGEN_DECREASE_INTERVAL) {
            float decrease = OxygenConstants.OXYGEN_DECREASE_AMOUNT;
            if (locationIsGreened) {
                decrease = Math.max(0f, decrease - 2f);
            }
            currentOxygen -= decrease;
            scavengingTimer = 0f;

            if (currentOxygen < OxygenConstants.MIN_OXYGEN) {
                currentOxygen = OxygenConstants.MIN_OXYGEN;
            }
        }
    }
    // просто споживання кисню
    public void consumeOxygen(float amount) {
        currentOxygen -= amount;
        if (currentOxygen < OxygenConstants.MIN_OXYGEN) {
            currentOxygen = OxygenConstants.MIN_OXYGEN;
        }
    }

    public void setOxygen(float level) {
        this.currentOxygen = Math.max(OxygenConstants.MIN_OXYGEN,
                                      Math.min(OxygenConstants.MAX_OXYGEN, level));
    }

    public void setAtBase(boolean atBase) {
        this.isAtBase = atBase;
    }

    public void setBaseZone(BaseZone baseZone) {
        this.baseZone = baseZone;
    }
    // Оновлює позицію гравця та перевіряє, чи знаходиться він у зоні бази
    public void updatePositionTile(TileCoord coord) {
        this.lastKnownPosition = coord;
        if (baseZone != null) {
            this.isAtBase = baseZone.isInBaseZone(coord);
        }
    }
    public float getOxygen() {
        return currentOxygen;
    }
    public float getOxygenPercent() {
        return currentOxygen / OxygenConstants.MAX_OXYGEN;
    }

    public boolean isAtBase() {
        return isAtBase;
    }

    public boolean isCritical() {
        return currentOxygen <= 20f;
    }
    public boolean isOxygenDepleted() {
        return currentOxygen <= 0f;
    }

}
