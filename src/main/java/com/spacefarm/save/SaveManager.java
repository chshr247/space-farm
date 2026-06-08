package com.spacefarm.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.*;
import com.spacefarm.DifficultyLevel;
import com.spacefarm.inventory.*;
import com.spacefarm.session.GameSession;
import com.spacefarm.world.ScavengingLocation;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manages saving and loading the game state using GSON.
 */
public class SaveManager {
    private static final String SAVE_FILE = "savegame.json";
    private final Gson gson;

    public SaveManager() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Item.class, new ItemAdapter())
                .setPrettyPrinting()
                .create();
    }

    /**
     * Save the current game session.
     */
    public void save(GameSession session) {
        SaveState state;
        if (session.isGameOver()) {
            state = createDefaultState(session.getDifficulty());
        } else {
            state = captureState(session);
        }
        String jsonText = gson.toJson(state);
        Gdx.files.local(SAVE_FILE).writeString(jsonText, false);
    }

    private SaveState captureState(GameSession session) {
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

        // 5. Wallet
        state.wallet = new SaveState.WalletData();
        state.wallet.balance = session.getWallet().getBalance();

        state.gameOver = session.isGameOver();
        return state;
    }

    private SaveState createDefaultState(DifficultyLevel difficulty) {
        SaveState state = new SaveState();

        state.inventory = new SaveState.InventoryData();
        // The default GameSession/Inventory is initialized with 8 slots, so we must reflect this
        state.inventory.slots = new Item[8]; 
        state.inventory.slots[0] = WateringCan.getInstance();
        state.inventory.slots[1] = new Seed(5);
        state.inventory.slots[2] = Sickle.getInstance();
        state.inventory.selectedSlot = 0;

        state.oxygen = new SaveState.OxygenData();
        state.oxygen.currentOxygen = 100f;
        state.oxygen.isAtBase = true;

        state.farming = new SaveState.FarmingData();
        state.farming.crops = new HashMap<>();

        state.locations = new ArrayList<>();
        // OutdoorZone will re-initialize locations if list is empty or doesn't match
        // But we save an empty list to indicate fresh start for locations.

        state.wallet = new SaveState.WalletData();
        state.wallet.balance = difficulty.startingMoney;

        state.gameOver = false;
        return state;
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
            SaveState state = gson.fromJson(file.readString(), SaveState.class);
            if (state == null) {
                return false;
            }

            // 1. Inventory
            if (state.inventory != null) {
                session.getInventory().setSlots(state.inventory.slots);
                session.getInventory().selectSlot(state.inventory.selectedSlot);
            }

            // 2. Oxygen
            if (state.oxygen != null) {
                session.getOxygenManager().setOxygen(state.oxygen.currentOxygen);
                session.getOxygenManager().setAtBase(state.oxygen.isAtBase);
            }

            // 3. Farming
            if (state.farming != null && state.farming.crops != null) {
                session.getFarmingSystem().setCrops(state.farming.crops);
            }

            // 4. Locations
            if (state.locations != null) {
                List<ScavengingLocation> locations = session.getOutdoorZone().getLocations();
                for (int i = 0; i < Math.min(locations.size(), state.locations.size()); i++) {
                    ScavengingLocation loc = locations.get(i);
                    SaveState.LocationData locData = state.locations.get(i);
                    loc.loadState(locData.isCleared, locData.lastClearedTime, locData.isScavenging, locData.scavengingStartTime);
                }
            }

            // 5. Wallet
            if (state.wallet != null) {
                session.getWallet().setBalance(state.wallet.balance);
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

    /**
     * Custom adapter for polymorphic Item serialization/deserialization.
     */
    private static class ItemAdapter implements JsonSerializer<Item>, JsonDeserializer<Item> {
        @Override
        public JsonElement serialize(Item src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null) return JsonNull.INSTANCE;
            JsonObject result = context.serialize(src).getAsJsonObject();
            result.addProperty("itemType", src.getType().name());
            return result;
        }

        @Override
        public Item deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json == null || json.isJsonNull()) return null;
            JsonObject jsonObject = json.getAsJsonObject();
            JsonElement typeElement = jsonObject.get("itemType");
            if (typeElement == null) return null;

            String typeStr = typeElement.getAsString();
            Item.ItemType type = Item.ItemType.valueOf(typeStr);

            switch (type) {
                case WATERING_CAN: return WateringCan.getInstance();
                case SEED: return context.deserialize(json, Seed.class);
                case RARE_SEED: return context.deserialize(json, RareSeed.class);
                case LEGENDARY_SEED: return context.deserialize(json, LegendarySeed.class);
                case SICKLE: return Sickle.getInstance();
                case PLANT_FOOD: return context.deserialize(json, PlantFood.class);
                case CRYSTAL:            return context.deserialize(json, Crystal.class);
                case BIO_COMPOST:        return new BioCompost();
                case LIVING_DEW:         return new LivingDew();
                case MYCORRHIZA_NETWORK: return new MycorrhizaNetwork();
                case UNIVERSE_FLOWER:    return new UniverseFlower();
                case EDEN_CORE:          return new EdenCore();
                case EMPTY:              return null;
                default: throw new JsonParseException("Unknown item type: " + type);
            }
        }
    }
}
