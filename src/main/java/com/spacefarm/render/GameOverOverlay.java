package com.spacefarm.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Renders a stylised Game Over screen when oxygen is depleted.
 */
public class GameOverOverlay {

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch   batch;
    private final BitmapFont    titleFont;
    private final BitmapFont    subFont;
    private final BitmapFont    hintFont;

    public GameOverOverlay() {
        shapeRenderer = new ShapeRenderer();
        batch         = new SpriteBatch();
        titleFont     = FontUtils.createFont("fonts/ArialBold.ttf", 56);
        subFont       = FontUtils.createFont("fonts/ArialBold.ttf", 22);
        hintFont      = FontUtils.createFont("fonts/ArialBold.ttf", 16);
    }

    public void render(int sw, int sh) {
        float cx = sw / 2f;
        float cy = sh / 2f;

        // ── 1. Full-screen vignette ───────────────────────────────────────────
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // deep dark overlay
        shapeRenderer.setColor(0.04f, 0f, 0f, 0.82f);
        shapeRenderer.rect(0, 0, sw, sh);

        // centre glow (dark crimson panel)
        float panelW = 600f, panelH = 360f;
        float px = cx - panelW / 2f, py = cy - panelH / 2f;
        shapeRenderer.setColor(0.10f, 0.02f, 0.02f, 0.96f);
        shapeRenderer.rect(px, py, panelW, panelH);

        // inner accent strip — top
        shapeRenderer.setColor(0.55f, 0.05f, 0.05f, 1f);
        shapeRenderer.rect(px, py + panelH - 4f, panelW, 4f);
        // inner accent strip — bottom
        shapeRenderer.rect(px, py, panelW, 4f);

        // decorative left / right side bars
        shapeRenderer.setColor(0.40f, 0.04f, 0.04f, 1f);
        shapeRenderer.rect(px, py, 4f, panelH);
        shapeRenderer.rect(px + panelW - 4f, py, 4f, panelH);

        // corner squares
        float cs = 14f;
        shapeRenderer.setColor(0.85f, 0.15f, 0.15f, 1f);
        shapeRenderer.rect(px - 2f,               py + panelH - cs, cs, cs);
        shapeRenderer.rect(px + panelW - cs + 2f, py + panelH - cs, cs, cs);
        shapeRenderer.rect(px - 2f,               py,               cs, cs);
        shapeRenderer.rect(px + panelW - cs + 2f, py,               cs, cs);

        shapeRenderer.end();

        // ── 2. Thin border lines ──────────────────────────────────────────────
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.75f, 0.10f, 0.10f, 1f);
        shapeRenderer.rect(px, py, panelW, panelH);
        shapeRenderer.setColor(0.45f, 0.07f, 0.07f, 1f);
        shapeRenderer.rect(px + 8f, py + 8f, panelW - 16f, panelH - 16f);
        shapeRenderer.end();

        // ── 3. Text — evenly distributed inside the panel ────────────────────
        // Panel inner area: py+8 .. py+panelH-8  (accounting for inset frame)
        // Five rows, equally spaced:
        //   row0 (GAME OVER title)  ~top 25%
        //   divider line            ~top 45%
        //   row1 (subtitle)         ~top 52%
        //   row2 (flavour)          ~top 67%
        //   row3 (hint)             ~bottom 15%

        float innerTop    = py + panelH - 20f;   // usable top
        float innerBottom = py + 20f;             // usable bottom
        float innerH      = innerTop - innerBottom;

        float titleY   = innerTop  - innerH * 0.10f;  // ~top 10% margin
        float dividerY = innerTop  - innerH * 0.44f;
        float subY     = innerTop  - innerH * 0.50f;
        float flavourY = innerTop  - innerH * 0.67f;
        float hintY    = innerBottom + innerH * 0.10f;

        GlyphLayout layout = new GlyphLayout();
        batch.begin();

        // "GAME OVER"
        titleFont.setColor(1f, 0.18f, 0.18f, 1f);
        layout.setText(titleFont, "GAME OVER");
        titleFont.draw(batch, "GAME OVER",
                cx - layout.width / 2f, titleY);

        batch.end();

        // divider
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.6f, 0.08f, 0.08f, 1f);
        float lineW = 400f;
        shapeRenderer.rect(cx - lineW / 2f, dividerY, lineW, 2f);
        shapeRenderer.end();

        batch.begin();

        // subtitle
        subFont.setColor(0.95f, 0.65f, 0.65f, 1f);
        layout.setText(subFont, "Кисень вичерпано");
        subFont.draw(batch, "Кисень вичерпано",
                cx - layout.width / 2f, subY);

        // flavour
        hintFont.setColor(0.60f, 0.35f, 0.35f, 1f);
        String flavour = "Атмосферу не вдалося відновити. Планета залишається мертвою.";
        layout.setText(hintFont, flavour);
        hintFont.draw(batch, flavour,
                cx - layout.width / 2f, flavourY);

        // bottom hint
        hintFont.setColor(0.40f, 0.22f, 0.22f, 1f);
        String hint = "Закрийте гру та спробуйте ще раз";
        layout.setText(hintFont, hint);
        hintFont.draw(batch, hint,
                cx - layout.width / 2f, hintY);

        batch.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        titleFont.dispose();
        subFont.dispose();
        hintFont.dispose();
    }
}