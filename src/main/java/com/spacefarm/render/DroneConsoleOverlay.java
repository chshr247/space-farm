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

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.Gdx.graphics;

/**
 * UI for interacting with the Drone (Selling crystals and Upgrades).
 * Features smooth animations and scrollable upgrades.
 */
public class DroneConsoleOverlay {
    private final GameSession session;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final BitmapFont smallFont;
    private final BitmapFont titleFont;
    private final OrthographicCamera screenCamera;
    private final GlyphLayout layout = new GlyphLayout();

    private boolean visible = false;
    private int activeTab = 0; // 0 = Sell, 1 = Upgrades

    // Animation states
    private enum DroneState { IDLE, FLYING_AWAY, RETURNING }
    private DroneState droneState = DroneState.IDLE;
    private float animationTimer = 0f;
    private static final float FLIGHT_DURATION = 5.0f;
    private float pendingBalanceUpdate = 0f;
    private float scale = 0f;

    private final float width = 500f;
    private final float height = 450f;
    private float x, y;

    private int tradeSlotCount = 0;
    private static final float SLOT_SIZE = 64f;
    private static final int CRYSTAL_PRICE = 50;

    // Scrolling
    private float scrollY = 0;
    private float maxScrollY = 0;
    private static final float LIST_VIEWPORT_HEIGHT = 300f;

    // Upgrades
    private static class UpgradeItem {
        String id;
        String name;
        String description;
        float cost;
        int level;
        int maxLevel;

        UpgradeItem(String id, String name, String description, float cost, int maxLevel) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.cost = cost;
            this.maxLevel = maxLevel;
            this.level = 0;
        }

