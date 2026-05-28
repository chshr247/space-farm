package com.spacefarm.render;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class ContextMenuOverlay {
    private static final float MENU_SIZE = 48f;

    private final ShapeRenderer renderer = new ShapeRenderer();
    private boolean visible;
    private float x;
    private float y;

    public void showAt(float worldX, float worldY) {
        this.x = worldX + MENU_SIZE / 2;
        this.y = worldY + MENU_SIZE / 2;
        this.visible = true;
    }

    public void hide() {
        this.visible = false;
    }

    public void render(OrthographicCamera camera) {
        if (!visible) {
            return;
        }
        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(0f, 0f, 0f, 0.65f);
        renderer.rect(x, y, MENU_SIZE, MENU_SIZE);
        renderer.end();

        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(1f, 1f, 1f, 0.8f);
        renderer.rect(x, y, MENU_SIZE, MENU_SIZE);
        renderer.end();
    }

    public void dispose() {
        renderer.dispose();
    }
}

