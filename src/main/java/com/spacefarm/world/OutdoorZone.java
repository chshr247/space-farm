package com.spacefarm.world;

import java.util.*;

public class OutdoorZone {
    private List<ScavengingLocation> locations;
    private int baseX, baseY, baseWidth, baseHeight;
    private int borderX, borderY, borderWidth, borderHeight;

    public OutdoorZone(BaseZone base, int mapWidth, int mapHeight) {
        this.baseX = base.getBaseX();
        this.baseY = base.getBaseY();
        this.baseWidth = base.getBaseWidth();
        this.baseHeight = base.getBaseHeight();
        this.locations = new ArrayList<>();
        
        initializeBorder();
        initializeLocations();
    }

    // ініціалізація сірого контуру навколо бази (наша зона поза базою)
    private void initializeBorder() {
        int bWidthX = OutdoorConstants.BORDER_WIDTH_X;
        int bWidthY = OutdoorConstants.BORDER_WIDTH_Y;
        this.borderX = baseX - bWidthX;
        this.borderY = baseY - bWidthY;
        this.borderWidth = baseWidth + 2 * bWidthX;
        this.borderHeight = baseHeight + 2 * bWidthY;
    }

    private void initializeLocations() {
        // звичайні локації
        int width = OutdoorConstants.OUTDOOR_LOCATION_WIDTH;
        int height = OutdoorConstants.OUTDOOR_LOCATION_HEIGHT;
        
        // 5 positions tightly packed around the base
        int[][] positions = {
            // Top edge
            {baseX + baseWidth / 2 - width / 2, borderY},
            // Bottom edge
            {baseX + baseWidth / 2 - width / 2, borderY + borderHeight - height},
            // Left edge
            {borderX, baseY + baseHeight / 2 - height / 2},
            // Right edge
            {borderX + borderWidth - width, baseY + baseHeight / 2 - height / 2},
            // Top-left corner
            {borderX, borderY}
        };
        
        for (int i = 0; i < OutdoorConstants.NUM_LOCATIONS; i++) {
            int x = positions[i][0]; // x координата локації, де 0 це індекс масиву позицій
            int y = positions[i][1]; // y координата локації, де 1 це індекс масиву позицій
            int color = OutdoorConstants.LOCATION_COLORS[i];
            
            ScavengingLocation location = new ScavengingLocation(x, y, width, height, color);
            locations.add(location);
        }
        
        // Add seed wheel location at bottom-right corner
        int seedWheelWidth = SeedWheelConstants.SEED_WHEEL_WIDTH;
        int seedWheelHeight = SeedWheelConstants.SEED_WHEEL_HEIGHT;
        int seedWheelX = borderX + borderWidth - seedWheelWidth;
        int seedWheelY = borderY + borderHeight - seedWheelHeight;
        int seedWheelColor = 0x8b7355;  // Brown color for seed wheel area
        
        ScavengingLocation seedWheel = new ScavengingLocation(seedWheelX, seedWheelY, 
                seedWheelWidth, seedWheelHeight, seedWheelColor, ScavengingLocation.LocationType.SEED_WHEEL);
        locations.add(seedWheel);
    }

    public boolean isInBorder(int x, int y) {
        // перевірка чи тайл в сірій зоні але не на самій базі
        boolean inOuter = x >= borderX && x < borderX + borderWidth &&
                         y >= borderY && y < borderY + borderHeight;
        boolean inInner = x >= baseX && x < baseX + baseWidth &&
                         y >= baseY && y < baseY + baseHeight;
        return inOuter && !inInner;
    }
    
    public boolean isInBorder(TileCoord coord) {
        return isInBorder(coord.x(), coord.y());
    }

    public boolean isInOutdoor(int x, int y) {
        for (ScavengingLocation loc : locations) {
            if (x >= loc.getTopLeft().x() && x < loc.getTopLeft().x() + loc.getWidth() &&
                y >= loc.getTopLeft().y() && y < loc.getTopLeft().y() + loc.getHeight()) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(int x, int y) {
        return x >= borderX && x < borderX + borderWidth &&
               y >= borderY && y < borderY + borderHeight;
    }

    public ScavengingLocation getLocationAt(TileCoord coord) {
        for (ScavengingLocation loc : locations) {
            if (coord.x() >= loc.getTopLeft().x() && coord.x() < loc.getTopLeft().x() + loc.getWidth() &&
                coord.y() >= loc.getTopLeft().y() && coord.y() < loc.getTopLeft().y() + loc.getHeight()) {
                return loc;
            }
        }
        return null;
    }

    public void greenLocation(int index) {
        if (index >= 0 && index < locations.size()) {
            locations.get(index).setGreened(true);
        }
    }

    public List<ScavengingLocation> getLocations() { return locations; }
    public List<ScavengingLocation> getScavengingLocations() {
        List<ScavengingLocation> scavenging = new ArrayList<>();
        for (ScavengingLocation loc : locations) {
            if (loc.isScavenging()) scavenging.add(loc);
        }
        return scavenging;
    }
    public int getBorderX() { return borderX; }
    public int getBorderY() { return borderY; }
    public int getBorderWidth() { return borderWidth; }
    public int getBorderHeight() { return borderHeight; }
    public int getBaseWidth() { return baseWidth; }
    public int getBaseHeight() { return baseHeight; }
}
