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
import com.spacefarm.world.OutdoorConstants;
import com.spacefarm.world.ScavengingLocation;
import com.spacefarm.world.SeedWheelConstants;
import com.spacefarm.world.TileCoord;
import com.spacefarm.farming.FarmingConstants;

public class GameInteractionService {
    private final GameSession session;
    private TileCoord lastSelected;
    private ScavengingLocation currentSeedWheelLocation;

    public GameInteractionService(GameSession session) {
        this.session = session;
    }

    public void update(float deltaTime) {
        session.getSeedWheelOverlay().update(deltaTime);
        session.getTreeBoxUI().update(deltaTime);

        // If player dismissed result modal → hasResult() is true → add items → hide overlay
        if (session.getSeedWheelOverlay().hasResult()) {
            FarmingConstants.CropType resultType = session.getSeedWheelOverlay().getResultAndReset();
            handleSeedWheelResult(resultType);

            if (currentSeedWheelLocation != null) {
                currentSeedWheelLocation.completeScavenging();
                currentSeedWheelLocation = null;
            }
            session.getSeedWheelOverlay().setVisible(false);
        }

        if (session.getOxygenManager().isOxygenDepleted() && !session.isGameOver() && !session.isVictory()) {
            session.setGameOver(true);
        }

        if (session.isGameOver() || session.isVictory()) {
            return;
        }

        session.getFarmingSystem().update(deltaTime);
        session.getOxygenManager().update(deltaTime);
        updateScavenging(deltaTime);
    }

    public boolean handleTouchDown(int screenX, int screenY, int button) {
        if (session.isGameOver() || session.isVictory()) {
            return false;
        }

        // TreeBoxUI intercepts all clicks when visible
        if (session.getTreeBoxUI().isVisible()) {
            int result = session.getTreeBoxUI().handleClick(screenX, screenY, Gdx.graphics.getHeight());

            if (result >= 0 && result < 5) {
                boolean canConfirm = session.getTreeBoxUI().isUnlocked(result)
                        && !session.getTreeBoxUI().isConfirmed(result)
                        && session.getInventory().hasTreePhaseItem(result);

                if (canConfirm) {
                    session.getInventory().removeTreePhaseItem(result);
                    session.getTreeBoxUI().confirmPhase(result);
                    session.getOutdoorZone().greenLocation(result);
                    session.getOutdoorZoneRenderer().applyGreenTiles(result);
                    
                    // Special cases for greening multiple areas
                    if (result == 1) {
                        // When "Жива Роса" is confirmed, green location index 5 (Seed Wheel) as well
                        session.getOutdoorZone().greenLocation(5);
                        session.getOutdoorZoneRenderer().applyGreenTiles(5);
                    }
                    
                    session.getBaseZone().expandZone(4);
                    session.getBaseZone().setTreePhase(session.getTreeBoxUI().getPhase());

                    // Victory check: if we just confirmed index 4 (5th phase "Ядро Едему")
                    if (result == 4) {
                        session.setVictory(true);
                    }
                }
            }

            return true;
        }

        if (button == Buttons.LEFT) {
            if (session.getInventoryUI().handleTouchDown(screenX, screenY)) {
                return true;
            }
        }

        if (session.getDroneConsoleOverlay().isVisible()) {
            if (session.getDroneConsoleOverlay().handleTouchDown(screenX, screenY)) {
                return true;
            }
            session.getDroneConsoleOverlay().setVisible(false);
            return true;
        }

        // ── Seed wheel overlay ─────────────────────────────────────────────────
        if (session.getSeedWheelOverlay().isVisible()) {
            if (button == Buttons.LEFT) {
                float adjustedY = Gdx.graphics.getHeight() - screenY;

                // 1. Result modal buttons ("ЗАБРАТИ" / "ЗАКРИТИ") — swallows the click
                if (session.getSeedWheelOverlay().handleTouchDown(screenX, adjustedY)) {
                    return true;
                }

                // 2. Spin button — only when modal is not shown
                if (session.getSeedWheelOverlay().isButtonHit(screenX, adjustedY)) {
                    session.getSeedWheelOverlay().startSpin();
                    session.getAudioManager().playWheelSound();
                    return true;
                }
            }
            return false;
        }
        // ──────────────────────────────────────────────────────────────────────

        if (button == Buttons.RIGHT) {
            showContextMenu(screenX, screenY);
            return true;
        }

        session.getContextMenu().hide();
        handleTileClick(screenX, screenY);
        return true;
    }

    public boolean handleTouchDragged(int screenX, int screenY) {
        if (session.isGameOver() || session.isVictory()) {
            return false;
        }
        return session.getInventoryUI().handleTouchDragged(screenX, screenY);
    }

