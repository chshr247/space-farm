package com.spacefarm.session;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.spacefarm.inventory.Crystal;
import com.spacefarm.inventory.Item;
import com.spacefarm.inventory.LegendarySeed;
import com.spacefarm.inventory.PlantFood;
import com.spacefarm.inventory.RareSeed;
import com.spacefarm.inventory.Seed;
import com.spacefarm.oxygen.OxygenConstants;
import com.spacefarm.world.ScavengingLocation;
import com.spacefarm.world.SeedWheelConstants;
import com.spacefarm.world.TileCoord;

public class GameInteractionService {
    private final GameSession session;
    private TileCoord lastSelected;
    private ScavengingLocation currentSeedWheelLocation;

    public GameInteractionService(GameSession session) {
        this.session = session;
    }

    public void update(float deltaTime) {
        session.getSeedWheelOverlay().update(deltaTime);

        if (session.getSeedWheelOverlay().hasResult()) {
            int resultType = session.getSeedWheelOverlay().getResultAndReset();
            handleSeedWheelResult(resultType);

            if (currentSeedWheelLocation != null) {
                currentSeedWheelLocation.completeScavenging();
                currentSeedWheelLocation = null;
                session.getSeedWheelOverlay().setVisible(false);
            }
        }

        if (session.getOxygenManager().isOxygenDepleted() && !session.isGameOver()) {
            session.setGameOver(true);
        }

        if (session.isGameOver()) {
            return;
        }

        session.getFarmingSystem().update(deltaTime);
        session.getOxygenManager().update(deltaTime);
        updateScavenging(deltaTime);
    }

    public boolean handleTouchDown(int screenX, int screenY, int button) {
        if (session.isGameOver()) {
            return false;
        }

        if (button == Buttons.LEFT) {
            // Check inventory first so we can start dragging items while console is open
            if (session.getInventoryUI().handleTouchDown(screenX, screenY)) {
                return true;
            }
        }

        if (session.getDroneConsoleOverlay().isVisible()) {
            if (session.getDroneConsoleOverlay().handleTouchDown(screenX, screenY)) {
                return true;
            }
            // If we clicked outside the console (and not on inventory), close it
            session.getDroneConsoleOverlay().setVisible(false);
            return true;
        }

        if (session.getSeedWheelOverlay().isVisible()) {
            if (button == Buttons.LEFT) {
                float adjustedY = Gdx.graphics.getHeight() - screenY;
                if (session.getSeedWheelOverlay().isButtonHit(screenX, adjustedY)) {
                    session.getSeedWheelOverlay().startSpin();
                    return true;
                }
            }
            return false;
        }

        if (button == Buttons.RIGHT) {
            showContextMenu(screenX, screenY);
            return true;
        }

        session.getContextMenu().hide();
        handleTileClick(screenX, screenY);
        return true;
    }

    public boolean handleTouchDragged(int screenX, int screenY) {
        if (session.isGameOver()) {
            return false;
        }
        return session.getInventoryUI().handleTouchDragged(screenX, screenY);
    }

    public boolean handleTouchUp(int screenX, int screenY, int button) {
        if (session.isGameOver()) {
            return false;
        }
        if (button == Buttons.LEFT) {
            int draggedSlot = session.getInventoryUI().getDraggedSlotIndex();
            if (draggedSlot != -1) {
                int targetSlot = session.getInventoryUI().getTargetSlot(screenX, screenY);
                session.getInventoryUI().handleTouchUp(screenX, screenY);

                if (targetSlot == -1) {
                    // Dropped outside inventory
                    if (session.getDroneConsoleOverlay().isOverTradeSlot(screenX, screenY)) {
                        Item item = session.getInventory().getItem(draggedSlot);
                        if (item != null && item.getType() == Item.ItemType.CRYSTAL) {
                            session.getDroneConsoleOverlay().addCrystal();
                            session.getInventory().removeItem(draggedSlot);
                            return true;
                        }
                    }

                    // Apply to tile if not console
                    int prevSelected = session.getInventory().getSelectedSlot();
                    session.getInventory().selectSlot(draggedSlot);
                    handleTileClick(screenX, screenY);
                    
                    session.getInventory().selectSlot(prevSelected);
                }
                return true;
            }
            return session.getInventoryUI().handleTouchUp(screenX, screenY);
        }
        return false;
    }

    public boolean handleKeyDown(int keycode) {
        if (keycode >= Keys.NUM_1 && keycode <= Keys.NUM_8) {
            int slotIndex = keycode - Keys.NUM_1;
            session.getInventory().selectSlot(slotIndex);
            return true;
        }

        if (keycode == Keys.E) {
            Item selected = session.getInventory().getSelectedItem();
            if (selected != null) {
                if (selected.getType() == Item.ItemType.PLANT_FOOD) {
                    if (session.getInventory().consumePlantFood()) {
                        session.getOxygenManager().consumeFood();
                        removeSelectedStackIfEmpty();
                    }
                } else if (selected.getType() == Item.ItemType.RARE_SEED) {
                    RareSeed rareSeed = (RareSeed) selected;
                    if (rareSeed.useSeed()) {
                        float oxygenToAdd = OxygenConstants.MAX_OXYGEN * (SeedWheelConstants.RARE_SEED_OXYGEN_RESTORE / 100f);
                        session.getOxygenManager().setOxygen(session.getOxygenManager().getOxygen() + oxygenToAdd);
                        removeSelectedStackIfEmpty();
                    }
                } else if (selected.getType() == Item.ItemType.LEGENDARY_SEED) {
                    LegendarySeed legendarySeed = (LegendarySeed) selected;
                    if (legendarySeed.useSeed()) {
                        float oxygenToAdd = OxygenConstants.MAX_OXYGEN * (SeedWheelConstants.LEGENDARY_SEED_OXYGEN_RESTORE / 100f);
                        session.getOxygenManager().setOxygen(session.getOxygenManager().getOxygen() + oxygenToAdd);
                        removeSelectedStackIfEmpty();
                    }
                }
            }
            return true;
        }

        return false;
    }

