package com.spacefarm.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.spacefarm.world.OutdoorZone;
import com.spacefarm.world.OutdoorConstants;
import com.spacefarm.world.ScavengingLocation;
import com.spacefarm.world.TileCoord;
import com.spacefarm.world.SeedWheelConstants;

import java.util.*;

public class OutdoorZoneRenderer {
    private OutdoorZone outdoorZone;
    private TiledMapTileLayer zoneLayer;
    private TiledMapTileLayer borderLayer;
    private TiledMap map;
    private Texture borderTileTexture;
    private Texture[] locationTextures;
    private Texture[] droneTextures;
    private Texture wheelTexture;
    private Texture greenTileTexture;
    private SpriteBatch batch;
    private Set<Long> greenedTiles = new HashSet<>();
    private int worldMinX;
    private int worldMinY;
    private com.spacefarm.session.GameSession session;
    private TiledMap referenceMap;

    public OutdoorZoneRenderer(OutdoorZone outdoorZone, TiledMapTileLayer zoneLayer, int tileSize, int worldMinX, int worldMinY, com.spacefarm.session.GameSession session) {
        this(outdoorZone, zoneLayer, null, tileSize, worldMinX, worldMinY, session);
    }

    public void setReferenceMap(TiledMap referenceMap) {
        this.referenceMap = referenceMap;
    }

    private Texture whitePixel;

    public OutdoorZoneRenderer(OutdoorZone outdoorZone, TiledMapTileLayer zoneLayer, TiledMap map, int tileSize, int worldMinX, int worldMinY, com.spacefarm.session.GameSession session) {
        this.outdoorZone = outdoorZone;
        this.zoneLayer = zoneLayer;
        this.map = map;
        this.worldMinX = worldMinX;
        this.worldMinY = worldMinY;
        this.session = session;
        this.batch = new SpriteBatch();
        this.whitePixel = createSolidTexture(1, 1, 255, 255, 255, 255);
        createTextures(tileSize);
        if(map != null) {
            borderLayer = new TiledMapTileLayer(zoneLayer.getWidth(), zoneLayer.getHeight(),
                    zoneLayer.getTileWidth(), zoneLayer.getTileHeight());
            borderLayer.setOffsetX(worldMinX * zoneLayer.getTileWidth());
            borderLayer.setOffsetY(worldMinY * zoneLayer.getTileHeight());
            map.getLayers().add(borderLayer);
        }
    }

    private void createTextures(int tileSize) {
        int borderColor = OutdoorConstants.BORDER_COLOR;
        int r = (borderColor >> 16) & 0xFF;
        int g = (borderColor >> 8) & 0xFF;
        int b = borderColor & 0xFF;
        borderTileTexture = createSolidTexture(tileSize, tileSize, r, g, b, 128);
        int locationCount = outdoorZone.getLocations().size();
        locationTextures = new Texture[locationCount];
        droneTextures = new Texture[locationCount];
        int locWidth = OutdoorConstants.OUTDOOR_LOCATION_WIDTH * tileSize;
        int locHeight = OutdoorConstants.OUTDOOR_LOCATION_HEIGHT * tileSize;

        for(int i = 0; i < locationCount; i++) {
            int color = outdoorZone.getLocations().get(i).getColor();
            int lr = (color >> 16) & 0xFF;
            int lg = (color >> 8) & 0xFF;
            int lb = color & 0xFF;
            locationTextures[i] = createSolidTexture(tileSize, tileSize, lr, lg, lb, 128);
            
            // Try to load crystal textures (crystal-1 to crystal-4)
            int crystalNum = (i % 4) + 1;
            String path = "sprite/object-map/crystal-" + crystalNum + ".png";
            try {
                if (Gdx.files.internal(path).exists()) {
                    droneTextures[i] = new Texture(Gdx.files.internal(path));
                } else {
                    Gdx.app.error("OutdoorZoneRenderer", "File not found: " + path);
                    droneTextures[i] = createCrystalDroneTexture(locWidth, locHeight);
                }
            } catch (Exception e) {
                Gdx.app.error("OutdoorZoneRenderer", "Error loading " + path + ": " + e.getMessage());
                droneTextures[i] = createCrystalDroneTexture(locWidth, locHeight);
            }
        }

        try {
            if (Gdx.files.internal("sprite/object-map/wheel.png").exists()) {
                wheelTexture = new Texture(Gdx.files.internal("sprite/object-map/wheel.png"));
            } else {
                wheelTexture = createSolidTexture(tileSize * 2, tileSize * 2, 139, 115, 85, 255);
            }
        } catch (Exception e) {
            wheelTexture = createSolidTexture(tileSize * 2, tileSize * 2, 139, 115, 85, 255);
        }

        greenTileTexture = createSolidTexture(tileSize, tileSize, 34, 139, 34, 255);
    }

