package com.spacefarm.inventory;

public class EdenCore extends Item {
    public EdenCore() {
        super("Eden Core", "Tree phase 5 item. Shop price: $8000");
    }
    @Override
    public ItemType getType() { return ItemType.EDEN_CORE; }
}