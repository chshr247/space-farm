package com.spacefarm.input;

import com.badlogic.gdx.InputAdapter;
import com.spacefarm.session.GameSession;

public class GameInputRouter extends InputAdapter {
    private final CameraController cameraController;
    private final GameSession gameSession;

    public GameInputRouter(CameraController cameraController, GameSession gameSession) {
        this.cameraController = cameraController;
        this.gameSession = gameSession;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (cameraController.touchDown(screenX, screenY, pointer, button)) {
            return true;
        }
        return gameSession.handleTouchDown(screenX, screenY, button);
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
        return gameSession.handleKeyDown(keycode);
    }
}

