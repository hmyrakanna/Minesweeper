package ui.components;

import model.Cell;
import model.GameState;
import ui.theme.GameTheme;
import javax.swing.*;
import java.awt.*;

public class ThemePreviewPanel extends RoundedPanel {
    private final JLabel title = new JLabel("✦ САПЕР", SwingConstants.CENTER);
    private final RoundedButton sampleButton;
    private final CellButton[] buttons = new CellButton[15];
    public ThemePreviewPanel(GameTheme theme) {
        super(16, theme);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        title.setFont(new Font("SansSerif", Font.BOLD, 19));
        add(title, BorderLayout.NORTH);
        JPanel grid = new JPanel(new GridLayout(3, 5, 3, 3));
        grid.setOpaque(false);
        for (int i = 0; i < buttons.length; i++) {
            Cell cell = new Cell(i/5, i%5);
            if (i == 4) cell.toggleFlag();
            if (i == 6 || i == 7 || i == 8 || i == 11 || i == 12 || i == 13) {
                cell.reveal();
                cell.setAdjacentMines(i % 3);
            }
            buttons[i] = new CellButton(cell, theme);
            buttons[i].setFocusable(false);
            grid.add(buttons[i]);
        }
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        center.setOpaque(false);
        center.add(grid);
        add(center, BorderLayout.CENTER);
        sampleButton = new RoundedButton("Нова гра", 12, theme);
        sampleButton.setFocusable(false);
        sampleButton.setActive(true);
        sampleButton.setPreferredSize(new Dimension(160, 36));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        bottom.setOpaque(false);
        bottom.add(sampleButton);
        add(bottom, BorderLayout.SOUTH);
        setTheme(theme);
    }
    @Override public void setTheme(GameTheme theme) {
        super.setTheme(theme);
        title.setForeground(theme.getAccentPrimary());
        sampleButton.setTheme(theme);
        for (CellButton button : buttons) button.sync(GameState.WON, theme);
    }
}
