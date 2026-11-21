package model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Board {
    private final int width, height, totalMines;
    private final Cell[][] grid;
    private final MinePlacer minePlacer;
    private boolean started;
    private GameState gameState = GameState.PLAYING;

    public Board(GameConfig config) {
        this(config, new FirstClick());
    }

    public Board(GameConfig config, MinePlacer minePlacer) {
        width = config.width();
        height = config.height();
        totalMines = config.mines();
        this.minePlacer = minePlacer;
        grid = new Cell[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                grid[row][col] = new Cell(row, col);
            }
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getTotalMines() { return totalMines; }
    public GameState getGameState() { return gameState; }
    public boolean hasStarted() { return started; }
    public Cell getCell(int row, int col) { return grid[row][col]; }

    public int getRevealedCellsCount() {
        int count = 0;
        for (Cell[] row : grid) {
            for (Cell cell : row) {
                if (cell.isRevealed() && !cell.isMine()) count++;
            }
        }
        return count;
    }

    public int getRemainingMines() {
        int remaining = totalMines;
        for (Cell[] row : grid) {
            for (Cell cell : row) {
                if (cell.isFlagged()) remaining--;
            }
        }
        return remaining;
    }

    private List<Cell> neighbors(Cell cell) {
        List<Cell> result = new ArrayList<>();
        for (int row = cell.getRow() - 1; row <= cell.getRow() + 1; row++) {
            for (int col = cell.getCol() - 1; col <= cell.getCol() + 1; col++) {
                if (row < 0 || row >= height || col < 0 || col >= width) continue;
                if (grid[row][col] != cell) result.add(grid[row][col]);
            }
        }
        return result;
    }

    public void openCell(int row, int col) {
        if (gameState != GameState.PLAYING) return;
        Cell cell = getCell(row, col);
        if (!cell.isClosed()) return;

        if (!started) {
            minePlacer.placeMines(grid, totalMines, row, col);
            calculateAdjacentMines();
            started = true;
        }
        if (cell.isMine()) {
            cell.explode();
            gameState = GameState.LOST;
            revealAllMines();
        } else {
            revealArea(cell);
            checkWin();
        }
    }

    public void toggleFlag(int row, int col) {
        if (gameState == GameState.PLAYING) getCell(row, col).toggleFlag();
    }

    private void revealArea(Cell start) {
        Queue<Cell> queue = new ArrayDeque<>();
        start.reveal();
        queue.add(start);
        while (!queue.isEmpty()) {
            Cell current = queue.remove();
            if (current.getAdjacentMines() != 0) continue;
            for (Cell neighbor : neighbors(current)) {
                if (neighbor.isSafeClosed()) {
                    neighbor.reveal();
                    queue.add(neighbor);
                }
            }
        }
    }

    private void calculateAdjacentMines() {
        for (Cell[] row : grid) {
            for (Cell cell : row) {
                if (cell.isMine()) continue;
                int count = 0;
                for (Cell neighbor : neighbors(cell)) {
                    if (neighbor.isMine()) count++;
                }
                cell.setAdjacentMines(count);
            }
        }
    }

    private void revealAllMines() {
        for (Cell[] row : grid) {
            for (Cell cell : row) {
                if (cell.isMine()) cell.reveal();
            }
        }
    }

    private void checkWin() {
        if (getRevealedCellsCount() != width * height - totalMines) return;
        gameState = GameState.WON;
        for (Cell[] row : grid) {
            for (Cell cell : row) {
                if (cell.isMine() && !cell.isFlagged()) cell.toggleFlag();
            }
        }
    }

    public void chord(int row, int col) {
        if (gameState != GameState.PLAYING) return;
        Cell cell = getCell(row, col);
        if (!cell.isRevealed() || cell.getAdjacentMines() == 0) return;
        List<Cell> nearby = neighbors(cell);
        int flags = 0;
        for (Cell neighbor : nearby) {
            if (neighbor.isFlagged()) flags++;
        }
        if (flags == cell.getAdjacentMines()) {
            for (Cell neighbor : nearby) {
                openCell(neighbor.getRow(), neighbor.getCol());
            }
        }
    }

    public String snapshot() {
        if (gameState != GameState.PLAYING) throw new IllegalStateException("Гру завершено");
        StringBuilder result = new StringBuilder();
        for (Cell[] row : grid) {
            for (Cell cell : row) result.append(cell.saveCode());
        }
        return result.toString();
    }

    public static Board restore(GameConfig config, boolean started, String snapshot) {
        Board board = new Board(config);
        if (snapshot.length() != config.width() * config.height()) {
            throw new IllegalArgumentException("Неправильний розмір збереження");
        }
        int index = 0, mines = 0;
        for (Cell[] row : board.grid) {
            for (Cell cell : row) {
                cell.restoreCode(snapshot.charAt(index++));
                if (cell.isMine()) mines++;
                if (!started && (cell.isMine() || cell.isRevealed())) {
                    throw new IllegalArgumentException("Некоректний стан першого ходу");
                }
            }
        }
        int revealed = board.getRevealedCellsCount();
        if (started && (mines != config.mines() || revealed == 0 || revealed >= config.safeCells())) {
            throw new IllegalArgumentException("Некоректний стан поля");
        }
        board.started = started;
        if (started) board.calculateAdjacentMines();
        return board;
    }
}
