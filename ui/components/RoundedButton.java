package ui.components;

import ui.theme.GameTheme;

import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoundedButton extends JButton {
    private final int radius;
    private GameTheme theme;
    private boolean active;
    private boolean hovered;

    public RoundedButton(String text, int radius, GameTheme theme) {
        super(text);
        this.radius = radius;
        this.theme = theme;
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(new Font("SansSerif", Font.BOLD, 13));

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

    public void setTheme(GameTheme theme) {
        this.theme = theme;
        repaint();
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        Color fill = active
                ? (hovered ? theme.getAccentPrimary().brighter() : theme.getAccentPrimary())
                : (hovered ? theme.getHiddenCellHover() : theme.getHiddenCellBg());
        if (getModel().isPressed()) fill = fill.darker();
        Color border = active ? theme.getAccentPrimary() : theme.getPanelBorder();
        Color textColor = active ? new Color(39, 23, 43) : theme.getTextPrimary();
        if (!isEnabled()) {
            fill = theme.getPanelBackground();
            border = theme.getPanelBorder();
            textColor = theme.getTextMuted();
        }

        g2.setColor(fill);
        g2.fillRoundRect(1, 1, w - 2, h - 2, radius, radius);

        g2.setColor(border);
        g2.setStroke(new BasicStroke(hasFocus() ? 2f : 1f));
        if (hasFocus()) g2.setColor(theme.getAccentSecondary());
        g2.drawRoundRect(1, 1, w - 2, h - 2, radius, radius);

        g2.setFont(getFont());
        g2.setColor(textColor);
        FontMetrics fm = g2.getFontMetrics();
        int textX = (w - fm.stringWidth(getText())) / 2;
        int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }
}
