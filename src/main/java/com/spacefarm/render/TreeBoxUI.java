package com.spacefarm.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.spacefarm.inventory.Inventory;

/**
 * Redesigned Tree progress panel.
 *
 * Style matches SeedWheelOverlay: dark space theme, cyan accents,
 * panel borders with corner squares.
 *
 * Layout: central modal, 5 phase rows stacked vertically.
 * Each row: phase dot → item icon → name → status badge → confirm button.
 */
public class TreeBoxUI {

    // ── Phase data ────────────────────────────────────────────────────────────
    private static final String[] PHASE_NAMES = {
            "Біо-Компост",
            "Жива Роса",
            "Мережа Мікориз",
            "Квітка Всесвіту",
            "Ядро Едему"
    };
    private static final String[] PHASE_ITEMS = {
            "Біо-Компост",
            "Жива Роса",
            "Мережа Мікориз",
            "Квітка Всесвіту",
            "Ядро Едему"
    };
    private static final String[] PHASE_DESC = {
            "Відновлює ґрунт для першого цвіту",
            "Зволожує коріння магічного дерева",
            "З'єднує дерево з мережею планети",
            "Пробуджує космічну свідомість дерева",
            "Активує фінальне перетворення Едему"
    };

    public static final int CLICK_CLOSE = -2;
    public static final int CLICK_NONE  = -1;
    private static final int BOX_COUNT  = 5;

    // ── Panel geometry ────────────────────────────────────────────────────────
    private static final float PANEL_W   = 720f;
    private static final float PANEL_H   = 580f;
    private static final float ROW_H     = 82f;
    private static final float ROW_GAP   = 8f;
    private static final float BTN_W     = 136f;
    private static final float BTN_H     = 38f;

    // ── Accent colours ────────────────────────────────────────────────────────
    private static final float AC_R = 0.10f, AC_G = 0.88f, AC_B = 1.00f; // cyan
    private static final float GR_R = 0.15f, GR_G = 0.90f, GR_B = 0.40f; // green

    // ── Rendering ─────────────────────────────────────────────────────────────
    private final ShapeRenderer sr;
    private final SpriteBatch   batch;
    private final BitmapFont    titleFont;
    private final BitmapFont    bodyFont;
    private final BitmapFont    smallFont;
    private final BitmapFont    hintFont;
    private final Texture[]     itemTextures;
    private final GlyphLayout   gl = new GlyphLayout();

    // ── State ─────────────────────────────────────────────────────────────────
    private boolean   visible  = false;
    private int       phase    = 1;
    private Inventory inventory;

    private final boolean[] confirmed = new boolean[BOX_COUNT];
    private final float[]   flashTimer   = new float[BOX_COUNT]; // completion flash

    // Hit-boxes (set during render)
    private final float[] btnX = new float[BOX_COUNT];
    private final float[] btnY = new float[BOX_COUNT];
    private float closeX, closeY, closeSz;

    // ── Constructor ───────────────────────────────────────────────────────────

