package com.spacefarm.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.spacefarm.oxygen.OxygenManager;

/**
 * Renders oxygen level indicator in top-left corner.
 */
public class OxygenUI {
    private final OxygenManager oxygenManager;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch   batch;
    private final BitmapFont    font;
    private final BitmapFont    labelFont;

    private static final float BAR_W  = 24f;
    private static final float BAR_H  = 180f;
    private static final float BAR_X  = 20f;  // shifted right
    private static final float BAR_Y  = 60f;  // shifted up to make room for % below
    private static final float PAD    = 5f;   // padding around bar
    private static final int   TICKS  = 5;    // divider lines

    public OxygenUI(OxygenManager oxygenManager) {
        this.oxygenManager = oxygenManager;
        this.shapeRenderer = new ShapeRenderer();
        this.batch         = new SpriteBatch();
        this.font          = FontUtils.createFont("fonts/ArialBold.ttf", 16);
        this.labelFont     = FontUtils.createFont("fonts/ArialBold.ttf", 13);
    }

    /**
     * Render the oxygen bar in screen space.
     */
    public void render(int screenWidth, int screenHeight) {
        float oxygenPercent = oxygenManager.getOxygenPercent();
        float filledH       = BAR_H * oxygenPercent;

        float panelX = BAR_X - PAD;
        float panelY = BAR_Y - PAD;
        float panelW = BAR_W + PAD * 2;
        float panelH = BAR_H + PAD * 2;

        // Background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // dark panel background
        shapeRenderer.setColor(0.05f, 0.10f, 0.08f, 0.92f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);

        // bar background (darker inset)
        shapeRenderer.setColor(0.04f, 0.08f, 0.06f, 1f);
        shapeRenderer.rect(BAR_X, BAR_Y, BAR_W, BAR_H);

        // Oxygen level — color based on oxygen level
        if (oxygenPercent > 0.5f) {
            shapeRenderer.setColor(0.10f, 0.85f, 0.60f, 0.95f);   // Green
        } else if (oxygenPercent > 0.2f) {
            shapeRenderer.setColor(0.90f, 0.75f, 0.10f, 0.95f);   // Yellow
        } else {
            shapeRenderer.setColor(0.90f, 0.20f, 0.20f, 0.95f);   // Red (critical)
        }

        // Draw from bottom up
        shapeRenderer.rect(BAR_X, BAR_Y + BAR_H - filledH, BAR_W, filledH);

        // corner accents (teal squares)
        float cs = 4f;
        shapeRenderer.setColor(0.18f, 0.85f, 0.62f, 1f);
        shapeRenderer.rect(panelX,              panelY + panelH - cs, cs, cs);
        shapeRenderer.rect(panelX + panelW - cs, panelY + panelH - cs, cs, cs);
        shapeRenderer.rect(panelX,              panelY,              cs, cs);
        shapeRenderer.rect(panelX + panelW - cs, panelY,             cs, cs);

        shapeRenderer.end();

        // tick lines across bar
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.18f, 0.85f, 0.62f, 0.20f);
        for (int i = 1; i < TICKS; i++) {
            float ty = BAR_Y + (BAR_H / TICKS) * i;
            shapeRenderer.line(BAR_X, ty, BAR_X + BAR_W, ty);
        }

        // panel border (teal)
        shapeRenderer.setColor(0.12f, 0.55f, 0.40f, 0.85f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);

        // inner inset border (dimmer)
        shapeRenderer.setColor(0.08f, 0.35f, 0.25f, 0.5f);
        shapeRenderer.rect(panelX + 2f, panelY + 2f, panelW - 4f, panelH - 4f);

        shapeRenderer.end();

        // text
        GlyphLayout layout = new GlyphLayout();
        batch.begin();

        // O2 label above bar
        labelFont.setColor(0.18f, 0.85f, 0.62f, 1f);
        layout.setText(labelFont, "O2");
        labelFont.draw(batch, "O2",
                panelX + (panelW - layout.width) / 2f,
                panelY + panelH + 18f);

        // percentage below bar
        font.setColor(0.18f, 0.85f, 0.62f, 1f);
        if (oxygenPercent <= 0.2f) font.setColor(1f, 0.3f, 0.3f, 1f);
        else if (oxygenPercent <= 0.5f) font.setColor(0.95f, 0.80f, 0.20f, 1f);

        String pct = String.format("%.0f%%", oxygenManager.getOxygen());
        layout.setText(font, pct);
        font.draw(batch, pct,
                panelX + (panelW - layout.width) / 2f,
                panelY - 6f);

        batch.end();

        // LOW OXYGEN warning banner
        if (oxygenManager.isCritical()) {
            float boxW = 300f, boxH = 52f;
            float boxX = (screenWidth - boxW) / 2f;
            float boxY = screenHeight - 60f;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.10f, 0.0f, 0.0f, 0.85f);
            shapeRenderer.rect(boxX, boxY, boxW, boxH);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.9f, 0.1f, 0.1f, 1f);
            shapeRenderer.rect(boxX, boxY, boxW, boxH);
            shapeRenderer.rect(boxX + 2f, boxY + 2f, boxW - 4f, boxH - 4f);
            shapeRenderer.end();

            batch.begin();
            font.getData().setScale(1.6f);
            font.setColor(1f, 0.3f, 0.3f, 1f);
            GlyphLayout wl = new GlyphLayout(font, "LOW OXYGEN");
            font.draw(batch, wl,
                    boxX + (boxW - wl.width) / 2f,
                    boxY + (boxH + wl.height) / 2f);
            font.getData().setScale(1f);
            batch.end();
        }
    }

    /**
     * Dispose of resources.
     */
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        labelFont.dispose();
    }
}