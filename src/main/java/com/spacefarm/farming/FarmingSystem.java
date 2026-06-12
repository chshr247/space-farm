package com.spacefarm.farming;

import com.spacefarm.world.TileCoord;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages all crops on the map.
 */
public class FarmingSystem {
    private final Map<String, Crop> crops;
    private final int minX;
    private final int minY;
    private final int maxX;
    private final int maxY;

    public FarmingSystem(int minX, int minY, int maxX, int maxY) {
        this.crops = new HashMap<>();
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public void update(float deltaTime) {
        crops.values().forEach(crop -> crop.update(deltaTime));
        crops.entrySet().removeIf(entry -> entry.getValue().isDead());
    }

    // Саджає рослину на координату TileCoord
    public boolean plantSeed(TileCoord coord, FarmingConstants.CropType type) {
        if (!isValidCoord(coord)) {
            return false;
        }

        String key = getCropKey(coord);
        if (crops.containsKey(key)) {
            return false;
        }

        crops.put(key, new Crop(type));
        return true;
    }

    // Саджає рослину на координату x, y
    public boolean plantSeed(int x, int y, FarmingConstants.CropType type) {
        return plantSeed(new TileCoord(x, y), type);
    }

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

    public Crop getCrop(TileCoord coord) {
        if (!isValidCoord(coord)) {
            return null;
        }
        return crops.get(getCropKey(coord));
    }

    public Crop getCrop(int x, int y) {
        return getCrop(new TileCoord(x, y));
    }

    public boolean removeCrop(TileCoord coord) {
        if (!isValidCoord(coord)) {
            return false;
        }

        return crops.remove(getCropKey(coord)) != null;
    }

    public boolean hasCrop(TileCoord coord) {
        if (!isValidCoord(coord)) {
            return false;
        }
        return crops.containsKey(getCropKey(coord));
    }

    public boolean hasCrop(int x, int y) {
        return hasCrop(new TileCoord(x, y));
    }

    public void clear() {
        crops.clear();
    }

    public Map<String, Crop> getCrops() {
        return crops;
    }

    public void setCrops(Map<String, Crop> crops) {
        this.crops.clear();
        this.crops.putAll(crops);
    }

    public int getCropCount() {
        return crops.size();
    }

    private String getCropKey(TileCoord coord) {
        return coord.x() + "," + coord.y();
    }

    public boolean harvestCrop(TileCoord coord) {
        if (!isValidCoord(coord)) {
            return false;
        }

        Crop crop = getCrop(coord);
        if (crop == null) {
            return false;
        }

        if (crop.getGrowthStage() == FarmingConstants.GrowthStage.MATURE) {
            removeCrop(coord);
            return true;
        }

        return false;
    }

    private boolean isValidCoord(TileCoord coord) {
        return coord != null && coord.x() >= minX && coord.x() < maxX &&
                coord.y() >= minY && coord.y() < maxY;
    }

    public int getMapWidth() {
        return maxX - minX;
    }

    public int getMapHeight() {
        return maxY - minY;
    }

    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
}