package ui.components;

import model.Cell;
import model.GameState;
import ui.theme.GameTheme;

import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CellButton extends JButton {
    private final int row;
    private final int col;
    private Cell cell;
    private GameState gameState = GameState.PLAYING;
    private GameTheme theme;
    private boolean hovered;

    public CellButton(int row, int col, Cell cell, GameTheme theme) {
        this.row = row;
        this.col = col;
        this.cell = cell;
        this.theme = theme;

        setPreferredSize(new Dimension(34, 34));
        setMinimumSize(new Dimension(26, 26));
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(new Font("SansSerif", Font.BOLD, 14));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void sync(Cell cell, GameState gameState, GameTheme theme) {
        this.cell = cell;
        this.gameState = gameState;
        this.theme = theme;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int arc = 8;

        if (!cell.isRevealed()) {
            Color bg = hovered && gameState == GameState.PLAYING
                    ? theme.getHiddenCellHover()
                    : theme.getHiddenCellBg();
            g2.setColor(bg);
            g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);

            g2.setColor(theme.getHiddenCellBorder());
            g2.setStroke(new BasicStroke(1.1f));
            g2.drawRoundRect(1, 1, w - 2, h - 2, arc, arc);

            if (cell.isFlagged()) {
                drawFlag(g2, w, h);
                if (gameState == GameState.LOST && !cell.isMine()) {
                    g2.setColor(theme.getMineColor());
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawLine(w / 5, h / 5, w * 4 / 5, h * 4 / 5);
                    g2.drawLine(w * 4 / 5, h / 5, w / 5, h * 4 / 5);
                }
            } else {
                drawSparkle(g2, w / 2f, h / 2f, Math.min(w, h) * 0.11f, theme.getTextMuted());
            }
        } else {
            g2.setColor(cell.isExploded() ? theme.getMineColor().darker() : theme.getRevealedCellBg());
            g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);

            g2.setColor(theme.getRevealedCellBorder());
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(1, 1, w - 2, h - 2, arc, arc);

            if (cell.isMine()) {
                drawMine(g2, w, h);
            } else if (cell.getAdjacentMines() > 0) {
                String text = String.valueOf(cell.getAdjacentMines());
                g2.setFont(getFont().deriveFont(Font.BOLD,
                        Math.max(14f, Math.min(w, h) * 0.48f)));
                g2.setColor(theme.getNumberColor(cell.getAdjacentMines()));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(text)) / 2;
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, tx, ty);
            }
        }

        g2.dispose();
    }

    private void drawFlag(Graphics2D g2, int w, int h) {
        g2.setColor(theme.getFlagColor());
        int poleX = w / 3;
        int topY = h / 4;
        int bottomY = h * 3 / 4;
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(poleX, topY, poleX, bottomY);

        int[] xs = {poleX, w * 3 / 4, poleX};
        int[] ys = {topY, topY + h / 6, topY + h / 3};
        g2.fillPolygon(xs, ys, 3);
    }

    private void drawMine(Graphics2D g2, int w, int h) {
        int cx = w / 2;
        int cy = h / 2;
        int r = Math.min(w, h) / 4;

        g2.setColor(cell.isExploded() ? Color.WHITE : theme.getMineColor());
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);

        g2.setStroke(new BasicStroke(1.8f));
        g2.drawLine(cx - r - 3, cy, cx + r + 3, cy);
        g2.drawLine(cx, cy - r - 3, cx, cy + r + 3);
    }

    private void drawSparkle(Graphics2D g2, float cx, float cy, float size, Color color) {
        g2.setColor(color);
        int[] xs = {
                Math.round(cx),
                Math.round(cx + size * 0.35f),
                Math.round(cx + size),
                Math.round(cx + size * 0.35f),
                Math.round(cx),
                Math.round(cx - size * 0.35f),
                Math.round(cx - size),
                Math.round(cx - size * 0.35f)
        };
        int[] ys = {
                Math.round(cy - size),
                Math.round(cy - size * 0.35f),
                Math.round(cy),
                Math.round(cy + size * 0.35f),
                Math.round(cy + size),
                Math.round(cy + size * 0.35f),
                Math.round(cy),
                Math.round(cy - size * 0.35f)
        };
        g2.fillPolygon(xs, ys, 8);
    }
}
