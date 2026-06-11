package com.spacefarm.inventory;

// Default seed restores 10% oxygen
public class Seed extends Item {
    private int quantity;
    private static final int MAX_QUANTITY = 20;

    public Seed(int initialQuantity) {
        super("Насіння", "Насіння для посадження (" + initialQuantity + ")");
        this.quantity = Math.min(initialQuantity, MAX_QUANTITY);
    }

    public Seed() {
        this(MAX_QUANTITY);
    }

    @Override
    public ItemType getType() {
        return ItemType.SEED;
    }

    public boolean useSeed() {
        if (quantity > 0) {
            quantity--;
            return true;
        }
        return false;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, Math.min(quantity, MAX_QUANTITY));
    }

    @Override
    public String getDescription() {
        return "Насіння для посадження (" + quantity + "/" + MAX_QUANTITY + ")";
    }
}

