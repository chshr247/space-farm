package com.spacefarm.inventory;

/**
 * Watering can tool for watering plants.
 */
public class WateringCan extends Item {
    private static final WateringCan INSTANCE = new WateringCan();

    protected WateringCan() {
        super("Лійка", "Інструмент для поливання рослин");
    }

    public static WateringCan getInstance() {
        return INSTANCE;
    }

    @Override
    public ItemType getType() {
        return ItemType.WATERING_CAN;
    }
}

