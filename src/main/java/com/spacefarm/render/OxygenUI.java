package com.spacefarm.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.spacefarm.oxygen.OxygenManager;

/**
 * Renders oxygen level indicator in top-left corner.
 */
public class OxygenUI {
    private final OxygenManager oxygenManager;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;

    private static final float OXYGEN_BAR_WIDTH = 30f;
    private static final float OXYGEN_BAR_HEIGHT = 200f;
    private static final float OXYGEN_BAR_X = 10f;
    private static final float OXYGEN_BAR_Y = 10f;

    public OxygenUI(OxygenManager oxygenManager) {
        this.oxygenManager = oxygenManager;
        this.shapeRenderer = new ShapeRenderer();
        this.batch = new SpriteBatch();
        this.font = FontUtils.createFont("fonts/ArialBold.ttf", 20);
        this.font.setColor(Color.WHITE);
    }

    /**
     * Render the oxygen bar in screen space.
     */
    public void render(int screenWidth, int screenHeight) {
        // Background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.8f);
        shapeRenderer.rect(OXYGEN_BAR_X, OXYGEN_BAR_Y, OXYGEN_BAR_WIDTH, OXYGEN_BAR_HEIGHT);
        shapeRenderer.end();

        // Oxygen level
        float oxygenPercent = oxygenManager.getOxygenPercent();
        float filledHeight = OXYGEN_BAR_HEIGHT * oxygenPercent;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Color based on oxygen level
        if (oxygenPercent > 0.5f) {
            shapeRenderer.setColor(0.2f, 0.8f, 0.2f, 0.9f);  // Green
        } else if (oxygenPercent > 0.2f) {
            shapeRenderer.setColor(0.9f, 0.8f, 0.2f, 0.9f);  // Yellow
        } else {
            shapeRenderer.setColor(0.9f, 0.2f, 0.2f, 0.9f);  // Red (critical)
        }

        // Draw from bottom up
        shapeRenderer.rect(OXYGEN_BAR_X, OXYGEN_BAR_Y + OXYGEN_BAR_HEIGHT - filledHeight,
                          OXYGEN_BAR_WIDTH, filledHeight);
        shapeRenderer.end();

        // Border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.8f, 0.8f, 0.8f, 1.0f);
        shapeRenderer.rect(OXYGEN_BAR_X, OXYGEN_BAR_Y, OXYGEN_BAR_WIDTH, OXYGEN_BAR_HEIGHT);
        shapeRenderer.end();

        // Draw oxygen percentage text
        batch.begin();
        font.setColor(Color.WHITE);
        String oxygenText = String.format("%.0f%%", oxygenManager.getOxygen());
        font.draw(batch, oxygenText, OXYGEN_BAR_X + OXYGEN_BAR_WIDTH + 10f, OXYGEN_BAR_Y + OXYGEN_BAR_HEIGHT / 2f);
        batch.end();
    }

    /**
     * Dispose of resources.
     */
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}

