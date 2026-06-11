package com.spacefarm.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

/**
 * Two-screen main menu — all sizes are proportional to the actual screen.
 *
 *   Screen 1 — MAIN:       Нова гра | Продовжити | Вийти
 *   Screen 2 — DIFFICULTY: Легкий   | Нормальний | Важкий  + Назад
 *
 * Call handleInput() once per frame BEFORE render().
 */
public class MainMenuOverlay {

    public enum Action { NONE, NEW_EASY, NEW_NORMAL, NEW_HARD, CONTINUE, QUIT }
    private enum Screen { MAIN, DIFFICULTY }

    private Screen  screen   = Screen.MAIN;
    private final boolean hasSave;
    private boolean prevTouch = false;

    private final Rectangle btnNewGame  = new Rectangle();
    private final Rectangle btnContinue = new Rectangle();
    private final Rectangle btnQuit     = new Rectangle();
    private final Rectangle btnEasy     = new Rectangle();
    private final Rectangle btnNormal   = new Rectangle();
    private final Rectangle btnHard     = new Rectangle();
    private final Rectangle btnBack     = new Rectangle();

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch   batch;
    private final BitmapFont    titleFont;
    private final BitmapFont    subFont;
    private final BitmapFont    btnFont;
    private final BitmapFont    descFont;
    private final BitmapFont    hintFont;
    private final BitmapFont    backFont;


    public MainMenuOverlay(boolean hasSave) {
        this.hasSave  = hasSave;
        shapeRenderer = new ShapeRenderer();
        batch         = new SpriteBatch();
        titleFont = FontUtils.createFont("fonts/ArialBold.ttf", 54);
        subFont   = FontUtils.createFont("fonts/ArialBold.ttf", 20);
        btnFont   = FontUtils.createFont("fonts/ArialBold.ttf", 24);
        descFont  = FontUtils.createFont("fonts/ArialBold.ttf", 15);
        hintFont  = FontUtils.createFont("fonts/ArialBold.ttf", 14);
        backFont  = FontUtils.createFont("fonts/ArialBold.ttf", 18);
    }

    // Input

    public Action handleInput() {
        boolean touched     = Gdx.input.isTouched();
        boolean justClicked = touched && !prevTouch;
        prevTouch = touched;
        if (!justClicked) return Action.NONE;

        int mx = Gdx.input.getX();
        int my = Gdx.graphics.getHeight() - Gdx.input.getY();

        if (screen == Screen.MAIN) {
            if (btnNewGame.contains(mx, my))             { screen = Screen.DIFFICULTY; return Action.NONE; }
            if (hasSave && btnContinue.contains(mx, my)) return Action.CONTINUE;
            if (btnQuit.contains(mx, my))                return Action.QUIT;
        } else {
            if (btnEasy.contains(mx, my))   return Action.NEW_EASY;
            if (btnNormal.contains(mx, my)) return Action.NEW_NORMAL;
            if (btnHard.contains(mx, my))   return Action.NEW_HARD;
            if (btnBack.contains(mx, my))   { screen = Screen.MAIN; return Action.NONE; }
        }
        return Action.NONE;
    }

    //Render

    public void render(int sw, int sh) {
        if (screen == Screen.MAIN) renderMain(sw, sh);
        else                       renderDifficulty(sw, sh);
    }

