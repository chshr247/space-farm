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
        int tileSize = baseLayer.getTileWidth();

        this.mapRenderer = new OrthogonalTiledMapRenderer(map);
        this.gridOverlay = new GridOverlay(baseLayer);
        this.cropRenderer = new CropRenderer(gameSession.getFarmingSystem(), baseLayer);
        this.outdoorZoneRenderer = new OutdoorZoneRenderer(gameSession.getOutdoorZone(), baseLayer, map, tileSize);
        this.baseZoneRenderer = new BaseZoneRenderer(gameSession.getBaseZone(), baseLayer, tileSize);
        this.oxygenUI = new OxygenUI(gameSession.getOxygenManager());
        this.treeBoxUI = gameSession.getTreeBoxUI();
    }

    public void render(OrthographicCamera camera, int screenWidth, int screenHeight) {
        mapRenderer.setView(camera);
        mapRenderer.render();

        gridOverlay.render(camera);
        cropRenderer.render(camera);
        baseZoneRenderer.render(camera);

        int upgradeLevel = gameSession.getDroneConsoleOverlay().getScavengeUpgradeLevel();
        long durationMillis = Math.max(30000L, com.spacefarm.world.OutdoorConstants.SCAVENGING_DURATION_MILLIS - upgradeLevel * 30000L);
        outdoorZoneRenderer.render(camera, durationMillis);

        gameSession.getContextMenu().render(camera);

        if (gameSession.isGameOver()) {
            // Game over: render only the overlay
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