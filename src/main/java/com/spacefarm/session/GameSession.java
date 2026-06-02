package com.spacefarm.session;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.spacefarm.farming.FarmingSystem;
import com.spacefarm.input.TilePicker;
import com.spacefarm.input.WorldBounds;
import com.spacefarm.inventory.Inventory;
import com.spacefarm.inventory.Seed;
import com.spacefarm.inventory.Sickle;
import com.spacefarm.oxygen.OxygenManager;
import com.spacefarm.render.ContextMenuOverlay;
import com.spacefarm.render.GameOverOverlay;
import com.spacefarm.render.InventoryUI;
import com.spacefarm.render.SeedWheelOverlay;
import com.spacefarm.world.BaseZone;
import com.spacefarm.world.OutdoorZone;

public class GameSession {
    private static final int DEFAULT_TILE_SIZE = 64;
    private static final int DEFAULT_MAP_WIDTH = 128;
    private static final int DEFAULT_MAP_HEIGHT = 128;

    private TiledMap map;
    private TiledMapTileLayer baseLayer;
    private TiledMapTileLayer selectionLayer;
    private BaseZone baseZone;
    private OutdoorZone outdoorZone;
    private FarmingSystem farmingSystem;
    private Inventory inventory;
    private OxygenManager oxygenManager;
    private InventoryUI inventoryUI;
    private ContextMenuOverlay contextMenu;
    private GameOverOverlay gameOverOverlay;
    private SeedWheelOverlay seedWheelOverlay;
    private GameInteractionService interactionService;
    private TilePicker tilePicker;
    private boolean gameOver;
    private Texture baseTileTexture;
    private Texture highlightTexture;

