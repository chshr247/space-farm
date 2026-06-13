package com.spacefarm.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.spacefarm.farming.FarmingConstants;
import com.spacefarm.world.SeedWheelConstants;

/**
 * Fortune-wheel overlay.
 *
 * Flow:
 *   1. Player clicks wheel location → setVisible(true)
 *   2. Player clicks "КРУТИТИ" → GameInteractionService calls startSpin()
 *   3. Wheel spins → determineResult(), then 1s delay → result modal shown
 *   4. Player clicks "ЗАБРАТИ" / "ЗАКРИТИ" → handleTouchDown() sets showResult=false
 *   5. Next update() → GameInteractionService detects hasResult()==true
 *      → getResultAndReset() → adds items → setVisible(false) → game continues
 */
public class SeedWheelOverlay {

    // ── Sector degrees ──────────────────────────────────────────────────────
    private static final float DEG_COMMON    = 360f * (SeedWheelConstants.COMMON_SEED_CHANCE    / 100f); // 216°
    private static final float DEG_RARE      = 360f * (SeedWheelConstants.RARE_SEED_CHANCE      / 100f); // 108°
    private static final float DEG_LEGENDARY = 360f * (SeedWheelConstants.LEGENDARY_SEED_CHANCE / 100f); //  36°

    // ── Sector colours ──────────────────────────────────────────────────────
    private static final Color COLOR_COMMON    = fromRGB(SeedWheelConstants.COLOR_COMMON);
    private static final Color COLOR_RARE      = fromRGB(SeedWheelConstants.COLOR_EPIC);
    private static final Color COLOR_LEGENDARY = fromRGB(SeedWheelConstants.COLOR_LEGENDARY);

    // ── Panel accent colours (cyan = wheel/common, green = epic/legendary) ──
    private static final float AC_R = 0.10f, AC_G = 0.88f, AC_B = 1.00f; // cyan
    private static final float GR_R = 0.15f, GR_G = 0.90f, GR_B = 0.40f; // green

    // ── Rendering ───────────────────────────────────────────────────────────
    private final ShapeRenderer sr;
    private final SpriteBatch   batch;
    private final BitmapFont    titleFont;
    private final BitmapFont    bodyFont;
    private final BitmapFont    btnFont;
    private final BitmapFont    hintFont;
    private final GlyphLayout   gl = new GlyphLayout();

    // ── State ───────────────────────────────────────────────────────────────
    private boolean isVisible  = false;
    private boolean isSpinning = false;
    private boolean showResult = false;

    private float wheelRotation = 0f;
    private float spinTimeLeft  = 0f;
    private float totalRotation = 0f;
    private float resultDelay   = 0f;

    private FarmingConstants.CropType resultType = null;

    // ── Hit-boxes (set during render) ───────────────────────────────────────
    private final Rectangle spinBtnRect  = new Rectangle();
    private final Rectangle claimBtnRect = new Rectangle();
    private final Rectangle closeBtnRect = new Rectangle();

    // ── Wheel geometry (set during render) ──────────────────────────────────
    private float wx, wy, wr;

    // ────────────────────────────────────────────────────────────────────────

