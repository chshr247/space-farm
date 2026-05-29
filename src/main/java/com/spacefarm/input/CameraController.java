package com.spacefarm.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CameraController extends InputAdapter {
    private static final float BASE_MOVE_SPEED = 600f;
    private static final float ZOOM_STEP = 0.15f;
    private static final float ZOOM_SMOOTHNESS = 10f;
    private static final float SCROLL_PAN_SPEED = 50f;
    private static final long SCROLL_MERGE_WINDOW_NANOS = 30_000_000L;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final WorldBounds bounds;
    private final float minZoom;

    private boolean dragging;
    private float dragStartX;
    private float dragStartY;
    private float targetZoom;
    private float lastScrollX;
    private float lastScrollY;
    private long lastScrollXTime;
    private long lastScrollYTime;

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
        if (button == Buttons.MIDDLE || Gdx.input.isKeyPressed(Keys.SPACE)) {
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
        if (button == Buttons.MIDDLE) {
            dragging = false;
        }
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (isZoomGesture(amountX, amountY)) {
            float maxZoomOut = getMaxZoomOut();
            targetZoom = MathUtils.clamp(targetZoom + amountY * ZOOM_STEP, minZoom, maxZoomOut);
            return true;
        }

        float mergedX = mergeScrollAxis(amountX, lastScrollX, lastScrollXTime);
        float mergedY = mergeScrollAxis(amountY, lastScrollY, lastScrollYTime);

        if (amountX != 0f) {
            lastScrollX = amountX;
            lastScrollXTime = TimeUtils.nanoTime();
        }
        if (amountY != 0f) {
            lastScrollY = amountY;
            lastScrollYTime = TimeUtils.nanoTime();
        }

        float worldDx = -mergedX * SCROLL_PAN_SPEED * camera.zoom;
        float worldDy = mergedY * SCROLL_PAN_SPEED * camera.zoom;
        if (worldDx != 0f || worldDy != 0f) {
            camera.position.add(worldDx, worldDy, 0f);
            clamp();
        }
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

        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > 0f) {
            float scale = moveSpeed / length;
            camera.position.add(dx * scale, dy * scale, 0f);
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

    private boolean isZoomGesture(float amountX, float amountY) {
        if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) {
            return true;
        }
        return Math.abs(amountX) < 0.01f && Math.abs(amountY) >= 1f;
    }

    private float mergeScrollAxis(float current, float last, long lastTime) {
        if (current != 0f) {
            return current;
        }
        long now = TimeUtils.nanoTime();
        if (now - lastTime <= SCROLL_MERGE_WINDOW_NANOS) {
            return last;
        }
        return 0f;
    }
}
