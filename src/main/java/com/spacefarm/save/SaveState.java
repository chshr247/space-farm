package com.spacefarm.save;

import com.spacefarm.farming.Crop;
import com.spacefarm.inventory.Item;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for saving the game state.
 */
public class SaveState {
    public InventoryData inventory;
    public OxygenData oxygen;
    public FarmingData farming;
    public List<LocationData> locations;
    public boolean gameOver;

    public static class InventoryData {
        public Item[] slots;
        public int selectedSlot;
    }

    public static class OxygenData {
        public float currentOxygen;
        public boolean isAtBase;
    }

    public static class FarmingData {
        public Map<String, Crop> crops;
    }

    public static class LocationData {
        public boolean isCleared;
        public long lastClearedTime;
        public boolean isScavenging;
        public long scavengingStartTime;
    }
}
