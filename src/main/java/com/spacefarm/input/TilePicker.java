package com.spacefarm.input;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.spacefarm.world.TileCoord;

public class TilePicker {
    private final OrthographicCamera camera;
    private final int tileWidth;
    private final int tileHeight;
    private final int minX;
    private final int minY;
    private final int maxX;
    private final int maxY;
    private final Vector3 tmp = new Vector3();

    public TilePicker(OrthographicCamera camera, int tileWidth, int tileHeight,
                      int minX, int minY, int maxX, int maxY) {
        this.camera = camera;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public TileCoord screenToTile(int screenX, int screenY) {
        tmp.set(screenX, screenY, 0f);
        camera.unproject(tmp);
        int x = (int) Math.floor(tmp.x / tileWidth);
        int y = (int) Math.floor(tmp.y / tileHeight);
        if (x < minX || y < minY || x >= maxX || y >= maxY) {
            return null;
        }
        return new TileCoord(x, y);
    }
}
