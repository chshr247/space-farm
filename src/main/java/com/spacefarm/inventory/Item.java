package com.spacefarm.inventory;

// Базовий клас для всіх об'єктів в інвентарі
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

    public abstract ItemType getType();

    public enum ItemType {
        WATERING_CAN,
        SEED,
        RARE_SEED,
        LEGENDARY_SEED,
        FERTILIZER,
        SICKLE,
        PLANT_FOOD,
        CRYSTAL,
        // Tree phase items (phases 1-5)
        BIO_COMPOST,
        LIVING_DEW,
        MYCORRHIZA_NETWORK,
        UNIVERSE_FLOWER,
        EDEN_CORE,
        EMPTY
    }
}

