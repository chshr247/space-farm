package com.spacefarm.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.spacefarm.farming.FarmingConstants; // Додано імпорт наших типів
import com.spacefarm.world.SeedWheelConstants;

/**
 * Renders the seed wheel fortune UI overlay.
 */
public class SeedWheelOverlay {
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final BitmapFont titleFont;

    // Wheel state
    private boolean isVisible = false;
    private boolean isSpinning = false;
    private float wheelRotation = 0f;
    private float wheelRotationSpeed = 0f;
    private float spinTimeRemaining = 0f;

    // ТЕПЕР ВИКОРИСТОВУЄМО НАШ ENUM ЗАМІСТЬ ЦИФР
    private FarmingConstants.CropType resultSeedType = null;

    // Wheel UI constants
    private static final float WHEEL_RADIUS = 120f;
    private static final float WHEEL_CENTER_X = 640f;
    private static final float WHEEL_CENTER_Y = 360f;
    private static final float BUTTON_WIDTH = 150f;
    private static final float BUTTON_HEIGHT = 50f;
    private static final float BUTTON_X = WHEEL_CENTER_X - BUTTON_WIDTH / 2f;
    private static final float BUTTON_Y = 150f;
    private static final float ARROW_HEIGHT = 30f;

    public SeedWheelOverlay() {
        this.shapeRenderer = new ShapeRenderer();
        this.batch = new SpriteBatch();

        this.titleFont = FontUtils.createFont("fonts/ArialBold.ttf", 36);
        this.titleFont.setColor(Color.WHITE);

        this.font = FontUtils.createFont("fonts/ArialBold.ttf", 22);
        this.font.setColor(Color.WHITE);
    }

    public void update(float deltaTime) {
        if (!isSpinning) {
            return;
        }

        spinTimeRemaining -= deltaTime;

        if (spinTimeRemaining > 0) {
            float decelerationFactor = spinTimeRemaining / SeedWheelConstants.SPIN_DURATION_SECONDS;
            wheelRotationSpeed = SeedWheelConstants.MAX_ROTATION_SPEED * decelerationFactor;
            wheelRotation += wheelRotationSpeed * deltaTime;
        } else {
            isSpinning = false;
            determineResult();
        }
    }

    public void startSpin() {
        if (!isSpinning) {
            isSpinning = true;
            spinTimeRemaining = SeedWheelConstants.SPIN_DURATION_SECONDS;
            wheelRotationSpeed = SeedWheelConstants.MAX_ROTATION_SPEED;
            wheelRotation = MathUtils.random(0f, 360f);
            resultSeedType = null; // Скидаємо результат
        }
    }

    private void determineResult() {
        float rotation = ((wheelRotation % 360f) + 360f) % 360f;
        float pointerAngleOnWheel = (90f - rotation + 360f) % 360f;

        float commonDegrees = 360f * (SeedWheelConstants.COMMON_SEED_CHANCE / 100f);
        float rareDegrees = 360f * (SeedWheelConstants.RARE_SEED_CHANCE / 100f);

        // Призначаємо типи з нашого FarmingConstants
        if (pointerAngleOnWheel < commonDegrees) {
            resultSeedType = FarmingConstants.CropType.DEFAULT;
        } else if (pointerAngleOnWheel < commonDegrees + rareDegrees) {
            resultSeedType = FarmingConstants.CropType.EPIC;
        } else {
            resultSeedType = FarmingConstants.CropType.LEGENDARY;
        }
    }

    // Повертає конкретний тип рослини
    public FarmingConstants.CropType getResultAndReset() {
        FarmingConstants.CropType result = resultSeedType;
        resultSeedType = null;
        return result;
    }

    public boolean hasResult() {
        return resultSeedType != null;
    }

