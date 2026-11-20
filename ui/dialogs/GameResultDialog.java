package ui.dialogs;

import model.GameState;
import ui.components.RoundedButton;
import ui.components.RoundedPanel;
import ui.theme.GameTheme;
import javax.swing.*;
import java.awt.*;

public class GameResultDialog extends StyledDialog {
    public GameResultDialog(JFrame owner, GameTheme theme, GameState state,
                            int seconds, int revealed, int safeCells,
                            boolean newRecord, int hintsUsed, Runnable restartAction) {
        super(owner, state == GameState.WON ? "Перемога" : "Поразка", "", theme);
        body.removeAll();
        boolean won = state == GameState.WON;
        Color accent = won ? theme.getAccentPrimary() : theme.getMineColor();
        body.add(label(won ? "★" : "✹", 48, Font.BOLD, accent));
        body.add(Box.createVerticalStrut(8));
        body.add(label(won ? "Перемога!" : "Ой, міна!", 27, Font.BOLD, accent));
        body.add(Box.createVerticalStrut(6));
        body.add(label(won ? "Усі безпечні клітинки відкрито" : "Наступного разу пощастить!",
                14, Font.PLAIN, theme.getTextMuted()));
        body.add(Box.createVerticalStrut(20));

        RoundedPanel stats = new RoundedPanel(14, theme);
        stats.setAlignmentX(Component.CENTER_ALIGNMENT);
        stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));
        stats.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        stats.add(label("ТВІЙ ЧАС", 11, Font.BOLD, theme.getTextMuted()));
        stats.add(Box.createVerticalStrut(5));
        stats.add(label(String.format("%02d:%02d", seconds / 60, seconds % 60),
                30, Font.BOLD, theme.getTextPrimary()));
        stats.add(Box.createVerticalStrut(6));
        stats.add(label(newRecord ? "✦ Новий рекорд!" :
                        "Відкрито " + revealed + " з " + safeCells + " клітинок",
                13, Font.BOLD, newRecord ? theme.getAccentPrimary() : theme.getTextMuted()));
        stats.add(Box.createVerticalStrut(7));
        stats.add(label("Використано підказок: " + hintsUsed + " / 3", 12, Font.PLAIN, theme.getTextMuted()));
        body.add(stats);

        RoundedButton again = actionButton(won ? "Грати ще" : "Спробувати ще", true, () -> {
            dispose();
            restartAction.run();
        });
        again.setPreferredSize(new Dimension(320, 44));
        addActions(again);
        RoundedButton view = actionButton("Переглянути поле", false, this::dispose);
        view.setPreferredSize(new Dimension(320, 40));
        JPanel viewRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        viewRow.setOpaque(false);
        viewRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewRow.add(view);
        body.add(Box.createVerticalStrut(8));
        body.add(viewRow);
        getRootPane().setDefaultButton(again);
    }
}
