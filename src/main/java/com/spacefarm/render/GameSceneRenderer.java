package com.spacefarm.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.spacefarm.session.GameSession;

public class GameSceneRenderer {
    private final GameSession gameSession;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final GridOverlay gridOverlay;
    private final CropRenderer cropRenderer;
    private final BaseZoneRenderer baseZoneRenderer;
    private final OutdoorZoneRenderer outdoorZoneRenderer;
    private final OxygenUI oxygenUI;
    private final TreeBoxUI treeBoxUI;

    public GameSceneRenderer(GameSession gameSession) {
        this.gameSession = gameSession;

        TiledMap map = gameSession.getMap();
        TiledMapTileLayer baseLayer = gameSession.getBaseLayer();
        TiledMapTileLayer zoneLayer = gameSession.getZoneLayer();
        int tileSize = baseLayer.getTileWidth();
        int worldMinX = gameSession.getWorldMinX();
        int worldMinY = gameSession.getWorldMinY();

        this.mapRenderer = new OrthogonalTiledMapRenderer(map);
        this.gridOverlay = new GridOverlay(baseLayer);
        this.cropRenderer = new CropRenderer(gameSession.getFarmingSystem(), baseLayer);
        
        // Initialize BaseZoneRenderer AFTER OutdoorZoneRenderer potentially clearing the layer
        this.outdoorZoneRenderer = new OutdoorZoneRenderer(gameSession.getOutdoorZone(), zoneLayer, map, tileSize, worldMinX, worldMinY, gameSession);
        this.outdoorZoneRenderer.setReferenceMap(gameSession.getReferenceMap());
        gameSession.setOutdoorZoneRenderer(this.outdoorZoneRenderer);
        
        // Ensure zoneLayer (base structures) is on top of borderLayer (outdoor greening)
        map.getLayers().remove(zoneLayer);
        map.getLayers().add(zoneLayer);
        
        this.baseZoneRenderer = new BaseZoneRenderer(gameSession.getBaseZone(), zoneLayer, tileSize, worldMinX, worldMinY);
        this.baseZoneRenderer.setReferenceMap(gameSession.getReferenceMap());
        gameSession.setBaseZoneRenderer(this.baseZoneRenderer);
        
        gameSession.setSceneRenderer(this);
        this.oxygenUI = new OxygenUI(gameSession.getOxygenManager());
        this.treeBoxUI = gameSession.getTreeBoxUI();
    }

    public void onMapChanged(TiledMap newMap) {
        mapRenderer.setMap(newMap);
        TiledMapTileLayer newBaseLayer = gameSession.getBaseLayer();
        gridOverlay.setLayer(newBaseLayer);
        cropRenderer.setLayer(newBaseLayer);
    }

    public void render(OrthographicCamera camera, int screenWidth, int screenHeight) {
        mapRenderer.setView(camera);
        mapRenderer.render();

        gridOverlay.render(camera);
        baseZoneRenderer.render(camera);
        cropRenderer.render(camera);

        int upgradeLevel = gameSession.getDroneConsoleOverlay().getScavengeUpgradeLevel();
        long durationMillis = Math.max(30000L, com.spacefarm.world.OutdoorConstants.SCAVENGING_DURATION_MILLIS - upgradeLevel * 30000L);
        outdoorZoneRenderer.render(camera, durationMillis);

        gameSession.getContextMenu().render(camera);

        if (gameSession.isVictory()) {
            gameSession.getVictoryOverlay().render(screenWidth, screenHeight);
        } else if (gameSession.isGameOver()) {
            gameSession.getGameOverOverlay().render(screenWidth, screenHeight);
        } else {
            oxygenUI.render(screenWidth, screenHeight);
            gameSession.getSeedWheelOverlay().render(screenWidth, screenHeight);
            gameSession.getDroneConsoleOverlay().render();
            gameSession.getInventoryUI().render(screenWidth, screenHeight);
            treeBoxUI.render(screenWidth, screenHeight);
        }
    }

    public void dispose() {
        mapRenderer.dispose();
        gridOverlay.dispose();
        baseZoneRenderer.dispose();
        outdoorZoneRenderer.dispose();
        cropRenderer.dispose();
        oxygenUI.dispose();
    }
}