    public void render(int screenWidth, int screenHeight) {
        if (!isVisible) return;

        // Overlay
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.6f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();

        // Background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        int bg = SeedWheelConstants.WHEEL_BACKGROUND_COLOR;
        float br = ((bg >> 16) & 0xFF) / 255f;
        float bgc = ((bg >> 8) & 0xFF) / 255f;
        float bb = (bg & 0xFF) / 255f;
        shapeRenderer.setColor(br, bgc, bb, 0.8f);
        shapeRenderer.circle(WHEEL_CENTER_X, WHEEL_CENTER_Y, WHEEL_RADIUS + 10f);
        shapeRenderer.end();

        drawWheelZones();
        drawArrow();
        drawButton();

        batch.begin();
        titleFont.draw(batch, "Колесо Фортуни", WHEEL_CENTER_X - 150f, WHEEL_CENTER_Y + WHEEL_RADIUS + 80f);

        if (isSpinning) {
            font.draw(batch, "Крутиться...", BUTTON_X + 20f, BUTTON_Y + 60f);
        } else if (hasResult()) {
            String resultText = "";
            // Використовуємо switch по Enum
            switch (resultSeedType) {
                case DEFAULT:
                    resultText = "Дефолтне насіння x" + SeedWheelConstants.COMMON_SEED_REWARD;
                    break;
                case EPIC:
                    resultText = "Епічне насіння x" + SeedWheelConstants.RARE_SEED_REWARD;
                    break;
                case LEGENDARY:
                    resultText = "Легендарне насіння x" + SeedWheelConstants.LEGENDARY_SEED_REWARD;
                    break;
            }
            font.setColor(1f, 1f, 0f, 1f);
            font.draw(batch, resultText, WHEEL_CENTER_X - 100f, 100f);
            font.setColor(1f, 1f, 1f, 1f);
        }

        batch.end();
    }

    private void drawWheelZones() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        float commonDegrees = 216f;
        float rareDegrees = 108f;
        float legendaryDegrees = 36f;
        float startAngle = wheelRotation;

        drawWheelSector(startAngle, commonDegrees, 0.2f, 0.7f, 0.2f);
        drawWheelSector(startAngle + commonDegrees, rareDegrees, 1f, 0f, 1f);
        drawWheelSector(startAngle + commonDegrees + rareDegrees, legendaryDegrees, 1f, 0.65f, 0f);
        shapeRenderer.end();
    }

    private void drawWheelSector(float startAngle, float sectorDegrees, float r, float g, float b) {
        shapeRenderer.setColor(r, g, b, 0.9f);
        float endAngle = startAngle + sectorDegrees;
        int segments = Math.max(3, (int)(sectorDegrees / 10f));

        for (int i = 0; i < segments; i++) {
            float angle1 = startAngle + (i * sectorDegrees / segments);
            float angle2 = startAngle + ((i + 1) * sectorDegrees / segments);
            float x1 = WHEEL_CENTER_X + WHEEL_RADIUS * MathUtils.cosDeg(angle1);
            float y1 = WHEEL_CENTER_Y + WHEEL_RADIUS * MathUtils.sinDeg(angle1);
            float x2 = WHEEL_CENTER_X + WHEEL_RADIUS * MathUtils.cosDeg(angle2);
            float y2 = WHEEL_CENTER_Y + WHEEL_RADIUS * MathUtils.sinDeg(angle2);
            shapeRenderer.triangle(WHEEL_CENTER_X, WHEEL_CENTER_Y, x1, y1, x2, y2);
        }
    }

    private void drawArrow() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 0f, 0f, 1f);
        float arrowX = WHEEL_CENTER_X;
        float arrowY = WHEEL_CENTER_Y + WHEEL_RADIUS + 15f;
        shapeRenderer.triangle(arrowX - 15f, arrowY, arrowX + 15f, arrowY, arrowX, arrowY - ARROW_HEIGHT);
        shapeRenderer.end();
    }

    private void drawButton() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (isSpinning) {
            shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.8f);
        } else {
            shapeRenderer.setColor(0.2f, 0.6f, 0.2f, 0.9f);
        }
        shapeRenderer.rect(BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.rect(BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        shapeRenderer.end();

        batch.begin();
        font.setColor(Color.WHITE);
        if (isSpinning) {
            font.draw(batch, "Крутиться...", BUTTON_X + 20f, BUTTON_Y + 35f);
        } else {
            font.draw(batch, "Крутити", BUTTON_X + 30f, BUTTON_Y + 35f);
        }
        batch.end();
    }

    public boolean isButtonHit(float screenX, float screenY) {
        return screenX >= BUTTON_X && screenX <= BUTTON_X + BUTTON_WIDTH &&
                screenY >= BUTTON_Y && screenY <= BUTTON_Y + BUTTON_HEIGHT;
    }

    public boolean isVisible() { return isVisible; }
    public void setVisible(boolean visible) { this.isVisible = visible; }
    public boolean isSpinning() { return isSpinning; }

    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        titleFont.dispose();
    }
}