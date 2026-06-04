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
 * Features smooth animations for window opening and drone delivery.
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

    // Animation states
    private enum DroneState { IDLE, FLYING_AWAY, RETURNING }
    private DroneState droneState = DroneState.IDLE;
    private float animationTimer = 0f;
    private static final float FLIGHT_DURATION = 5.0f; // 5s away, 5s back = 10s total
    private float pendingBalanceUpdate = 0f;
    private float scale = 0f;

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
        if (this.visible == visible) return;
        this.visible = visible;
        if (!visible) {
            // Return crystals to inventory if closing and not in flight
            if (droneState == DroneState.IDLE) {
                while (tradeSlotCount > 0) {
                    if (session.getInventory().addItem(new Crystal())) {
                        tradeSlotCount--;
                    } else {
                        break;
                    }
                }
            }
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void update(float deltaTime) {
        // Window open/close animation
        if (visible && scale < 1f) {
            scale = Math.min(1f, scale + deltaTime * 6f);
        } else if (!visible && scale > 0f) {
            scale = Math.max(0f, scale - deltaTime * 6f);
        }

        // Drone flight animation
        if (droneState != DroneState.IDLE) {
            animationTimer += deltaTime;
            
            float progress = animationTimer / FLIGHT_DURATION;
            
            // Calculate distance to fly beyond map edge
            float worldWidth = session.getBaseLayer().getWidth() * session.getBaseLayer().getTileWidth();
            float worldHeight = session.getBaseLayer().getHeight() * session.getBaseLayer().getTileHeight();
            
            // Current drone world position (base)
            float startX = session.getBaseZone().getDroneZoneCenter().x() * session.getBaseLayer().getTileWidth();
            float startY = session.getBaseZone().getDroneZoneCenter().y() * session.getBaseLayer().getTileHeight();
            
            // Target is far beyond corner of map
            float targetX = worldWidth + 1000f;
            float targetY = worldHeight + 1000f;
            
            float totalDistX = targetX - startX;
            float totalDistY = targetY - startY;

            float offX = 0;
            float offY = 0;
            
            if (droneState == DroneState.FLYING_AWAY) {
                offX = progress * totalDistX;
                offY = progress * totalDistY;
            } else {
                offX = (1f - progress) * totalDistX;
                offY = (1f - progress) * totalDistY;
            }
            session.getBaseZone().setDroneOffsets(offX, offY);

            if (animationTimer >= FLIGHT_DURATION) {
                animationTimer = 0;
                if (droneState == DroneState.FLYING_AWAY) {
                    droneState = DroneState.RETURNING;
                } else {
                    droneState = DroneState.IDLE;
                    session.getBaseZone().setDroneOffsets(0, 0);
                    session.getWallet().earn(pendingBalanceUpdate);
                    pendingBalanceUpdate = 0;
                }
            }
        }
    }

    public void render() {
        if (!visible && scale <= 0) return;

        int screenWidth = graphics.getWidth();
        int screenHeight = graphics.getHeight();
        screenCamera.setToOrtho(false, screenWidth, screenHeight);
        screenCamera.update();

        // Apply scale to dimensions
        float drawWidth = width * scale;
        float drawHeight = height * scale;
        x = (screenWidth - drawWidth) / 2f;
        y = (screenHeight - drawHeight) / 2f;

        shapeRenderer.setProjectionMatrix(screenCamera.combined);
        batch.setProjectionMatrix(screenCamera.combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Background with fade
        shapeRenderer.setColor(0.1f, 0.12f, 0.15f, 0.95f * scale);
        shapeRenderer.rect(x, y, drawWidth, drawHeight);

        // Header (only if enough scale)
        if (scale > 0.8f) {
            shapeRenderer.setColor(0.2f, 0.25f, 0.3f, 1f);
            shapeRenderer.rect(x, y + drawHeight - 50, drawWidth, 50);

            // Tabs
            renderTabButton(x, y + drawHeight - 90, activeTab == 0);
            renderTabButton(x + drawWidth/2, y + drawHeight - 90, activeTab == 1);
        }

        shapeRenderer.end();

        if (scale > 0.8f) {
            batch.begin();
            titleFont.setColor(1, 0.85f, 0, scale);
            titleFont.draw(batch, "DRONE CONSOLE", x + 20, y + drawHeight - 15);
            
            // Balance
            font.setColor(1, 1, 1, scale);
            String balanceText = String.format("Balance: $%.1f", session.getWallet().getBalance());
            layout.setText(font, balanceText);
            font.draw(batch, balanceText, x + drawWidth - layout.width - 20, y + drawHeight - 20);

            if (activeTab == 0) {
                renderSellTab();
            } else {
                renderUpgradesTab();
            }
            batch.end();
        }
        
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderTabButton(float tx, float ty, boolean active) {
        if (active) {
            shapeRenderer.setColor(0.3f, 0.4f, 0.5f, 1f);
        } else {
            shapeRenderer.setColor(0.15f, 0.2f, 0.25f, 1f);
        }
        shapeRenderer.rect(tx, ty, width * scale / 2, 40);
    }

    private void renderSellTab() {
        // Tab labels
        font.setColor(activeTab == 0 ? Color.WHITE : Color.GRAY);
        font.draw(batch, "SELL", x + (width*scale)/4 - 25, y + (height*scale) - 62);
        font.setColor(activeTab == 1 ? Color.WHITE : Color.GRAY);
        font.draw(batch, "UPGRADES", x + 3*(width*scale)/4 - 55, y + (height*scale) - 62);

        float slotX = x + (width*scale)/2 - SLOT_SIZE/2;
        float slotY = y + (height*scale)/2;

        if (droneState == DroneState.IDLE) {
            // Slot
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            shapeRenderer.rect(slotX, slotY, SLOT_SIZE, SLOT_SIZE);
            
            if (tradeSlotCount > 0) {
                shapeRenderer.setColor(0.4f, 0.8f, 1.0f, 0.95f);
                shapeRenderer.rect(slotX + 8, slotY + 8, SLOT_SIZE - 16, SLOT_SIZE - 16);
            }
            
            // Sell Button
            shapeRenderer.setColor(0.2f, 0.5f, 0.2f, 1f);
            shapeRenderer.rect(x + (width*scale)/2 - 60, y + 50, 120, 40);
            shapeRenderer.end();

            batch.begin();
            font.setColor(Color.WHITE);
            font.draw(batch, "Drop Crystals here:", x + (width*scale)/2 - 85, y + (height*scale)/2 + 100);
            
            if (tradeSlotCount > 0) {
                font.setColor(Color.WHITE);
                font.draw(batch, "x" + tradeSlotCount, slotX + SLOT_SIZE - 25, slotY + 20);
                font.setColor(Color.GREEN);
                font.draw(batch, "Total Value: $" + (tradeSlotCount * CRYSTAL_PRICE), x + (width*scale)/2 - 65, y + (height*scale)/2 - 20);
            } else {
                font.setColor(Color.GRAY);
                font.draw(batch, "Value: $0", x + (width*scale)/2 - 40, y + (height*scale)/2 - 20);
            }
            font.setColor(Color.WHITE);
            font.draw(batch, "SELL", x + (width*scale)/2 - 25, y + 78);
        } else {
            // Animation state
            font.setColor(Color.GOLD);
            String status = droneState == DroneState.FLYING_AWAY ? "Drone delivering crystals..." : "Drone returning with payment...";
            font.draw(batch, status, x + (width*scale)/2 - 110, y + (height*scale)/2 + 40);
            
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            // Progress Bar
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            float barW = (width*scale) - 160;
            shapeRenderer.rect(x + 80, y + (height*scale)/2 - 10, barW, 12);
            
            shapeRenderer.setColor(Color.GOLD);
            float progress = animationTimer / FLIGHT_DURATION;
            if (droneState == DroneState.RETURNING) progress = 1f - progress;
            shapeRenderer.rect(x + 80, y + (height*scale)/2 - 10, barW * progress, 12);
            shapeRenderer.end();
            batch.begin();
        }
    }

    private void renderUpgradesTab() {
        font.setColor(activeTab == 0 ? Color.WHITE : Color.GRAY);
        font.draw(batch, "SELL", x + (width*scale)/4 - 25, y + (height*scale) - 62);
        font.setColor(activeTab == 1 ? Color.WHITE : Color.GRAY);
        font.draw(batch, "UPGRADES", x + 3*(width*scale)/4 - 55, y + (height*scale) - 62);

        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "Upgrades module offline", x + (width*scale)/2 - 100, y + (height*scale)/2);
    }

    public boolean handleTouchDown(float screenX, float screenY) {
        if (!visible || scale < 0.9f || droneState != DroneState.IDLE) return false;

        float worldX = screenX;
        float worldY = graphics.getHeight() - screenY;

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
        if (tradeSlotCount > 0 && droneState == DroneState.IDLE) {
            pendingBalanceUpdate = tradeSlotCount * CRYSTAL_PRICE;
            tradeSlotCount = 0;
            droneState = DroneState.FLYING_AWAY;
            animationTimer = 0;
        }
    }

    public boolean isOverTradeSlot(float screenX, float screenY) {
        if (!visible || activeTab != 0 || droneState != DroneState.IDLE) return false;
        float worldX = screenX;
        float worldY = graphics.getHeight() - screenY;
        float slotX = x + width/2 - SLOT_SIZE/2;
        float slotY = y + height/2;
        return worldX >= slotX && worldX <= slotX + SLOT_SIZE &&
               worldY >= slotY && worldY <= slotY + SLOT_SIZE;
    }

    public void addCrystal() {
        if (droneState == DroneState.IDLE) {
            this.tradeSlotCount++;
        }
    }

    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        titleFont.dispose();
    }
}