    public TreeBoxUI() {
        sr    = new ShapeRenderer();
        batch = new SpriteBatch();

        itemTextures = new Texture[BOX_COUNT];
        for (int i = 0; i < BOX_COUNT; i++) {
            itemTextures[i] = new Texture("sprite/tree-item/item-" + (i + 1) + ".png");
        }
        titleFont = FontUtils.createFont("fonts/ArialBold.ttf", 36);
        bodyFont  = FontUtils.createFont("fonts/ArialBold.ttf", 18);
        smallFont = FontUtils.createFont("fonts/ArialBold.ttf", 16);
        hintFont  = FontUtils.createFont("fonts/ArialBold.ttf", 13);
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    public void show()         { visible = true; }
    public void hide()         { visible = false; }
    public void toggle()       { visible = !visible; }
    public boolean isVisible() { return visible; }
    public int  getPhase()     { return phase; }

    public boolean isUnlocked(int i) {
        if (i < 0 || i >= BOX_COUNT) return false;
        return i == 0 || confirmed[i - 1];
    }

    public boolean isConfirmed(int i) {
        if (i < 0 || i >= BOX_COUNT) return false;
        return confirmed[i];
    }

    public void confirmPhase(int i) {
        if (i >= 0 && i < BOX_COUNT && !confirmed[i] && isUnlocked(i)) {
            confirmed[i]  = true;
            flashTimer[i] = 0.4f; // flash animation
            phase++;
        }
    }

    public boolean isComplete() {
        for (boolean c : confirmed) { if (!c) return false; }
        return true;
    }

    /** Call once per frame to advance flash animations. */
    public void update(float delta) {
        for (int i = 0; i < BOX_COUNT; i++) {
            if (flashTimer[i] > 0f) flashTimer[i] = Math.max(0f, flashTimer[i] - delta);
        }
    }

    /** Returns CLICK_CLOSE, CLICK_NONE, or 0..4 (confirm button index). */
    public int handleClick(float screenX, float screenY, int screenHeight) {
        if (!visible) return CLICK_NONE;
        float y = screenHeight - screenY;

        // Close button
        if (screenX >= closeX && screenX <= closeX + closeSz
                && y >= closeY && y <= closeY + closeSz) {
            hide();
            return CLICK_CLOSE;
        }

        // Confirm buttons
        for (int i = 0; i < BOX_COUNT; i++) {
            if (screenX >= btnX[i] && screenX <= btnX[i] + BTN_W
                    && y >= btnY[i] && y <= btnY[i] + BTN_H) {
                return i;
            }
        }

        return CLICK_NONE;
    }

    // ── Render ─────────────────────────────────────────────────────────────────

    public void render(int sw, int sh) {
        if (!visible) return;

        // Dim background
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.65f);
        sr.rect(0, 0, sw, sh);
        sr.end();

        float px = (sw - PANEL_W) / 2f;
        float py = (sh - PANEL_H) / 2f;

        drawPanel(px, py);
        drawHeader(px, py, sw);
        drawProgressBar(px, py);
        drawPhaseRows(px, py);
    }

    // ── Panel chrome ──────────────────────────────────────────────────────────

    private void drawPanel(float px, float py) {
        float border = 4f;
        float cs     = Math.min(PANEL_W, PANEL_H) * 0.028f;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        // Background
        sr.setColor(0.02f, 0.05f, 0.11f, 0.97f);
        sr.rect(px, py, PANEL_W, PANEL_H);
        // Top / bottom accent bars
        sr.setColor(AC_R, AC_G, AC_B, 1f);
        sr.rect(px, py + PANEL_H - border, PANEL_W, border);
        sr.rect(px, py,                    PANEL_W, border);
        // Side bars (dimmer)
        sr.setColor(AC_R * 0.65f, AC_G * 0.65f, AC_B * 0.65f, 1f);
        sr.rect(px,                py, border, PANEL_H);
        sr.rect(px + PANEL_W - border, py, border, PANEL_H);
        // Corner squares
        sr.setColor(AC_R, AC_G, AC_B, 1f);
        sr.rect(px - 2f,               py + PANEL_H - cs, cs, cs);
        sr.rect(px + PANEL_W - cs + 2f, py + PANEL_H - cs, cs, cs);
        sr.rect(px - 2f,               py,                 cs, cs);
        sr.rect(px + PANEL_W - cs + 2f, py,                 cs, cs);
        sr.end();

        // Outer border line
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(AC_R, AC_G, AC_B, 0.75f);
        sr.rect(px, py, PANEL_W, PANEL_H);
        sr.setColor(AC_R * 0.45f, AC_G * 0.45f, AC_B * 0.45f, 0.40f);
        sr.rect(px + 10f, py + 10f, PANEL_W - 20f, PANEL_H - 20f);
        sr.end();
    }

    private void drawHeader(float px, float py, int sw) {
        // Title
        batch.begin();
        titleFont.setColor(AC_R, AC_G, AC_B, 1f);
        String title = "МАГІЧНЕ ДЕРЕВО";
        gl.setText(titleFont, title);
        titleFont.draw(batch, title,
                sw / 2f - gl.width / 2f,
                py + PANEL_H - 16f);
        batch.end();

        // Divider under title
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(AC_R, AC_G, AC_B, 0.55f);
        sr.rect(px + PANEL_W * 0.1f, py + PANEL_H - 56f, PANEL_W * 0.8f, 2f);
        sr.end();

        // Close button
        closeSz = 30f;
        closeX  = px + PANEL_W - closeSz - 12f;
        closeY  = py + PANEL_H - closeSz - 12f;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.50f, 0.08f, 0.08f, 0.90f);
        sr.rect(closeX, closeY, closeSz, closeSz);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(1f, 0.35f, 0.35f, 1f);
        sr.rect(closeX, closeY, closeSz, closeSz);
        sr.end();

