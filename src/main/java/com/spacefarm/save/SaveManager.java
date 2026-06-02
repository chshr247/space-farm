package com.spacefarm.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.spacefarm.inventory.*;
import com.spacefarm.session.GameSession;
import com.spacefarm.world.ScavengingLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages saving and loading the game state using JSON.
 */
public class SaveManager {
    private static final String SAVE_FILE = "savegame.json";
    private final Json json;

    public SaveManager() {
        json = new Json();
        // Setup polymorphism for items
        json.setTypeName("itemClass");
        json.addClassTag("watering_can", WateringCan.class);
        json.addClassTag("seed", Seed.class);
        json.addClassTag("rare_seed", RareSeed.class);
        json.addClassTag("legendary_seed", LegendarySeed.class);
        json.addClassTag("sickle", Sickle.class);
        json.addClassTag("plant_food", PlantFood.class);
        json.addClassTag("crystal", Crystal.class);
    }

    /**
     * Save the current game session.
     */
    public void save(GameSession session) {
        SaveState state = new SaveState();

        // 1. Inventory
        state.inventory = new SaveState.InventoryData();
        state.inventory.slots = session.getInventory().getSlots();
        state.inventory.selectedSlot = session.getInventory().getSelectedSlot();

        // 2. Oxygen
        state.oxygen = new SaveState.OxygenData();
        state.oxygen.currentOxygen = session.getOxygenManager().getOxygen();
        state.oxygen.isAtBase = session.getOxygenManager().isAtBase();

        // 3. Farming
        state.farming = new SaveState.FarmingData();
        state.farming.crops = session.getFarmingSystem().getCrops();

        // 4. Locations
        state.locations = new ArrayList<>();
        for (ScavengingLocation loc : session.getOutdoorZone().getLocations()) {
            SaveState.LocationData locData = new SaveState.LocationData();
            locData.isCleared = loc.isCleared();
            locData.lastClearedTime = loc.getLastClearedTime();
            locData.isScavenging = loc.isScavenging();
            locData.scavengingStartTime = loc.getScavengingStartTime();
            state.locations.add(locData);
        }

        state.gameOver = session.isGameOver();

        String jsonText = json.prettyPrint(state);
        Gdx.files.local(SAVE_FILE).writeString(jsonText, false);
    }

    /**
     * Load the game session from file.
     * Returns true if successful.
     */
    public boolean load(GameSession session) {
        FileHandle file = Gdx.files.local(SAVE_FILE);
        if (!file.exists()) {
            return false;
        }

        try {
            SaveState state = json.fromJson(SaveState.class, file.readString());

            // 1. Inventory
            session.getInventory().setSlots(state.inventory.slots);
            session.getInventory().selectSlot(state.inventory.selectedSlot);

            // 2. Oxygen
            session.getOxygenManager().setOxygen(state.oxygen.currentOxygen);
            session.getOxygenManager().setAtBase(state.oxygen.isAtBase);

            // 3. Farming
            session.getFarmingSystem().setCrops(state.farming.crops);

            // 4. Locations
            List<ScavengingLocation> locations = session.getOutdoorZone().getLocations();
            for (int i = 0; i < Math.min(locations.size(), state.locations.size()); i++) {
                ScavengingLocation loc = locations.get(i);
                SaveState.LocationData locData = state.locations.get(i);
                loc.loadState(locData.isCleared, locData.lastClearedTime, locData.isScavenging, locData.scavengingStartTime);
            }

            session.setGameOver(state.gameOver);
            return true;
        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Error loading save file", e);
            return false;
        }
    }

    public boolean hasSaveFile() {
        return Gdx.files.local(SAVE_FILE).exists();
    }
}
