package com.spacefarm.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.spacefarm.farming.Crop;
import com.spacefarm.farming.FarmingConstants;
import com.spacefarm.farming.FarmingSystem;

/**
 * Renders crops and their growth stages.
 */
public class CropRenderer {
    private final FarmingSystem farmingSystem;
    private final TiledMapTileLayer baseLayer;
    private final SpriteBatch batch;
    private final Texture[] cropTextures;
    private final Texture waterIndicatorTexture;

    public CropRenderer(FarmingSystem farmingSystem, TiledMapTileLayer baseLayer) {
        this.farmingSystem = farmingSystem;
        this.baseLayer = baseLayer;
        this.batch = new SpriteBatch();
        this.cropTextures = new Texture[4];
        this.waterIndicatorTexture = createWaterIndicatorTexture();
        createCropTextures();
    }

    /**
     * Render all crops on the map.
     */
    public void render(OrthographicCamera camera) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        int mapWidth = baseLayer.getWidth();
        int mapHeight = baseLayer.getHeight();
        float tileWidth = baseLayer.getTileWidth();
        float tileHeight = baseLayer.getTileHeight();

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                Crop crop = farmingSystem.getCrop(x, y);
                if (crop != null) {
                    float worldX = x * tileWidth;
                    float worldY = y * tileHeight;
                    renderCrop(crop, worldX, worldY, tileWidth, tileHeight);
                }
            }
        }

        batch.end();
    }

    private void renderCrop(Crop crop, float worldX, float worldY, float tileWidth, float tileHeight) {
        // Render crop sprite based on growth stage
        int stageIndex = crop.getGrowthStage().ordinal();
        Texture cropTexture = cropTextures[Math.min(stageIndex, 3)];

        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(cropTexture, worldX, worldY, tileWidth, tileHeight);

        // Render water indicator
        renderWaterIndicator(crop, worldX, worldY, tileWidth, tileHeight);
    }

    private void renderWaterIndicator(Crop crop, float worldX, float worldY, float tileWidth, float tileHeight) {
        float waterProgress = crop.getWaterProgress();
        FarmingConstants.WaterState waterState = crop.getWaterState();

        Color indicatorColor;
        switch (waterState) {
            case WELL_WATERED:
                indicatorColor = new Color(0.2f, 0.7f, 0.2f, 0.8f); // Green
                break;
            case NORMAL:
                indicatorColor = new Color(0.3f, 0.6f, 0.9f, 0.6f);  // Blue
                break;
            case THIRSTY:
                indicatorColor = new Color(0.9f, 0.8f, 0.2f, 0.6f);  // Yellow
                break;
            case DYING:
                indicatorColor = new Color(0.9f, 0.3f, 0.2f, 0.8f);  // Red
                break;
            default:
                indicatorColor = new Color(1f, 1f, 1f, 1f);
        }

        batch.setColor(indicatorColor);

        // Draw a bar on the bottom of the tile showing water level
        float barHeight = tileHeight * 0.15f;
        float barWidth = tileWidth * waterProgress;

        batch.draw(waterIndicatorTexture, worldX, worldY, barWidth, barHeight);
    }

    /**
     * Create textures for each growth stage.
     */
    private void createCropTextures() {
        // Stage 0: Seed (small brown dot)
        cropTextures[0] = createStageTexture(8, 8, 0.6f, 0.4f, 0.2f);

        // Stage 1: Sprout (small green)
        cropTextures[1] = createStageTexture(16, 16, 0.2f, 0.8f, 0.2f);

        // Stage 2: Young (medium green)
        cropTextures[2] = createStageTexture(24, 24, 0.3f, 0.9f, 0.2f);

        // Stage 3: Mature (large green)
        cropTextures[3] = createStageTexture(28, 28, 0.2f, 0.7f, 0.1f);
    }

    private Texture createStageTexture(int width, int height, float r, float g, float b) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, 1f);

        // Draw a simple circle/ellipse for the plant
        int centerX = width / 2;
        int centerY = height / 2;
        int radiusX = width / 3;
        int radiusY = height / 3;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int dx = x - centerX;
                int dy = y - centerY;
                // Simple ellipse equation
                if ((dx * dx) / (radiusX * radiusX) + (dy * dy) / (radiusY * radiusY) <= 1) {
                    pixmap.drawPixel(x, y);
                }
            }
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createWaterIndicatorTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Dispose of all textures.
     */
    public void dispose() {
        for (Texture texture : cropTextures) {
            if (texture != null) {
                texture.dispose();
            }
        }
        if (waterIndicatorTexture != null) {
            waterIndicatorTexture.dispose();
        }
        batch.dispose();
    }
}


