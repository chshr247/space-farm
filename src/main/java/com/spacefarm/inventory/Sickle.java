package com.spacefarm.inventory;


public class Sickle extends Item {
    private static final Sickle INSTANCE = new Sickle();

    protected Sickle() {
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

