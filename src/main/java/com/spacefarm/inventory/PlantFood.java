package com.spacefarm.inventory;

// Плід з вирощеної рослини
public class PlantFood extends Item {
    private int quantity;
    private static final int MAX_QUANTITY = 64;
    // Конструктор для створення їжі з заданою кількістю
    public PlantFood(int initialQuantity) {
        super("Урожай", "Їжа для споживання (" + initialQuantity + ")");
        this.quantity = Math.min(initialQuantity, MAX_QUANTITY);
    }
    // Це конструктор для створення їжі з 1 одиниці кількості за замовчуванням
    public PlantFood() {
        this(1);
    }

    @Override
    public ItemType getType() {
        return ItemType.PLANT_FOOD;
    }

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

