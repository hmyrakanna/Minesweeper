package ui.components;

import ui.theme.GameTheme;

import javax.swing.*;
import java.awt.*;

public class GameHeader extends RoundedPanel {
    private final JLabel minesTitle = new JLabel("МІНИ", SwingConstants.CENTER);
    private final JLabel timerTitle = new JLabel("ЧАС", SwingConstants.CENTER);
    private final JLabel minesLabel = new JLabel("010", SwingConstants.CENTER);
    private final JLabel timerLabel = new JLabel("00:00", SwingConstants.CENTER);
    private final RoundedButton restartButton;

    public GameHeader(GameTheme theme, Runnable restartAction) {
        super(18, theme);
        setLayout(new BorderLayout(12, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        minesTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        timerTitle.setFont(minesTitle.getFont());
        minesLabel.setFont(new Font("Monospaced", Font.BOLD, 22));
        timerLabel.setFont(minesLabel.getFont());

        restartButton = new RoundedButton("↻ Нова гра", 12, theme);
        restartButton.setPreferredSize(new Dimension(148, 40));
        restartButton.setActive(true);
        restartButton.addActionListener(e -> restartAction.run());
        JPanel middle = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        middle.setOpaque(false);
        middle.add(restartButton);

        add(createCounter(minesTitle, minesLabel), BorderLayout.WEST);
        add(middle, BorderLayout.CENTER);
        add(createCounter(timerTitle, timerLabel), BorderLayout.EAST);
        setTheme(theme);
    }

    private JPanel createCounter(JLabel title, JLabel value) {
        JPanel counter = new JPanel(new BorderLayout(0, 2));
        counter.setOpaque(false);
        counter.setPreferredSize(new Dimension(104, 48));
        counter.add(title, BorderLayout.NORTH);
        counter.add(value, BorderLayout.CENTER);
        return counter;
    }

    public void updateMines(int mines) {
        minesLabel.setText(String.format("%03d", mines));
    }

    public void updateTime(int seconds) {
        timerLabel.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
    }

    @Override
    public void setTheme(GameTheme theme) {
        super.setTheme(theme);
        minesTitle.setForeground(theme.getTextMuted());
        timerTitle.setForeground(theme.getTextMuted());
        minesLabel.setForeground(theme.getAccentPrimary());
        timerLabel.setForeground(theme.getAccentSecondary());
        restartButton.setTheme(theme);
    }
}
