package model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FirstClick implements MinePlacer {
    @Override
    public void placeMines(Cell[][] grid, int totalMines, int safeRow, int safeCol) {
        List<Cell> candidates = new ArrayList<>();
        for (Cell[] row : grid) {
            for (Cell cell : row) {
                if (Math.abs(cell.getRow() - safeRow) > 1 || Math.abs(cell.getCol() - safeCol) > 1) {
                    candidates.add(cell);
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
