package model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FirstClick implements MinePlacer {
    @Override
    public void placeMines(Cell[][] grid, int width, int height, int totalMines, int safeRow, int safeCol) {
        List<Cell> candidates = new ArrayList<>();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (Math.abs(row - safeRow) > 1 || Math.abs(col - safeCol) > 1) {
                    candidates.add(grid[row][col]);
                }
            }
        }
        if (totalMines < 0 || totalMines > candidates.size()) {
            throw new IllegalArgumentException("Недопустима кількість мін");
        }
        Collections.shuffle(candidates);
        for (int i = 0; i < totalMines; i++) {
            candidates.get(i).setMine(true);
        }
    }
    
}
