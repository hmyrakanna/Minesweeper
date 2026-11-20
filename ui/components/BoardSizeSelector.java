package ui.components;

import model.GameConfig;
import ui.theme.GameTheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.BiConsumer;

public class BoardSizeSelector extends JPanel {
    private static final int STEP = 14;
    private final GameTheme theme;
    private int columns = 12, rows = 12;
    private final BiConsumer<Integer, Integer> onSizeChanged;

    public BoardSizeSelector(GameTheme theme, BiConsumer<Integer, Integer> onSizeChanged) {
        this.theme = theme;
        this.onSizeChanged = onSizeChanged;
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        setPreferredSize(new Dimension(GameConfig.MAX_WIDTH * STEP, GameConfig.MAX_HEIGHT * STEP));
        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { selectAt(e.getX(), e.getY()); }
            @Override public void mouseDragged(MouseEvent e) { selectAt(e.getX(), e.getY()); }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }
    private void selectAt(int x, int y) {
        int left = (getWidth() - GameConfig.MAX_WIDTH * STEP) / 2;
        int top = (getHeight() - GameConfig.MAX_HEIGHT * STEP) / 2;
        int w = Math.max(1, Math.min(GameConfig.MAX_WIDTH, Math.floorDiv(x - left, STEP) + 1));
        int h = Math.max(1, Math.min(GameConfig.MAX_HEIGHT, Math.floorDiv(y - top, STEP) + 1));
        select(w, h);
    }
    public void select(int width, int height) {
        columns = Math.max(1, Math.min(GameConfig.MAX_WIDTH, width));
        rows = Math.max(1, Math.min(GameConfig.MAX_HEIGHT, height));
        onSizeChanged.accept(columns, rows);
        repaint();
    }
    public boolean isValidSize() { return columns >= GameConfig.MIN_SIZE && rows >= GameConfig.MIN_SIZE; }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int left = (getWidth() - GameConfig.MAX_WIDTH * STEP) / 2;
        int top = (getHeight() - GameConfig.MAX_HEIGHT * STEP) / 2;
        Color selection = isValidSize() ? theme.getAccentPrimary() : new Color(224, 74, 102);
        for (int r = 0; r < GameConfig.MAX_HEIGHT; r++) {
            for (int c = 0; c < GameConfig.MAX_WIDTH; c++) {
                g.setColor(r < rows && c < columns ? selection : theme.getRevealedCellBg());
                g.fillRoundRect(left + c * STEP, top + r * STEP, STEP - 2, STEP - 2, 4, 4);
            }
        }
        g.dispose();
    }
}
