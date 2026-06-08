package com.spacefarm.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioManager {
    private Sound plantSound;
    private Sound harvestSound;
    private Sound waterSound;
    private Sound wheelSound;
    private Music soundtrack;

    public AudioManager() {
        Gdx.app.log("AudioManager", "Initializing sounds...");
        try {
            plantSound = Gdx.audio.newSound(Gdx.files.internal("sound/garden.mp3"));
            harvestSound = Gdx.audio.newSound(Gdx.files.internal("sound/pickup-plants.mp3"));
            waterSound = Gdx.audio.newSound(Gdx.files.internal("sound/watering-garden.mp3"));
            wheelSound = Gdx.audio.newSound(Gdx.files.internal("sound/wheel.mp3"));
            
            soundtrack = Gdx.audio.newMusic(Gdx.files.internal("sound/soundtrack.mp3"));
            soundtrack.setLooping(true);
            soundtrack.setVolume(1.0f);
            Gdx.app.log("AudioManager", "Sounds initialized successfully.");
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Error loading sounds: " + e.getMessage());
        }
    }

    public void playPlantSound() {
        if (plantSound != null) {
            plantSound.play();
        } else {
            Gdx.app.error("AudioManager", "plantSound is null!");
        }
    }

    public void playHarvestSound() {
        if (harvestSound != null) {
            harvestSound.play();
        } else {
            Gdx.app.error("AudioManager", "harvestSound is null!");
        }
    }

    public void playWaterSound() {
        if (waterSound != null) {
            waterSound.play();
        } else {
            Gdx.app.error("AudioManager", "waterSound is null!");
        }
    }

    public void playWheelSound() {
        if (wheelSound != null) {
            Gdx.app.log("AudioManager", "Playing wheel sound");
            wheelSound.play();
        } else {
            Gdx.app.error("AudioManager", "wheelSound is null!");
        }
    }

    public void playMusic() {
        if (soundtrack != null) {
            if (!soundtrack.isPlaying()) {
                Gdx.app.log("AudioManager", "Starting music playback");
                soundtrack.play();
            }
        } else {
            Gdx.app.error("AudioManager", "soundtrack is null!");
        }
    }

    public void stopMusic() {
        if (soundtrack != null && soundtrack.isPlaying()) {
            soundtrack.stop();
        }
    }

    public void dispose() {
        if (plantSound != null) plantSound.dispose();
        if (harvestSound != null) harvestSound.dispose();
        if (waterSound != null) waterSound.dispose();
        if (wheelSound != null) wheelSound.dispose();
        if (soundtrack != null) soundtrack.dispose();
    }
}