    // MAIN SCREEN
    private void renderMain(int sw, int sh) {
        // Panel
        float panelW = sw * 0.50f;
        float panelH = sh * 0.68f;
        float cx = sw * 0.5f;
        float cy = sh * 0.5f;
        float px = cx - panelW * 0.5f;
        float py = cy - panelH * 0.5f;

        drawPanel(sw, sh, px, py, panelW, panelH,
                0.05f, 0.10f, 0.18f,
                0.10f, 0.55f, 0.65f,
                0.07f, 0.38f, 0.48f);

        GlyphLayout layout = new GlyphLayout();

        // Title
        float titleY = py + panelH - panelH * 0.08f;
        batch.begin();
        titleFont.setColor(0.20f, 0.88f, 1.00f, 1f);
        layout.setText(titleFont, "SPACE FARM");
        titleFont.draw(batch, "SPACE FARM", cx - layout.width * 0.5f, titleY);

        // Subtitle
        float subY = py + panelH - panelH * 0.20f;
        subFont.setColor(0.35f, 0.70f, 0.80f, 1f);
        String sub = "Вирощуй. Виживай. Відновлюй атмосферу.";
        layout.setText(subFont, sub);
        subFont.draw(batch, sub, cx - layout.width * 0.5f, subY);
        batch.end();

        // Divider
        float divY = py + panelH - panelH * 0.28f;
        drawHRule(cx, divY, panelW * 0.72f, 0.10f, 0.45f, 0.55f);

        // Buttons
        float btnW   = panelW * 0.58f;
        float btnH   = panelH * 0.10f;
        float btnGap = panelH * 0.03f;

        int    count      = hasSave ? 3 : 2;
        float  totalH     = count * btnH + (count - 1) * btnGap;
        float  areaCy     = py + panelH * 0.42f;
        float  stackBot   = areaCy - totalH * 0.5f;

        if (hasSave) {
            btnNewGame.set( cx - btnW * 0.5f, stackBot + 2 * (btnH + btnGap), btnW, btnH);
            btnContinue.set(cx - btnW * 0.5f, stackBot +     (btnH + btnGap), btnW, btnH);
            btnQuit.set(    cx - btnW * 0.5f, stackBot,                        btnW, btnH);
        } else {
            btnNewGame.set(cx - btnW * 0.5f, stackBot + (btnH + btnGap), btnW, btnH);
            btnQuit.set(   cx - btnW * 0.5f, stackBot,                    btnW, btnH);
        }

        drawButton(btnNewGame,  "НОВА ГРА",   0.10f,0.45f,0.55f, 0.18f,0.80f,0.95f, btnFont);
        if (hasSave)
            drawButton(btnContinue, "ПРОДОВЖИТИ", 0.07f,0.32f,0.40f, 0.14f,0.60f,0.72f, btnFont);
        drawButton(btnQuit, "ВИЙТИ",       0.22f,0.06f,0.06f, 0.70f,0.18f,0.18f, btnFont);

        // Footer — 3% from bottom
        float footerY = py + panelH * 0.04f;
        drawCentredText(hintFont, "© Space Farm  —  виживання починається тут",
                cx, footerY, 0.22f, 0.40f, 0.48f);
    }

    // DIFFICULTY SCREEN

    private void renderDifficulty(int sw, int sh) {
        float panelW = sw * 0.50f;
        float panelH = sh * 0.78f;
        float cx = sw * 0.5f;
        float cy = sh * 0.5f;
        float px = cx - panelW * 0.5f;
        float py = cy - panelH * 0.5f;

        drawPanel(sw, sh, px, py, panelW, panelH,
                0.06f, 0.08f, 0.16f,
                0.55f, 0.38f, 0.10f,
                0.42f, 0.28f, 0.07f);

        GlyphLayout layout = new GlyphLayout();

        // Title
        float titleY = py + panelH - panelH * 0.07f;
        batch.begin();
        titleFont.setColor(1.00f, 0.72f, 0.20f, 1f);
        layout.setText(titleFont, "РІВЕНЬ СКЛАДНОСТІ");
        float scaleX = Math.min(1f, (panelW * 0.85f) / layout.width);
        titleFont.getData().setScale(scaleX, 1f);
        layout.setText(titleFont, "РІВЕНЬ СКЛАДНОСТІ");
        titleFont.draw(batch, "РІВЕНЬ СКЛАДНОСТІ", cx - layout.width * 0.5f, titleY);
        titleFont.getData().setScale(1f, 1f);
        batch.end();

        // Divider
        float divY = py + panelH - panelH * 0.18f;
        drawHRule(cx, divY, panelW * 0.80f, 0.55f, 0.38f, 0.10f);

        // Three difficulty buttons
        float btnW      = panelW * 0.62f;
        float btnH      = panelH * 0.11f;
        float descH     = panelH * 0.06f;
        float slotH     = btnH + descH;
        float slotGap   = panelH * 0.015f;
        float totalH    = 3 * slotH + 2 * slotGap;
        float areaCy    = py + panelH * 0.44f;
        float stackBot  = areaCy - totalH * 0.5f;

        // Hard (top), Normal (middle), Easy (bottom)
        float hardSlotY   = stackBot + 2 * (slotH + slotGap);
        float normalSlotY = stackBot +     (slotH + slotGap);
        float easySlotY   = stackBot;

        btnHard.set(  cx - btnW * 0.5f, hardSlotY   + descH, btnW, btnH);
        btnNormal.set(cx - btnW * 0.5f, normalSlotY + descH, btnW, btnH);
        btnEasy.set(  cx - btnW * 0.5f, easySlotY   + descH, btnW, btnH);

        drawButton(btnHard,   "ВАЖКИЙ",      0.22f,0.06f,0.06f, 0.90f,0.20f,0.20f, btnFont);
        drawButton(btnNormal, "НОРМАЛЬНИЙ",  0.06f,0.20f,0.28f, 0.18f,0.80f,0.95f, btnFont);
        drawButton(btnEasy,   "ЛЕГКИЙ",      0.06f,0.22f,0.08f, 0.25f,0.85f,0.30f, btnFont);

        float descPad = panelH * 0.005f;
        drawCentredText(descFont, "Кисень -6%   •   2 грядки   •   100₿",
                cx, btnHard.y   - descPad, 0.70f, 0.16f, 0.16f);
        drawCentredText(descFont, "Кисень -4%   •   5 грядок   •   1000₿",
                cx, btnNormal.y - descPad, 0.14f, 0.60f, 0.72f);
        drawCentredText(descFont, "Кисень -2%   •   10 грядок  •   1500₿",
                cx, btnEasy.y   - descPad, 0.20f, 0.65f, 0.22f);

        // Back button — bottom-left corner of panel
        float backW = panelW * 0.22f;
        float backH = panelH * 0.065f;
        btnBack.set(px + panelW * 0.03f, py + panelH * 0.03f, backW, backH);
        drawButton(btnBack, "← НАЗАД", 0.08f,0.08f,0.14f, 0.40f,0.40f,0.60f, backFont);
    }

