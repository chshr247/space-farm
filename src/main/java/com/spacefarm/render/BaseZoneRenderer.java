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

/**
 * Renders the base zone with green tiles and special structures.
 */
public class BaseZoneRenderer {
    private BaseZone baseZone;
    private TiledMapTileLayer baseLayer;

    // Textures for different areas
    private Texture greenTileTexture;
    private Texture treeTileTexture;
    private Texture gardenTileTexture;
    private Texture droneTileTexture;
    private Texture treeTextureOverlay;
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

// Створюємо новий порожній Pixmap потрібного розміру мапи (наприклад, 64x64)
        Pixmap scaledPixmap = new Pixmap(tileSize, tileSize, originalPixmap.getFormat());

// Вмикаємо фільтрацію NearestNeighbour, щоб піксель-арт залишався чітким і не "милив"
        scaledPixmap.setFilter(Pixmap.Filter.NearestNeighbour);

// Масштабуємо (розтягуємо) оригінальне зображення на новий Pixmap
        scaledPixmap.drawPixmap(originalPixmap,
                0, 0, originalPixmap.getWidth(), originalPixmap.getHeight(),
                0, 0, scaledPixmap.getWidth(), scaledPixmap.getHeight()
        );

// Створюємо фінальну текстуру з розтягнутого зображення
        gardenTileTexture = new Texture(scaledPixmap);

// Очищуємо пам'ять від тимчасових Pixmap
        originalPixmap.dispose();
        scaledPixmap.dispose();

        // Drone zone tile (grayish for metal/tech)
        droneTileTexture = createSolidTexture(tileSize, tileSize, 70, 70, 80, 255); // Slate gray

        // Create overlay textures for tree and drone
        treeTextureOverlay = createTreeSpriteTexture(tileSize * 10, tileSize * 20);
        droneTextureOverlay = createDroneSpriteTexture(tileSize * 5, tileSize * 5);
    }

    private Texture createTreeSpriteTexture(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // Background transparent
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();

        // Draw tree trunk (brown)
        pixmap.setColor(139 / 255f, 69 / 255f, 19 / 255f, 1f);
        int trunkWidth = width / 4;
        int trunkHeight = height / 3;
        pixmap.fillRectangle((width - trunkWidth) / 2, 0, trunkWidth, trunkHeight);

        // Draw tree crown (green)
        pixmap.setColor(34 / 255f, 139 / 255f, 34 / 255f, 1f);
        int crownWidth = width / 2;
        int crownHeight = height * 2 / 3;
        pixmap.fillRectangle((width - crownWidth) / 2, trunkHeight - crownHeight / 3, crownWidth, crownHeight);

        // Draw a lighter green highlight
        pixmap.setColor(50 / 255f, 205 / 255f, 50 / 255f, 0.7f);
        pixmap.fillCircle(width / 2, trunkHeight + crownHeight / 4, crownWidth / 3);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createDroneSpriteTexture(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // Background transparent
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();

        // Draw drone body (yellow/gold with purple)
        pixmap.setColor(255 / 255f, 215 / 255f, 0 / 255f, 1f);
        int bodySize = Math.min(width, height) / 3;
        pixmap.fillRectangle((width - bodySize) / 2, (height - bodySize) / 2, bodySize, bodySize);

        // Draw drone propellers (4 small circles)
        pixmap.setColor(128 / 255f, 128 / 255f, 128 / 255f, 1f);
        int propSize = bodySize / 3;
        // Top-left
        pixmap.fillCircle((width - bodySize) / 2 - propSize, (height + bodySize) / 2, propSize / 2);
        // Top-right
        pixmap.fillCircle((width + bodySize) / 2 + propSize, (height + bodySize) / 2, propSize / 2);
        // Bottom-left
        pixmap.fillCircle((width - bodySize) / 2 - propSize, (height - bodySize) / 2, propSize / 2);
        // Bottom-right
        pixmap.fillCircle((width + bodySize) / 2 + propSize, (height - bodySize) / 2, propSize / 2);

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

                    // Determine which tile type to use
                    if (baseZone.isTreeArea(coord)) {
                        tileTexture = treeTileTexture;
                    } else if (baseZone.isGardenBed(coord)) {
                        tileTexture = gardenTileTexture;
                    } else if (baseZone.isDroneZone(coord)) {
                        tileTexture = droneTileTexture;
                    }

                    // Set the tile
                    StaticTiledMapTile tile = new StaticTiledMapTile(new TextureRegion(tileTexture));
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
        // Render structure overlays (tree and drone sprites)
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Render tree overlay
        renderTreeOverlay();

        // Render drone overlay
        renderDroneOverlay();

        batch.end();
    }

    private void renderTreeOverlay() {
        int tileSize = baseLayer.getTileWidth();
        TileCoord treePos = baseZone.getTreeCenter();

        float treeStartX = treePos.x() * tileSize;
        float treeStartY = treePos.y() * tileSize;
        float treeWidth = baseZone.getTreeWidth() * tileSize;
        float treeHeight = baseZone.getTreeHeight() * tileSize;

        batch.setColor(1, 1, 1, 0.9f);
        batch.draw(treeTextureOverlay, treeStartX, treeStartY, treeWidth, treeHeight);
    }

    private void renderDroneOverlay() {
        int tileSize = baseLayer.getTileWidth();
        TileCoord dronePos = baseZone.getDroneZoneCenter();

        float droneStartX = dronePos.x() * tileSize;
        float droneStartY = dronePos.y() * tileSize;
        float droneSize = baseZone.getDroneZoneSize() * tileSize;

        batch.setColor(1, 1, 1, 0.85f);
        batch.draw(droneTextureOverlay, droneStartX, droneStartY, droneSize, droneSize);
    }

    public void dispose() {
        if (greenTileTexture != null) greenTileTexture.dispose();
        if (treeTileTexture != null) treeTileTexture.dispose();
        if (gardenTileTexture != null) gardenTileTexture.dispose();
        if (droneTileTexture != null) droneTileTexture.dispose();
        if (treeTextureOverlay != null) treeTextureOverlay.dispose();
        if (droneTextureOverlay != null) droneTextureOverlay.dispose();
        if (batch != null) batch.dispose();
    }
}


