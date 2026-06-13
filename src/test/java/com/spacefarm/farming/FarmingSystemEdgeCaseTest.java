package com.spacefarm.farming;

import com.spacefarm.world.TileCoord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FarmingSystemEdgeCaseTest {

    @Test
    void plantSeedRejectsNullCoordinate() {
        FarmingSystem system = new FarmingSystem(0, 0, 5, 5);

        boolean planted = system.plantSeed((TileCoord) null, FarmingConstants.CropType.DEFAULT);

        assertFalse(planted);
        assertEquals(0, system.getCropCount());
    }

    @Test
    void plantSeedRejectsCoordinatesOnExclusiveUpperBoundary() {
        FarmingSystem system = new FarmingSystem(0, 0, 5, 5);

        boolean plantedOnMaxX = system.plantSeed(new TileCoord(5, 2), FarmingConstants.CropType.DEFAULT);
        boolean plantedOnMaxY = system.plantSeed(new TileCoord(2, 5), FarmingConstants.CropType.DEFAULT);

        assertFalse(plantedOnMaxX);
        assertFalse(plantedOnMaxY);
        assertEquals(0, system.getCropCount());
    }

    @Test
    void plantSeedRejectsDuplicatePlantingOnSameTile() {
        FarmingSystem system = new FarmingSystem(0, 0, 5, 5);
        TileCoord coord = new TileCoord(1, 1);

        boolean firstPlant = system.plantSeed(coord, FarmingConstants.CropType.DEFAULT);
        boolean secondPlant = system.plantSeed(coord, FarmingConstants.CropType.EPIC);

        assertTrue(firstPlant);
        assertFalse(secondPlant);
        assertEquals(1, system.getCropCount());
        assertEquals(FarmingConstants.CropType.DEFAULT, system.getCrop(coord).getType());
    }

    @Test
    void harvestCropOnlyWorksForMaturePlants() {
        FarmingSystem system = new FarmingSystem(0, 0, 5, 5);
        TileCoord coord = new TileCoord(1, 1);

        system.plantSeed(coord, FarmingConstants.CropType.DEFAULT);

        assertFalse(system.harvestCrop(coord));

        system.getCrop(coord).update(FarmingConstants.STAGE_1_DURATION);
        system.getCrop(coord).update(FarmingConstants.STAGE_2_DURATION);
        system.getCrop(coord).update(FarmingConstants.STAGE_3_DURATION);

        assertTrue(system.harvestCrop(coord));
        assertFalse(system.hasCrop(coord));
        assertEquals(0, system.getCropCount());
    }

    @Test
    void updateRemovesCropAfterItHasBeenLeftToDie() {
        FarmingSystem system = new FarmingSystem(0, 0, 5, 5);
        TileCoord coord = new TileCoord(2, 2);

        system.plantSeed(coord, FarmingConstants.CropType.DEFAULT);
        system.update(FarmingConstants.DRYING_DURATION * 1.5f + 0.1f);

        assertFalse(system.hasCrop(coord));
        assertEquals(0, system.getCropCount());
    }
}

