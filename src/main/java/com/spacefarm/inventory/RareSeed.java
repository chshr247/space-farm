package com.spacefarm.inventory;

/**
 * Rare seed item - restores 20% oxygen.
 */
public class RareSeed extends Item {
    private int quantity;
    private static final int MAX_QUANTITY = 5;
    public static final float OXYGEN_RESTORE_PERCENT = 20f;

    public RareSeed(int initialQuantity) {
        super("Рідкісне насіння", "Рідкісне насіння (" + initialQuantity + ")");
        this.quantity = Math.min(initialQuantity, MAX_QUANTITY);
    }

    public RareSeed() {
        this(MAX_QUANTITY);
    }

    @Override
    public ItemType getType() {
        return ItemType.RARE_SEED;
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
        return "Рідкісне насіння (" + quantity + "/" + MAX_QUANTITY + ")";
    }
}

