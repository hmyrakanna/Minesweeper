package model;

import java.util.Objects;

public record GameConfig(Difficulty difficulty, int width, int height, int mines) {
    public static final int MIN_SIZE = 5, MAX_WIDTH = 36, MAX_HEIGHT = 24;

    public GameConfig {
        Objects.requireNonNull(difficulty);
        if (width < MIN_SIZE || width > MAX_WIDTH || height < MIN_SIZE || height > MAX_HEIGHT
                || mines < 1 || mines > width * height - 9) {
            throw new IllegalArgumentException("Недопустимі параметри поля");
        }
        if (difficulty != Difficulty.CUSTOM && (width != difficulty.getWidth()
                || height != difficulty.getHeight() || mines != difficulty.getMines())) {
            throw new IllegalArgumentException("Параметри не відповідають складності");
        }
    }

    public static GameConfig preset(Difficulty difficulty) {
        return new GameConfig(difficulty, difficulty.getWidth(), difficulty.getHeight(), difficulty.getMines());
    }
    public int safeCells() { return width * height - mines; }
    public String sizeLabel() { return width + "×" + height; }
}
