package com.spacefarm.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.spacefarm.inventory.Crystal;
import com.spacefarm.inventory.Item;
import com.spacefarm.session.GameSession;

import static com.badlogic.gdx.Gdx.graphics;

/**
 * UI for interacting with the Drone (Selling crystals and Upgrades).
 */
public class DroneConsoleOverlay {
    private final GameSession session;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final BitmapFont titleFont;
    private final OrthographicCamera screenCamera;
    private final GlyphLayout layout = new GlyphLayout();

    private boolean visible = false;
    private int activeTab = 0; // 0 = Sell, 1 = Upgrades

    private final float width = 500f;
    private final float height = 400f;
    private float x, y;

    private int tradeSlotCount = 0;
    private static final float SLOT_SIZE = 64f;
    private static final int CRYSTAL_PRICE = 50;

    public DroneConsoleOverlay(GameSession session) {
        this.session = session;
        this.shapeRenderer = new ShapeRenderer();
        this.batch = new SpriteBatch();
        this.font = FontUtils.createFont("fonts/ArialBold.ttf", 20);
        this.titleFont = FontUtils.createFont("fonts/ArialBold.ttf", 28);
        
        this.screenCamera = new OrthographicCamera();
        this.screenCamera.setToOrtho(false, graphics.getWidth(), graphics.getHeight());
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (!visible) {
            // Return crystals to inventory if closing
            while (tradeSlotCount > 0) {
                if (session.getInventory().addItem(new Crystal())) {
                    tradeSlotCount--;
                } else {
                    // Inventory full, stop returning
                    break;
                }
            }
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void render() {
        if (!visible) return;

        int screenWidth = graphics.getWidth();
        int screenHeight = graphics.getHeight();
        screenCamera.setToOrtho(false, screenWidth, screenHeight);
        screenCamera.update();

        x = (screenWidth - width) / 2f;
        y = (screenHeight - height) / 2f;

        shapeRenderer.setProjectionMatrix(screenCamera.combined);
        batch.setProjectionMatrix(screenCamera.combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Background
        shapeRenderer.setColor(0.1f, 0.12f, 0.15f, 0.95f);
        shapeRenderer.rect(x, y, width, height);

        // Header
        shapeRenderer.setColor(0.2f, 0.25f, 0.3f, 1f);
        shapeRenderer.rect(x, y + height - 50, width, 50);

        // Tabs
        renderTabButton(x, y + height - 90, "SELL", activeTab == 0);
        renderTabButton(x + width/2, y + height - 90, "UPGRADES", activeTab == 1);

        shapeRenderer.end();

        batch.begin();
        titleFont.setColor(Color.GOLD);
        titleFont.draw(batch, "DRONE CONSOLE", x + 20, y + height - 15);
        
        // Balance
        font.setColor(Color.WHITE);
        String balanceText = String.format("Balance: $%.1f", session.getWallet().getBalance());
        layout.setText(font, balanceText);
        font.draw(batch, balanceText, x + width - layout.width - 20, y + height - 20);

        if (activeTab == 0) {
            renderSellTab();
        } else {
            renderUpgradesTab();
        }
        batch.end();
        
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderTabButton(float tx, float ty, String label, boolean active) {
        if (active) {
            shapeRenderer.setColor(0.3f, 0.4f, 0.5f, 1f);
        } else {
            shapeRenderer.setColor(0.15f, 0.2f, 0.25f, 1f);
        }
        shapeRenderer.rect(tx, ty, width / 2, 40);
    }

    private void renderSellTab() {
        // Tab labels
        font.setColor(activeTab == 0 ? Color.WHITE : Color.GRAY);
        font.draw(batch, "SELL", x + width/4 - 25, y + height - 62);
        font.setColor(activeTab == 1 ? Color.WHITE : Color.GRAY);
        font.draw(batch, "UPGRADES", x + 3*width/4 - 55, y + height - 62);

        // Slot for crystal
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        float slotX = x + width/2 - SLOT_SIZE/2;
        float slotY = y + height/2;
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(slotX, slotY, SLOT_SIZE, SLOT_SIZE);
        
        if (tradeSlotCount > 0) {
            shapeRenderer.setColor(0.4f, 0.8f, 1.0f, 0.95f);
            shapeRenderer.rect(slotX + 8, slotY + 8, SLOT_SIZE - 16, SLOT_SIZE - 16);
        }
        
        // Sell Button
        shapeRenderer.setColor(0.2f, 0.5f, 0.2f, 1f);
        shapeRenderer.rect(x + width/2 - 60, y + 50, 120, 40);
        shapeRenderer.end();

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "Drop Crystals here:", x + width/2 - 85, y + height/2 + 100);
        
        if (tradeSlotCount > 0) {
            font.setColor(Color.WHITE);
            font.draw(batch, "x" + tradeSlotCount, slotX + SLOT_SIZE - 25, slotY + 20);
            
            font.setColor(Color.GREEN);
            font.draw(batch, "Total Value: $" + (tradeSlotCount * CRYSTAL_PRICE), x + width/2 - 65, y + height/2 - 20);
        } else {
            font.setColor(Color.GRAY);
            font.draw(batch, "Value: $0", x + width/2 - 40, y + height/2 - 20);
        }

        font.draw(batch, "SELL", x + width/2 - 25, y + 78);
    }

    private void renderUpgradesTab() {
        font.setColor(activeTab == 0 ? Color.WHITE : Color.GRAY);
        font.draw(batch, "SELL", x + width/4 - 25, y + height - 62);
        font.setColor(activeTab == 1 ? Color.WHITE : Color.GRAY);
        font.draw(batch, "UPGRADES", x + 3*width/4 - 55, y + height - 62);

        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "Coming soon...", x + width/2 - 60, y + height/2);
    }

    public boolean handleTouchDown(float screenX, float screenY) {
        if (!visible) return false;

        float worldX = screenX;
        float worldY = graphics.getHeight() - screenY;

        // Check if click is inside console
        if (worldX < x || worldX > x + width || worldY < y || worldY > y + height) {
            return false;
        }

        // Tabs
        if (worldY >= y + height - 90 && worldY <= y + height - 50) {
            if (worldX >= x && worldX < x + width/2) activeTab = 0;
            else if (worldX >= x + width/2 && worldX <= x + width) activeTab = 1;
            return true;
        }

        // Sell Button
        if (activeTab == 0 && tradeSlotCount > 0) {
            if (worldX >= x + width/2 - 60 && worldX <= x + width/2 + 60 &&
                worldY >= y + 50 && worldY <= y + 90) {
                sellItem();
                return true;
            }
        }

        return true; 
    }

    private void sellItem() {
        if (tradeSlotCount > 0) {
            session.getWallet().earn(tradeSlotCount * CRYSTAL_PRICE);
            tradeSlotCount = 0;
        }
    }

    public boolean isOverTradeSlot(float screenX, float screenY) {
        if (!visible || activeTab != 0) return false;
        float worldX = screenX;
        float worldY = graphics.getHeight() - screenY;
        float slotX = x + width/2 - SLOT_SIZE/2;
        float slotY = y + height/2;
        return worldX >= slotX && worldX <= slotX + SLOT_SIZE &&
               worldY >= slotY && worldY <= slotY + SLOT_SIZE;
    }

    public void addCrystal() {
        this.tradeSlotCount++;
    }

    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        titleFont.dispose();
    }
}
