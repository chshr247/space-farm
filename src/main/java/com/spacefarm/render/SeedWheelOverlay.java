package com.spacefarm.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
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
    private float wheelRotation = 0f;  // Current rotation in degrees
    private float wheelRotationSpeed = 0f;  // Rotation speed in degrees per second
    private float spinTimeRemaining = 0f;  // Time left in the spin
    private int resultSeedType = -1;  // -1 = none, 0 = common, 1 = rare, 2 = legendary
    private float minSpinTimeRemaining = 0f;  // Minimum time at current speed

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

        this.titleFont = new BitmapFont();
        this.titleFont.setColor(Color.WHITE);
        this.titleFont.getData().setScale(2f);

        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);
        this.font.getData().setScale(1.2f);
    }

    /**
     * Update wheel animation.
     */
    public void update(float deltaTime) {
        if (!isSpinning) {
            return;
        }

        spinTimeRemaining -= deltaTime;

        // Decelerate smoothly
        if (spinTimeRemaining > 0) {
            // Calculate deceleration factor
            float decelerationFactor = spinTimeRemaining / SeedWheelConstants.SPIN_DURATION_SECONDS;
            wheelRotationSpeed = SeedWheelConstants.MAX_ROTATION_SPEED * decelerationFactor;
            wheelRotation += wheelRotationSpeed * deltaTime;
        } else {
            // Spin complete - determine result based on final rotation
            isSpinning = false;
            determineResult();
        }
    }

    /**
     * Start a new spin.
     */
    public void startSpin() {
        if (!isSpinning) {
            isSpinning = true;
            spinTimeRemaining = SeedWheelConstants.SPIN_DURATION_SECONDS;
            wheelRotationSpeed = SeedWheelConstants.MAX_ROTATION_SPEED;
            wheelRotation = MathUtils.random(0f, 360f);  // Random starting angle
            resultSeedType = -1;
        }
    }

    /**
     * Determine the result based on current wheel rotation.
     */
    private void determineResult() {
        float normalizedRotation = wheelRotation % 360f;

        // Zones (each takes up a portion of the circle):
        // 0-60°: Common (60%)
        // 60-90°: Rare (30%)
        // 90-100°: Legendary (10%)
        // Then it repeats

        float zoneRotation = normalizedRotation % 100f;

        if (zoneRotation < 60f) {
            resultSeedType = 0;  // Common
        } else if (zoneRotation < 90f) {
            resultSeedType = 1;  // Rare
        } else {
            resultSeedType = 2;  // Legendary
        }
    }

    /**
     * Get the result and reset.
     */
    public int getResultAndReset() {
        int result = resultSeedType;
        resultSeedType = -1;
        return result;
    }

    /**
     * Check if there's a new result.
     */
    public boolean hasResult() {
        return resultSeedType >= 0;
    }

    /**
     * Render the wheel UI.
     */
    public void render(int screenWidth, int screenHeight) {
        if (!isVisible) {
            return;
        }

        // Semi-transparent overlay
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.6f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();

        // Draw wheel background circle
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0x222222 / 255f, 0x222222 / 255f, 0x222222 / 255f, 0.8f);
        shapeRenderer.circle(WHEEL_CENTER_X, WHEEL_CENTER_Y, WHEEL_RADIUS + 10f);
        shapeRenderer.end();

        // Draw wheel zones
        drawWheelZones();

        // Draw pointer arrow at top
        drawArrow();

        // Draw spin button
        drawButton();

        // Draw title
        batch.begin();
        titleFont.draw(batch, "Колесо Фортуни", WHEEL_CENTER_X - 150f, WHEEL_CENTER_Y + WHEEL_RADIUS + 80f);

        if (isSpinning) {
            font.draw(batch, String.format("Крутиться..."), BUTTON_X + 20f, BUTTON_Y + 60f);
        } else if (hasResult()) {
            String resultText = "";
            switch (resultSeedType) {
                case 0:
                    resultText = "Звичайне насіння x" + SeedWheelConstants.COMMON_SEED_REWARD;
                    break;
                case 1:
                    resultText = "Рідкісне насіння x" + SeedWheelConstants.RARE_SEED_REWARD;
                    break;
                case 2:
                    resultText = "Легендарне насіння x" + SeedWheelConstants.LEGENDARY_SEED_REWARD;
                    break;
            }
            font.setColor(1f, 1f, 0f, 1f);
            font.draw(batch, resultText, WHEEL_CENTER_X - 100f, 100f);
            font.setColor(1f, 1f, 1f, 1f);
        }

        batch.end();
    }

    /**
     * Draw the wheel zones.
     */
    private void drawWheelZones() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Calculate zone angles based on distribution
        // Common: 60% = 216°, Rare: 30% = 108°, Legendary: 10% = 36°

        float commonDegrees = 216f;
        float rareDegrees = 108f;
        float legendaryDegrees = 36f;

        // Apply current rotation
        float startAngle = wheelRotation;

        // Draw common sector
        drawWheelSector(startAngle, commonDegrees, 0.2f, 0.7f, 0.2f);

        // Draw rare sector
        drawWheelSector(startAngle + commonDegrees, rareDegrees, 1f, 0f, 1f);

        // Draw legendary sector
        drawWheelSector(startAngle + commonDegrees + rareDegrees, legendaryDegrees, 1f, 0.65f, 0f);

        shapeRenderer.end();
    }

    /**
     * Draw a sector of the wheel.
     */
    private void drawWheelSector(float startAngle, float sectorDegrees, float r, float g, float b) {
        shapeRenderer.setColor(r, g, b, 0.9f);

        float endAngle = startAngle + sectorDegrees;
        int segments = Math.max(3, (int)(sectorDegrees / 10f));

        // Draw sector as triangles from center
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

    /**
     * Draw the pointer arrow at top.
     */
    private void drawArrow() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 0f, 0f, 1f);

        float arrowX = WHEEL_CENTER_X;
        float arrowY = WHEEL_CENTER_Y + WHEEL_RADIUS + 15f;

        // Draw triangle pointing down
        shapeRenderer.triangle(
            arrowX - 15f, arrowY,
            arrowX + 15f, arrowY,
            arrowX, arrowY - ARROW_HEIGHT
        );

        shapeRenderer.end();
    }

    /**
     * Draw the spin button.
     */
    private void drawButton() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (isSpinning) {
            shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.8f);
        } else {
            shapeRenderer.setColor(0.2f, 0.6f, 0.2f, 0.9f);
        }

        shapeRenderer.rect(BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        shapeRenderer.end();

        // Button border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.rect(BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        shapeRenderer.end();

        // Button text
        batch.begin();
        font.setColor(Color.WHITE);
        if (isSpinning) {
            font.draw(batch, "Крутиться...", BUTTON_X + 20f, BUTTON_Y + 35f);
        } else {
            font.draw(batch, "Крутити", BUTTON_X + 30f, BUTTON_Y + 35f);
        }
        batch.end();
    }

    /**
     * Check if a screen position is within the spin button.
     */
    public boolean isButtonHit(float screenX, float screenY) {
        return screenX >= BUTTON_X && screenX <= BUTTON_X + BUTTON_WIDTH &&
               screenY >= BUTTON_Y && screenY <= BUTTON_Y + BUTTON_HEIGHT;
    }

    // Getters/Setters
    public boolean isVisible() { return isVisible; }
    public void setVisible(boolean visible) { this.isVisible = visible; }
    public boolean isSpinning() { return isSpinning; }

    /**
     * Dispose of resources.
     */
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        titleFont.dispose();
    }
}


