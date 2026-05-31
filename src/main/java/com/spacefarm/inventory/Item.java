package com.spacefarm.inventory;

/**
 * Base class for all inventory items.
 */
public abstract class Item {
    protected String name;
    protected String description;

    public Item(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get the item type for identification.
     */
    public abstract ItemType getType();

    public enum ItemType {
        WATERING_CAN,
        SEED,
        FERTILIZER,
        SICKLE,
        PLANT_FOOD,
        CRYSTAL,
        EMPTY
    }
}