    private void applyBorderTiles() {
        if(borderLayer == null) return;
        int borderX = outdoorZone.getBorderX();
        int borderY = outdoorZone.getBorderY();
        int borderWidth = outdoorZone.getBorderWidth();
        int borderHeight = outdoorZone.getBorderHeight();
        for(int x = borderX; x < borderX + borderWidth; x++) {
            for(int y = borderY; y < borderY + borderHeight; y++) {
                int layerX = x - worldMinX;
                int layerY = y - worldMinY;
                if(layerX >= 0 && layerX < borderLayer.getWidth() && layerY >= 0 && layerY < borderLayer.getHeight()) {
                    if(outdoorZone.isInBorder(x, y)) {
                        StaticTiledMapTile tile = new StaticTiledMapTile(new TextureRegion(borderTileTexture));
                        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                        cell.setTile(tile);
                        borderLayer.setCell(layerX, layerY, cell);
                    }
                }
            }
        }
    }

    public void applyGreenTiles(int locationIndex) {
        if (borderLayer == null) return;
        if (locationIndex < 0 || locationIndex >= outdoorZone.getLocations().size()) return;
        ScavengingLocation location = outdoorZone.getLocations().get(locationIndex);

        int borderOnlyTiles = outdoorZone.getBorderWidth() * outdoorZone.getBorderHeight()
                - outdoorZone.getBaseWidth() * outdoorZone.getBaseHeight();
        int targetTiles = Math.max(1, borderOnlyTiles / 5);

        TiledMapTileLayer refLayer = null;
        if (referenceMap != null) {
            for (com.badlogic.gdx.maps.MapLayer layer : referenceMap.getLayers()) {
                if (layer instanceof TiledMapTileLayer) {
                    refLayer = (TiledMapTileLayer) layer;
                    break;
                }
            }
        }

        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        java.util.Set<Long> visited = new java.util.HashSet<>(greenedTiles);
        java.util.Queue<int[]> queue = new java.util.LinkedList<>();

        int startX = location.getTopLeft().x();
        int startY = location.getTopLeft().y();
        for (int x = startX; x < startX + location.getWidth(); x++) {
            for (int y = startY; y < startY + location.getHeight(); y++) {
                long key = ((long) x << 32) | (y & 0xFFFFFFFFL);
                if (visited.add(key)) {
                    queue.add(new int[]{x, y});
                }
            }
        }

        for (long gk : greenedTiles) {
            int gx = (int)(gk >> 32);
            int gy = (int)(gk & 0xFFFFFFFFL);
            for (int d = 0; d < 4; d++) {
                int nx = gx + dx[d];
                int ny = gy + dy[d];
                long nk = ((long) nx << 32) | (ny & 0xFFFFFFFFL);
                if (visited.add(nk)) {
                    queue.add(new int[]{nx, ny});
                }
            }
        }

        int painted = 0;
        while (!queue.isEmpty() && painted < targetTiles) {
            int[] curr = queue.poll();
            int x = curr[0], y = curr[1];

            if (x >= 0 && x < borderLayer.getWidth()
                    && y >= 0 && y < borderLayer.getHeight()
                    && (outdoorZone.isInBorder(x, y) || outdoorZone.isInOutdoor(x, y))) {

                long tileKey = ((long) x << 32) | (y & 0xFFFFFFFFL);
                if (!greenedTiles.contains(tileKey)) {
                    TiledMapTileLayer.Cell cell = null;
                    if (refLayer != null && x < refLayer.getWidth() && y < refLayer.getHeight()) {
                        TiledMapTileLayer.Cell refCell = refLayer.getCell(x, y);
                        if (refCell != null) {
                            cell = new TiledMapTileLayer.Cell();
                            cell.setTile(refCell.getTile());
                            cell.setFlipHorizontally(refCell.getFlipHorizontally());
                            cell.setFlipVertically(refCell.getFlipVertically());
                            cell.setRotation(refCell.getRotation());
                        }
                    }
                    
                    if (cell == null) {
                        cell = new TiledMapTileLayer.Cell();
                        cell.setTile(new com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile(new TextureRegion(greenTileTexture)));
                    }

                    borderLayer.setCell(x, y, cell);
                    greenedTiles.add(tileKey);
                    painted++;
                }

                for (int d = 0; d < 4; d++) {
                    int nx = x + dx[d];
                    int ny = y + dy[d];
                    long nkey = ((long) nx << 32) | (ny & 0xFFFFFFFFL);
                    if (visited.add(nkey)) {
                        queue.add(new int[]{nx, ny});
                    }
                }
            }
        }
    }

    public void clearGreenedTiles() {
        if (borderLayer == null) return;
        for (int x = 0; x < borderLayer.getWidth(); x++) {
            for (int y = 0; y < borderLayer.getHeight(); y++) {
                borderLayer.setCell(x, y, null);
            }
        }
        greenedTiles.clear();
    }

