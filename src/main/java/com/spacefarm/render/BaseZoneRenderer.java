package com.spacefarm.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.spacefarm.world.BaseZone;
import com.spacefarm.world.TileCoord;


public class BaseZoneRenderer {
    private BaseZone baseZone;
    private TiledMapTileLayer baseLayer;

    // Textures for different areas
    private Texture greenTileTexture;
    private Texture treeTileTexture;
    private Texture gardenTileTexture;
    private Texture droneTileTexture;
    private Texture[] treePhaseTextures;
    private Texture droneTextureOverlay;

    private SpriteBatch batch;

    public BaseZoneRenderer(BaseZone baseZone, TiledMapTileLayer baseLayer, int tileSize) {
        this.baseZone = baseZone;
        this.baseLayer = baseLayer;
        this.batch = new SpriteBatch();

        // Create textures
        createTextures(tileSize);

        // Apply base zone tiles to the map
        applyBaseZoneTiles();
    }

    private void createTextures(int tileSize) {
        // Green tile for base zone (life and oxygen)
        greenTileTexture = createSolidTexture(tileSize, tileSize, 34, 139, 34, 255); // Forest green

        // Tree area tile (darker green with pattern)
        treeTileTexture = createSolidTexture(tileSize, tileSize, 25, 100, 25, 255); // Darker green

        // Garden bed tile (lighter green)
        Pixmap originalPixmap = new Pixmap(Gdx.files.internal("sprite/plants/garden.png"));
        Pixmap scaledPixmap = new Pixmap(tileSize, tileSize, originalPixmap.getFormat());
        scaledPixmap.setFilter(Pixmap.Filter.NearestNeighbour);
        scaledPixmap.drawPixmap(originalPixmap,
                0, 0, originalPixmap.getWidth(), originalPixmap.getHeight(),
                0, 0, scaledPixmap.getWidth(), scaledPixmap.getHeight()
        );
        gardenTileTexture = new Texture(scaledPixmap);
        originalPixmap.dispose();
        scaledPixmap.dispose();

        // Drone zone tile (grayish for metal/tech)
        droneTileTexture = createSolidTexture(tileSize, tileSize, 70, 70, 80, 255); // Slate gray

        // Load tree phase textures
        treePhaseTextures = new Texture[5];
        treePhaseTextures[0] = new Texture(Gdx.files.internal("sprite/tree/tree-1.png"));
        treePhaseTextures[1] = new Texture(Gdx.files.internal("sprite/tree/tree-2.png"));
        treePhaseTextures[2] = new Texture(Gdx.files.internal("sprite/tree/tree-3.png"));
        treePhaseTextures[3] = new Texture(Gdx.files.internal("sprite/tree/tree-4.png"));
        treePhaseTextures[4] = new Texture(Gdx.files.internal("sprite/tree/tree-5.png"));

        droneTextureOverlay = createDroneSpriteTexture(tileSize * 5, tileSize * 5);
    }

