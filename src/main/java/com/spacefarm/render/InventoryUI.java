package com.spacefarm.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.spacefarm.inventory.Inventory;
import com.spacefarm.inventory.Item;

import static com.badlogic.gdx.Gdx.graphics;

/**
 * Renders the inventory UI at the bottom center of the screen.
 */
public class InventoryUI {
    private final Inventory inventory;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final OrthographicCamera screenCamera;
    private final GlyphLayout layout = new GlyphLayout();

    private static final float SLOT_SIZE = 48f;
    private static final float SLOT_SPACING = 4f;
    private static final float BOTTOM_PADDING = 20f;
    private static final float ROW_SIZE = 8;
    private static final float TOGGLE_BUTTON_SIZE = 40f;
    private static final float ANIMATION_SPEED = 6f;

    private boolean isExpanded = false;
    private float targetExpansionY = 0f;
    private float currentExpansionY = 0f;
    private float buttonX;
    private float buttonY;

    private int draggedSlotIndex = -1;
    private float dragX = 0f;
    private float dragY = 0f;

    public InventoryUI(Inventory inventory, int screenWidth, int screenHeight) {
        this.inventory = inventory;
        this.shapeRenderer = new ShapeRenderer();
        this.batch = new SpriteBatch();
        this.font = FontUtils.createFont("fonts/ArialBold.ttf", 18);
        this.font.setColor(Color.WHITE);

        // Create a camera for screen-space rendering (Y-up)
        this.screenCamera = new OrthographicCamera();
        this.screenCamera.setToOrtho(false, screenWidth, screenHeight);
        this.screenCamera.update();
    }

    /**
     * Update animations.
     */
    public void update(float deltaTime) {
        targetExpansionY = isExpanded ? (2 * (SLOT_SIZE + SLOT_SPACING)) : 0f;
        float alpha = 1f - (float) Math.exp(-ANIMATION_SPEED * deltaTime);
        currentExpansionY += (targetExpansionY - currentExpansionY) * alpha;
    }

    /**
     * Render the inventory UI.
     */
    public void render(int screenWidth, int screenHeight) {
        // Update camera in case screen size changed
        screenCamera.setToOrtho(false, screenWidth, screenHeight);
        screenCamera.update();

        // Calculate position: centered at BOTTOM
        float totalWidth = ROW_SIZE * (SLOT_SIZE + SLOT_SPACING);
        float startX = (screenWidth - totalWidth) / 2f;
        float baseBottomY = BOTTOM_PADDING;

        shapeRenderer.setProjectionMatrix(screenCamera.combined);
        batch.setProjectionMatrix(screenCamera.combined);

        // Animation: Row 0 (toolbar) moves up, Row 1 and 2 appear below it
        float toolbarY = baseBottomY + currentExpansionY;
        float maxExpansion = 2 * (SLOT_SIZE + SLOT_SPACING);
        float progress = Math.max(0f, Math.min(1f, currentExpansionY / maxExpansion));

        // Ensure strictly 0 alpha when fully collapsed to avoid ghosting
        float extraRowsAlpha = 0f;
        if (progress > 0.01f) {
            extraRowsAlpha = (float) Math.sqrt(progress);
        }

        // Draw toggle button (moves with toolbar)
        buttonX = startX - TOGGLE_BUTTON_SIZE - 10f;
        buttonY = toolbarY + (SLOT_SIZE - TOGGLE_BUTTON_SIZE) / 2f;
        renderToggleButton(buttonX, buttonY);

        // Draw rows
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        for (int row = 0; row < 3; row++) {
            float rowY = toolbarY - row * (SLOT_SIZE + SLOT_SPACING);
            float rowAlpha = (row == 0) ? 1f : extraRowsAlpha;

            // Only skip if completely off screen or practically invisible
            if (rowY + SLOT_SIZE < 0 || rowAlpha <= 0.01f) continue;

            for (int col = 0; col < ROW_SIZE; col++) {
                int slotIndex = (int) (row * ROW_SIZE + col);
                float slotX = startX + col * (SLOT_SIZE + SLOT_SPACING);

                boolean isSelected = (slotIndex == inventory.getSelectedSlot());
                renderSlot(slotX, rowY, slotIndex, isSelected, rowAlpha);
            }
        }
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Render dragged item
        if (draggedSlotIndex != -1) {
            Item draggedItem = inventory.getItem(draggedSlotIndex);
            if (draggedItem != null) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                Color itemColor = getItemColor(draggedItem);
                shapeRenderer.setColor(itemColor.r, itemColor.g, itemColor.b, itemColor.a);
                float padding = 8f;
                float size = SLOT_SIZE - padding * 2;
                shapeRenderer.rect(dragX - size / 2f, dragY - size / 2f, size, size);
                shapeRenderer.end();
                Gdx.gl.glDisable(GL20.GL_BLEND);
            }
        }

