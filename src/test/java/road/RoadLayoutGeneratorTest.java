package road;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import core.Grid;

public class RoadLayoutGeneratorTest {
    @Test
    public void testBuildDeterministicGeneratesExpectedRoadsAndFootprints() {
        int width = 10;
        int height = 10;
        int nVertical = 2;
        int nHorizontal = 2;
        int spacing = 2;
        long seed = 42L;
        boolean randomizedPositions = false;

        Grid grid = new Grid(width, height);
        RoadLayoutGenerator generator = new RoadLayoutGenerator();

        RoadLayout layout = generator.buildDeterministic(grid, width, height, nVertical, nHorizontal, spacing, seed,
                randomizedPositions);

        assertEquals(nVertical + nHorizontal, layout.getRoads().size(),
                "Number of roads should match vertical + horizontal");
        assertFalse(layout.getIntersectionFootprints().isEmpty(),
                "There should be at least one intersection footprint");

        boolean hasRoadCells = false;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid.getCell(x, y).getDirection() != null) {
                    hasRoadCells = true;
                    break;
                }
            }
        }
        assertTrue(hasRoadCells, "Grid should contain road cells with directions");
    }
}