    public SeedWheelOverlay() {
        sr    = new ShapeRenderer();
        batch = new SpriteBatch();

        titleFont = FontUtils.createFont("fonts/ArialBold.ttf", 40);
        bodyFont  = FontUtils.createFont("fonts/ArialBold.ttf", 20);
        btnFont   = FontUtils.createFont("fonts/ArialBold.ttf", 22);
        hintFont  = FontUtils.createFont("fonts/ArialBold.ttf", 13);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Public API
    // ════════════════════════════════════════════════════════════════════════

    public void update(float delta) {
        if (!isSpinning && resultDelay <= 0f) return;

        if (isSpinning) {
            spinTimeLeft -= delta;
            if (spinTimeLeft > 0f) {
                float t     = spinTimeLeft / SeedWheelConstants.SPIN_DURATION_SECONDS;
                float speed = SeedWheelConstants.MAX_ROTATION_SPEED * t;
                float step  = speed * delta;
                wheelRotation += step;
                totalRotation += step;
            } else {
                // Guarantee minimum rotations
                float minDeg = SeedWheelConstants.MIN_ROTATIONS * 360f;
                if (totalRotation < minDeg) {
                    wheelRotation += (minDeg - totalRotation);
                }
                isSpinning   = false;
                spinTimeLeft = 0f;
                determineResult();
                resultDelay  = 1.0f;
            }
        } else if (resultDelay > 0f) {
            resultDelay -= delta;
            if (resultDelay <= 0f) {
                resultDelay = 0f;
                showResult  = true;
            }
        }
    }

    public void startSpin() {
        if (isSpinning || showResult) return;
        isSpinning    = true;
        spinTimeLeft  = SeedWheelConstants.SPIN_DURATION_SECONDS;
        totalRotation = 0f;
        resultDelay   = 0f;
        resultType    = null;
        wheelRotation += MathUtils.random(0f, 360f);
    }

    public FarmingConstants.CropType getResultAndReset() {
        FarmingConstants.CropType r = resultType;
        resultType = null;
        showResult = false;
        return r;
    }

    /** True only after spin done + delay passed + player dismissed modal. */
    public boolean hasResult() {
        return resultType != null && !showResult && resultDelay <= 0f;
    }

    public boolean isVisible()            { return isVisible; }
    public boolean isSpinning()           { return isSpinning; }
    public void    setVisible(boolean v)  { 
        isVisible = v; 
    }

    /**
     * Called from GameInteractionService.handleTouchDown().
     * Returns true (swallows click) when result modal is open.
     * Returns false when wheel is shown, so caller handles spin button.
     */
    public boolean handleTouchDown(float screenX, float screenY) {
        if (!isVisible) return false;
        if (showResult) {
            if (claimBtnRect.contains(screenX, screenY)
                    || closeBtnRect.contains(screenX, screenY)) {
                showResult = false;
            }
            return true;
        }
        return false;
    }

    /** Returns true if the "КРУТИТИ" button was hit. */
    public boolean isButtonHit(float screenX, float screenY) {
        return !isSpinning && !showResult && spinBtnRect.contains(screenX, screenY);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Render
    // ════════════════════════════════════════════════════════════════════════

    public void render(int sw, int sh) {
        if (!isVisible) return;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.65f);
        sr.rect(0, 0, sw, sh);
        sr.end();

        if (showResult) {
            renderResultModal(sw, sh);
        } else {
            renderWheelPanel(sw, sh);
        }
    }

    // ── Wheel panel ──────────────────────────────────────────────────────────

    private void renderWheelPanel(int sw, int sh) {
        float panelW = sw * 0.44f;
        float panelH = sh * 0.74f;
        float cx = sw * 0.5f;
        float cy = sh * 0.5f;
        float px = cx - panelW * 0.5f;
        float py = cy - panelH * 0.5f;

        drawPanel(px, py, panelW, panelH, 0.02f, 0.05f, 0.11f, AC_R, AC_G, AC_B);

        wr = Math.min(panelW, panelH) * 0.30f;
        wx = cx;
        wy = cy + panelH * 0.04f;

        drawWheelBackground();
        drawWheelSectors();
        drawWheelRim();
        drawPointer();
        drawHub();

        batch.begin();
        titleFont.setColor(AC_R, AC_G, AC_B, 1f);
        gl.setText(titleFont, "Колесо Фортуни");
        titleFont.draw(batch, "Колесо Фортуни",
                cx - gl.width * 0.5f, py + panelH - panelH * 0.04f);
        batch.end();

        float btnW = panelW * 0.55f;
        float btnH = panelH * 0.09f;
        spinBtnRect.set(cx - btnW * 0.5f, py + panelH * 0.065f, btnW, btnH);

        if (isSpinning) {
            drawButton(spinBtnRect, "КРУТИТЬСЯ...",
                    0.05f, 0.06f, 0.07f, 0.35f, 0.35f, 0.35f);
        } else {
            drawButton(spinBtnRect, "КРУТИТИ",
                    0.05f, 0.22f, 0.27f, AC_R, AC_G, AC_B);
        }

        drawLegend(cx, py + panelH * 0.185f);
    }

    private void drawWheelBackground() {
        int bg = SeedWheelConstants.WHEEL_BACKGROUND_COLOR;
        float r = ((bg >> 16) & 0xFF) / 255f;
        float g = ((bg >>  8) & 0xFF) / 255f;
        float b = ( bg        & 0xFF) / 255f;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(r, g, b, 1f);
        fillCircle(wx, wy, wr + 4f, 60);
        sr.end();
    }

    private void drawWheelSectors() {
        float start = wheelRotation;
        drawSector(start,                         DEG_COMMON,    COLOR_COMMON,    "Звичайне",   "насіння");
        drawSector(start + DEG_COMMON,            DEG_RARE,      COLOR_RARE,      "Епічне",     "насіння");
        drawSector(start + DEG_COMMON + DEG_RARE, DEG_LEGENDARY, COLOR_LEGENDARY, "Легендарне", "насіння");
    }

    private void drawSector(float startDeg, float sweepDeg, Color color,
                            String line1, String line2) {
        int segs = Math.max(3, (int)(sweepDeg / 4f));

        // Dark inner
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(color.r * 0.65f, color.g * 0.65f, color.b * 0.65f, 0.92f);
        for (int i = 0; i < segs; i++) {
            float a1 = startDeg + (float)  i      / segs * sweepDeg;
            float a2 = startDeg + (float)(i + 1)  / segs * sweepDeg;
            sr.triangle(wx, wy,
                    wx + wr * MathUtils.cosDeg(a1), wy + wr * MathUtils.sinDeg(a1),
                    wx + wr * MathUtils.cosDeg(a2), wy + wr * MathUtils.sinDeg(a2));
        }
        sr.end();

        // Lighter outer ring
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(color.r, color.g, color.b, 0.90f);
        float innerR = wr * 0.45f;
        for (int i = 0; i < segs; i++) {
            float a1 = startDeg + (float)  i      / segs * sweepDeg;
            float a2 = startDeg + (float)(i + 1)  / segs * sweepDeg;
            float ix1 = wx + innerR * MathUtils.cosDeg(a1), iy1 = wy + innerR * MathUtils.sinDeg(a1);
            float ix2 = wx + innerR * MathUtils.cosDeg(a2), iy2 = wy + innerR * MathUtils.sinDeg(a2);
            float ox1 = wx + wr     * MathUtils.cosDeg(a1), oy1 = wy + wr     * MathUtils.sinDeg(a1);
            float ox2 = wx + wr     * MathUtils.cosDeg(a2), oy2 = wy + wr     * MathUtils.sinDeg(a2);
            sr.triangle(ix1, iy1, ox1, oy1, ox2, oy2);
            sr.triangle(ix1, iy1, ox2, oy2, ix2, iy2);
        }
        sr.end();

        // Dividing line
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0f, 0f, 0f, 0.5f);
        sr.line(wx, wy,
                wx + wr * MathUtils.cosDeg(startDeg),
                wy + wr * MathUtils.sinDeg(startDeg));
        sr.end();

        // Label — larger font for bigger sectors
        float mid   = startDeg + sweepDeg * 0.5f;
        float lx    = wx + wr * 0.60f * MathUtils.cosDeg(mid);
        float ly    = wy + wr * 0.60f * MathUtils.sinDeg(mid);
        float scale = sweepDeg > 100f ? 1.30f : sweepDeg > 40f ? 1.10f : 1.00f;

        batch.begin();
        bodyFont.getData().setScale(scale);
        bodyFont.setColor(1f, 1f, 1f, 1f);
        gl.setText(bodyFont, line1);
        float offset = (line2 != null) ? 8f * scale : 0f;
        bodyFont.draw(batch, line1, lx - gl.width * 0.5f, ly + offset + gl.height * 0.5f);
        if (line2 != null) {
            gl.setText(bodyFont, line2);
            bodyFont.draw(batch, line2, lx - gl.width * 0.5f, ly - offset + gl.height * 0.5f);
        }
        bodyFont.getData().setScale(1f);
        batch.end();
    }

