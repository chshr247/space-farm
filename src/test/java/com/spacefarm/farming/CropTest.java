package com.spacefarm.farming;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CropTest {

    @Test
    void cropMovesFromSeedToSproutAfterFirstStageDuration() {
        Crop crop = new Crop(FarmingConstants.CropType.DEFAULT);

        crop.update(FarmingConstants.STAGE_1_DURATION);

        assertEquals(FarmingConstants.GrowthStage.SPROUT, crop.getGrowthStage());
        assertEquals(0f, crop.getGrowthTimer());
    }
}

