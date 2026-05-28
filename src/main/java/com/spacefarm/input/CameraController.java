package com.spacefarm.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CameraController extends InputAdapter {
    private static final float BASE_MOVE_SPEED = 600f;
    private static final float ZOOM_STEP = 0.15f;
    private static final float ZOOM_SMOOTHNESS = 10f;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final WorldBounds bounds;
    private final float minZoom;

    private boolean dragging;
    private float dragStartX;
    private float dragStartY;
    private float targetZoom;

    public CameraController(OrthographicCamera camera, Viewport viewport, WorldBounds bounds,
                            float minZoom, float maxZoom) {
        this.camera = camera;
        this.viewport = viewport;
        this.bounds = bounds;
        this.minZoom = minZoom;
        this.targetZoom = camera.zoom;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Buttons.RIGHT || com.badlogic.gdx.Gdx.input.isKeyPressed(Keys.SPACE)) {
            dragging = true;
            dragStartX = screenX;
            dragStartY = screenY;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!dragging) {
            return false;
        }
        float dx = screenX - dragStartX;
        float dy = screenY - dragStartY;
        float worldDx = -dx * camera.zoom;
        float worldDy = dy * camera.zoom;
        camera.position.add(worldDx, worldDy, 0f);
        dragStartX = screenX;
        dragStartY = screenY;
        clamp();
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Buttons.RIGHT) {
            dragging = false;
        }
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        float maxZoomOut = getMaxZoomOut();
        targetZoom = MathUtils.clamp(targetZoom + amountY * ZOOM_STEP, minZoom, maxZoomOut);
        return true;
    }

    public void update(float deltaSeconds) {
        float moveSpeed = BASE_MOVE_SPEED * camera.zoom * deltaSeconds;
        float dx = 0f;
        float dy = 0f;

        if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) {
            dx -= moveSpeed;
        }
        if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) {
            dx += moveSpeed;
        }
        if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN)) {
            dy -= moveSpeed;
        }
        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)) {
            dy += moveSpeed;
        }

        if (dx != 0f || dy != 0f) {
            camera.position.add(dx, dy, 0f);
        }

        float maxZoomOut = getMaxZoomOut();
        targetZoom = MathUtils.clamp(targetZoom, minZoom, maxZoomOut);
        camera.zoom = smooth(camera.zoom, targetZoom, deltaSeconds, ZOOM_SMOOTHNESS);

        clamp();
    }

    public void clamp() {
        float maxZoomOut = getMaxZoomOut();
        camera.zoom = MathUtils.clamp(camera.zoom, minZoom, maxZoomOut);

        float halfViewportWidth = (viewport.getWorldWidth() * 0.5f) * camera.zoom;
        float halfViewportHeight = (viewport.getWorldHeight() * 0.5f) * camera.zoom;

        float minX = bounds.minX() + halfViewportWidth;
        float maxX = bounds.maxX() - halfViewportWidth;
        float minY = bounds.minY() + halfViewportHeight;
        float maxY = bounds.maxY() - halfViewportHeight;

        camera.position.x = MathUtils.clamp(camera.position.x, minX, Math.max(minX, maxX));
        camera.position.y = MathUtils.clamp(camera.position.y, minY, Math.max(minY, maxY));
    }

    private float smooth(float current, float target, float deltaSeconds, float smoothness) {
        float alpha = 1f - (float) Math.exp(-smoothness * deltaSeconds);
        return current + (target - current) * alpha;
    }

    private float getMaxZoomOut() {
        float worldWidth = bounds.maxX() - bounds.minX();
        float worldHeight = bounds.maxY() - bounds.minY();
        float maxZoomX = worldWidth / viewport.getWorldWidth();
        float maxZoomY = worldHeight / viewport.getWorldHeight();
        float maxZoomOut = Math.min(maxZoomX, maxZoomY);
        return Math.max(minZoom, maxZoomOut);
    }
}