    private Texture createCrystalDroneTexture(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(255/255f, 0/255f, 255/255f, 1f); // Magenta fallback
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void applyOutdoorZoneTiles() {
        for(ScavengingLocation location : outdoorZone.getLocations()) {
            int startX = location.getTopLeft().x();
            int startY = location.getTopLeft().y();
            int width = location.getWidth();
            int height = location.getHeight();
            int colorIndex = outdoorZone.getLocations().indexOf(location);
            if (colorIndex < 0 || colorIndex >= locationTextures.length) continue;
            Texture locationTexture = locationTextures[colorIndex];
            for(int x = startX; x < startX + width; x++) {
                for(int y = startY; y < startY + height; y++) {
                    int layerX = x - worldMinX;
                    int layerY = y - worldMinY;
                    if(layerX >= 0 && layerX < zoneLayer.getWidth() && layerY >= 0 && layerY < zoneLayer.getHeight()) {
                        StaticTiledMapTile tile = new StaticTiledMapTile(new TextureRegion(locationTexture));
                        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                        cell.setTile(tile);
                        zoneLayer.setCell(layerX, layerY, cell);
                    }
                }
            }
        }
    }

    private Texture createSolidTexture(int width, int height, int r, int g, int b, int a) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(r/255f, g/255f, b/255f, a/255f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void render(OrthographicCamera camera, long scavengeDuration) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        int tileSize = zoneLayer.getTileWidth();
        int locationCount = Math.min(outdoorZone.getLocations().size(), droneTextures.length);
        for(int i = 0; i < locationCount; i++) {
            ScavengingLocation location = outdoorZone.getLocations().get(i);
            float locStartX = location.getTopLeft().x() * tileSize;
            float locStartY = location.getTopLeft().y() * tileSize;
            float locWidth = location.getWidth() * tileSize;
            float locHeight = location.getHeight() * tileSize;

            if (location.getLocationType() == ScavengingLocation.LocationType.SEED_WHEEL) {
                batch.setColor(1, 1, 1, 1f);
                float targetSize = tileSize * 6f;
                float drawX = locStartX + (locWidth - targetSize) / 2f;
                float drawY = locStartY + (locHeight - targetSize) / 2f;
                batch.draw(wheelTexture, drawX, drawY, targetSize, targetSize);
            } else {
                batch.setColor(1, 1, 1, 1.0f);
                float crystalScale = 0.5f;
                float drawW = locWidth * crystalScale;
                float drawH = locHeight * crystalScale;
                float drawX = locStartX + (locWidth - drawW) / 2f;
                float drawY = locStartY + (locHeight - drawH) / 2f;
                batch.draw(droneTextures[i], drawX, drawY, drawW, drawH);
            }

            if(location.isScavenging()) renderProgressBar(location, locStartX, locStartY, locWidth, locHeight, scavengeDuration);
            if(location.isInCooldown()) renderCooldownIndicator(location, locStartX, locStartY, locWidth, locHeight);
        }
        batch.end();
    }

    private void renderProgressBar(ScavengingLocation location, float x, float y, float width, float height, long scavengeDuration) {
        float progress = location.getScavengingProgress(scavengeDuration) / 100f;
        float barHeight = 8;
        float barY = y + height + 5;
        
        // Background (gray)
        batch.setColor(0.2f, 0.2f, 0.2f, 0.8f);
        batch.draw(whitePixel, x, barY, width, barHeight);
        
        // Progress (green)
        batch.setColor(0, 1, 0, 1);
        batch.draw(whitePixel, x, barY, width * progress, barHeight);
    }

    private void renderCooldownIndicator(ScavengingLocation location, float x, float y, float width, float height) {
        float progress = location.getCooldownProgress() / 100f;
        if (progress <= 0) return;
        
        float barHeight = 6;
        float barY = y + height + 5;
        
        // Background (dark red/gray)
        batch.setColor(0.3f, 0, 0, 0.5f);
        batch.draw(whitePixel, x, barY, width, barHeight);
        
        // Cooldown remaining (bright red)
        batch.setColor(1, 0, 0, 0.8f);
        batch.draw(whitePixel, x, barY, width * progress, barHeight);
    }

    public TiledMapTileLayer getBorderLayer() { return borderLayer; }
    public void setMap(TiledMap map) { this.map = map; }

    public void dispose() {
        if(borderTileTexture != null) borderTileTexture.dispose();
        if(locationTextures != null) for(Texture t : locationTextures) if(t != null) t.dispose();
        if(droneTextures != null) for(Texture t : droneTextures) if(t != null) t.dispose();
        if(wheelTexture != null) wheelTexture.dispose();
        if(greenTileTexture != null) greenTileTexture.dispose();
        if(batch != null) batch.dispose();
    }
}
