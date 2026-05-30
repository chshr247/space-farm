package com.spacefarm.inventory;

/**
 * Sickle tool for harvesting crops.
 */
public class Sickle extends Item {
    private static final Sickle INSTANCE = new Sickle();

    private Sickle() {
        super("Коса", "Інструмент для збору врожаю");
    }

    public static Sickle getInstance() {
        return INSTANCE;
    }

    @Override
    public ItemType getType() {
        return ItemType.SICKLE;
    }
}