    public boolean handleTouchUp(int screenX, int screenY, int button) {
        if (session.isGameOver() || session.isVictory()) {
            return false;
        }
        if (button == Buttons.LEFT) {
            int draggedSlot = session.getInventoryUI().getDraggedSlotIndex();
            if (draggedSlot != -1) {
                int targetSlot = session.getInventoryUI().getTargetSlot(screenX, screenY);
                session.getInventoryUI().handleTouchUp(screenX, screenY);

                if (targetSlot == -1) {
                    if (session.getDroneConsoleOverlay().isOverTradeSlot(screenX, screenY)) {
                        Item item = session.getInventory().getItem(draggedSlot);
                        if (item != null && item.getType() == Item.ItemType.CRYSTAL) {
                            session.getDroneConsoleOverlay().addCrystal();
                            session.getInventory().removeItem(draggedSlot);
                            return true;
                        }
                    }
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
        if (coord == null) return;
        
        Gdx.app.log("GameInteraction", "Clicked tile: " + coord.x() + ", " + coord.y());

        session.getOxygenManager().updatePositionTile(coord);

        if (session.getBaseZone().isDroneZone(coord)) {
            session.getDroneConsoleOverlay().setVisible(true);
            return;
        }

        if (lastSelected != null) {
            session.getSelectionLayer().setCell(lastSelected.x() - session.getWorldMinX(), lastSelected.y() - session.getWorldMinY(), null);
        }
        session.getSelectionLayer().setCell(coord.x() - session.getWorldMinX(), coord.y() - session.getWorldMinY(), session.createHighlightCell());
        lastSelected = coord;

        if (session.getBaseZone().isTreeArea(coord)) {
            session.getTreeBoxUI().show();
            return;
        }

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
                if (session.getFarmingSystem().waterCrop(coord)) {
                    session.getAudioManager().playWaterSound();
                }
            }
        } else if (session.getInventory().isSeedSelected()) {
            if (!session.getBaseZone().isGardenBed(coord)) return;

            Item selectedSeed = session.getInventory().getSelectedItem();
            FarmingConstants.CropType cropType = FarmingConstants.CropType.DEFAULT;

            if (selectedSeed != null) {
                if (selectedSeed.getType() == Item.ItemType.RARE_SEED) {
                    cropType = FarmingConstants.CropType.EPIC;
                } else if (selectedSeed.getType() == Item.ItemType.LEGENDARY_SEED) {
                    cropType = FarmingConstants.CropType.LEGENDARY;
                }
            }

            if (!session.getFarmingSystem().hasCrop(coord) && session.getFarmingSystem().plantSeed(coord, cropType)) {
                session.getInventory().useSeed();
                session.getAudioManager().playPlantSound();
                removeSelectedStackIfEmpty();
            }
        } else if (session.getInventory().isSickleSelected()) {
            if (session.getFarmingSystem().hasCrop(coord) && session.getFarmingSystem().harvestCrop(coord)) {
                session.getInventory().addPlantFood(1);
                session.getAudioManager().playHarvestSound();
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

    private void handleSeedWheelResult(FarmingConstants.CropType resultType) {
        if (resultType == FarmingConstants.CropType.DEFAULT) {
            session.getInventory().addItem(new Seed(SeedWheelConstants.COMMON_SEED_REWARD));
        } else if (resultType == FarmingConstants.CropType.EPIC) {
            session.getInventory().addItem(new RareSeed(SeedWheelConstants.RARE_SEED_REWARD));
        } else if (resultType == FarmingConstants.CropType.LEGENDARY) {
            session.getInventory().addItem(new LegendarySeed(SeedWheelConstants.LEGENDARY_SEED_REWARD));
        }
    }

    private void updateScavenging(float deltaTime) {
        // Use the constant duration for scavenging
        long durationMillis = OutdoorConstants.SCAVENGING_DURATION_MILLIS;

        for (ScavengingLocation location : session.getOutdoorZone().getScavengingLocations()) {
            if (location.isScavenging()) {
                session.getOxygenManager().consumeOxygenDuringScavenging(deltaTime, location.isGreened());
                if (location.isScavengingComplete(durationMillis)) {
                    location.completeScavenging();
                    session.getInventory().addItem(new Crystal());
                    Gdx.app.log("GameInteraction", "Scavenging complete! Crystal added.");
                }
            }
        }
    }

    private void removeSelectedStackIfEmpty() {
        int slot     = session.getInventory().getSelectedSlot();
        Item selected = session.getInventory().getSelectedItem();
        if (selected == null) return;

        if (selected.getType() == Item.ItemType.SEED) {
            if (((Seed) selected).getQuantity() == 0)
                session.getInventory().removeItem(slot);
        } else if (selected.getType() == Item.ItemType.RARE_SEED) {
            if (((RareSeed) selected).getQuantity() == 0)
                session.getInventory().removeItem(slot);
        } else if (selected.getType() == Item.ItemType.LEGENDARY_SEED) {
            if (((LegendarySeed) selected).getQuantity() == 0)
                session.getInventory().removeItem(slot);
        } else if (selected.getType() == Item.ItemType.PLANT_FOOD) {
            if (((PlantFood) selected).getQuantity() == 0)
                session.getInventory().removeItem(slot);
        }
    }
}