package com.spacefarm;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
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
import com.spacefarm.render.GridOverlay;
import com.spacefarm.world.TileCoord;

public class GameApp extends ApplicationAdapter {
    private static final int DEFAULT_TILE_SIZE = 32;
    private static final int DEFAULT_MAP_WIDTH = 40;
    private static final int DEFAULT_MAP_HEIGHT = 30;
    private static final float MIN_ZOOM = 0.5f;
    private static final float MAX_ZOOM = 2.5f;

    private OrthographicCamera camera;
    private Viewport viewport;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private GridOverlay gridOverlay;
    private CameraController cameraController;

    private Texture baseTileTexture;
    private Texture highlightTexture;
    private TiledMapTileLayer baseLayer;
    private TiledMapTileLayer selectionLayer;
    private TilePicker tilePicker;

    private TileCoord lastSelected;

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

        selectionLayer = new TiledMapTileLayer(baseLayer.getWidth(), baseLayer.getHeight(),
                baseLayer.getTileWidth(), baseLayer.getTileHeight());
        map.getLayers().add(selectionLayer);

        tilePicker = new TilePicker(camera, baseLayer.getTileWidth(), baseLayer.getTileHeight(),
                baseLayer.getWidth(), baseLayer.getHeight());
        gridOverlay = new GridOverlay(baseLayer);

        centerCameraOnMap();
        cameraController = new CameraController(camera, viewport, buildWorldBounds(), MIN_ZOOM, MAX_ZOOM);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (cameraController.touchDown(screenX, screenY, pointer, button)) {
                    return true;
                }
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

        cameraController.update(Gdx.graphics.getDeltaTime());
        camera.update();
        renderer.setView(camera);
        renderer.render();

        gridOverlay.render(camera);
    }

    @Override
    public void dispose() {
        if (renderer != null) {
            renderer.dispose();
        }
        if (gridOverlay != null) {
            gridOverlay.dispose();
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
    }

    private void handleTileClick(int screenX, int screenY) {
        TileCoord coord = tilePicker.screenToTile(screenX, screenY);
        if (coord == null) {
            return;
        }

        if (lastSelected != null) {
            selectionLayer.setCell(lastSelected.x(), lastSelected.y(), null);
        }

        selectionLayer.setCell(coord.x(), coord.y(), createHighlightCell());
        lastSelected = coord;
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
        float worldWidth = baseLayer.getWidth() * baseLayer.getTileWidth();
        float worldHeight = baseLayer.getHeight() * baseLayer.getTileHeight();
        camera.position.set(worldWidth / 2f, worldHeight / 2f, 0f);
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
}
