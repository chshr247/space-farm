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
        // Anchor point: the absolute center of the initial base
        int initialCenterX = baseX + baseWidth / 2;
        int initialCenterY = baseY + baseHeight / 2;

        // Magical tree shifted 2 tiles left for harmony
        int treeCenterX = initialCenterX - 2;
        this.treeWidth = 10;
        this.treeHeight = 20;
        this.treeCenter = new TileCoord(treeCenterX - treeWidth / 2, initialCenterY - treeHeight / 2);

        // Garden beds
        this.gardenBeds = new ArrayList<>();
        for (int i = 0; i < BaseZoneConstants.STARTING_GARDEN_BEDS; i++) {
            internalAddGardenBed();
        }

        // Space drone anchored relative to the tree
        this.droneZoneSize = 5;
        int droneX = treeCenter.x() + treeWidth + 7;
        int droneY = treeCenter.y();
        this.droneZoneCenter = new TileCoord(droneX, droneY);
    }

    private void internalAddGardenBed() {
        int i = gardenBeds.size();
        int bedX, bedY;
        
        // We use treeCenter as the absolute anchor so positions don't shift when base expands
        int treeLeft = treeCenter.x();
        int treeTop  = treeCenter.y() + treeHeight;

        if (i < 10) {
            // Section 1: Left side of the tree - moved one more tile to the right
            int gardenStartX = treeLeft - 8; 
            bedX = gardenStartX + (i % 2) * 3;
            bedY = treeTop - 2 - (i / 2) * 4;
        } else {
            // Section 2: Right side of the tree - moved further right
            int j = i - 10;
            int sec2StartX = treeLeft + treeWidth + 4;
            bedX = sec2StartX + (j % 2) * 3;
            bedY = treeTop - 2 - (j / 2) * 4;
        }
        gardenBeds.add(new TileCoord(bedX, bedY));
    }

    // Adds one new garden bed (called when player buys upgrade)
    public boolean addGardenBed() {
        if (gardenBeds.size() >= BaseZoneConstants.MAX_GARDEN_BEDS) {
            return false;
        }
        internalAddGardenBed();
        dirty = true;
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
