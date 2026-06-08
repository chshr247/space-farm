package com.spacefarm.inventory;

public class UniverseFlower extends Item {
    public UniverseFlower() {
        super("Universe Flower", "Tree phase 4 item. Shop price: $4000");
    }
    @Override
    public ItemType getType() { return ItemType.UNIVERSE_FLOWER; }
}
