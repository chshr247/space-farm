package com.spacefarm.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.spacefarm.inventory.Inventory;

public class TreeBoxUI {

    // Phase metadata (ASCII-compatible for default BitmapFont)
    private static final String[] PHASE_NAMES  = {
            "Bio-Compost", "Living Dew", "Mycorrhiza", "Cosmos Flwr", "Eden Core"
    };
    private static final String[] PHASE_PRICES = {
            "$500", "$1000", "$2000", "$4000", "$8000"
    };

    // Return values from handleClick()
    public static final int CLICK_CLOSE = -2;   // close button
    public static final int CLICK_NONE  = -1;   // panel swallowed, no action

    private static final float PANEL_W    = 480f;
    private static final float PANEL_H    = 420f;
    private static final float BOX_SIZE   = 72f;
    private static final float BOX_INNER  = 50f;
    private static final float BOX_GAP    = 20f;
    private static final float BTN_W      = 72f;
    private static final float BTN_H      = 26f;
    private static final float BTN_BOX_GAP = 4f;
    private static final float CLOSE_SIZE = 26f;
    private static final int   BOX_COUNT  = 5;

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch   batch;
    private final BitmapFont    font;
    private final BitmapFont    smallFont;
    private final BitmapFont    tinyFont;

    private boolean visible = false;
    private int     phase   = 1;
    private float   closeX, closeY;
    private Inventory inventory; // injected from GameSession

    private final float[]   btnX      = new float[BOX_COUNT];
    private final float[]   btnY      = new float[BOX_COUNT];
    private final boolean[] confirmed = new boolean[BOX_COUNT];

    public TreeBoxUI() {
        shapeRenderer = new ShapeRenderer();
        batch         = new SpriteBatch();

        font = new BitmapFont();
        font.getData().setScale(1.1f);
        font.setColor(Color.WHITE);

        smallFont = new BitmapFont();
        smallFont.getData().setScale(0.8f);
        smallFont.setColor(Color.WHITE);

        tinyFont = new BitmapFont();
        tinyFont.getData().setScale(0.7f);
        tinyFont.setColor(new Color(0.85f, 0.85f, 0.85f, 1f));
    }

    /** Inject inventory so the UI can check item availability. */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void show()         { visible = true; }
    public void hide()         { visible = false; }
    public void toggle()       { visible = !visible; }
    public boolean isVisible() { return visible; }
    public int  getPhase()     { return phase; }

    public boolean isUnlocked(int i) {
        if (i < 0 || i >= BOX_COUNT) return false;
        if (i == 0) return true;
        return confirmed[i - 1];
    }

    public boolean isConfirmed(int i) {
        if (i < 0 || i >= BOX_COUNT) return false;
        return confirmed[i];
    }

    /**
     * Called by GameInteractionService after verifying inventory.
     * Marks phase i as confirmed and increments the phase counter.
     */
    public void confirmPhase(int i) {
        if (i >= 0 && i < BOX_COUNT && !confirmed[i]) {
            confirmed[i] = true;
            phase++;
        }
    }

    public boolean isComplete() {
        for (boolean c : confirmed) { if (!c) return false; }
        return true;
    }

    /**
     * Handles a click on the panel.
     *
     * Returns:
     *   CLICK_CLOSE (-2)  — close button was clicked
     *   CLICK_NONE  (-1)  — click consumed by panel background
     *   0..4              — confirm button index was clicked
     */
    public int handleClick(float screenX, float screenY, int screenHeight) {
        if (!visible) return CLICK_NONE;
        float y = screenHeight - screenY; // convert to OpenGL bottom-up Y

        // Close button
        if (screenX >= closeX && screenX <= closeX + CLOSE_SIZE
                && y >= closeY && y <= closeY + CLOSE_SIZE) {
            hide();
            return CLICK_CLOSE;
        }

        // Confirm buttons
        for (int i = 0; i < BOX_COUNT; i++) {
            if (screenX >= btnX[i] && screenX <= btnX[i] + BTN_W
                    && y >= btnY[i] && y <= btnY[i] + BTN_H) {
                return i; // GameInteractionService will verify and confirm
            }
        }

        return CLICK_NONE; // swallow all other clicks
    }

