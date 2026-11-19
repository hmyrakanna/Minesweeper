package model;

import java.util.LinkedList;
import java.util.Queue;

public class Board {
    private final int width;
    private final int height;
    private final int totalMines;
    private final Cell[][] grid;
    private final MinePlacer minePlacer;

    private boolean isFirstClick = true;
    private GameState gameState = GameState.PLAYING;
    private int revealedCellsCount = 0;

    public Board(int width, int height, int totalMines, MinePlacer minePlacer) {
        this.width = width;
        this.height = height;
        this.totalMines = Math.min(totalMines, width * height - 1);
        this.grid = new Cell[height][width];
        this.minePlacer = minePlacer;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                grid[row][col] = new Cell(row, col);
            }
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTotalMines() {
        return totalMines;
    }

    public Cell getCell(int row, int col) {
        return grid[row][col];
    }

    public void openCell(int row, int col) {
        if (gameState != GameState.PLAYING) return;

        Cell cell = grid[row][col];
        if (cell.isRevealed() || cell.isFlagged()) return;

        if (isFirstClick) {
            minePlacer.placeMines(grid, width, height, totalMines, row, col);
            calculateAdjacentMines();
            isFirstClick = false;
        }

        if (cell.isMine()) {
            cell.reveal();
            gameState = GameState.LOST;
            revealAllMines();
            return;
        }

        revealRecursive(row, col);
        checkWinCondition();
    }

    public void toggleFlag(int row, int col) {
        if (gameState != GameState.PLAYING) return;
        grid[row][col].toggleFlag();
    }

    private void revealRecursive(int startR, int startC) {
        Queue<Cell> queue = new LinkedList<>();
        Cell startCell = grid[startR][startC];
        if (startCell.isRevealed() || startCell.isFlagged()) return;

        queue.add(startCell);

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            if (current.isRevealed() || current.isFlagged()) continue;

            if (current.reveal()) {
                revealedCellsCount++;
            }

            if (current.getAdjacentMines() == 0) {
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;
                        int newRow = current.getRow() + dr;
                        int newCol = current.getCol() + dc;
                        if (isValid(newRow, newCol)) {
                            Cell neighbor = grid[newRow][newCol];
                            if (!neighbor.isRevealed() && !neighbor.isFlagged() && !neighbor.isMine()) {
                                queue.add(neighbor);
                            }
                        }
                    }
                }
            }
        }
    }

        private void calculateAdjacentMines() {
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    if (grid[row][col].isMine()) continue;

                    int count = 0;
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            if (dr == 0 && dc == 0) continue;
                            int newRow = row + dr;
                            int newCol = col + dc;
                            if (isValid(newRow, newCol) && grid[newRow][newCol].isMine()) {
                                count++;
                            }
                        }
                    }

                    grid[row][col].setAdjacentMines(count);
                }
            }
        }

        private boolean isValid(int row, int col) {
            return row >= 0 && row < height && col >= 0 && col < width;
        }

        private void revealAllMines() {
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    if (grid[row][col].isMine()) {
                        grid[row][col].reveal();
                    }
                }
            }
        }

        private void checkWinCondition() {
            int totalCells = width * height;
            if (revealedCellsCount == totalCells - totalMines) {
                gameState = GameState.WON;
                flagAllRemainingMines();
            }
        }

        private void flagAllRemainingMines() {
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    Cell cell = grid[row][col];
                    if (cell.isMine() && !cell.isFlagged()) {
                        cell.toggleFlag();
                    }
                }
            }
        }
    
    public void chord(int row, int col) {
        if (gameState != GameState.PLAYING) return;

        Cell cell = grid[row][col];
        if (!cell.isRevealed() || cell.getAdjacentMines() == 0) return;

        int flaggedCount = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int newRow = row + dr;
                int newCol = col + dc;
                if (isValid(newRow, newCol) && grid[newRow][newCol].isFlagged()) {
                    flaggedCount++;
                }
            }
        }

        if (flaggedCount == cell.getAdjacentMines()) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    int newRow = row + dr;
                    int newCol = col + dc;
                    if (isValid(newRow, newCol)) {
                        Cell neighbor = grid[newRow][newCol];
                        if (!neighbor.isRevealed() && !neighbor.isFlagged()) {
                            openCell(newRow, newCol);
                        }
                    }
                }
            }
        }
    }
}

