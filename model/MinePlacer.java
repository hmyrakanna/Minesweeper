package model;

public interface MinePlacer {
    void placeMines(Cell[][] grid, int width, int height, int totalMines, int safeRow, int safeCol);
}
