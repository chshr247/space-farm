package com.spacefarm.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Renders Game Over screen when oxygen is depleted.
 */
public class GameOverOverlay {
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont titleFont;
    private final BitmapFont textFont;

    public GameOverOverlay() {
        this.shapeRenderer = new ShapeRenderer();
        this.batch = new SpriteBatch();

        // Create title font (large)
        this.titleFont = FontUtils.createFont("fonts/ArialBold.ttf", 48);
        this.titleFont.setColor(Color.WHITE);

        // Create text font (normal)
        this.textFont = FontUtils.createFont("fonts/ArialBold.ttf", 24);
        this.textFont.setColor(Color.WHITE);
    }

    /**
     * Render the Game Over overlay.
     */
    public void render(int screenWidth, int screenHeight) {
        // Semi-transparent black overlay
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.7f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();

        // Draw Game Over text in center
        batch.begin();

        String gameOverText = "GAME OVER";
        String messageText = "Oxygen depleted!";

        // Measure text to center it
        GlyphLayout layout = new GlyphLayout();
        layout.setText(titleFont, gameOverText);
        float gameOverWidth = layout.width;

        layout.setText(textFont, messageText);
        float messageWidth = layout.width;

        // Draw Game Over text (red)
        titleFont.setColor(1f, 0.2f, 0.2f, 1f);
        titleFont.draw(batch, gameOverText,
                       (screenWidth - gameOverWidth) / 2f,
                       screenHeight * 0.6f);

        // Draw message text (white)
        textFont.setColor(1f, 1f, 1f, 1f);
        textFont.draw(batch, messageText,
                      (screenWidth - messageWidth) / 2f,
                      screenHeight * 0.45f);

        batch.end();
    }

    /**
     * Dispose of resources.
     */
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        titleFont.dispose();
        textFont.dispose();
    }
}

