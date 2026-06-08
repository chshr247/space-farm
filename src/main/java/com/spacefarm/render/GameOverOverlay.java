package com.spacefarm.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class GameOverOverlay {

    public enum Action { NONE, RESTART, MAIN_MENU }

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch   batch;
    private final BitmapFont    titleFont;
    private final BitmapFont    subFont;
    private final BitmapFont    bodyFont;
    private final BitmapFont    btnFont;
    private final BitmapFont    hintFont;

    private final Rectangle btnRestart  = new Rectangle();
    private final Rectangle btnMainMenu = new Rectangle();
    private boolean prevTouch = true; // ignore any touch already held when overlay appears

    public GameOverOverlay() {
        shapeRenderer = new ShapeRenderer();
        batch         = new SpriteBatch();
        titleFont = FontUtils.createFont("fonts/ArialBold.ttf", 56);
        subFont   = FontUtils.createFont("fonts/ArialBold.ttf", 24);
        bodyFont  = FontUtils.createFont("fonts/ArialBold.ttf", 16);
        btnFont   = FontUtils.createFont("fonts/ArialBold.ttf", 22);
        hintFont  = FontUtils.createFont("fonts/ArialBold.ttf", 13);
    }

    /** Call once per frame BEFORE render(). */
    public Action handleInput() {
        boolean touched     = Gdx.input.isTouched();
        boolean justClicked = touched && !prevTouch;
        prevTouch = touched;
        if (!justClicked) return Action.NONE;

        int mx = Gdx.input.getX();
        int my = Gdx.graphics.getHeight() - Gdx.input.getY();

        if (btnRestart.contains(mx, my))  return Action.RESTART;
        if (btnMainMenu.contains(mx, my)) return Action.MAIN_MENU;
        return Action.NONE;
    }

    public void render(int sw, int sh) {
        float panelW = sw * 0.50f;
        float panelH = sh * 0.65f;
        float cx = sw * 0.5f;
        float cy = sh * 0.5f;
        float px = cx - panelW * 0.5f;
        float py = cy - panelH * 0.5f;

        drawPanel(px, py, panelW, panelH);

        GlyphLayout layout = new GlyphLayout();

        batch.begin();
        titleFont.setColor(1f, 0.18f, 0.18f, 1f);
        layout.setText(titleFont, "GAME OVER");
        float scaleX = Math.min(1f, (panelW * 0.82f) / layout.width);
        titleFont.getData().setScale(scaleX, 1f);
        layout.setText(titleFont, "GAME OVER");
        titleFont.draw(batch, "GAME OVER", cx - layout.width * 0.5f, py + panelH - panelH * 0.08f);
        titleFont.getData().setScale(1f, 1f);
        batch.end();

        drawHRule(cx, py + panelH - panelH * 0.26f, panelW * 0.72f);

        batch.begin();
        subFont.setColor(0.95f, 0.55f, 0.55f, 1f);
        layout.setText(subFont, "Кисень вичерпано");
        subFont.draw(batch, "Кисень вичерпано", cx - layout.width * 0.5f, py + panelH - panelH * 0.33f);

        bodyFont.setColor(0.55f, 0.28f, 0.28f, 1f);
        String flavour = "Атмосферу не вдалося відновити. Планета залишається мертвою.";
        layout.setText(bodyFont, flavour);
        float flavourScale = Math.min(1f, (panelW * 0.82f) / layout.width);
        bodyFont.getData().setScale(flavourScale, 1f);
        layout.setText(bodyFont, flavour);
        bodyFont.draw(batch, flavour, cx - layout.width * 0.5f, py + panelH - panelH * 0.46f);
        bodyFont.getData().setScale(1f, 1f);
        batch.end();

        float btnW    = panelW * 0.55f;
        float btnH    = panelH * 0.11f;
        float btnGap  = panelH * 0.04f;
        float stackTop = py + panelH * 0.28f + (2 * btnH + btnGap) * 0.5f;

        btnRestart.set( cx - btnW * 0.5f, stackTop - btnH,              btnW, btnH);
        btnMainMenu.set(cx - btnW * 0.5f, stackTop - 2 * btnH - btnGap, btnW, btnH);

        drawButton(btnRestart,  "СПРОБУВАТИ ЩЕ РАЗ", 0.22f, 0.05f, 0.05f, 0.90f, 0.20f, 0.20f);
        drawButton(btnMainMenu, "ГОЛОВНЕ МЕНЮ",       0.10f, 0.06f, 0.12f, 0.55f, 0.20f, 0.60f);

        batch.begin();
        hintFont.setColor(0.30f, 0.14f, 0.14f, 1f);
        String hint = "Не здавайся — планета чекає на тебе";
        layout.setText(hintFont, hint);
        hintFont.draw(batch, hint, cx - layout.width * 0.5f, py + panelH * 0.04f);
        batch.end();
    }

    private void drawPanel(float px, float py, float pw, float ph) {
        float border = 4f;
        float cs = Math.min(pw, ph) * 0.028f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.10f, 0.02f, 0.02f, 0.97f);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.setColor(0.55f, 0.05f, 0.05f, 1f);
        shapeRenderer.rect(px, py + ph - border, pw, border);
        shapeRenderer.rect(px, py,               pw, border);
        shapeRenderer.setColor(0.40f, 0.04f, 0.04f, 1f);
        shapeRenderer.rect(px,             py, border, ph);
        shapeRenderer.rect(px + pw - border, py, border, ph);
        shapeRenderer.setColor(0.85f, 0.15f, 0.15f, 1f);
        shapeRenderer.rect(px - 2f,           py + ph - cs, cs, cs);
        shapeRenderer.rect(px + pw - cs + 2f, py + ph - cs, cs, cs);
        shapeRenderer.rect(px - 2f,           py,           cs, cs);
        shapeRenderer.rect(px + pw - cs + 2f, py,           cs, cs);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.75f, 0.10f, 0.10f, 0.90f);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.setColor(0.40f, 0.06f, 0.06f, 0.60f);
        shapeRenderer.rect(px + 10f, py + 10f, pw - 20f, ph - 20f);
        shapeRenderer.end();
    }

    private void drawButton(Rectangle r, String label,
                            float bgR, float bgG, float bgB,
                            float fgR, float fgG, float fgB) {
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
        btnFont.setColor(fgR, fgG, fgB, 1f);
        layout.setText(btnFont, label);
        float scaleX = Math.min(1f, (r.width * 0.85f) / layout.width);
        btnFont.getData().setScale(scaleX, 1f);
        layout.setText(btnFont, label);
        btnFont.draw(batch, label,
                r.x + (r.width  - layout.width)  * 0.5f,
                r.y + (r.height + layout.height) * 0.5f);
        btnFont.getData().setScale(1f, 1f);
        batch.end();
    }

    private void drawHRule(float cx, float y, float w) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.60f, 0.08f, 0.08f, 1f);
        shapeRenderer.rect(cx - w * 0.5f, y, w, 2f);
        shapeRenderer.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        titleFont.dispose();
        subFont.dispose();
        bodyFont.dispose();
        btnFont.dispose();
        hintFont.dispose();
    }
}