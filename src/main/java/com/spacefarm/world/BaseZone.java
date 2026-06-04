package com.spacefarm.world;

import java.util.*;
import com.spacefarm.world.BaseZoneConstants;

/**
 * Represents the base/habitat zone with special areas for buildings and structures.
 */
public class BaseZone {
    // Base zone dimensions
    private int baseX;
    private int baseY;
    private int baseWidth;
    private int baseHeight;

    // Special structures
    private TileCoord treeCenter;
    private int treeWidth;
    private int treeHeight;

    private List<TileCoord> gardenBeds;
    private TileCoord droneZoneCenter;
    private int droneZoneSize;

    public BaseZone(int centerX, int centerY, int zoneWidth, int zoneHeight) {
        // Position base zone in the center of the map
        this.baseX = centerX - zoneWidth / 2;
        this.baseY = centerY - zoneHeight / 2;
        this.baseWidth = zoneWidth;
        this.baseHeight = zoneHeight;

        initializeStructures();
    }

    private void initializeStructures() {
        // Magical tree in the center (20x10 tiles)
        int centerX = baseX + baseWidth / 2;
        int centerY = baseY + baseHeight / 2;
        this.treeWidth = 10;
        this.treeHeight = 20;
        this.treeCenter = new TileCoord(centerX - treeWidth / 2, centerY - treeHeight / 2);

        // Garden beds in one corner (top-left area)
        this.gardenBeds = new ArrayList<>();
        int gardenStartX = baseX + 3;
        int gardenStartY = baseY + baseHeight - 8;
        for (int i = 0; i < BaseZoneConstants.STARTING_GARDEN_BEDS; i++) {
            int bedX = gardenStartX + (i % 2) * 4;
            int bedY = gardenStartY - (i / 2) * 4;
            gardenBeds.add(new TileCoord(bedX, bedY));
        }

        // Space drone in opposite corner (bottom-right area)
        this.droneZoneSize = 5;
        int droneX = baseX + baseWidth - droneZoneSize - 3;
        int droneY = baseY + 3;
        this.droneZoneCenter = new TileCoord(droneX, droneY);
    }
            /*Adds one new garden bed (called when player buys upgrade).
            * Returns true if successful, false if MAX_GARDEN_BEDS limit is reached.
            */
    public boolean addGardenBed() {
        if (gardenBeds.size() >= BaseZoneConstants.MAX_GARDEN_BEDS) {
            return false; // Досягнуто максимум
        }
        int gardenStartX = baseX + 3;
        int gardenStartY = baseY + baseHeight - 8;
        int i = gardenBeds.size();
        int bedX = gardenStartX + (i % 2) * 4;
        int bedY = gardenStartY - (i / 2) * 4;
        gardenBeds.add(new TileCoord(bedX, bedY));
        return true;
    }

    /**
     * Check if a tile is within the base zone.
     */
    public boolean isInBaseZone(TileCoord coord) {
        return isInBaseZone(coord.x(), coord.y());
    }

    public boolean isInBaseZone(int x, int y) {
        return x >= baseX && x < baseX + baseWidth &&
               y >= baseY && y < baseY + baseHeight;
    }

    /**
     * Check if a tile is within the magical tree area.
     */
    public boolean isTreeArea(TileCoord coord) {
        return isTreeArea(coord.x(), coord.y());
    }

    public boolean isTreeArea(int x, int y) {
        int treeStartX = treeCenter.x();
        int treeStartY = treeCenter.y();
        return x >= treeStartX && x < treeStartX + treeWidth &&
               y >= treeStartY && y < treeStartY + treeHeight;
    }

    /**
     * Check if a tile is within a garden bed area.
     */
    public boolean isGardenBed(TileCoord coord) {
        return isGardenBed(coord.x(), coord.y());
    }

    public boolean isGardenBed(int x, int y) {
        for (TileCoord bed : gardenBeds) {
            // Each bed is 2x2
            if (x >= bed.x() && x < bed.x() + 2 &&
                y >= bed.y() && y < bed.y() + 2) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a tile is within the drone zone.
     */
    public boolean isDroneZone(TileCoord coord) {
        return isDroneZone(coord.x(), coord.y());
    }

    public boolean isDroneZone(int x, int y) {
        int droneStartX = droneZoneCenter.x();
        int droneStartY = droneZoneCenter.y();
        return x >= droneStartX && x < droneStartX + droneZoneSize &&
               y >= droneStartY && y < droneStartY + droneZoneSize;
    }

    // Getters
    public int getBaseX() { return baseX; }
    public int getBaseY() { return baseY; }
    public int getBaseWidth() { return baseWidth; }
    public int getBaseHeight() { return baseHeight; }

    public TileCoord getTreeCenter() { return treeCenter; }
    public int getTreeWidth() { return treeWidth; }
    public int getTreeHeight() { return treeHeight; }

    public List<TileCoord> getGardenBeds() { return new ArrayList<>(gardenBeds); }
    public TileCoord getDroneZoneCenter() { return droneZoneCenter; }
    public int getDroneZoneSize() { return droneZoneSize; }
    public int getGardenBedCount() { return gardenBeds.size();}
}

