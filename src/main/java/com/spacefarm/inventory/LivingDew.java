package com.spacefarm.inventory;

public class LivingDew extends Item {
    public LivingDew() {
        super("Living Dew", "Tree phase 2 item. Shop price: $1000");
    }
    @Override
    public ItemType getType() { return ItemType.LIVING_DEW; }
}