    private void drawWheelRim() {
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(AC_R, AC_G, AC_B, 0.85f);
        drawCircleLines(wx, wy, wr + 2f, 80);
        sr.end();
    }

    private void drawPointer() {
        float hw = 13f;
        float ah = 26f;
        float ay = wy + wr + 6f;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(1f, 0.08f, 0.08f, 1f);
        sr.triangle(wx - hw, ay + ah, wx + hw, ay + ah, wx, ay);
        sr.end();
    }

    private void drawHub() {
        float r = wr * 0.09f;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.04f, 0.06f, 0.12f, 1f);
        fillCircle(wx, wy, r, 32);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(AC_R, AC_G, AC_B, 1f);
        drawCircleLines(wx, wy, r, 32);
        sr.end();
    }

    private void drawLegend(float cx, float y) {
        String[] labels = {
                "Звичайне ×" + SeedWheelConstants.COMMON_SEED_REWARD    + " (60%)",
                "Епічне ×"   + SeedWheelConstants.RARE_SEED_REWARD      + " (30%)",
                "Легендарне ×" + SeedWheelConstants.LEGENDARY_SEED_REWARD + " (10%)"
        };
        Color[] cols = { COLOR_COMMON, COLOR_RARE, COLOR_LEGENDARY };

        float dot = 10f, gap = 18f, totalW = 0f;
        for (String l : labels) { gl.setText(hintFont, l); totalW += dot + 5f + gl.width + gap; }

        float x = cx - totalW * 0.5f;
        for (int i = 0; i < labels.length; i++) {
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(cols[i].r, cols[i].g, cols[i].b, 0.9f);
            sr.rect(x, y - dot * 0.5f, dot, dot);
            sr.end();
            x += dot + 5f;
            batch.begin();
            hintFont.setColor(0.55f, 0.75f, 0.80f, 1f);
            gl.setText(hintFont, labels[i]);
            hintFont.draw(batch, labels[i], x, y + gl.height * 0.5f);
            batch.end();
            x += gl.width + gap;
        }
    }

    // ── Result modal ─────────────────────────────────────────────────────────

    private void renderResultModal(int sw, int sh) {
        boolean rare = (resultType == FarmingConstants.CropType.EPIC
                || resultType == FarmingConstants.CropType.LEGENDARY);

        float panelW = sw * 0.46f;
        float panelH = sh * 0.60f;
        float cx = sw * 0.5f;
        float cy = sh * 0.5f;
        float px = cx - panelW * 0.5f;
        float py = cy - panelH * 0.5f;

        float bgR, bgG, bgB, acR, acG, acB;
        if (rare) {
            bgR = 0.02f; bgG = 0.10f; bgB = 0.04f;
            acR = GR_R;  acG = GR_G;  acB = GR_B;
        } else {
            bgR = 0.02f; bgG = 0.05f; bgB = 0.11f;
            acR = AC_R;  acG = AC_G;  acB = AC_B;
        }

        drawPanel(px, py, panelW, panelH, bgR, bgG, bgB, acR, acG, acB);

        batch.begin();
        titleFont.setColor(acR, acG, acB, 1f);
        String title = rare ? "УДАЧА!" : "РЕЗУЛЬТАТ";
        gl.setText(titleFont, title);
        titleFont.draw(batch, title, cx - gl.width * 0.5f, py + panelH - panelH * 0.09f);
        batch.end();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(acR, acG, acB, 0.65f);
        sr.rect(cx - panelW * 0.35f, py + panelH - panelH * 0.24f, panelW * 0.70f, 2f);
        sr.end();

        String sub, body1, body2, rewardText;
        switch (resultType) {
            case EPIC:
                sub        = "Рідкісна знахідка!";
                body1      = "Ти видобув епічне насіння з руїн.";
                body2      = "+Кисень " + (int)SeedWheelConstants.RARE_SEED_OXYGEN_RESTORE + "% після посадки.";
                rewardText = "Епічне насіння  ×" + SeedWheelConstants.RARE_SEED_REWARD;
                break;
            case LEGENDARY:
                sub        = "Легендарна знахідка!";
                body1      = "Неймовірна удача — найрідкісніший скарб планети!";
                body2      = "+Кисень " + (int)SeedWheelConstants.LEGENDARY_SEED_OXYGEN_RESTORE + "% після посадки.";
                rewardText = "Легендарне насіння  ×" + SeedWheelConstants.LEGENDARY_SEED_REWARD;
                break;
            default:
                sub        = "Знайдено звичайне насіння";
                body1      = "Непогана знахідка для початку.";
                body2      = "Посади та вирости врожай, щоб збільшити кисень.";
                rewardText = "Звичайне насіння  ×" + SeedWheelConstants.COMMON_SEED_REWARD;
                break;
        }

        batch.begin();

        bodyFont.getData().setScale(1.1f);
        bodyFont.setColor(acR * 0.75f + 0.25f, acG * 0.75f + 0.25f, acB * 0.75f + 0.25f, 1f);
        gl.setText(bodyFont, sub);
        bodyFont.draw(batch, sub, cx - gl.width * 0.5f, py + panelH - panelH * 0.31f);
        bodyFont.getData().setScale(1f);

        bodyFont.setColor(acR * 0.50f, acG * 0.50f + 0.08f, acB * 0.50f, 1f);
        gl.setText(bodyFont, body1);
        bodyFont.draw(batch, body1, cx - gl.width * 0.5f, py + panelH - panelH * 0.415f);
        gl.setText(bodyFont, body2);
        bodyFont.draw(batch, body2, cx - gl.width * 0.5f, py + panelH - panelH * 0.495f);

        bodyFont.getData().setScale(1.15f);
        bodyFont.setColor(rare ? new float[]{1f, 0.85f, 0.25f, 1f}[0] : acR,
                rare ? 0.85f : acG,
                rare ? 0.25f : acB, 1f);
        if (rare) bodyFont.setColor(1f, 0.85f, 0.25f, 1f);
        else      bodyFont.setColor(acR, acG, acB, 1f);
        gl.setText(bodyFont, rewardText);
        bodyFont.draw(batch, rewardText, cx - gl.width * 0.5f, py + panelH * 0.42f);
        bodyFont.getData().setScale(1f);

        hintFont.setColor(acR * 0.40f, acG * 0.40f + 0.05f, acB * 0.40f, 1f);
        String hint = rare ? "Фермере, планета пишається тобою!"
                : "Продовжуй досліджувати, щоб знайти більше!";
        gl.setText(hintFont, hint);
        hintFont.draw(batch, hint, cx - gl.width * 0.5f, py + panelH * 0.07f);
        batch.end();

        float btnW   = panelW * 0.58f;
        float btnH   = panelH * 0.10f;
        float btnGap = panelH * 0.03f;
        float top    = py + panelH * 0.26f + btnH + btnGap * 0.5f;

        claimBtnRect.set(cx - btnW * 0.5f, top - btnH,            btnW, btnH);
        closeBtnRect.set(cx - btnW * 0.5f, top - 2*btnH - btnGap, btnW, btnH);

        drawButton(claimBtnRect,
                rare ? "ЗАБРАТИ НАГОРОДУ" : "ЗАБРАТИ",
                bgR + 0.03f, bgG + 0.12f, bgB + 0.04f,
                acR, acG, acB);
        drawButton(closeBtnRect,
                "ЗАКРИТИ",
                bgR + 0.01f, bgG + 0.04f, bgB + 0.02f,
                acR * 0.55f, acG * 0.55f, acB * 0.55f);
    }

    // ── Result determination ──────────────────────────────────────────────────

    private void determineResult() {
        float rot   = ((wheelRotation % 360f) + 360f) % 360f;
        float angle = (90f - rot + 360f) % 360f;

        if (angle < DEG_COMMON) {
            resultType = FarmingConstants.CropType.DEFAULT;
        } else if (angle < DEG_COMMON + DEG_RARE) {
            resultType = FarmingConstants.CropType.EPIC;
        } else {
            resultType = FarmingConstants.CropType.LEGENDARY;
        }
    }

    // ── Drawing helpers ───────────────────────────────────────────────────────

    private void drawPanel(float px, float py, float pw, float ph,
                           float bgR, float bgG, float bgB,
                           float acR, float acG, float acB) {
        float border = 4f;
        float cs     = Math.min(pw, ph) * 0.028f;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(bgR, bgG, bgB, 0.97f);
        sr.rect(px, py, pw, ph);
        sr.setColor(acR, acG, acB, 1f);
        sr.rect(px, py + ph - border, pw, border);
        sr.rect(px, py,               pw, border);
        sr.setColor(acR * 0.65f, acG * 0.65f, acB * 0.65f, 1f);
        sr.rect(px,             py, border, ph);
        sr.rect(px + pw - border, py, border, ph);
        sr.setColor(acR, acG, acB, 1f);
        sr.rect(px - 2f,           py + ph - cs, cs, cs);
        sr.rect(px + pw - cs + 2f, py + ph - cs, cs, cs);
        sr.rect(px - 2f,           py,            cs, cs);
        sr.rect(px + pw - cs + 2f, py,            cs, cs);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(acR, acG, acB, 0.75f);
        sr.rect(px, py, pw, ph);
        sr.setColor(acR * 0.45f, acG * 0.45f, acB * 0.45f, 0.40f);
        sr.rect(px + 10f, py + 10f, pw - 20f, ph - 20f);
        sr.end();
    }

    private void drawButton(Rectangle r, String label,
                            float bgR, float bgG, float bgB,
                            float fgR, float fgG, float fgB) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(bgR, bgG, bgB, 0.92f);
        sr.rect(r.x, r.y, r.width, r.height);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(fgR, fgG, fgB, 0.85f);
        sr.rect(r.x, r.y, r.width, r.height);
        sr.end();

        batch.begin();
        btnFont.setColor(fgR, fgG, fgB, 1f);
        gl.setText(btnFont, label);
        float sx = Math.min(1f, (r.width * 0.85f) / gl.width);
        btnFont.getData().setScale(sx, 1f);
        gl.setText(btnFont, label);
        btnFont.draw(batch, label,
                r.x + (r.width  - gl.width)  * 0.5f,
                r.y + (r.height + gl.height) * 0.5f);
        btnFont.getData().setScale(1f, 1f);
        batch.end();
    }

    private void fillCircle(float cx, float cy, float r, int segs) {
        for (int i = 0; i < segs; i++) {
            float a1 = 360f *  i      / segs;
            float a2 = 360f * (i + 1) / segs;
            sr.triangle(cx, cy,
                    cx + r * MathUtils.cosDeg(a1), cy + r * MathUtils.sinDeg(a1),
                    cx + r * MathUtils.cosDeg(a2), cy + r * MathUtils.sinDeg(a2));
        }
    }

    private void drawCircleLines(float cx, float cy, float r, int segs) {
        for (int i = 0; i < segs; i++) {
            float a1 = 360f *  i      / segs;
            float a2 = 360f * (i + 1) / segs;
            sr.line(cx + r * MathUtils.cosDeg(a1), cy + r * MathUtils.sinDeg(a1),
                    cx + r * MathUtils.cosDeg(a2), cy + r * MathUtils.sinDeg(a2));
        }
    }

    private static Color fromRGB(int rgb) {
        return new Color(((rgb >> 16) & 0xFF) / 255f,
                ((rgb >>  8) & 0xFF) / 255f,
                ( rgb        & 0xFF) / 255f, 1f);
    }

    public void dispose() {
        sr.dispose();
        batch.dispose();
        titleFont.dispose();
        bodyFont.dispose();
        btnFont.dispose();
        hintFont.dispose();
    }
}