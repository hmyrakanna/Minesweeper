package model;
import java.util.Random;

public class FirstClick implements MinePlacer {
    @Override
    public void placeMines(Cell[][] grid, int width, int height, int totalMines, int safeRow, int safeCol) {
        Random random = new Random();
        int placed = 0;

        while (placed < totalMines) {
            int row = random.nextInt(height);
            int col = random.nextInt(width);

            if (grid[row][col].isMine()) {
                continue;
            }

            if (Math.abs(row - safeRow) <= 1 && Math.abs(col - safeCol) <= 1) {
                continue;
            }

            grid[row][col].setMine(true);
            placed++;
        }
    }
    
}