    // Drawing helpers

    private void drawPanel(int sw, int sh,
                           float px, float py, float pw, float ph,
                           float bgR, float bgG, float bgB,
                           float acR, float acG, float acB,
                           float sideR, float sideG, float sideB) {
        float cs = Math.min(pw, ph) * 0.028f; // corner square size
        float border = 4f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(bgR, bgG, bgB, 0.97f);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.setColor(acR, acG, acB, 1f);
        shapeRenderer.rect(px, py + ph - border, pw, border);
        shapeRenderer.rect(px, py,               pw, border);
        shapeRenderer.setColor(sideR, sideG, sideB, 1f);
        shapeRenderer.rect(px,           py, border, ph);
        shapeRenderer.rect(px + pw - border, py, border, ph);
        shapeRenderer.setColor(acR, acG, acB, 1f);
        shapeRenderer.rect(px - 2f,           py + ph - cs, cs, cs);
        shapeRenderer.rect(px + pw - cs + 2f, py + ph - cs, cs, cs);
        shapeRenderer.rect(px - 2f,           py,           cs, cs);
        shapeRenderer.rect(px + pw - cs + 2f, py,           cs, cs);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(acR, acG, acB, 0.80f);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.setColor(sideR, sideG, sideB, 0.55f);
        shapeRenderer.rect(px + 10f, py + 10f, pw - 20f, ph - 20f);
        shapeRenderer.end();
    }

    private void drawButton(Rectangle r, String label,
                            float bgR, float bgG, float bgB,
                            float fgR, float fgG, float fgB,
                            BitmapFont font) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(bgR, bgG, bgB, 0.92f);
        shapeRenderer.rect(r.x, r.y, r.width, r.height);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(fgR, fgG, fgB, 0.85f);
        shapeRenderer.rect(r.x, r.y, r.width, r.height);
        shapeRenderer.end();

        GlyphLayout layout = new GlyphLayout();
        batch.begin();
        font.setColor(fgR, fgG, fgB, 1f);
        layout.setText(font, label);
        font.draw(batch, label,
                r.x + (r.width  - layout.width)  * 0.5f,
                r.y + (r.height + layout.height) * 0.5f);
        batch.end();
    }

    private void drawHRule(float cx, float y, float w,
                           float r, float g, float b) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(r, g, b, 1f);
        shapeRenderer.rect(cx - w * 0.5f, y, w, 2f);
        shapeRenderer.end();
    }

    /** Draw text centred on cx, at y (baseline). */
    private void drawCentredText(BitmapFont font, String text,
                                 float cx, float y,
                                 float r, float g, float b) {
        GlyphLayout layout = new GlyphLayout();
        batch.begin();
        font.setColor(r, g, b, 1f);
        layout.setText(font, text);
        font.draw(batch, text, cx - layout.width * 0.5f, y);
        batch.end();
    }

    //Dispose

    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        titleFont.dispose();
        subFont.dispose();
        btnFont.dispose();
        descFont.dispose();
        hintFont.dispose();
        backFont.dispose();
    }
}