    public void create(OrthographicCamera camera) {
        map = loadMapOrFallback();

        baseLayer = findFirstTileLayer(map);
        if (baseLayer == null) {
            baseLayer = createFallbackLayer();
            map.getLayers().add(baseLayer);
        }

        int mapCenterX = baseLayer.getWidth() / 2;
        int mapCenterY = baseLayer.getHeight() / 2;
        baseZone = new BaseZone(mapCenterX, mapCenterY, 32, 32);

        outdoorZone = new OutdoorZone(baseZone, baseLayer.getWidth(), baseLayer.getHeight());

        selectionLayer = new TiledMapTileLayer(baseLayer.getWidth(), baseLayer.getHeight(),
                baseLayer.getTileWidth(), baseLayer.getTileHeight());
        map.getLayers().add(selectionLayer);

        tilePicker = new TilePicker(camera, baseLayer.getTileWidth(), baseLayer.getTileHeight(),
                baseLayer.getWidth(), baseLayer.getHeight());

        farmingSystem = new FarmingSystem(baseLayer.getWidth(), baseLayer.getHeight());
        inventory = new Inventory();
        inventory.addItem(1, new Seed(5));
        inventory.addItem(2, Sickle.getInstance());

        oxygenManager = new OxygenManager();
        oxygenManager.setBaseZone(baseZone);

        inventoryUI = new InventoryUI(inventory, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        contextMenu = new ContextMenuOverlay();
        gameOverOverlay = new GameOverOverlay();
        seedWheelOverlay = new SeedWheelOverlay();
        interactionService = new GameInteractionService(this);
        gameOver = false;

        centerCameraOnMap(camera);
    }

    public void update(float deltaTime) {
        inventoryUI.update(deltaTime);
        interactionService.update(deltaTime);
    }

    public boolean handleTouchDown(int screenX, int screenY, int button) {
        return interactionService.handleTouchDown(screenX, screenY, button);
    }

    public boolean handleKeyDown(int keycode) {
        return interactionService.handleKeyDown(keycode);
    }

    public void dispose() {
        if (map != null) {
            map.dispose();
        }
        if (baseTileTexture != null) {
            baseTileTexture.dispose();
        }
        if (highlightTexture != null) {
            highlightTexture.dispose();
        }
        if (inventoryUI != null) {
            inventoryUI.dispose();
        }
        if (contextMenu != null) {
            contextMenu.dispose();
        }
        if (gameOverOverlay != null) {
            gameOverOverlay.dispose();
        }
        if (seedWheelOverlay != null) {
            seedWheelOverlay.dispose();
        }
    }

    public TiledMap getMap() {
        return map;
    }

    public TiledMapTileLayer getBaseLayer() {
        return baseLayer;
    }

    public BaseZone getBaseZone() {
        return baseZone;
    }

    public OutdoorZone getOutdoorZone() {
        return outdoorZone;
    }

    public FarmingSystem getFarmingSystem() {
        return farmingSystem;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public OxygenManager getOxygenManager() {
        return oxygenManager;
    }

    public InventoryUI getInventoryUI() {
        return inventoryUI;
    }

    public ContextMenuOverlay getContextMenu() {
        return contextMenu;
    }

    public GameOverOverlay getGameOverOverlay() {
        return gameOverOverlay;
    }

    public SeedWheelOverlay getSeedWheelOverlay() {
        return seedWheelOverlay;
    }

    public TilePicker getTilePicker() {
        return tilePicker;
    }

    public TiledMapTileLayer getSelectionLayer() {
        return selectionLayer;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public WorldBounds getWorldBounds() {
        float worldWidth = baseLayer.getWidth() * baseLayer.getTileWidth();
        float worldHeight = baseLayer.getHeight() * baseLayer.getTileHeight();
        return new WorldBounds(0f, 0f, worldWidth, worldHeight);
    }

    private TiledMap loadMapOrFallback() {
        FileHandle tmx = Gdx.files.internal("maps/world.tmx");
        if (tmx.exists()) {
            return new TmxMapLoader().load("maps/world.tmx");
        }
        return new TiledMap();
    }

    private TiledMapTileLayer findFirstTileLayer(TiledMap map) {
        MapLayers layers = map.getLayers();
        for (MapLayer layer : layers) {
            if (layer instanceof TiledMapTileLayer) {
                return (TiledMapTileLayer) layer;
            }
        }
        return null;
    }

    private void centerCameraOnMap(OrthographicCamera camera) {
        int baseX = baseZone.getBaseX();
        int baseY = baseZone.getBaseY();
        int baseWidth = baseZone.getBaseWidth();
        int baseHeight = baseZone.getBaseHeight();

        float tileWidth = baseLayer.getTileWidth();
        float tileHeight = baseLayer.getTileHeight();
        float baseCenterX = (baseX + baseWidth / 2f) * tileWidth;
        float baseCenterY = (baseY + baseHeight / 2f) * tileHeight;

        camera.position.set(baseCenterX, baseCenterY, 0f);
        camera.update();
    }

    private TiledMapTileLayer createFallbackLayer() {
        baseTileTexture = createSolidTexture(DEFAULT_TILE_SIZE, DEFAULT_TILE_SIZE, 60, 70, 90, 255);
        StaticTiledMapTile tile = new StaticTiledMapTile(new TextureRegion(baseTileTexture));

        TiledMapTileLayer layer = new TiledMapTileLayer(DEFAULT_MAP_WIDTH, DEFAULT_MAP_HEIGHT, DEFAULT_TILE_SIZE, DEFAULT_TILE_SIZE);
        for (int x = 0; x < DEFAULT_MAP_WIDTH; x++) {
            for (int y = 0; y < DEFAULT_MAP_HEIGHT; y++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(tile);
                layer.setCell(x, y, cell);
            }
        }
        return layer;
    }

    TiledMapTileLayer.Cell createHighlightCell() {
        if (highlightTexture == null) {
            highlightTexture = createSolidTexture(baseLayer.getTileWidth(), baseLayer.getTileHeight(), 255, 255, 0, 120);
        }
        StaticTiledMapTile tile = new StaticTiledMapTile(new TextureRegion(highlightTexture));
        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
        cell.setTile(tile);
        return cell;
    }

    private Texture createSolidTexture(int width, int height, int r, int g, int b, int a) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(r / 255f, g / 255f, b / 255f, a / 255f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