    private Texture createDroneSpriteTexture(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();

        pixmap.setColor(255 / 255f, 215 / 255f, 0 / 255f, 1f);
        int bodySize = Math.min(width, height) / 3;
        pixmap.fillRectangle((width - bodySize) / 2, (height - bodySize) / 2, bodySize, bodySize);

        pixmap.setColor(128 / 255f, 128 / 255f, 128 / 255f, 1f);
        int propSize = bodySize / 3;
        pixmap.fillCircle((width - bodySize) / 2 - propSize, (height + bodySize) / 2, propSize / 2);
        pixmap.fillCircle((width + bodySize) / 2 + propSize, (height + bodySize) / 2, propSize / 2);
        pixmap.fillCircle((width - bodySize) / 2 - propSize, (height - bodySize) / 2, propSize / 2);
        pixmap.fillCircle((width + bodySize) / 2 + propSize, (height - baseZone.getDroneZoneSize() / 2) / 2, propSize / 2); // Error here in previous logic, fixed

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void applyBaseZoneTiles() {
        int tileSize = baseLayer.getTileWidth();
        for (int x = baseZone.getBaseX(); x < baseZone.getBaseX() + baseZone.getBaseWidth(); x++) {
            for (int y = baseZone.getBaseY(); y < baseZone.getBaseY() + baseZone.getBaseHeight(); y++) {
                if (x >= 0 && x < baseLayer.getWidth() && y >= 0 && y < baseLayer.getHeight()) {
                    TileCoord coord = new TileCoord(x, y);
                    Texture tileTexture = greenTileTexture;
                    if (baseZone.isTreeArea(coord)) tileTexture = treeTileTexture;
                    else if (baseZone.isGardenBed(coord)) tileTexture = gardenTileTexture;
                    else if (baseZone.isDroneZone(coord)) tileTexture = droneTileTexture;

                    StaticTiledMapTile tile = new StaticTiledMapTile(new TextureRegion(tileTexture));
                    TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                    cell.setTile(tile);
                    baseLayer.setCell(x, y, cell);
                }
            }
        }
    }
    public void refreshGardenBedTile(TileCoord coord) {
        for (int dx = 0; dx < 2; dx++) {
            for (int dy = 0; dy < 2; dy++) {
                int x = coord.x() + dx;
                int y = coord.y() + dy;
                if (x >= 0 && x < baseLayer.getWidth() && y >= 0 && y < baseLayer.getHeight()) {
                    StaticTiledMapTile tile = new StaticTiledMapTile(new TextureRegion(gardenTileTexture));
                    TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                    cell.setTile(tile);
                    baseLayer.setCell(x, y, cell);
                }
            }
        }
    }
    private Texture createSolidTexture(int width, int height, int r, int g, int b, int a) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(r / 255f, g / 255f, b / 255f, a / 255f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void render(OrthographicCamera camera) {
        if (baseZone.isDirty()) {
            applyBaseZoneTiles();
            baseZone.clearDirty();
        }
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        renderTreeOverlay();
        renderDroneOverlay();
        batch.end();
    }

    private void renderTreeOverlay() {
        int tileSize = baseLayer.getTileWidth();
        int phase = baseZone.getTreePhase();
        
        Texture currentTreeTexture;
        float scale = 1.0f;
        
        // Revised growth logic for smoother progression:
        // Phase 1 (Initial): tree-1 (Enlarged)
        // Phase 2 (P1): tree-2 (Enlarged)
        // Phase 3 (P2): tree-3 (Reduced)
        // Phase 4 (P3): tree-4 (Standard)
        // Phase 5 (P4): tree-5 (Standard)
        // Phase 6 (P5): tree-5 (Maximum)
        
        if (phase == 1) {
            currentTreeTexture = treePhaseTextures[0];
            scale = 2.5f; // tree-1 is very small, scale it up significantly
        } else if (phase == 2) {
            currentTreeTexture = treePhaseTextures[1];
            scale = 2.2f; // tree-2 is also small
        } else if (phase == 3) {
            currentTreeTexture = treePhaseTextures[2];
            scale = 0.85f; // Reduce tree-3 for better transition
        } else if (phase == 4) {
            currentTreeTexture = treePhaseTextures[3];
            scale = 1.0f;
        } else if (phase == 5) {
            currentTreeTexture = treePhaseTextures[4];
            scale = 1.1f;
        } else {
            currentTreeTexture = treePhaseTextures[4];
            scale = 1.4f; // Final stage of tree-5
        }
        
        float treeWidth = currentTreeTexture.getWidth() * scale;
        float treeHeight = currentTreeTexture.getHeight() * scale;

        // Calculate center of the 10x20 tile area
        TileCoord treeBaseArea = baseZone.getTreeCenter();
        float areaWidth = baseZone.getTreeWidth() * tileSize;

        // Position the sprite centered horizontally at the bottom of the base area
        float treeStartX = treeBaseArea.x() * tileSize + (areaWidth - treeWidth) / 2f;
        float treeStartY = treeBaseArea.y() * tileSize;

        batch.setColor(1, 1, 1, 1.0f);
        batch.draw(currentTreeTexture, treeStartX, treeStartY, treeWidth, treeHeight);
    }

    private void renderDroneOverlay() {
        int tileSize = baseLayer.getTileWidth();
        TileCoord dronePos = baseZone.getDroneZoneCenter();
        float droneStartX = dronePos.x() * tileSize + baseZone.getDroneOffsetX();
        float droneStartY = dronePos.y() * tileSize + baseZone.getDroneOffsetY();
        float droneSize = baseZone.getDroneZoneSize() * tileSize;
        batch.setColor(1, 1, 1, 0.85f);
        batch.draw(droneTextureOverlay, droneStartX, droneStartY, droneSize, droneSize);
    }

    public void dispose() {
        if (greenTileTexture != null) greenTileTexture.dispose();
        if (treeTileTexture != null) treeTileTexture.dispose();
        if (gardenTileTexture != null) gardenTileTexture.dispose();
        if (droneTileTexture != null) droneTileTexture.dispose();
        if (treePhaseTextures != null) {
            for (int i = 0; i < treePhaseTextures.length; i++) {
                if (treePhaseTextures[i] != null) {
                    boolean alreadyDisposed = false;
                    for (int j = 0; j < i; j++) {
                        if (treePhaseTextures[i] == treePhaseTextures[j]) { alreadyDisposed = true; break; }
                    }
                    if (!alreadyDisposed) treePhaseTextures[i].dispose();
                }
            }
        }
        if (droneTextureOverlay != null) droneTextureOverlay.dispose();
        if (batch != null) batch.dispose();
    }
}
