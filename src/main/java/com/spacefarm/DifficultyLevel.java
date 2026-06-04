package com.spacefarm;

public enum DifficultyLevel {
    EASY   (2f,               10,                   1500f),
    NORMAL (4f,               5,                    1000f),
    HARD   (6f,               2,                    100f);

    /** Oxygen decrease per 10-second scavenging interval (in %). */
    public final float oxygenDecreaseAmount;

    /** Number of garden beds available at game start. */
    public final int startingGardenBeds;

    /** Starting amount of money. */
    public final float startingMoney;

    DifficultyLevel(float oxygenDecreaseAmount, int startingGardenBeds, float startingMoney) {
        this.oxygenDecreaseAmount = oxygenDecreaseAmount;
        this.startingGardenBeds  = startingGardenBeds;
        this.startingMoney       = startingMoney;
    }
}

