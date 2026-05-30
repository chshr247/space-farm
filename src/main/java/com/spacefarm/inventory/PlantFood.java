package com.spacefarm.inventory;

/**
 * Plant food harvested from mature crops.
 */
public class PlantFood extends Item {
    private int quantity;
    private static final int MAX_QUANTITY = 64;

    public PlantFood(int initialQuantity) {
        super("Урожай", "Помідори для їдження (" + initialQuantity + ")");
        this.quantity = Math.min(initialQuantity, MAX_QUANTITY);
    }

    public PlantFood() {
        this(1);
    }

    @Override
    public ItemType getType() {
        return ItemType.PLANT_FOOD;
    }

    /**
     * Use one food (decrement quantity).
     * Returns true if food was consumed, false if quantity is 0.
     */
    public boolean consumeFood() {
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

    public void addQuantity(int amount) {
        this.quantity = Math.min(quantity + amount, MAX_QUANTITY);
    }

    @Override
    public String getDescription() {
        return "Урожай (" + quantity + "/" + MAX_QUANTITY + ")";
    }
}

