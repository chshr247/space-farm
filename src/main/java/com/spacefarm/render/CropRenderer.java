package com.spacefarm.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.spacefarm.farming.Crop;
import com.spacefarm.farming.FarmingConstants;
import com.spacefarm.farming.FarmingConstants.GrowthStage;
import com.spacefarm.farming.FarmingSystem;

import java.util.HashMap;
import java.util.Map;


public class CropRenderer {
    private final FarmingSystem farmingSystem;
    private TiledMapTileLayer baseLayer;
    private final SpriteBatch batch;

    public void setLayer(TiledMapTileLayer layer) {
        this.baseLayer = layer;
    }
    private final Map<FarmingConstants.CropType, Texture[]> cropTexturesMap;
    private final Texture waterIndicatorTexture;

    public CropRenderer(FarmingSystem farmingSystem, TiledMapTileLayer baseLayer) {
        this.farmingSystem = farmingSystem;
        this.baseLayer = baseLayer;
        this.batch = new SpriteBatch();
        this.cropTexturesMap = new HashMap<>();
        this.waterIndicatorTexture = createWaterIndicatorTexture();

        cropTexturesMap.put(FarmingConstants.CropType.DEFAULT, loadTextures("sprite/plants/stage1.png", "sprite/plants/stage2.png", "sprite/plants/stage3.png"));

        cropTexturesMap.put(FarmingConstants.CropType.EPIC, loadTextures("sprite/plants/epic_stage1.png", "sprite/plants/epic_stage2.png", "sprite/plants/epic_stage2.png"));

        cropTexturesMap.put(FarmingConstants.CropType.LEGENDARY, loadTextures("sprite/plants/leg_stage1.png", "sprite/plants/leg_stage2.png", "sprite/plants/leg_stage3.png"));
    }

    private Texture[] loadTextures(String stage1, String stage2, String stage3) {
        Texture[] textures = new Texture[4];
        Texture seed = new Texture(stage1);
        Texture sprout = new Texture(stage2);
        Texture mature = new Texture(stage3);

        textures[GrowthStage.SEED.ordinal()]   = seed;
        textures[GrowthStage.SPROUT.ordinal()] = sprout;
        textures[GrowthStage.YOUNG.ordinal()]  = mature;
        textures[GrowthStage.MATURE.ordinal()] = mature;
        return textures;
    }

    public void render(OrthographicCamera camera) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        int mapWidth = baseLayer.getWidth();
        int mapHeight = baseLayer.getHeight();
        float tileWidth = baseLayer.getTileWidth();
        float tileHeight = baseLayer.getTileHeight();

        for (int y = mapHeight - 1; y >= 0; y--) {
            for (int x = 0; x < mapWidth; x++) {
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
        int stageIndex = crop.getGrowthStage().ordinal();

        // правильний масив текстур залежно від типу рослини
        Texture[] texturesForType = cropTexturesMap.get(crop.getType());
        Texture cropTexture = texturesForType[Math.min(stageIndex, 3)];

        float textureWidth = cropTexture.getWidth();
        float textureHeight = cropTexture.getHeight();
        float drawWidth = tileWidth;
        float drawHeight = tileWidth * (textureHeight / textureWidth);

        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(cropTexture, worldX, worldY, drawWidth, drawHeight);

        renderWaterIndicator(crop, worldX, worldY, tileWidth, tileHeight);
    }

    private void renderWaterIndicator(Crop crop, float worldX, float worldY, float tileWidth, float tileHeight) {
        float waterProgress = crop.getWaterProgress();
        FarmingConstants.WaterState waterState = crop.getWaterState();

        Color indicatorColor;
        switch (waterState) {
            case WELL_WATERED:
                indicatorColor = new Color(0.2f, 0.7f, 0.2f, 0.4f); // Green
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

        float barHeight = tileHeight * 0.15f;
        float barWidth = tileWidth * waterProgress;

        batch.draw(waterIndicatorTexture, worldX, worldY, barWidth, barHeight);
    }

    private Texture createWaterIndicatorTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void dispose() {
        // Очищаємо всі текстури з Map
        for (Texture[] textures : cropTexturesMap.values()) {
            if (textures[0] != null) textures[0].dispose();
            if (textures[1] != null) textures[1].dispose();
            if (textures[2] != null) textures[2].dispose();
        }

        if (waterIndicatorTexture != null) {
            waterIndicatorTexture.dispose();
        }
        batch.dispose();
    }
}