        boolean isMaxed() {
            return level >= maxLevel;
        }
    }

    private final List<UpgradeItem> treeUpgrades = new ArrayList<>();
    private final List<UpgradeItem> baseUpgrades = new ArrayList<>();

    public DroneConsoleOverlay(GameSession session) {
        this.session = session;
        this.shapeRenderer = new ShapeRenderer();
        this.batch = new SpriteBatch();
        this.font = FontUtils.createFont("fonts/ArialBold.ttf", 20);
        this.smallFont = FontUtils.createFont("fonts/ArialBold.ttf", 14);
        this.titleFont = FontUtils.createFont("fonts/ArialBold.ttf", 28);
        
        this.screenCamera = new OrthographicCamera();
        this.screenCamera.setToOrtho(false, graphics.getWidth(), graphics.getHeight());

        initializeUpgrades();
        calculateMaxScroll();
    }

    private void initializeUpgrades() {
        treeUpgrades.add(new UpgradeItem("tree_growth", "Root Expansion", "Faster crop growth", 200, 1));
        treeUpgrades.add(new UpgradeItem("tree_oxygen", "Leaf Density", "Better oxygen generation", 350, 1));
        treeUpgrades.add(new UpgradeItem("tree_water", "Hydration Core", "Slower evaporation", 500, 1));
        treeUpgrades.add(new UpgradeItem("tree_rare", "Rare Bloom", "Rare seed chance +10%", 750, 1));
        treeUpgrades.add(new UpgradeItem("tree_final", "Tree of Life", "Max oxygen +50%", 1500, 1));

        baseUpgrades.add(new UpgradeItem("base_bed", "Garden Bed #1", "Adds 1 garden bed", 150, 1));
        baseUpgrades.add(new UpgradeItem("base_bed_2", "Garden Bed #2", "Adds 1 garden bed", 300, 1));
        baseUpgrades.add(new UpgradeItem("base_oxygen", "Oxygen Sealer", "Slower oxygen loss", 400, 1));
        baseUpgrades.add(new UpgradeItem("base_scavenge", "Drone Scanner", "Faster scavenging", 600, 1));
        baseUpgrades.add(new UpgradeItem("base_inv", "Backpack Mod", "Larger inventory", 800, 1));
        baseUpgrades.add(new UpgradeItem("base_shield", "Crystal Shield", "Crops never die", 2000, 1));
    }

    private void calculateMaxScroll() {
        float totalH = 30 + (treeUpgrades.size() * 55) + 40 + (baseUpgrades.size() * 55);
        maxScrollY = Math.max(0, totalH - LIST_VIEWPORT_HEIGHT);
    }

    public void setVisible(boolean visible) {
        if (this.visible == visible) return;
        this.visible = visible;
        if (visible) {
            scrollY = 0;
        } else {
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
        if (visible && scale < 1f) {
            scale = Math.min(1f, scale + deltaTime * 6f);
        } else if (!visible && scale > 0f) {
            scale = Math.max(0f, scale - deltaTime * 6f);
        }

        if (droneState != DroneState.IDLE) {
            animationTimer += deltaTime;
            float progress = animationTimer / FLIGHT_DURATION;
            float worldWidth = session.getBaseLayer().getWidth() * session.getBaseLayer().getTileWidth();
            float worldHeight = session.getBaseLayer().getHeight() * session.getBaseLayer().getTileHeight();
            float startX = session.getBaseZone().getDroneZoneCenter().x() * session.getBaseLayer().getTileWidth();
            float startY = session.getBaseZone().getDroneZoneCenter().y() * session.getBaseLayer().getTileHeight();
            float targetX = worldWidth + 1000f;
            float targetY = worldHeight + 1000f;
            float totalDistX = targetX - startX;
            float totalDistY = targetY - startY;

            if (droneState == DroneState.FLYING_AWAY) {
                session.getBaseZone().setDroneOffsets(progress * totalDistX, progress * totalDistY);
            } else {
                session.getBaseZone().setDroneOffsets((1f - progress) * totalDistX, (1f - progress) * totalDistY);
            }

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

        float drawWidth = width * scale;
        float drawHeight = height * scale;
        x = (screenWidth - drawWidth) / 2f;
        y = (screenHeight - drawHeight) / 2f;

        shapeRenderer.setProjectionMatrix(screenCamera.combined);
        batch.setProjectionMatrix(screenCamera.combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.12f, 0.15f, 0.95f * scale);
        shapeRenderer.rect(x, y, drawWidth, drawHeight);

        if (scale > 0.8f) {
            shapeRenderer.setColor(0.2f, 0.25f, 0.3f, 1f);
            shapeRenderer.rect(x, y + drawHeight - 50, drawWidth, 50);
            renderTabButton(x, y + drawHeight - 90, activeTab == 0);
            renderTabButton(x + drawWidth / 2, y + drawHeight - 90, activeTab == 1);
        }
        shapeRenderer.end();

        if (scale > 0.8f) {
            batch.begin();
            titleFont.setColor(1, 0.85f, 0, scale);
            titleFont.draw(batch, "DRONE CONSOLE", x + 20, y + drawHeight - 15);
            
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
        if (active) shapeRenderer.setColor(0.3f, 0.4f, 0.5f, 1f);
        else shapeRenderer.setColor(0.15f, 0.2f, 0.25f, 1f);
        shapeRenderer.rect(tx, ty, width * scale / 2, 40);
    }

    private void renderSellTab() {
        font.setColor(activeTab == 0 ? Color.WHITE : Color.GRAY);
        font.draw(batch, "SELL", x + (width*scale)/4 - 25, y + (height*scale) - 62);
        font.setColor(activeTab == 1 ? Color.WHITE : Color.GRAY);
        font.draw(batch, "UPGRADES", x + 3*(width*scale)/4 - 55, y + (height*scale) - 62);

        float slotX = x + (width*scale)/2 - SLOT_SIZE/2;
        float slotY = y + (height*scale)/2;

        if (droneState == DroneState.IDLE) {
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            shapeRenderer.rect(slotX, slotY, SLOT_SIZE, SLOT_SIZE);
            if (tradeSlotCount > 0) {
                shapeRenderer.setColor(0.4f, 0.8f, 1.0f, 0.95f);
                shapeRenderer.rect(slotX + 8, slotY + 8, SLOT_SIZE - 16, SLOT_SIZE - 16);
            }
            shapeRenderer.setColor(0.2f, 0.5f, 0.2f, 1f);
            shapeRenderer.rect(x + (width*scale)/2 - 60, y + 50, 120, 40);
            shapeRenderer.end();

            batch.begin();
            font.setColor(Color.WHITE);
            font.draw(batch, "Drop Crystals here:", x + (width*scale)/2 - 85, y + (height*scale)/2 + 100);
            if (tradeSlotCount > 0) {
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
            font.setColor(Color.GOLD);
            String status = droneState == DroneState.FLYING_AWAY ? "Drone delivering crystals..." : "Drone returning with payment...";
            layout.setText(font, status);
            font.draw(batch, status, x + (width*scale)/2 - layout.width/2, y + (height*scale)/2 + 40);
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
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

        float listX = x + 30;
        float startY = y + 70;
        float currentY = startY + (height*scale) - 130 - 70 + scrollY;
        float clipMinY = y + 20;
        float clipMaxY = y + (height*scale) - 100;

        // Tree Category
        if (currentY - 20 < clipMaxY && currentY + 10 > clipMinY) {
            font.setColor(Color.LIME);
            font.draw(batch, "--- TREE MODULES ---", listX, currentY);
        }
        currentY -= 35;
        for (UpgradeItem upg : treeUpgrades) {
            if (currentY - 40 < clipMaxY && currentY + 10 > clipMinY) renderUpgradeItem(upg, listX, currentY);
            currentY -= 55;
        }

        // Base Category
        currentY -= 15;
        if (currentY - 20 < clipMaxY && currentY + 10 > clipMinY) {
            font.setColor(Color.CYAN);
            font.draw(batch, "--- BASE IMPROVEMENTS ---", listX, currentY);
        }
        currentY -= 35;
        for (UpgradeItem upg : baseUpgrades) {
            if (currentY - 40 < clipMaxY && currentY + 10 > clipMinY) renderUpgradeItem(upg, listX, currentY);
            currentY -= 55;
        }

        // Scrollbar track
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 1f);
        shapeRenderer.rect(x + width - 15, y + 20, 10, LIST_VIEWPORT_HEIGHT);
        // Thumb
        if (maxScrollY > 0) {
            shapeRenderer.setColor(Color.GRAY);
            float thumbH = Math.max(20, (LIST_VIEWPORT_HEIGHT / (maxScrollY + LIST_VIEWPORT_HEIGHT)) * LIST_VIEWPORT_HEIGHT);
            float thumbPos = (scrollY / maxScrollY) * (LIST_VIEWPORT_HEIGHT - thumbH);
            shapeRenderer.rect(x + width - 15, y + 20 + LIST_VIEWPORT_HEIGHT - thumbH - thumbPos, 10, thumbH);
        }
        shapeRenderer.end();
        batch.begin();
    }

    private void renderUpgradeItem(UpgradeItem upg, float ix, float iy) {
        font.setColor(Color.WHITE);
        String text = upg.name + (upg.isMaxed() ? "" : " - $" + (int)upg.cost);
        font.draw(batch, text, ix, iy);
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, upg.description, ix + 10, iy - 22);

        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (upg.isMaxed()) shapeRenderer.setColor(0.25f, 0.25f, 0.25f, 1f);
        else if (session.getWallet().getBalance() >= upg.cost) shapeRenderer.setColor(0.2f, 0.6f, 0.2f, 1f);
        else shapeRenderer.setColor(0.6f, 0.2f, 0.2f, 1f);
        
        float btnX = x + width - 110;
        float btnY = iy - 28;
        float btnW = 80;
        float btnH = 32;
        shapeRenderer.rect(btnX, btnY, btnW, btnH);
        shapeRenderer.end();
        
        batch.begin();
        String btnText = upg.isMaxed() ? "OWNED" : "BUY";
        layout.setText(smallFont, btnText);
        smallFont.setColor(Color.WHITE);
        smallFont.draw(batch, btnText, btnX + (btnW - layout.width)/2, btnY + (btnH + layout.height)/2);
    }

    public boolean handleTouchDown(float screenX, float screenY) {
        if (!visible || scale < 0.9f || droneState != DroneState.IDLE) return false;
        float worldX = screenX;
        float worldY = graphics.getHeight() - screenY;

        if (worldX < x || worldX > x + width || worldY < y || worldY > y + height) return false;

        if (worldY >= y + height - 90 && worldY <= y + height - 50) {
            if (worldX >= x && worldX < x + width/2) activeTab = 0;
            else if (worldX >= x + width/2 && worldX <= x + width) activeTab = 1;
            return true;
        }

        if (activeTab == 0) {
            if (tradeSlotCount > 0 && worldX >= x + width/2 - 60 && worldX <= x + width/2 + 60 &&
                worldY >= y + 50 && worldY <= y + 90) {
                sellItem();
                return true;
            }
        } else {
            handleUpgradeClicks(worldX, worldY);
        }
        return true; 
    }

    private void handleUpgradeClicks(float wx, float wy) {
        if (wy < y + 20 || wy > y + height - 100) return;

        float listX = x + 30;
        float startY = y + 70;
        float currentY = startY + (height*scale) - 130 - 70 + scrollY;
        
        currentY -= 35; // Tree Header
        for (UpgradeItem upg : treeUpgrades) {
            if (isBuyButtonClicked(wx, wy, currentY)) { buyUpgrade(upg); return; }
            currentY -= 55;
        }
        currentY -= 15 + 35; // Base Header
        for (UpgradeItem upg : baseUpgrades) {
            if (isBuyButtonClicked(wx, wy, currentY)) { buyUpgrade(upg); return; }
            currentY -= 55;
        }
    }

    private boolean isBuyButtonClicked(float wx, float wy, float itemY) {
        return wx >= x + width - 110 && wx <= x + width - 30 &&
               wy >= itemY - 28 && wy <= itemY + 4;
    }

    private void buyUpgrade(UpgradeItem upg) {
        if (!upg.isMaxed() && session.getWallet().getBalance() >= upg.cost) {
            session.getWallet().spend(upg.cost);
            upg.level++;
            Gdx.app.log("DroneConsole", "Purchased: " + upg.name);
        }
    }

    public boolean handleScrolled(float amount) {
        if (!visible || activeTab != 1) return false;
        scrollY = Math.max(0, Math.min(maxScrollY, scrollY + amount * 30f));
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
        if (droneState == DroneState.IDLE) this.tradeSlotCount++;
    }

    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        smallFont.dispose();
        titleFont.dispose();
    }
}
