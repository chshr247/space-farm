package com.spacefarm;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacefarm.input.CameraController;
import com.spacefarm.input.TilePicker;
import com.spacefarm.input.WorldBounds;
import com.spacefarm.render.ContextMenuOverlay;
import com.spacefarm.render.CropRenderer;
import com.spacefarm.render.GameOverOverlay;
import com.spacefarm.render.GridOverlay;
import com.spacefarm.render.InventoryUI;
import com.spacefarm.render.OxygenUI;
import com.spacefarm.render.BaseZoneRenderer;
import com.spacefarm.render.OutdoorZoneRenderer;
import com.spacefarm.render.SeedWheelOverlay;
import com.spacefarm.world.TileCoord;
import com.spacefarm.world.BaseZone;
import com.spacefarm.world.OutdoorZone;
import com.spacefarm.world.ScavengingLocation;
import com.spacefarm.world.SeedWheelConstants;
import com.spacefarm.farming.FarmingSystem;
import com.spacefarm.inventory.Inventory;
import com.spacefarm.inventory.Item;
import com.spacefarm.inventory.Seed;
import com.spacefarm.inventory.RareSeed;
import com.spacefarm.inventory.LegendarySeed;
import com.spacefarm.inventory.PlantFood;
import com.spacefarm.inventory.Sickle;
import com.spacefarm.inventory.Crystal;
import com.spacefarm.oxygen.OxygenManager;
import com.spacefarm.oxygen.OxygenConstants;

public class GameApp extends ApplicationAdapter {
    private static final int DEFAULT_TILE_SIZE = 32;
    private static final int DEFAULT_MAP_WIDTH = 256;  // Було 64, тепер 256 для border
    private static final int DEFAULT_MAP_HEIGHT = 256; // Було 64, тепер 256 для border
    private static final float MIN_ZOOM = 0.5f;
    private static final float MAX_ZOOM = 2.5f;

    private OrthographicCamera camera;
    private Viewport viewport;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private GridOverlay gridOverlay;
    private CameraController cameraController;
    private ContextMenuOverlay contextMenu;

    private Texture baseTileTexture;
    private Texture highlightTexture;
    private TiledMapTileLayer baseLayer;
    private TiledMapTileLayer selectionLayer;
    private TilePicker tilePicker;

    private BaseZone baseZone;
    private BaseZoneRenderer baseZoneRenderer;
    private OutdoorZone outdoorZone;
    private OutdoorZoneRenderer outdoorZoneRenderer;