        // Draw currently selected item name
        Item selectedItem = inventory.getSelectedItem();
        if (selectedItem != null) {
            batch.begin();
            batch.setColor(1f, 1f, 1f, 1f); // Always opaque
            String displayText = selectedItem.getDescription();
            layout.setText(font, displayText);
            float textX = (screenWidth - layout.width) / 2f;
            float textY = toolbarY + SLOT_SIZE + 25f;  // Above the toolbar
            font.setColor(1f, 1f, 1f, 1f);
            font.draw(batch, displayText, textX, textY);
            batch.end();
        }
    }

    private void renderToggleButton(float x, float y) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 0.9f);
        shapeRenderer.rect(x, y, TOGGLE_BUTTON_SIZE, TOGGLE_BUTTON_SIZE);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.8f, 0.8f, 0.8f, 1.0f);
        shapeRenderer.rect(x, y, TOGGLE_BUTTON_SIZE, TOGGLE_BUTTON_SIZE);

        float midX = x + TOGGLE_BUTTON_SIZE / 2f;
        float midY = y + TOGGLE_BUTTON_SIZE / 2f;
        float s = TOGGLE_BUTTON_SIZE / 4f;
        shapeRenderer.line(midX - s, midY, midX + s, midY);
        if (!isExpanded) {
            shapeRenderer.line(midX, midY - s, midX, midY + s);
        }
        shapeRenderer.end();
    }

    public boolean handleTouchDown(float screenX, float screenY) {
        float worldX = screenX;
        float worldY = graphics.getHeight() - screenY;

        if (worldX >= buttonX && worldX <= buttonX + TOGGLE_BUTTON_SIZE &&
            worldY >= buttonY && worldY <= buttonY + TOGGLE_BUTTON_SIZE) {
            isExpanded = !isExpanded;
            return true;
        }

        float totalWidth = ROW_SIZE * (SLOT_SIZE + SLOT_SPACING);
        float startX = (graphics.getWidth() - totalWidth) / 2f;
        float toolbarY = BOTTOM_PADDING + currentExpansionY;

        for (int row = 0; row < 3; row++) {
            float rowY = toolbarY - row * (SLOT_SIZE + SLOT_SPACING);
            float rowAlpha = (row == 0) ? 1f : Math.min(1f, currentExpansionY / (2 * (SLOT_SIZE + SLOT_SPACING)));

            if (rowAlpha > 0.5f && worldY >= rowY && worldY <= rowY + SLOT_SIZE) {
                for (int col = 0; col < ROW_SIZE; col++) {
                    float slotX = startX + col * (SLOT_SIZE + SLOT_SPACING);
                    if (worldX >= slotX && worldX <= slotX + SLOT_SIZE) {
                        int slotIndex = (int) (row * ROW_SIZE + col);
                        inventory.selectSlot(slotIndex);

                        if (inventory.getItem(slotIndex) != null) {
                            draggedSlotIndex = slotIndex;
                            dragX = worldX;
                            dragY = worldY;
                        }
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean handleTouchDragged(float screenX, float screenY) {
        if (draggedSlotIndex != -1) {
            dragX = screenX;
            dragY = graphics.getHeight() - screenY;
            return true;
        }
        return false;
    }

    public int getDraggedSlotIndex() {
        return draggedSlotIndex;
    }

    public int getTargetSlot(float screenX, float screenY) {
        float worldX = screenX;
        float worldY = graphics.getHeight() - screenY;

        float totalWidth = ROW_SIZE * (SLOT_SIZE + SLOT_SPACING);
        float startX = (graphics.getWidth() - totalWidth) / 2f;
        float toolbarY = BOTTOM_PADDING + currentExpansionY;

        for (int row = 0; row < 3; row++) {
            float rowY = toolbarY - row * (SLOT_SIZE + SLOT_SPACING);
            float rowAlpha = (row == 0) ? 1f : Math.min(1f, currentExpansionY / (2 * (SLOT_SIZE + SLOT_SPACING)));

            if (rowAlpha > 0.5f && worldY >= rowY && worldY <= rowY + SLOT_SIZE) {
                for (int col = 0; col < ROW_SIZE; col++) {
                    float slotX = startX + col * (SLOT_SIZE + SLOT_SPACING);
                    if (worldX >= slotX && worldX <= slotX + SLOT_SIZE) {
                        return (int) (row * ROW_SIZE + col);
                    }
                }
            }
        }
        return -1;
    }

    public boolean handleTouchUp(float screenX, float screenY) {
        if (draggedSlotIndex == -1) {
            return false;
        }

        int targetSlotIndex = getTargetSlot(screenX, screenY);

        if (targetSlotIndex != -1 && targetSlotIndex != draggedSlotIndex) {
            inventory.swapItems(draggedSlotIndex, targetSlotIndex);
            inventory.selectSlot(targetSlotIndex);
        }

        draggedSlotIndex = -1;
        return true;
    }

    private void renderSlot(float x, float y, int slotIndex, boolean isSelected, float alpha) {
        Item item = inventory.getItem(slotIndex);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (isSelected) {
            shapeRenderer.setColor(0.3f, 0.6f, 0.9f, 0.8f * alpha);
        } else {
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.7f * alpha);
        }
        shapeRenderer.rect(x, y, SLOT_SIZE, SLOT_SIZE);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (isSelected) {
            shapeRenderer.setColor(0.0f, 1.0f, 1.0f, 1.0f * alpha);
            shapeRenderer.rect(x, y, SLOT_SIZE, SLOT_SIZE);
            shapeRenderer.rect(x - 2, y - 2, SLOT_SIZE + 4, SLOT_SIZE + 4);
        } else {
            shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.8f * alpha);
        }
        shapeRenderer.rect(x, y, SLOT_SIZE, SLOT_SIZE);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        if (item != null) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            Color itemColor = getItemColor(item);
            float itemAlpha = (slotIndex == draggedSlotIndex) ? 0.3f * alpha : itemColor.a * alpha;
            shapeRenderer.setColor(itemColor.r, itemColor.g, itemColor.b, itemAlpha);
            float padding = 8f;
            shapeRenderer.rect(x + padding, y + padding, SLOT_SIZE - padding * 2, SLOT_SIZE - padding * 2);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        if (slotIndex < ROW_SIZE) {
            batch.begin();
            font.setColor(1f, 1f, 1f, alpha);
            String slotNumber = String.valueOf(slotIndex + 1);
            font.draw(batch, slotNumber, x + SLOT_SIZE - 12f, y + 12f);
            batch.end();
        }
    }

    private Color getItemColor(Item item) {
        switch (item.getType()) {
            case WATERING_CAN: return new Color(0.2f, 0.7f, 1.0f, 0.9f);
            case SEED: return new Color(0.8f, 0.6f, 0.2f, 0.9f);
            case SICKLE: return new Color(0.6f, 0.4f, 0.4f, 0.9f);
            case PLANT_FOOD: return new Color(0.9f, 0.3f, 0.2f, 0.9f);
            case FERTILIZER: return new Color(0.9f, 0.7f, 0.1f, 0.9f);
            case CRYSTAL: return new Color(0.4f, 0.8f, 1.0f, 0.95f);
            default: return new Color(0.5f, 0.5f, 0.5f, 0.5f);
        }
    }

    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
