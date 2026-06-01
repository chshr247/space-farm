package com.spacefarm.inventory;

/**
 * Represents a crystal - a valuable item obtained from scavenging outdoor zones.
 */
public class Crystal extends Item {
    public Crystal() {
        super("Crystal", "A valuable space crystal obtained from scavenging");
    }

    @Override
    public ItemType getType() {
        return ItemType.CRYSTAL;
    }
}