    public boolean handleScrolled(float amountX, float amountY) {
        if (session.isGameOver()) return false;
        if (session.getDroneConsoleOverlay().isVisible()) {
            return session.getDroneConsoleOverlay().handleScrolled(amountY);
        }
        return false;
    }

    private void handleTileClick(int screenX, int screenY) {
        TileCoord coord = session.getTilePicker().screenToTile(screenX, screenY);
        if (coord == null) {
            return;
        }

        session.getOxygenManager().updatePositionTile(coord);

        if (session.getBaseZone().isDroneZone(coord)) {
            session.getDroneConsoleOverlay().setVisible(true);
            return;
        }

        if (lastSelected != null) {
            session.getSelectionLayer().setCell(lastSelected.x(), lastSelected.y(), null);
        }

        session.getSelectionLayer().setCell(coord.x(), coord.y(), session.createHighlightCell());
        lastSelected = coord;

        ScavengingLocation location = session.getOutdoorZone().getLocationAt(coord);
        if (location != null) {
            if (location.getLocationType() == ScavengingLocation.LocationType.SEED_WHEEL) {
                if (!location.isInCooldown()) {
                    session.getSeedWheelOverlay().setVisible(true);
                    currentSeedWheelLocation = location;
                }
            } else if (!location.isScavenging() && !location.isInCooldown()) {
                location.startScavenging();
            }
            return;
        }

        if (session.getInventory().isWateringCanSelected()) {
            if (session.getFarmingSystem().hasCrop(coord)) {
                session.getFarmingSystem().waterCrop(coord);
            }
        } else if (session.getInventory().isSeedSelected()) {
            if (!session.getBaseZone().isGardenBed(coord)) {
                // Посадка дозволена лише на грядках бази
                return;
            }
            if (!session.getFarmingSystem().hasCrop(coord) && session.getFarmingSystem().plantSeed(coord)) {
                session.getInventory().useSeed();
                removeSelectedStackIfEmpty();
            }
        } else if (session.getInventory().isSickleSelected()) {
            if (session.getFarmingSystem().hasCrop(coord) && session.getFarmingSystem().harvestCrop(coord)) {
                session.getInventory().addPlantFood(1);
            }
        }
    }

    private void showContextMenu(int screenX, int screenY) {
        TileCoord coord = session.getTilePicker().screenToTile(screenX, screenY);
        if (coord == null) {
            session.getContextMenu().hide();
            return;
        }

        float worldX = coord.x() * session.getBaseLayer().getTileWidth();
        float worldY = coord.y() * session.getBaseLayer().getTileHeight();
        session.getContextMenu().showAt(worldX, worldY);
    }

    private void handleSeedWheelResult(int resultType) {
        if (resultType == 0) {
            session.getInventory().addItem(new Seed(SeedWheelConstants.COMMON_SEED_REWARD));
        } else if (resultType == 1) {
            session.getInventory().addItem(new RareSeed(SeedWheelConstants.RARE_SEED_REWARD));
        } else if (resultType == 2) {
            session.getInventory().addItem(new LegendarySeed(SeedWheelConstants.LEGENDARY_SEED_REWARD));
        }
    }

    private void updateScavenging(float deltaTime) {
        for (ScavengingLocation location : session.getOutdoorZone().getScavengingLocations()) {
            if (location.isScavenging()) {
                session.getOxygenManager().consumeOxygenDuringScavenging(deltaTime);
                if (location.isScavengingComplete()) {
                    location.completeScavenging();
                    session.getInventory().addItem(new Crystal());
                }
            }
        }
    }

    private void removeSelectedStackIfEmpty() {
        int slot = session.getInventory().getSelectedSlot();
        Item selected = session.getInventory().getSelectedItem();
        if (selected == null) {
            return;
        }

        if (selected.getType() == Item.ItemType.SEED) {
            if (((Seed) selected).getQuantity() == 0) {
                session.getInventory().removeItem(slot);
            }
        } else if (selected.getType() == Item.ItemType.RARE_SEED) {
            if (((RareSeed) selected).getQuantity() == 0) {
                session.getInventory().removeItem(slot);
            }
        } else if (selected.getType() == Item.ItemType.LEGENDARY_SEED) {
            if (((LegendarySeed) selected).getQuantity() == 0) {
                session.getInventory().removeItem(slot);
            }
        } else if (selected.getType() == Item.ItemType.PLANT_FOOD) {
            if (((PlantFood) selected).getQuantity() == 0) {
                session.getInventory().removeItem(slot);
            }
        }
    }
}
