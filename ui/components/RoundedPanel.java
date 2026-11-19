package ui.components;

import ui.theme.GameTheme;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class RoundedPanel extends JPanel {
    private final int radius;
    private GameTheme theme;

    public RoundedPanel(int radius, GameTheme theme) {
        this.radius = radius;
        this.theme = theme;
        setOpaque(false);
    }

    public void setTheme(GameTheme theme) {
        this.theme = theme;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(theme.getPanelBackground());
        g2.fillRoundRect(0, 0, w - 1, h - 1, radius, radius);

        g2.setColor(theme.getPanelBorder());
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);

        g2.dispose();
        super.paintComponent(g);
    }
}