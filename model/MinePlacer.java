package model;

public interface MinePlacer {
    void placeMines(Cell[][] grid, int totalMines, int safeRow, int safeCol);
}
