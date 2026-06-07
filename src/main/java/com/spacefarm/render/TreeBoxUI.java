package com.spacefarm.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class TreeBoxUI {

    private static final float PANEL_W    = 460f;
    private static final float PANEL_H    = 380f;

    private static final float BOX_SIZE   = 70f;
    private static final float BOX_INNER  = 48f;
    private static final float BOX_GAP    = 18f;

    private static final float BTN_W      = 70f;
    private static final float BTN_H      = 24f;
    private static final float BTN_BOX_GAP = 6f;

    private static final float CLOSE_SIZE = 26f;

    private static final int BOX_COUNT = 5;

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch   batch;
    private final BitmapFont    font;
    private final BitmapFont    smallFont;

    private boolean visible = false;
    private int     phase   = 1;
    private float   closeX, closeY;

    private final float[]   btnX      = new float[BOX_COUNT];
    private final float[]   btnY      = new float[BOX_COUNT];
    private final boolean[] confirmed = new boolean[BOX_COUNT];

    public TreeBoxUI() {
        shapeRenderer = new ShapeRenderer();
        batch         = new SpriteBatch();
        font          = new BitmapFont();
        font.getData().setScale(1.1f);
        font.setColor(Color.WHITE);
        smallFont     = new BitmapFont();
        smallFont.getData().setScale(0.85f);
        smallFont.setColor(Color.WHITE);
    }

    public void show()         { visible = true; }
    public void hide()         { visible = false; }
    public void toggle()       { visible = !visible; }
    public boolean isVisible() { return visible; }

    public int getPhase() { return phase; }

    /** Returns true when all 5 tree phases have been confirmed. */
    public boolean isComplete() {
        for (boolean c : confirmed) {
            if (!c) return false;
        }
        return true;
    }

    /**
     * @param screenX raw LibGDX touchDown X (left = 0)
     * @param screenY raw LibGDX touchDown Y (top = 0) — converted internally
     */
    public boolean handleClick(float screenX, float screenY, int screenHeight) {
        if (!visible) return false;
        // Convert to OpenGL bottom-up Y
        float y = screenHeight - screenY;

        if (screenX >= closeX && screenX <= closeX + CLOSE_SIZE
                && y >= closeY && y <= closeY + CLOSE_SIZE) {
            hide();
            return true;
        }
        for (int i = 0; i < BOX_COUNT; i++) {
            if (screenX >= btnX[i] && screenX <= btnX[i] + BTN_W
                    && y >= btnY[i] && y <= btnY[i] + BTN_H) {
                if (isUnlocked(i) && !confirmed[i]) {
                    confirmed[i] = true;
                    phase++;
                }
                return true;
            }
        }
        // Swallow all clicks when panel is open
        return true;
    }

    public void render(int screenWidth, int screenHeight) {
        if (!visible) return;

        float px = (screenWidth  - PANEL_W) / 2f;
        float py = (screenHeight - PANEL_H) / 2f;

        float unitH = BOX_SIZE + BTN_BOX_GAP + BTN_H;
        float totalGroupH = 2 * unitH + BOX_GAP;
        float row2Y = py + (PANEL_H - totalGroupH) / 2f;
        float row1Y = row2Y + unitH + BOX_GAP;

        float row1TotalW = 3 * BOX_SIZE + 2 * BOX_GAP;
        float row1X = px + (PANEL_W - row1TotalW) / 2f;

        float row2TotalW = 2 * BOX_SIZE + BOX_GAP;
        float row2X = px + (PANEL_W - row2TotalW) / 2f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.15f, 0.15f, 0.18f, 0.93f);
        shapeRenderer.rect(px, py, PANEL_W, PANEL_H);

        for (int i = 0; i < 3; i++) {
            float bx = row1X + i * (BOX_SIZE + BOX_GAP);
            drawBox(bx, row1Y, isUnlocked(i), confirmed[i]);
            float bBtnY = row1Y - BTN_BOX_GAP - BTN_H;
            drawBtn(bx, bBtnY, isUnlocked(i), confirmed[i]);
            btnX[i] = bx;
            btnY[i] = bBtnY;
        }
        for (int i = 0; i < 2; i++) {
            float bx = row2X + i * (BOX_SIZE + BOX_GAP);
            drawBox(bx, row2Y, isUnlocked(3 + i), confirmed[3 + i]);
            float bBtnY = row2Y - BTN_BOX_GAP - BTN_H;
            drawBtn(bx, bBtnY, isUnlocked(3 + i), confirmed[3 + i]);
            btnX[3 + i] = bx;
            btnY[3 + i] = bBtnY;
        }

        closeX = px + PANEL_W - CLOSE_SIZE - 8f;
        closeY = py + PANEL_H - CLOSE_SIZE - 8f;
        shapeRenderer.setColor(0.7f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(closeX, closeY, CLOSE_SIZE, CLOSE_SIZE);

        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.5f, 0.55f, 1f);
        shapeRenderer.rect(px, py, PANEL_W, PANEL_H);
        shapeRenderer.setColor(0.5f, 0.4f, 0.25f, 1f);
        for (int i = 0; i < 3; i++) {
            float bx = row1X + i * (BOX_SIZE + BOX_GAP);
            shapeRenderer.rect(bx, row1Y, BOX_SIZE, BOX_SIZE);
        }
        for (int i = 0; i < 2; i++) {
            float bx = row2X + i * (BOX_SIZE + BOX_GAP);
            shapeRenderer.rect(bx, row2Y, BOX_SIZE, BOX_SIZE);
        }
        shapeRenderer.end();

        batch.begin();
        GlyphLayout layout = new GlyphLayout();

        String confirmLabel = "Confirm";
        layout.setText(smallFont, confirmLabel);
        for (int i = 0; i < BOX_COUNT; i++) {
            smallFont.draw(batch, confirmLabel,
                    btnX[i] + (BTN_W - layout.width) / 2f,
                    btnY[i] + (BTN_H + layout.height) / 2f);
        }

        font.getData().setScale(1.1f);
        String phaseText = "Phase: " + phase;
        layout.setText(font, phaseText);
        font.draw(batch, phaseText, px + 10f, py + PANEL_H - 10f);

        font.getData().setScale(1f);
        layout.setText(font, "X");
        font.draw(batch, "X",
                closeX + (CLOSE_SIZE - layout.width) / 2f,
                closeY + (CLOSE_SIZE + layout.height) / 2f);

        batch.end();
    }

    private void drawBox(float bx, float by, boolean unlocked, boolean done) {
        if (!unlocked) {
            shapeRenderer.setColor(0.18f, 0.18f, 0.18f, 1f);
            shapeRenderer.rect(bx, by, BOX_SIZE, BOX_SIZE);
        } else if (done) {
            shapeRenderer.setColor(0.15f, 0.35f, 0.15f, 1f);
            shapeRenderer.rect(bx, by, BOX_SIZE, BOX_SIZE);
            float ix = bx + (BOX_SIZE - BOX_INNER) / 2f;
            float iy = by + (BOX_SIZE - BOX_INNER) / 2f;
            shapeRenderer.setColor(0.3f, 0.75f, 0.3f, 1f);
            shapeRenderer.rect(ix, iy, BOX_INNER, BOX_INNER);
        } else {
            shapeRenderer.setColor(0.28f, 0.22f, 0.14f, 1f);
            shapeRenderer.rect(bx, by, BOX_SIZE, BOX_SIZE);
            float ix = bx + (BOX_SIZE - BOX_INNER) / 2f;
            float iy = by + (BOX_SIZE - BOX_INNER) / 2f;
            shapeRenderer.setColor(0.75f, 0.45f, 0.15f, 1f);
            shapeRenderer.rect(ix, iy, BOX_INNER, BOX_INNER);
        }
    }

    private void drawBtn(float bx, float by, boolean unlocked, boolean done) {
        if (!unlocked || done) {
            shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
        } else {
            shapeRenderer.setColor(0.2f, 0.55f, 0.2f, 1f);
        }
        shapeRenderer.rect(bx, by, BTN_W, BTN_H);
    }

    private boolean isUnlocked(int i) {
        if (i == 0) return true;
        return confirmed[i - 1];
    }

    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        smallFont.dispose();
    }
}