    private TileCoord lastSelected;
    private FarmingSystem farmingSystem;
    private CropRenderer cropRenderer;
    private Inventory inventory;
    private InventoryUI inventoryUI;
    private OxygenManager oxygenManager;
    private OxygenUI oxygenUI;
    private GameOverOverlay gameOverOverlay;
    private boolean isGameOver;
    private SeedWheelOverlay seedWheelOverlay;
    private ScavengingLocation currentSeedWheelLocation;  // Track which location the wheel is spinning for
    private long lastSeedWheelSpinTime = 0;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);

        map = loadMapOrFallback();
        renderer = new OrthogonalTiledMapRenderer(map);

        baseLayer = findFirstTileLayer(map);
        if (baseLayer == null) {
            baseLayer = createFallbackLayer(DEFAULT_MAP_WIDTH, DEFAULT_MAP_HEIGHT, DEFAULT_TILE_SIZE, DEFAULT_TILE_SIZE);
            map.getLayers().add(baseLayer);
        }

        // Initialize base zone
        int mapCenterX = baseLayer.getWidth() / 2;
        int mapCenterY = baseLayer.getHeight() / 2;
        baseZone = new BaseZone(mapCenterX, mapCenterY, 64, 64);

        // Initialize outdoor zone with map reference
        outdoorZone = new OutdoorZone(baseZone, baseLayer.getWidth(), baseLayer.getHeight());
        outdoorZoneRenderer = new OutdoorZoneRenderer(outdoorZone, baseLayer, map, DEFAULT_TILE_SIZE);

        // Initialize base zone renderer (after outdoor to not overwrite border)
        baseZoneRenderer = new BaseZoneRenderer(baseZone, baseLayer, DEFAULT_TILE_SIZE);

        selectionLayer = new TiledMapTileLayer(baseLayer.getWidth(), baseLayer.getHeight(),
                baseLayer.getTileWidth(), baseLayer.getTileHeight());
        map.getLayers().add(selectionLayer);

        tilePicker = new TilePicker(camera, baseLayer.getTileWidth(), baseLayer.getTileHeight(),
                baseLayer.getWidth(), baseLayer.getHeight());
        gridOverlay = new GridOverlay(baseLayer);
        contextMenu = new ContextMenuOverlay();

        // Initialize farming system
        farmingSystem = new FarmingSystem(baseLayer.getWidth(), baseLayer.getHeight());
        cropRenderer = new CropRenderer(farmingSystem, baseLayer);

        // Initialize inventory system
        inventory = new Inventory();
        inventoryUI = new InventoryUI(inventory, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Add seeds to slot 2
        inventory.addItem(1, new Seed(5));  // Index 1 = slot 2 in display

        // Add sickle to slot 3
        inventory.addItem(2, Sickle.getInstance());  // Index 2 = slot 3 in display

        // Initialize oxygen system
        oxygenManager = new OxygenManager();
        oxygenManager.setBaseZone(baseZone);
        oxygenUI = new OxygenUI(oxygenManager);
        
        // Initialize game over overlay
        gameOverOverlay = new GameOverOverlay();
        isGameOver = false;

        // Initialize seed wheel overlay
        seedWheelOverlay = new SeedWheelOverlay();
        currentSeedWheelLocation = null;

        centerCameraOnMap();
        cameraController = new CameraController(camera, viewport, buildWorldBounds(), MIN_ZOOM, MAX_ZOOM);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (isGameOver) {
                    return false;  // Ignore clicks when game is over
                }

                // Handle seed wheel button click
                if (seedWheelOverlay.isVisible()) {
                    if (button == Buttons.LEFT) {
                        // Check if button is hit
                        float adjustedY = Gdx.graphics.getHeight() - screenY;  // Invert Y for UI coordinates
                        if (seedWheelOverlay.isButtonHit(screenX, adjustedY)) {
                            seedWheelOverlay.startSpin();
                            lastSeedWheelSpinTime = System.currentTimeMillis();
                            return true;
                        }
                    }
                    return false;  // Don't process other interactions while wheel is visible
                }

                if (cameraController.touchDown(screenX, screenY, pointer, button)) {
                    return true;
                }
                if (button == Buttons.RIGHT) {
                    showContextMenu(screenX, screenY);
                    return true;
                }
                contextMenu.hide();
                handleTileClick(screenX, screenY);
                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return cameraController.touchDragged(screenX, screenY, pointer);
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                return cameraController.touchUp(screenX, screenY, pointer, button);
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                return cameraController.scrolled(amountX, amountY);
            }

            @Override
            public boolean keyDown(int keycode) {
                // Handle inventory slot selection (keys 1-8)
                if (keycode >= com.badlogic.gdx.Input.Keys.NUM_1 && 
                    keycode <= com.badlogic.gdx.Input.Keys.NUM_8) {
                    int slotIndex = keycode - com.badlogic.gdx.Input.Keys.NUM_1;
                    inventory.selectSlot(slotIndex);
                    return true;
                }
                
                // Handle eating plant food or seeds (E key)
                if (keycode == com.badlogic.gdx.Input.Keys.E) {
                    Item selected = inventory.getSelectedItem();

                    if (selected != null) {
                        // Handle plant food
                        if (selected.getType() == Item.ItemType.PLANT_FOOD) {
                            if (inventory.consumePlantFood()) {
                                // Increase oxygen
                                oxygenManager.consumeFood();

                                // Remove plant food if empty
                                PlantFood food =
                                    (PlantFood) inventory.getSelectedItem();
                                if (food != null && food.getQuantity() == 0) {
                                    // Find and remove the empty stack
                                    for (int i = 0; i < 8; i++) {
                                        Item item = inventory.getItem(i);
                                        if (item != null && item.getType() ==
                                            Item.ItemType.PLANT_FOOD) {
                                            PlantFood f =
                                                (PlantFood) item;
                                            if (f.getQuantity() == 0) {
                                                inventory.removeItem(i);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // Handle rare seed
                        else if (selected.getType() == Item.ItemType.RARE_SEED) {
                            RareSeed rareSeed = (RareSeed) selected;
                            if (rareSeed.useSeed()) {
                                // Restore 20% oxygen
                                float oxygenToAdd = OxygenConstants.MAX_OXYGEN *
                                    (SeedWheelConstants.RARE_SEED_OXYGEN_RESTORE / 100f);
                                oxygenManager.setOxygen(oxygenManager.getOxygen() + oxygenToAdd);

                                // Remove if empty
                                if (rareSeed.getQuantity() == 0) {
                                    inventory.removeItem(inventory.getSelectedSlot());
                                }
                            }
                        }
                        // Handle legendary seed
                        else if (selected.getType() == Item.ItemType.LEGENDARY_SEED) {
                            LegendarySeed legendarySeed = (LegendarySeed) selected;
                            if (legendarySeed.useSeed()) {
                                // Restore 50% oxygen
                                float oxygenToAdd = OxygenConstants.MAX_OXYGEN *
                                    (SeedWheelConstants.LEGENDARY_SEED_OXYGEN_RESTORE / 100f);
                                oxygenManager.setOxygen(oxygenManager.getOxygen() + oxygenToAdd);

                                // Remove if empty
                                if (legendarySeed.getQuantity() == 0) {
                                    inventory.removeItem(inventory.getSelectedSlot());
                                }
                            }
                        }
                    }
                    return true;
                }
                
                return false;
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        cameraController.clamp();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.08f, 0.09f, 0.12f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float deltaTime = Gdx.graphics.getDeltaTime();

        // Check if oxygen is depleted
        if (oxygenManager.isOxygenDepleted() && !isGameOver) {
            isGameOver = true;
        }

        // Update seed wheel UI
        seedWheelOverlay.update(deltaTime);

        // Handle seed wheel result
        if (seedWheelOverlay.hasResult()) {
            int resultType = seedWheelOverlay.getResultAndReset();
            handleSeedWheelResult(resultType);

            // Mark location as cleared and set cooldown
            if (currentSeedWheelLocation != null) {
                currentSeedWheelLocation.completeScavenging();
                currentSeedWheelLocation = null;

                // Close wheel UI after a short delay
                seedWheelOverlay.setVisible(false);
            }
        }

        // Only update game if not game over
        if (!isGameOver) {
            cameraController.update(deltaTime);
            // Update farming system
            farmingSystem.update(deltaTime);

            // Update oxygen system
            oxygenManager.update(deltaTime);
            
            // Update scavenging system
            updateScavenging(deltaTime);
        }

        camera.update();
        renderer.setView(camera);
        renderer.render();

        gridOverlay.render(camera);
        cropRenderer.render(camera);
        baseZoneRenderer.render(camera);
        outdoorZoneRenderer.render(camera);
        contextMenu.render(camera);

        // Render inventory UI (screen space)
        inventoryUI.render(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Render oxygen UI (screen space)
        oxygenUI.render(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Render seed wheel overlay if visible
        seedWheelOverlay.render(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Render game over overlay if game is over
        if (isGameOver) {
            gameOverOverlay.render(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
    }

    @Override
    public void dispose() {
        if (renderer != null) {
            renderer.dispose();
        }
        if (gridOverlay != null) {
            gridOverlay.dispose();
        }
        if (baseZoneRenderer != null) {
            baseZoneRenderer.dispose();
        }
        if (outdoorZoneRenderer != null) {
            outdoorZoneRenderer.dispose();
        }
        if (cropRenderer != null) {
            cropRenderer.dispose();
        }
        if (inventoryUI != null) {
            inventoryUI.dispose();
        }
        if (oxygenUI != null) {
            oxygenUI.dispose();
        }
        if (gameOverOverlay != null) {
            gameOverOverlay.dispose();
        }
        if (seedWheelOverlay != null) {
            seedWheelOverlay.dispose();
        }
        if (map != null) {
            map.dispose();
        }
        if (baseTileTexture != null) {
            baseTileTexture.dispose();
        }
        if (highlightTexture != null) {
            highlightTexture.dispose();
        }
        if (contextMenu != null) {
            contextMenu.dispose();
        }
    }

    private void handleTileClick(int screenX, int screenY) {
        TileCoord coord = tilePicker.screenToTile(screenX, screenY);
        if (coord == null) {
            return;
        }

        // Update oxygen manager with current position
        oxygenManager.updatePositionTile(coord);

        if (lastSelected != null) {
            selectionLayer.setCell(lastSelected.x(), lastSelected.y(), null);
        }

        selectionLayer.setCell(coord.x(), coord.y(), createHighlightCell());
        lastSelected = coord;

        // Check if in outdoor zone - handle based on location type
        ScavengingLocation location = outdoorZone.getLocationAt(coord);
        if (location != null) {
            if (location.getLocationType() == ScavengingLocation.LocationType.SEED_WHEEL) {
                // Show seed wheel UI
                if (!location.isInCooldown()) {
                    seedWheelOverlay.setVisible(true);
                    currentSeedWheelLocation = location;
                }
            } else {
                // Regular crystal scavenging
                if (!location.isCleared() && !location.isScavenging() && !location.isInCooldown()) {
                    location.startScavenging();
                }
            }
            return;
        }

        // Check if watering can is selected
        if (inventory.isWateringCanSelected()) {
            // Water the crop if it exists
            if (farmingSystem.hasCrop(coord)) {
                farmingSystem.waterCrop(coord);
            }
        } else if (inventory.isSeedSelected()) {
            // Plant seed only if no crop exists and seed is selected
            if (!farmingSystem.hasCrop(coord)) {
                if (farmingSystem.plantSeed(coord)) {
                    // Use one seed from inventory
                    inventory.useSeed();
                    
                    // Remove seeds from inventory if they run out
                    Item selectedItem = inventory.getSelectedItem();
                    if (selectedItem != null) {
                        if (selectedItem.getType() == Item.ItemType.SEED) {
                            Seed seed = (Seed) selectedItem;
                            if (seed.getQuantity() == 0) {
                                inventory.removeItem(inventory.getSelectedSlot());
                            }
                        } else if (selectedItem.getType() == Item.ItemType.RARE_SEED) {
                            RareSeed seed = (RareSeed) selectedItem;
                            if (seed.getQuantity() == 0) {
                                inventory.removeItem(inventory.getSelectedSlot());
                            }
                        } else if (selectedItem.getType() == Item.ItemType.LEGENDARY_SEED) {
                            LegendarySeed seed = (LegendarySeed) selectedItem;
                            if (seed.getQuantity() == 0) {
                                inventory.removeItem(inventory.getSelectedSlot());
                            }
                        }
                    }
                }
            }
        } else if (inventory.isSickleSelected()) {
            // Harvest mature crop
            if (farmingSystem.hasCrop(coord)) {
                if (farmingSystem.harvestCrop(coord)) {
                    // Add harvested plant food to inventory
                    inventory.addPlantFood(1);
                }
            }
        }
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

    private WorldBounds buildWorldBounds() {
        float worldWidth = baseLayer.getWidth() * baseLayer.getTileWidth();
        float worldHeight = baseLayer.getHeight() * baseLayer.getTileHeight();
        return new WorldBounds(0f, 0f, worldWidth, worldHeight);
    }

    private void centerCameraOnMap() {
        // Center camera on the base zone center, not the map center
        int baseX = baseZone.getBaseX();
        int baseY = baseZone.getBaseY();
        int baseWidth = baseZone.getBaseWidth();
        int baseHeight = baseZone.getBaseHeight();

        float baseCenterX = (baseX + baseWidth / 2f) * DEFAULT_TILE_SIZE;
        float baseCenterY = (baseY + baseHeight / 2f) * DEFAULT_TILE_SIZE;

        camera.position.set(baseCenterX, baseCenterY, 0f);
        camera.update();
    }


    private TiledMapTileLayer createFallbackLayer(int width, int height, int tileWidth, int tileHeight) {
        baseTileTexture = createSolidTexture(tileWidth, tileHeight, 60, 70, 90, 255);
        StaticTiledMapTile tile = new StaticTiledMapTile(new TextureRegion(baseTileTexture));

        TiledMapTileLayer layer = new TiledMapTileLayer(width, height, tileWidth, tileHeight);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(tile);
                layer.setCell(x, y, cell);
            }
        }
        return layer;
    }

    private TiledMapTileLayer.Cell createHighlightCell() {
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

    private void showContextMenu(int screenX, int screenY) {
        TileCoord coord = tilePicker.screenToTile(screenX, screenY);
        if (coord == null) {
            contextMenu.hide();
            return;
        }


        float worldX = coord.x() * baseLayer.getTileWidth();
        float worldY = coord.y() * baseLayer.getTileHeight();
        contextMenu.showAt(worldX, worldY);
    }
    
    private void handleSeedWheelResult(int resultType) {
        // resultType: 0 = common, 1 = rare, 2 = legendary

        if (resultType == 0) {
            // Common seeds
            inventory.addItem(new Seed(SeedWheelConstants.COMMON_SEED_REWARD));
        } else if (resultType == 1) {
            // Rare seeds
            inventory.addItem(new RareSeed(SeedWheelConstants.RARE_SEED_REWARD));
        } else if (resultType == 2) {
            // Legendary seeds
            inventory.addItem(new LegendarySeed(SeedWheelConstants.LEGENDARY_SEED_REWARD));
        }
    }

    private void updateScavenging(float deltaTime) {
        for (ScavengingLocation location : outdoorZone.getScavengingLocations()) {
            if (location.isScavenging()) {
                // Consume oxygen using stable accumulation (2% every 10 seconds)
                oxygenManager.consumeOxygenDuringScavenging(deltaTime);

                // Check if scavenging is complete
                if (location.isScavengingComplete()) {
                    location.completeScavenging();
                    // Add crystal to inventory
                    inventory.addItem(new Crystal());
                }
            }
        }
    }
}
