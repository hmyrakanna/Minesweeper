package model;

public enum Difficulty {
    BEGINNER("Beginner", 9, 9, 10),
    INTERMEDIATE("Intermediate", 16, 16, 40),
    EXPERT("Expert", 30, 16, 99),
    CUSTOM("Custom", 0, 0, 0);

    private final int width;
    private final int height;
    private final int mines;
    private final String title;

    Difficulty(String title, int width, int height, int mines) {
        this.width = width;
        this.height = height;
        this.mines = mines;
        this.title = title;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getMines() {
        return mines;
    }

    public String getTitle() {
        return title;
    }
}
