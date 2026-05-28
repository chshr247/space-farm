package com.spacefarm;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Space Farm");
        config.setWindowedMode(1920, 1080);
        config.useVsync(true);
        new Lwjgl3Application(new GameApp(), config);
    }
}

