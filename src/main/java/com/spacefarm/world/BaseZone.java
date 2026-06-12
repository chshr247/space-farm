package com.spacefarm.world;

import java.util.*;
import com.spacefarm.world.BaseZoneConstants;


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
    private float droneOffsetX = 0f;
    private float droneOffsetY = 0f;
    private int treePhase = 1;

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

        // Garden beds (centered on the left side of the tree)
        this.gardenBeds = new ArrayList<>();
        int gardenStartX = baseX + 2;
        int gardenStartY = baseY + baseHeight / 2 + 4; 
        for (int i = 0; i < BaseZoneConstants.STARTING_GARDEN_BEDS; i++) {
            int bedX = gardenStartX + (i % 2) * 3;
            int bedY = gardenStartY - (i / 2) * 4;
            gardenBeds.add(new TileCoord(bedX, bedY));
        }

        // Space drone (bottom-right area)
        this.droneZoneSize = 5;
        int droneX = baseX + baseWidth - droneZoneSize - 3;
        int droneY = baseY + 3;
        this.droneZoneCenter = new TileCoord(droneX, droneY);
    }

    // Adds one new garden bed (called when player buys upgrade)
    public boolean addGardenBed() {
        if (gardenBeds.size() >= BaseZoneConstants.MAX_GARDEN_BEDS) {
            return false;
        }
        int gardenStartX = baseX + 2;
        int gardenStartY = baseY + baseHeight / 2 + 4;
        int i = gardenBeds.size();
        int bedX, bedY;
        if (i < 12) {
            // Section 1: 2 columns to the left of the tree
            bedX = gardenStartX + (i % 2) * 3;
            bedY = gardenStartY - (i / 2) * 4;
        } else {
            // Section 2: 2 columns to the right of the tree
            int j = i - 12;
            int sec2StartX = treeCenter.x() + treeWidth + 1;
            bedX = sec2StartX + (j % 2) * 3;
            bedY = gardenStartY - (j / 2) * 4;
        }
        gardenBeds.add(new TileCoord(bedX, bedY));
        return true;
    }


    public boolean isInBaseZone(TileCoord coord) {
        return isInBaseZone(coord.x(), coord.y());
    }

    public boolean isInBaseZone(int x, int y) {
        return x >= baseX && x < baseX + baseWidth &&
                y >= baseY && y < baseY + baseHeight;
    }

    public boolean isTreeArea(TileCoord coord) {
        return isTreeArea(coord.x(), coord.y());
    }

    public boolean isTreeArea(int x, int y) {
        int treeStartX = treeCenter.x();
        int treeStartY = treeCenter.y();
        return x >= treeStartX && x < treeStartX + treeWidth &&
                y >= treeStartY && y < treeStartY + treeHeight;
    }

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

    public boolean isDroneZone(TileCoord coord) {
        return isDroneZone(coord.x(), coord.y());
    }

    public boolean isDroneZone(int x, int y) {
        int droneStartX = droneZoneCenter.x();
        int droneStartY = droneZoneCenter.y();
        return x >= droneStartX && x < droneStartX + droneZoneSize &&
                y >= droneStartY && y < droneStartY + droneZoneSize;
    }

    private boolean dirty = false;
    // Розширює зелену зону після апгрейду фази дерева
    public void expandZone(int tiles) {
        baseX      -= tiles;
        baseY      -= tiles;
        baseWidth  += tiles * 2;
        baseHeight += tiles * 2;
        dirty = true;
    }
    public boolean isDirty()  { return dirty; }
    public void clearDirty()  { dirty = false; }

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
    public int getTreePhase() { return treePhase; }
    public void setTreePhase(int phase) { this.treePhase = phase; }
    public float getDroneOffsetX() { return droneOffsetX; }
    public float getDroneOffsetY() { return droneOffsetY; }
    public void setDroneOffsets(float x, float y) {
        this.droneOffsetX = x;
        this.droneOffsetY = y;
    }
}
