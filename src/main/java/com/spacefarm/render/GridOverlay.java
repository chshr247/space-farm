package com.spacefarm.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class GridOverlay {
    private final ShapeRenderer renderer = new ShapeRenderer();
    private TiledMapTileLayer layer;

    public GridOverlay(TiledMapTileLayer layer) {
        this.layer = layer;
    }

    public void setLayer(TiledMapTileLayer layer) {
        this.layer = layer;
    }

    public void render(OrthographicCamera camera) {
        float tileWidth = layer.getTileWidth();
        float tileHeight = layer.getTileHeight();
        int width = layer.getWidth();
        int height = layer.getHeight();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(1f, 1f, 1f, 0.05f);

        for (int x = 0; x <= width; x++) {
            float worldX = x * tileWidth;
            renderer.line(worldX, 0f, worldX, height * tileHeight);
        }
        for (int y = 0; y <= height; y++) {
            float worldY = y * tileHeight;
            renderer.line(0f, worldY, width * tileWidth, worldY);
        }

        renderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void dispose() {
        renderer.dispose();
    }
}

