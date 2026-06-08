package com.spacefarm.inventory;

public class BioCompost extends Item {
    public BioCompost() {
        super("Bio-Compost", "Tree phase 1 item. Shop price: $500");
    }
    @Override
    public ItemType getType() { return ItemType.BIO_COMPOST; }
}