    public void render(int screenWidth, int screenHeight) {
        if (!visible) return;

        float px = (screenWidth  - PANEL_W) / 2f;
        float py = (screenHeight - PANEL_H) / 2f;

        float unitH      = BOX_SIZE + BTN_BOX_GAP + BTN_H;
        float totalGroupH = 2 * unitH + BOX_GAP;
        float row2Y      = py + (PANEL_H - totalGroupH) / 2f;
        float row1Y      = row2Y + unitH + BOX_GAP;

        float row1TotalW = 3 * BOX_SIZE + 2 * BOX_GAP;
        float row1X      = px + (PANEL_W - row1TotalW) / 2f;
        float row2TotalW = 2 * BOX_SIZE + BOX_GAP;
        float row2X      = px + (PANEL_W - row2TotalW) / 2f;

        // ── Background panel ──────────────────────────────────────────────
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.12f, 0.12f, 0.15f, 0.95f);
        shapeRenderer.rect(px, py, PANEL_W, PANEL_H);

        // ── Boxes + buttons ───────────────────────────────────────────────
        for (int i = 0; i < 3; i++) {
            float bx   = row1X + i * (BOX_SIZE + BOX_GAP);
            boolean hasItem = inventory != null && inventory.hasTreePhaseItem(i);
            drawBox(bx, row1Y, isUnlocked(i), confirmed[i]);
            float bBtnY = row1Y - BTN_BOX_GAP - BTN_H;
            drawBtn(bx, bBtnY, isUnlocked(i), confirmed[i], hasItem);
            btnX[i] = bx;
            btnY[i] = bBtnY;
        }
        for (int i = 0; i < 2; i++) {
            float bx   = row2X + i * (BOX_SIZE + BOX_GAP);
            boolean hasItem = inventory != null && inventory.hasTreePhaseItem(3 + i);
            drawBox(bx, row2Y, isUnlocked(3 + i), confirmed[3 + i]);
            float bBtnY = row2Y - BTN_BOX_GAP - BTN_H;
            drawBtn(bx, bBtnY, isUnlocked(3 + i), confirmed[3 + i], hasItem);
            btnX[3 + i] = bx;
            btnY[3 + i] = bBtnY;
        }

        // ── Close button ──────────────────────────────────────────────────
        closeX = px + PANEL_W - CLOSE_SIZE - 8f;
        closeY = py + PANEL_H - CLOSE_SIZE - 8f;
        shapeRenderer.setColor(0.72f, 0.18f, 0.18f, 1f);
        shapeRenderer.rect(closeX, closeY, CLOSE_SIZE, CLOSE_SIZE);

        shapeRenderer.end();

