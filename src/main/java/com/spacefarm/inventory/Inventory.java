package com.spacefarm.inventory;

// малий інвентар на 8 слотів
public class Inventory {
    private int inventorySize = 8;
    private static final int ROW_SIZE = 8;
    private Item[] slots;
    private int selectedSlot;

    public Inventory() {
        this.slots = new Item[inventorySize];
        this.selectedSlot = 0;
        // ініціалізація лійки в перший слот
        slots[0] = WateringCan.getInstance();

    }

    public void setSlots(Item[] newSlots) {
        if (newSlots != null) {
            this.slots = newSlots;
            this.inventorySize = newSlots.length;
        }
    }
    // Розширення інвентарю додаванням ще одного ряду слотів
    public void expandInventory() {
        int newSize = this.inventorySize + ROW_SIZE;
        Item[] newSlots = new Item[newSize];
        System.arraycopy(this.slots, 0, newSlots, 0, this.slots.length);
        this.slots = newSlots;
        this.inventorySize = newSize;
    }

    public void selectSlot(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < inventorySize) {
            selectedSlot = slotIndex;
        }
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }
    // отримати об'єкт з обраного слоту зі змінної
    public Item getSelectedItem() {
        return slots[selectedSlot];
    }
    // отримати об'єкт з конкретного слоту за індексом у параметрах
    public Item getItem(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < inventorySize) {
            return slots[slotIndex];
        }
        return null;
    }

    public boolean addItem(int slotIndex, Item item) {
        if (slotIndex >= 0 && slotIndex < inventorySize && slots[slotIndex] == null) {
            slots[slotIndex] = item;
            return true;
        }
        return false;
    }
    public Item removeItem(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < inventorySize) {
            Item item = slots[slotIndex];
            slots[slotIndex] = null;
            return item;
        }
        return null;
    }

    public void swapItems(int slot1, int slot2) {
        if (slot1 >= 0 && slot1 < inventorySize && slot2 >= 0 && slot2 < inventorySize) {
            Item temp = slots[slot1];
            slots[slot1] = slots[slot2];
            slots[slot2] = temp;
        }
    }

    public int getSize() {
        return inventorySize;
    }

    public boolean isWateringCanSelected() {
        Item selected = getSelectedItem();
        return selected != null && selected.getType() == Item.ItemType.WATERING_CAN;
    }
    public boolean isSeedSelected() {
        Item selected = getSelectedItem();
        if (selected != null) {
            Item.ItemType type = selected.getType();
            if (type == Item.ItemType.SEED) {
                Seed seed = (Seed) selected;
                return seed.getQuantity() > 0;  // Only return true if seeds are available
            } else if (type == Item.ItemType.RARE_SEED) {
                RareSeed seed = (RareSeed) selected;
                return seed.getQuantity() > 0;
            } else if (type == Item.ItemType.LEGENDARY_SEED) {
                LegendarySeed seed = (LegendarySeed) selected;
                return seed.getQuantity() > 0;
            }
        }
        return false;
    }

    public boolean useSeed() {
        Item selected = getSelectedItem();
        if (selected != null) {
            Item.ItemType type = selected.getType();
            if (type == Item.ItemType.SEED) {
                Seed seed = (Seed) selected;
                return seed.useSeed();
            } else if (type == Item.ItemType.RARE_SEED) {
                RareSeed seed = (RareSeed) selected;
                return seed.useSeed();
            } else if (type == Item.ItemType.LEGENDARY_SEED) {
                LegendarySeed seed = (LegendarySeed) selected;
                return seed.useSeed();
            }
        }
        return false;
    }

    public boolean isSickleSelected() {
        Item selected = getSelectedItem();
        return selected != null && selected.getType() == Item.ItemType.SICKLE;
    }

    public boolean isPlantFoodSelected() {
        Item selected = getSelectedItem();
        if (selected != null && selected.getType() == Item.ItemType.PLANT_FOOD) {
            PlantFood food = (PlantFood) selected;
            return food.getQuantity() > 0;
        }
        return false;
    }

    public boolean consumePlantFood() {
        Item selected = getSelectedItem();
        if (selected != null && selected.getType() == Item.ItemType.PLANT_FOOD) {
            PlantFood food = (PlantFood) selected;
            return food.consumeFood();
        }
        return false;
    }

    public boolean addPlantFood(int quantity) {
        // First try to add to existing plant food stack
        for (int i = 0; i < slots.length; i++) {
            Item item = slots[i];
            if (item != null && item.getType() == Item.ItemType.PLANT_FOOD) {
                PlantFood food = (PlantFood) item;
                int oldQuantity = food.getQuantity();
                food.addQuantity(quantity);
                return true;  // Added to existing stack
            }
        }
        // If no existing stack, find empty slot
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                slots[i] = new PlantFood(quantity);
                return true;
            }
        }
        return false;  // No space
    }
    public boolean addItem(Item item) {
        // Find an empty slot
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                slots[i] = item;
                return true;
            }
        }
        return false;  // No space
    }

    public Item[] getSlots() {
        return slots;
    }

    // Масив типів предметів, необхідних для кожної фази росту дерева (0-4)
    private static final Item.ItemType[] TREE_PHASE_ITEM_TYPES = {
            Item.ItemType.BIO_COMPOST,
            Item.ItemType.LIVING_DEW,
            Item.ItemType.MYCORRHIZA_NETWORK,
            Item.ItemType.UNIVERSE_FLOWER,
            Item.ItemType.EDEN_CORE
    };

    public boolean hasTreePhaseItem(int phaseIndex) {
        if (phaseIndex < 0 || phaseIndex >= TREE_PHASE_ITEM_TYPES.length) return false;
        Item.ItemType required = TREE_PHASE_ITEM_TYPES[phaseIndex];
        for (Item item : slots) {
            if (item != null && item.getType() == required) return true;
        }
        return false;
    }

    public boolean removeTreePhaseItem(int phaseIndex) {
        if (phaseIndex < 0 || phaseIndex >= TREE_PHASE_ITEM_TYPES.length) return false;
        Item.ItemType required = TREE_PHASE_ITEM_TYPES[phaseIndex];
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null && slots[i].getType() == required) {
                slots[i] = null;
                return true;
            }
        }
        return false;
    }
}

