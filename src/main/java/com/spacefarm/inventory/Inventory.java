package com.spacefarm.inventory;

/**
 * Manages the player's inventory with 8 slots.
 */
public class Inventory {
    private static final int INVENTORY_SIZE = 8;
    private final Item[] slots;
    private int selectedSlot;

    public Inventory() {
        this.slots = new Item[INVENTORY_SIZE];
        this.selectedSlot = 0;

        // Initialize with a watering can in the first slot
        slots[0] = WateringCan.getInstance();
    }

    /**
     * Select a slot by index (0-7).
     */
    public void selectSlot(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < INVENTORY_SIZE) {
            selectedSlot = slotIndex;
        }
    }

    /**
     * Get the currently selected slot.
     */
    public int getSelectedSlot() {
        return selectedSlot;
    }

    /**
     * Get the item in the selected slot.
     */
    public Item getSelectedItem() {
        return slots[selectedSlot];
    }

    /**
     * Get the item in a specific slot.
     */
    public Item getItem(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < INVENTORY_SIZE) {
            return slots[slotIndex];
        }
        return null;
    }

    /**
     * Add an item to a specific slot.
     */
    public boolean addItem(int slotIndex, Item item) {
        if (slotIndex >= 0 && slotIndex < INVENTORY_SIZE && slots[slotIndex] == null) {
            slots[slotIndex] = item;
            return true;
        }
        return false;
    }

    /**
     * Remove an item from a specific slot.
     */
    public Item removeItem(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < INVENTORY_SIZE) {
            Item item = slots[slotIndex];
            slots[slotIndex] = null;
            return item;
        }
        return null;
    }

    /**
     * Get the size of inventory.
     */
    public int getSize() {
        return INVENTORY_SIZE;
    }

    /**
     * Check if the selected item is a watering can.
     */
    public boolean isWateringCanSelected() {
        Item selected = getSelectedItem();
        return selected != null && selected.getType() == Item.ItemType.WATERING_CAN;
    }

    /**
     * Check if the selected item is seeds.
     */
    public boolean isSeedSelected() {
        Item selected = getSelectedItem();
        if (selected != null && selected.getType() == Item.ItemType.SEED) {
            Seed seed = (Seed) selected;
            return seed.getQuantity() > 0;  // Only return true if seeds are available
        }
        return false;
    }

    /**
     * Use a seed from the selected slot if it's seeds.
     * Returns true if a seed was used, false otherwise.
     */
    public boolean useSeed() {
        Item selected = getSelectedItem();
        if (selected != null && selected.getType() == Item.ItemType.SEED) {
            Seed seed = (Seed) selected;
            return seed.useSeed();
        }
        return false;
    }

    /**
     * Get all slots.
     */
    public Item[] getSlots() {
        return slots;
    }
}

