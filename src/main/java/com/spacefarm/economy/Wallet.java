package com.spacefarm.economy;

public class Wallet {
    private float balance;

    public Wallet(float startingBalance) {
        this.balance = startingBalance;
    }

    /** Returns current balance. */
    public float getBalance() {
        return balance;
    }

    /** Adds money to the wallet (earned from harvest, scavenging, etc.). */
    public void earn(float amount) {
        if (amount > 0) {
            balance += amount;
        }
    }

    /**
     * Tries to spend money.
     * Returns true and deducts the amount if funds are sufficient.
     * Returns false and does nothing if not enough money.
     */
    public boolean spend(float amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }

    /** Checks if the player can afford a purchase without spending. */
    public boolean canAfford(float amount) {
        return balance >= amount;
    }
}
