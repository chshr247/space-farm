package com.spacefarm.farming;

import com.spacefarm.world.TileCoord;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages all crops on the map.
 */
public class FarmingSystem {
    private final Map<String, Crop> crops;
    private final int mapWidth;
    private final int mapHeight;

    public FarmingSystem(int mapWidth, int mapHeight) {
        this.crops = new HashMap<>();
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    /**
     * Update all crops.
     */
    public void update(float deltaTime) {
        // Update all crops and remove dead ones
        crops.values().forEach(crop -> crop.update(deltaTime));
        crops.entrySet().removeIf(entry -> entry.getValue().isDead());
    }

    /**
     * Plant a seed at the given tile coordinate.
     * Returns true if successful, false if a crop already exists at that location.
     */
    public boolean plantSeed(TileCoord coord) {
        if (!isValidCoord(coord)) {
            return false;
        }

        String key = getCropKey(coord);
        if (crops.containsKey(key)) {
            return false; // Crop already exists
        }

        crops.put(key, new Crop());
        return true;
    }

    /**
     * Plant a seed at the given coordinates (x, y).
     */
    public boolean plantSeed(int x, int y) {
        return plantSeed(new TileCoord(x, y));
    }

    /**
     * Water a crop at the given tile coordinate.
     * Returns true if successful, false if no crop exists.
     */
    public boolean waterCrop(TileCoord coord) {
        if (!isValidCoord(coord)) {
            return false;
        }

        String key = getCropKey(coord);
        Crop crop = crops.get(key);
        if (crop == null) {
            return false;
        }

        crop.water();
        return true;
    }

    /**
     * Get the crop at the given tile coordinate.
     */
    public Crop getCrop(TileCoord coord) {
        if (!isValidCoord(coord)) {
            return null;
        }
        return crops.get(getCropKey(coord));
    }

    /**
     * Get the crop at the given coordinates (x, y).
     */
    public Crop getCrop(int x, int y) {
        return getCrop(new TileCoord(x, y));
    }

    /**
     * Remove a crop at the given tile coordinate.
     * Returns true if successful, false if no crop exists.
     */
    public boolean removeCrop(TileCoord coord) {
        if (!isValidCoord(coord)) {
            return false;
        }

        return crops.remove(getCropKey(coord)) != null;
    }

    /**
     * Check if a tile has a crop.
     */
    public boolean hasCrop(TileCoord coord) {
        if (!isValidCoord(coord)) {
            return false;
        }
        return crops.containsKey(getCropKey(coord));
    }

    /**
     * Check if a tile has a crop at the given coordinates (x, y).
     */
    public boolean hasCrop(int x, int y) {
        return hasCrop(new TileCoord(x, y));
    }

    /**
     * Clear all crops from the map.
     */
    public void clear() {
        crops.clear();
    }

    /**
     * Get the number of crops on the map.
     */
    public int getCropCount() {
        return crops.size();
    }

    /**
     * Get a string key for storing crops in the map.
     */
    private String getCropKey(TileCoord coord) {
        return coord.x() + "," + coord.y();
    }

    /**
     * Check if the given coordinate is valid within map bounds.
     */
    private boolean isValidCoord(TileCoord coord) {
        return coord != null && coord.x() >= 0 && coord.x() < mapWidth &&
                coord.y() >= 0 && coord.y() < mapHeight;
    }
}

