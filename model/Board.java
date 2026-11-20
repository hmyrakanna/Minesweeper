package model;

import java.util.ArrayDeque;
import java.util.Objects;
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
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Недопустимий розмір поля");
        }
        if (totalMines < 0 || totalMines > width * height - Math.min(3, width) * Math.min(3, height)) {
            throw new IllegalArgumentException("Недопустима кількість мін");
        }
        this.width = width;
        this.height = height;
        this.totalMines = totalMines;
        this.grid = new Cell[height][width];
        this.minePlacer = Objects.requireNonNull(minePlacer);

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

    public int getRevealedCellsCount() {
        return revealedCellsCount;
    }

    public boolean hasStarted() {
        return !isFirstClick;
    }

    // Compact, portable snapshot of a live board: mine, revealed and flag bits.
    public String snapshot() {
        if (gameState != GameState.PLAYING) throw new IllegalStateException("Гру завершено");
        StringBuilder result = new StringBuilder(width * height);
        for (Cell[] row : grid) {
            for (Cell cell : row) {
                int bits = (cell.isMine() ? 1 : 0) | (cell.isRevealed() ? 2 : 0)
                        | (cell.isFlagged() ? 4 : 0);
                result.append((char) ('0' + bits));
            }
        }
        return result.toString();
    }

    public static Board restore(GameConfig config, boolean started, String snapshot) {
        Board board = new Board(config.width(), config.height(), config.mines(), new FirstClick());
        if (snapshot.length() != config.width() * config.height()) {
            throw new IllegalArgumentException("Неправильний розмір збереження");
        }
        int mines = 0;
        for (int i = 0; i < snapshot.length(); i++) {
            int bits = snapshot.charAt(i) - '0';
            if (bits < 0 || bits > 7 || (bits & 2) != 0 && (bits & 5) != 0
                    || !started && (bits & 3) != 0) {
                throw new IllegalArgumentException("Некоректний стан клітинки");
            }
            Cell cell = board.getCell(i / config.width(), i % config.width());
            if ((bits & 1) != 0) { cell.setMine(true); mines++; }
            if ((bits & 2) != 0) { cell.reveal(); board.revealedCellsCount++; }
            if ((bits & 4) != 0) cell.toggleFlag();
        }
        if (mines != (started ? config.mines() : 0)
                || started && (board.revealedCellsCount == 0 || board.revealedCellsCount >= config.safeCells())) {
            throw new IllegalArgumentException("Некоректний стан поля");
        }
        board.isFirstClick = !started;
        if (started) board.calculateAdjacentMines();
        return board;
    }

    public int getRemainingMines() {
        int flags = 0;
        for (Cell[] row : grid) {
            for (Cell cell : row) {
                if (cell.isFlagged()) flags++;
            }
        }
        return totalMines - flags;
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
            cell.explode();
            gameState = GameState.LOST;
            revealAllMines();
            return;
        }

        revealArea(row, col);
        checkWinCondition();
    }

    public void toggleFlag(int row, int col) {
        if (gameState != GameState.PLAYING) return;
        grid[row][col].toggleFlag();
    }

    private void revealArea(int startR, int startC) {
        Queue<Cell> queue = new ArrayDeque<>();
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

