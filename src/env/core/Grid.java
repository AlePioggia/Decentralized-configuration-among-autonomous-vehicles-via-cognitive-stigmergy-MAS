package core;

import java.util.stream.IntStream;

public class Grid {
    private int width;
    private int height;
    private Cell[][] cells;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[width][height];
        initializeCells(width, height);
    }

    private void initializeCells(int width, int height) {
        IntStream.range(0, width).forEach(x -> IntStream.range(0, height).forEach(y -> cells[x][y] = new Cell(x, y)));
    }

    public Cell getCell(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IndexOutOfBoundsException("Cell coordinates out of bounds");
        }
        return cells[x][y];
    }

    public Cell[][] getGridCells() {
        return this.cells;
    }

    public void setCell(Cell cell) {
        this.cells[cell.getPosition().getX()][cell.getPosition().getY()] = cell;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
