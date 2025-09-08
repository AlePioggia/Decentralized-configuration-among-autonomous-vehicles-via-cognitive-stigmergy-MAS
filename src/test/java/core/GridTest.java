package core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

public class GridTest {

    @Test
    public void testGridInitialization() {
        Grid grid = new Grid(5, 5);
        assertNotNull(grid);
        assertEquals(5, grid.getWidth());
        assertEquals(5, grid.getHeight());
    }

    @Test
    public void testGetOccupiedCells() {
        Grid grid = new Grid(3, 3);
        Cell firstCell = new Cell(0, 0);
        firstCell.setOccupied(true);
        grid.setCell(firstCell);

        Cell secondCell = new Cell(1, 1);
        secondCell.setOccupied(true);
        grid.setCell(secondCell);

        List<Object[]> occupiedCells = grid.getOccupiedCells();
        assertEquals(2, occupiedCells.size());
        assertArrayEquals(new Object[] { 0, 0 }, occupiedCells.get(0));
        assertArrayEquals(new Object[] { 1, 1 }, occupiedCells.get(1));
    }

}