        batch.begin();
        bodyFont.setColor(1f, 0.35f, 0.35f, 1f);
        gl.setText(bodyFont, "✕");
        bodyFont.draw(batch, "X",
                closeX + (closeSz - gl.width) / 2f,
                closeY + (closeSz + gl.height) / 2f);
        batch.end();

        // Phase counter top-right of title
        batch.begin();
        smallFont.setColor(GR_R, GR_G, GR_B, 1f);
        String phaseStr = "Фаза " + Math.min(5, phase) + " / 5";
        gl.setText(smallFont, phaseStr);
        smallFont.draw(batch, phaseStr,
                px + 18f,
                py + PANEL_H - 20f);
        batch.end();
    }

    private void drawProgressBar(float px, float py) {
        float barY  = py + PANEL_H - 62f;
        float barX  = px + PANEL_W * 0.08f;
        float barW  = PANEL_W * 0.84f;
        float barH  = 6f;

        int done = 0;
        for (boolean c : confirmed) { if (c) done++; }
        float fill = barW * ((float) done / BOX_COUNT);

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.06f, 0.10f, 0.16f, 1f);
        sr.rect(barX, barY, barW, barH);
        if (fill > 0f) {
            sr.setColor(GR_R, GR_G, GR_B, 1f);
            sr.rect(barX, barY, fill, barH);
        }
        sr.end();

        // Tick marks
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(AC_R * 0.5f, AC_G * 0.5f, AC_B * 0.5f, 0.6f);
        for (int i = 1; i < BOX_COUNT; i++) {
            float tx = barX + barW * i / BOX_COUNT;
            sr.line(tx, barY - 2f, tx, barY + barH + 2f);
        }
        sr.end();
    }

    private void drawPhaseRows(float px, float py) {
        float startY = py + PANEL_H - 80f - ROW_H;

        for (int i = 0; i < BOX_COUNT; i++) {
            float rowY   = startY - i * (ROW_H + ROW_GAP);
            boolean unlocked  = isUnlocked(i);
            boolean done      = confirmed[i];
            boolean hasItem   = inventory != null && inventory.hasTreePhaseItem(i);
            float   flash     = flashTimer[i];

            drawPhaseRow(px, rowY, i, unlocked, done, hasItem, flash);
        }
    }

    private void drawPhaseRow(float px, float rowY, int i,
                              boolean unlocked, boolean done,
                              boolean hasItem, float flash) {
        float rowX  = px + 16f;
        float rowW  = PANEL_W - 32f;

        // ── Row background ──
        sr.begin(ShapeRenderer.ShapeType.Filled);
        if (done) {
            sr.setColor(0.03f, 0.12f, 0.05f, 0.90f);
        } else if (!unlocked) {
            sr.setColor(0.04f, 0.04f, 0.06f, 0.80f);
        } else {
            sr.setColor(0.04f, 0.08f, 0.14f, 0.90f);
        }
        sr.rect(rowX, rowY, rowW, ROW_H);
        sr.end();

        // ── Flash overlay ──
        if (flash > 0f) {
            float alpha = Math.min(1f, flash) * 0.35f;
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(GR_R, GR_G, GR_B, alpha);
            sr.rect(rowX, rowY, rowW, ROW_H);
            sr.end();
        }

        // ── Row border ──
        sr.begin(ShapeRenderer.ShapeType.Line);
        if (done) {
            sr.setColor(GR_R, GR_G, GR_B, 0.70f);
        } else if (unlocked && hasItem) {
            sr.setColor(AC_R, AC_G, AC_B, 0.55f);
        } else if (!unlocked) {
            sr.setColor(0.20f, 0.20f, 0.25f, 0.50f);
        } else {
            sr.setColor(0.70f, 0.25f, 0.10f, 0.55f);
        }
        sr.rect(rowX, rowY, rowW, ROW_H);
        sr.end();

        // ── Phase number dot ──
        float dotX = rowX + 22f;
        float dotY = rowY + ROW_H / 2f;
        float dotR = 16f;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        if (done)       sr.setColor(GR_R * 0.4f, GR_G * 0.4f, GR_B * 0.4f, 1f);
        else if (unlocked) sr.setColor(AC_R * 0.2f, AC_G * 0.2f, AC_B * 0.2f, 1f);
        else               sr.setColor(0.08f, 0.08f, 0.10f, 1f);
        fillCircle(dotX, dotY, dotR, 24);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        if (done)       sr.setColor(GR_R, GR_G, GR_B, 1f);
        else if (unlocked) sr.setColor(AC_R, AC_G, AC_B, 0.80f);
        else               sr.setColor(0.25f, 0.25f, 0.30f, 0.60f);
        drawCircleLines(dotX, dotY, dotR, 24);
        sr.end();

        batch.begin();
        bodyFont.getData().setScale(1.1f);
        if (done)        bodyFont.setColor(GR_R, GR_G, GR_B, 1f);
        else if (unlocked) bodyFont.setColor(AC_R, AC_G, AC_B, 1f);
        else               bodyFont.setColor(0.35f, 0.35f, 0.40f, 1f);
        gl.setText(bodyFont, String.valueOf(i + 1));
        bodyFont.draw(batch, String.valueOf(i + 1), dotX - gl.width / 2f, dotY + gl.height / 2f);
        bodyFont.getData().setScale(1f);
        batch.end();

        // ── Item Icon ──
        float iconSz = ROW_H - 24f;
        float iconX  = dotX + dotR + 15f;
        float iconY  = rowY + (ROW_H - iconSz) / 2f;
        
        batch.begin();
        if (!unlocked) batch.setColor(1, 1, 1, 0.25f);
        else if (done) batch.setColor(1, 1, 1, 0.85f);
        else           batch.setColor(1, 1, 1, 1.0f);
        batch.draw(itemTextures[i], iconX, iconY, iconSz, iconSz);
        batch.setColor(1, 1, 1, 1);
        batch.end();

        // ── Texts ──
        float textX = iconX + iconSz + 18f;
        batch.begin();
        bodyFont.getData().setScale(1.05f);
        if (done)         bodyFont.setColor(GR_R * 0.8f + 0.2f, GR_G * 0.8f + 0.2f, GR_B * 0.8f + 0.2f, 1f);
        else if (unlocked) bodyFont.setColor(0.90f, 0.90f, 0.95f, 1f);
        else               bodyFont.setColor(0.35f, 0.35f, 0.40f, 1f);
        bodyFont.draw(batch, PHASE_NAMES[i], textX, rowY + ROW_H - 12f);
        bodyFont.getData().setScale(1f);

        hintFont.setColor(0.40f, 0.55f, 0.60f, 1f);
        hintFont.draw(batch, PHASE_DESC[i], textX, rowY + ROW_H - 34f);
        
        if (done) hintFont.setColor(GR_R * 0.6f, GR_G * 0.6f, GR_B * 0.6f, 1f);
        else if (!unlocked) hintFont.setColor(0.25f, 0.25f, 0.30f, 1f);
        else if (hasItem)   hintFont.setColor(GR_R, GR_G, GR_B, 0.90f);
        else                hintFont.setColor(0.90f, 0.35f, 0.20f, 1f);
        hintFont.draw(batch, "Потрібно: " + PHASE_ITEMS[i], textX, rowY + 18f);
        batch.end();

        // ── Status badge ──
        float badgeX = rowX + rowW - BTN_W - 110f;
        float badgeCY = rowY + ROW_H / 2f;
        if (done) {
            drawBadge(badgeX, badgeCY - 13f, 90f, 26f, 0.04f, 0.16f, 0.07f, GR_R, GR_G, GR_B, "✓ ГОТОВО");
        } else if (!unlocked) {
            drawBadge(badgeX, badgeCY - 13f, 90f, 26f, 0.08f, 0.08f, 0.10f, 0.30f, 0.30f, 0.35f, "ЗАБЛОКОВАНО");
        } else if (hasItem) {
            drawBadge(badgeX, badgeCY - 13f, 90f, 26f, 0.04f, 0.14f, 0.06f, GR_R * 0.8f, GR_G * 0.8f, GR_B * 0.8f, "В НАЯВНОСТІ");
        } else {
            drawBadge(badgeX, badgeCY - 13f, 90f, 26f, 0.14f, 0.06f, 0.03f, 0.85f, 0.30f, 0.10f, "ВІДСУТНІЙ");
        }

        // ── Confirm button ──
        float bx = rowX + rowW - BTN_W - 8f;
        float by = rowY + (ROW_H - BTN_H) / 2f;
        btnX[i] = bx;
        btnY[i] = by;

        if (done) {
            drawRowButton(bx, by, 0.05f, 0.13f, 0.06f, GR_R * 0.5f, GR_G * 0.5f, GR_B * 0.5f, "ГОТОВО");
        } else if (!unlocked || !hasItem) {
            drawRowButton(bx, by, 0.06f, 0.06f, 0.08f, 0.25f, 0.25f, 0.30f, "ПІДТВЕРДИТИ");
        } else {
            drawRowButton(bx, by, 0.05f, 0.22f, 0.27f, AC_R, AC_G, AC_B, "ПІДТВЕРДИТИ");
        }
    }

    private void drawBadge(float x, float y, float w, float h,
                            float bgR, float bgG, float bgB,
                            float fgR, float fgG, float fgB,
                            String text) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(bgR, bgG, bgB, 0.90f);
        sr.rect(x, y, w, h);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(fgR, fgG, fgB, 0.70f);
        sr.rect(x, y, w, h);
        sr.end();

        batch.begin();
        hintFont.getData().setScale(0.85f);
        hintFont.setColor(fgR, fgG, fgB, 1f);
        gl.setText(hintFont, text);
        float sx = Math.min(1f, (w - 6f) / gl.width);
        hintFont.getData().setScale(0.85f * sx, 0.85f);
        gl.setText(hintFont, text);
        hintFont.draw(batch, text, x + (w - gl.width) / 2f, y + (h + gl.height) / 2f);
        hintFont.getData().setScale(1f, 1f);
        batch.end();
    }

    private void drawRowButton(float bx, float by,
                               float bgR, float bgG, float bgB,
                               float fgR, float fgG, float fgB,
                               String label) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(bgR, bgG, bgB, 0.92f);
        sr.rect(bx, by, BTN_W, BTN_H);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(fgR, fgG, fgB, 0.85f);
        sr.rect(bx, by, BTN_W, BTN_H);
        sr.end();

        batch.begin();
        smallFont.setColor(fgR, fgG, fgB, 1f);
        gl.setText(smallFont, label);
        float sx = Math.min(1f, (BTN_W - 10f) / gl.width);
        smallFont.getData().setScale(sx, 1f);
        gl.setText(smallFont, label);
        smallFont.draw(batch, label, bx + (BTN_W - gl.width) / 2f, by + (BTN_H + gl.height) / 2f);
        smallFont.getData().setScale(1f, 1f);
        batch.end();
    }

    private void fillCircle(float cx, float cy, float r, int segs) {
        for (int i = 0; i < segs; i++) {
            double a1 = 2 * Math.PI *  i      / segs;
            double a2 = 2 * Math.PI * (i + 1) / segs;
            sr.triangle(cx, cy,
                    cx + r * (float)Math.cos(a1), cy + r * (float)Math.sin(a1),
                    cx + r * (float)Math.cos(a2), cy + r * (float)Math.sin(a2));
        }
    }

    private void drawCircleLines(float cx, float cy, float r, int segs) {
        for (int i = 0; i < segs; i++) {
            double a1 = 2 * Math.PI *  i      / segs;
            double a2 = 2 * Math.PI * (i + 1) / segs;
            sr.line(cx + r * (float)Math.cos(a1), cy + r * (float)Math.sin(a1),
                    cx + r * (float)Math.cos(a2), cy + r * (float)Math.sin(a2));
        }
    }

    public void dispose() {
        sr.dispose();
        batch.dispose();
        titleFont.dispose();
        bodyFont.dispose();
        smallFont.dispose();
        hintFont.dispose();
        for (Texture t : itemTextures) if (t != null) t.dispose();
    }
}
