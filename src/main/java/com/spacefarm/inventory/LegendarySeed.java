package com.spacefarm.inventory;

/**
 * Legendary seed item - restores 50% oxygen.
 */
public class LegendarySeed extends Item {
    private int quantity;
    private static final int MAX_QUANTITY = 5;
    public static final float OXYGEN_RESTORE_PERCENT = 50f;

    public LegendarySeed(int initialQuantity) {
        super("Легендарне насіння", "Легендарне насіння (" + initialQuantity + ")");
        this.quantity = Math.min(initialQuantity, MAX_QUANTITY);
    }

    public LegendarySeed() {
        this(MAX_QUANTITY);
    }

    @Override
    public ItemType getType() {
        return ItemType.LEGENDARY_SEED;
    }

    /**
     * Use one seed (decrement quantity).
     * Returns true if seed was used, false if quantity is 0.
     */
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
        return "Легендарне насіння (" + quantity + "/" + MAX_QUANTITY + ")";
    }
}

