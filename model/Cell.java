package model;

public class Cell {
    private final int row;
    private final int col;
    private boolean isMine;
    private boolean isRevealed;
    private boolean isFlagged;
    private boolean exploded;
    private int adjacentMines;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public boolean isRevealed() {
        return isRevealed;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public boolean isClosed() {
        return !isRevealed && !isFlagged;
    }

    public boolean isSafeClosed() {
        return !isMine && isClosed();
    }

    public boolean isExploded() {
        return exploded;
    }

    public void explode() {
        if (isMine && reveal()) {
            exploded = true;
        }
    }

    public int getAdjacentMines() {
        return adjacentMines;
    }

    public void setAdjacentMines(int adjacentMines) {
        this.adjacentMines = adjacentMines;
    }

    public boolean reveal() {
        if (isRevealed || isFlagged) {
            return false;
        }
        isRevealed = true;
        return true;
    }

    public void toggleFlag() {
        if (!isRevealed) {
            isFlagged = !isFlagged;
        }
    }

    char saveCode() {
        if (isFlagged) return isMine ? '5' : '4';
        if (isRevealed) return '2';
        return isMine ? '1' : '0';
    }

    void restoreCode(char code) {
        switch (code) {
            case '0': break;
            case '1': isMine = true; break;
            case '2': isRevealed = true; break;
            case '4': isFlagged = true; break;
            case '5': isMine = true; isFlagged = true; break;
            default: throw new IllegalArgumentException("Некоректний стан клітинки");
        }
    }
}
