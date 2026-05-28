package com.spacefarm.input;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.spacefarm.world.TileCoord;

public class TilePicker {
    private final OrthographicCamera camera;
    private final int tileWidth;
    private final int tileHeight;
    private final int mapWidth;
    private final int mapHeight;
    private final Vector3 tmp = new Vector3();

    public TilePicker(OrthographicCamera camera, int tileWidth, int tileHeight, int mapWidth, int mapHeight) {
        this.camera = camera;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    public TileCoord screenToTile(int screenX, int screenY) {
        tmp.set(screenX, screenY, 0f);
        camera.unproject(tmp);
        int x = (int) (tmp.x / tileWidth);
        int y = (int) (tmp.y / tileHeight);
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) {
            return null;
        }
        return new TileCoord(x, y);
    }
}

