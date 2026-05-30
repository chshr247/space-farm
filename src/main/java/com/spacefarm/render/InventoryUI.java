package com.spacefarm.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.spacefarm.inventory.Inventory;
import com.spacefarm.inventory.Item;
import com.spacefarm.inventory.Seed;

/**
 * Renders the inventory UI at the bottom center of the screen.
 * Rotated 180 degrees for correct orientation.
 */
public class InventoryUI {
    private final Inventory inventory;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final OrthographicCamera screenCamera;

    private static final float SLOT_SIZE = 48f;
    private static final float SLOT_SPACING = 4f;
    private static final float BOTTOM_PADDING = 20f;

    public InventoryUI(Inventory inventory, int screenWidth, int screenHeight) {
        this.inventory = inventory;
        this.shapeRenderer = new ShapeRenderer();
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);
        
        // Create a camera for screen-space rendering
        this.screenCamera = new OrthographicCamera();
        this.screenCamera.setToOrtho(true, screenWidth, screenHeight);
        this.screenCamera.update();
    }

    /**
     * Render the inventory UI.
     */
    public void render(int screenWidth, int screenHeight) {
        // Update camera in case screen size changed
        screenCamera.setToOrtho(true, screenWidth, screenHeight);
        screenCamera.update();

        // Calculate position: centered at BOTTOM
        float totalWidth = inventory.getSize() * (SLOT_SIZE + SLOT_SPACING);
        float startX = (screenWidth - totalWidth) / 2f;
        float startY = screenHeight - SLOT_SIZE - BOTTOM_PADDING;  // Bottom

        shapeRenderer.setProjectionMatrix(screenCamera.combined);
        batch.setProjectionMatrix(screenCamera.combined);

        // Draw slots in correct order (1-8 left to right)
        for (int i = 0; i < inventory.getSize(); i++) {
            float slotX = startX + i * (SLOT_SIZE + SLOT_SPACING);
            float slotY = startY;

            // Draw slot background
            boolean isSelected = (i == inventory.getSelectedSlot());
            renderSlot(slotX, slotY, i, isSelected);
        }

        // Draw currently selected item name
        Item selectedItem = inventory.getSelectedItem();
        if (selectedItem != null) {
            batch.begin();
            float textX = screenWidth / 2f - 60f;
            float textY = startY - 15f;  // Above the inventory
            String displayText = selectedItem.getDescription();
            font.draw(batch, displayText, textX, textY);
            batch.end();
        }
    }

    private void renderSlot(float x, float y, int slotIndex, boolean isSelected) {
        Item item = inventory.getItem(slotIndex);

        // Draw slot background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (isSelected) {
            shapeRenderer.setColor(0.3f, 0.6f, 0.9f, 0.8f); // Bright blue for selected
        } else {
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.7f); // Dark gray for unselected
        }
        shapeRenderer.rect(x, y, SLOT_SIZE, SLOT_SIZE);
        shapeRenderer.end();

        // Draw slot border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (isSelected) {
            shapeRenderer.setColor(0.0f, 1.0f, 1.0f, 1.0f); // Cyan border for selected
            shapeRenderer.rect(x, y, SLOT_SIZE, SLOT_SIZE);
            shapeRenderer.rect(x - 2, y - 2, SLOT_SIZE + 4, SLOT_SIZE + 4);
        } else {
            shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.8f); // Gray border for unselected
        }
        shapeRenderer.rect(x, y, SLOT_SIZE, SLOT_SIZE);
        shapeRenderer.end();

        // Draw item indicator
        if (item != null) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            Color itemColor = getItemColor(item);
            shapeRenderer.setColor(itemColor);
            // Draw smaller square in center to indicate item presence
            float padding = 8f;
            shapeRenderer.rect(x + padding, y + padding, SLOT_SIZE - padding * 2, SLOT_SIZE - padding * 2);
            shapeRenderer.end();
        }

        // Draw slot number (1-8 left to right)
        batch.begin();
        font.setColor(Color.WHITE);
        String slotNumber = String.valueOf(slotIndex + 1);  // 1-based slot number
        font.draw(batch, slotNumber, x + SLOT_SIZE - 12f, y + 12f);
        batch.end();
    }

    private Color getItemColor(Item item) {
        switch (item.getType()) {
            case WATERING_CAN:
                return new Color(0.2f, 0.7f, 1.0f, 0.9f); // Light blue
            case SEED:
                return new Color(0.8f, 0.6f, 0.2f, 0.9f); // Brown
            case FERTILIZER:
                return new Color(0.9f, 0.7f, 0.1f, 0.9f); // Orange
            default:
                return new Color(0.5f, 0.5f, 0.5f, 0.5f); // Gray
        }
    }

    /**
     * Dispose of resources.
     */
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}


