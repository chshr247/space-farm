package com.spacefarm.inventory;

// Об'єкт який випадає після зачистки звичайної локації
public class Crystal extends Item {
    public Crystal() {
        super("Crystal", "A valuable space crystal obtained from scavenging");
    }

    @Override
    public ItemType getType() {
        return ItemType.CRYSTAL;
    }
}

