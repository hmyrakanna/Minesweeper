package controller;

import model.Board;
import model.GameState;
import model.FirstClick;

import javax.swing.Timer;

public class GameController {
    private Board board;
    private final int width;
    private final int height;
    private final int totalMines;

    private int secondsElapsed;
    private final Timer gameTimer;
    private boolean timerRunning;

    private GameEventListener eventListener;

    public interface GameEventListener {
        void onTimerTick(int seconds);
        void onGameStatusChanged(GameState state);
    }

    public GameController(int width, int height, int totalMines) {
        this.width = width;
        this.height = height;
        this.totalMines = totalMines;

        this.gameTimer = new Timer(1000, e -> {
            if (timerRunning) {
                secondsElapsed++;
                if (eventListener != null) {
                    eventListener.onTimerTick(secondsElapsed);
                }
            }
        });

        initNewGame();
    }

    public void setEventListener(GameEventListener listener) {
        this.eventListener = listener;
        if (eventListener != null) {
            eventListener.onTimerTick(secondsElapsed);
            eventListener.onGameStatusChanged(board.getGameState());
        }
    }

    public void initNewGame() {
        this.board = new Board(width, height, totalMines, new FirstClick());
        this.secondsElapsed = 0;
        this.timerRunning = false;

        if (gameTimer.isRunning()) {
            gameTimer.stop();
        }

        if (eventListener != null) {
            eventListener.onTimerTick(0);
            eventListener.onGameStatusChanged(GameState.PLAYING);
        }
    }

    public void handleCellClick(int r, int c) {
        if (board.getGameState() != GameState.PLAYING) {
            return;
        }

        if (!timerRunning) {
            timerRunning = true;
            gameTimer.start();
        }

        board.openCell(r, c);
        checkState();
    }

    public void handleRightClick(int r, int c) {
        if (board.getGameState() != GameState.PLAYING) {
            return;
        }
        board.toggleFlag(r, c);
    }

    public void handleChordClick(int r, int c) {
        if (board.getGameState() != GameState.PLAYING) {
            return;
        }
        board.chord(r, c);
        checkState();
    }

    private void checkState() {
        GameState state = board.getGameState();
        if (state != GameState.PLAYING) {
            timerRunning = false;
            gameTimer.stop();
            if (eventListener != null) {
                eventListener.onGameStatusChanged(state);
            }
        }
    }

    public Board getBoard() {
        return board;
    }

    public int getSecondsElapsed() {
        return secondsElapsed;
    }
}