        // ── Outlines ──────────────────────────────────────────────────────
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.5f, 0.55f, 1f);
        shapeRenderer.rect(px, py, PANEL_W, PANEL_H);
        shapeRenderer.setColor(0.5f, 0.4f, 0.25f, 1f);
        for (int i = 0; i < 3; i++)
            shapeRenderer.rect(row1X + i * (BOX_SIZE + BOX_GAP), row1Y, BOX_SIZE, BOX_SIZE);
        for (int i = 0; i < 2; i++)
            shapeRenderer.rect(row2X + i * (BOX_SIZE + BOX_GAP), row2Y, BOX_SIZE, BOX_SIZE);
        shapeRenderer.end();

        // ── Text ──────────────────────────────────────────────────────────
        batch.begin();
        GlyphLayout layout = new GlyphLayout();

        // Title
        font.getData().setScale(1.1f);
        String title = "Magic Tree  (Phase: " + phase + ")";
        layout.setText(font, title);
        font.draw(batch, title, px + 10f, py + PANEL_H - 10f);

        // "X" on close
        font.getData().setScale(1f);
        layout.setText(font, "X");
        font.draw(batch, "X",
                closeX + (CLOSE_SIZE - layout.width) / 2f,
                closeY + (CLOSE_SIZE + layout.height) / 2f);

        // Per-box: name, price, inventory indicator, button label
        for (int i = 0; i < BOX_COUNT; i++) {
            float bx = btnX[i];
            float by = btnY[i];
            float boxTopY = by + BTN_H + BTN_BOX_GAP + BOX_SIZE;

            // Phase name inside box
            tinyFont.getData().setScale(0.68f);
            tinyFont.setColor(confirmed[i]
                    ? new Color(0.5f, 1f, 0.5f, 1f)
                    : Color.WHITE);
            String name = PHASE_NAMES[i];
            layout.setText(tinyFont, name);
            tinyFont.draw(batch, name,
                    bx + (BOX_SIZE - layout.width) / 2f,
                    boxTopY - 6f);

            // Price inside box
            tinyFont.getData().setScale(0.65f);
            tinyFont.setColor(new Color(1f, 0.85f, 0.4f, 1f));
            layout.setText(tinyFont, PHASE_PRICES[i]);
            tinyFont.draw(batch, PHASE_PRICES[i],
                    bx + (BOX_SIZE - layout.width) / 2f,
                    boxTopY - 6f - layout.height - 4f);

            // Inventory status
            if (isUnlocked(i) && !confirmed[i]) {
                boolean has = inventory != null && inventory.hasTreePhaseItem(i);
                tinyFont.getData().setScale(0.65f);
                tinyFont.setColor(has
                        ? new Color(0.3f, 1f, 0.3f, 1f)
                        : new Color(1f, 0.4f, 0.4f, 1f));
                String status = has ? "[Have it]" : "[Missing]";
                layout.setText(tinyFont, status);
                tinyFont.draw(batch, status,
                        bx + (BOX_SIZE - layout.width) / 2f,
                        by + BTN_H + BTN_BOX_GAP + 14f);
            }

            // Button label
            smallFont.getData().setScale(0.8f);
            smallFont.setColor(Color.WHITE);
            String btnLabel = confirmed[i] ? "Done" : "Confirm";
            layout.setText(smallFont, btnLabel);
            smallFont.draw(batch, btnLabel,
                    bx + (BTN_W - layout.width) / 2f,
                    by + (BTN_H + layout.height) / 2f);
        }

        batch.end();
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private void drawBox(float bx, float by, boolean unlocked, boolean done) {
        if (!unlocked) {
            shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 1f);
            shapeRenderer.rect(bx, by, BOX_SIZE, BOX_SIZE);
        } else if (done) {
            shapeRenderer.setColor(0.12f, 0.32f, 0.12f, 1f);
            shapeRenderer.rect(bx, by, BOX_SIZE, BOX_SIZE);
            float ix = bx + (BOX_SIZE - BOX_INNER) / 2f;
            float iy = by + (BOX_SIZE - BOX_INNER) / 2f;
            shapeRenderer.setColor(0.25f, 0.72f, 0.25f, 1f);
            shapeRenderer.rect(ix, iy, BOX_INNER, BOX_INNER);
        } else {
            shapeRenderer.setColor(0.26f, 0.20f, 0.12f, 1f);
            shapeRenderer.rect(bx, by, BOX_SIZE, BOX_SIZE);
            float ix = bx + (BOX_SIZE - BOX_INNER) / 2f;
            float iy = by + (BOX_SIZE - BOX_INNER) / 2f;
            shapeRenderer.setColor(0.72f, 0.42f, 0.12f, 1f);
            shapeRenderer.rect(ix, iy, BOX_INNER, BOX_INNER);
        }
    }

    private void drawBtn(float bx, float by, boolean unlocked, boolean done, boolean hasItem) {
        if (!unlocked || done) {
            shapeRenderer.setColor(0.28f, 0.28f, 0.28f, 1f);
        } else if (hasItem) {
            shapeRenderer.setColor(0.15f, 0.55f, 0.15f, 1f); // green — can confirm
        } else {
            shapeRenderer.setColor(0.50f, 0.25f, 0.10f, 1f); // orange-red — missing item
        }
        shapeRenderer.rect(bx, by, BTN_W, BTN_H);
    }

    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        smallFont.dispose();
        tinyFont.dispose();
    }
}