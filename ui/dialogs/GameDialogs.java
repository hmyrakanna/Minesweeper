package ui.dialogs;

import model.Difficulty;
import record.ScoreRecord;
import ui.components.RoundedButton;
import ui.components.RoundedPanel;
import ui.theme.*;
import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public final class GameDialogs {
    private GameDialogs() {}

    public record BoardConfig(int width, int height, int mines) {}

    public static Optional<BoardConfig> customBoard(JFrame owner, GameTheme theme) {
        StyledDialog dialog = new StyledDialog(owner, "Власне поле",
                "Налаштуйте розмір і кількість мін", theme);
        JSpinner width = spinner(12, 5, 36, theme);
        JSpinner height = spinner(12, 5, 24, theme);
        JSpinner mines = spinner(20, 1, 135, theme);
        JLabel limit = dialog.label("Максимум 135 мін", 12, Font.PLAIN, theme.getTextMuted());
        Runnable updateLimit = () -> {
            int max = (int) width.getValue() * (int) height.getValue() - 9;
            SpinnerNumberModel model = (SpinnerNumberModel) mines.getModel();
            if ((int) mines.getValue() > max) mines.setValue(max);
            model.setMaximum(max);
            limit.setText("Максимум " + max + " мін");
        };
        width.addChangeListener(e -> updateLimit.run());
        height.addChangeListener(e -> updateLimit.run());
        JPanel form = new JPanel(new GridLayout(3, 2, 18, 12));
        form.setOpaque(false);
        form.add(leftLabel("Ширина (5–36)", theme));
        form.add(width);
        form.add(leftLabel("Висота (5–24)", theme));
        form.add(height);
        form.add(leftLabel("Кількість мін", theme));
        form.add(mines);
        dialog.getBody().add(form);
        dialog.getBody().add(Box.createVerticalStrut(10));
        dialog.getBody().add(limit);
        BoardConfig[] result = new BoardConfig[1];
        RoundedButton create = dialog.actionButton("Створити", true, () -> {
            try {
                width.commitEdit();
                height.commitEdit();
                mines.commitEdit();
                int w = (int) width.getValue(), h = (int) height.getValue(), m = (int) mines.getValue();
                if (w < 5 || w > 36 || h < 5 || h > 24 || m < 1 || m > w * h - 9) {
                    limit.setText("Перевірте введені значення");
                    return;
                }
                result[0] = new BoardConfig(w, h, m);
                dialog.dispose();
            } catch (java.text.ParseException ex) {
                limit.setText("Перевірте введені значення");
            }
        });
        dialog.addActions(dialog.actionButton("Скасувати", false, dialog::dispose), create);
        dialog.getRootPane().setDefaultButton(create);
        dialog.showDialog();
        return Optional.ofNullable(result[0]);
    }

    public static void records(JFrame owner, GameTheme theme, ScoreRecord scoreRecord) {
        StyledDialog dialog = new StyledDialog(owner, "Рекорди", "Найкращий час за складністю", theme);
        for (Difficulty d : Difficulty.values()) {
            if (d == Difficulty.CUSTOM) continue;
            RoundedPanel row = new RoundedPanel(12, theme);
            row.setLayout(new BorderLayout(18, 0));
            row.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
            row.add(leftLabel(d.getTitle(), theme), BorderLayout.WEST);
            String time = scoreRecord.getBestTime(d)
                    .map(s -> String.format("%02d:%02d", s / 60, s % 60)).orElse("Немає рекорду");
            JLabel value = new JLabel(time);
            value.setForeground(theme.getAccentPrimary());
            value.setFont(new Font("SansSerif", Font.BOLD, 14));
            row.add(value, BorderLayout.EAST);
            row.setAlignmentX(Component.CENTER_ALIGNMENT);
            row.setPreferredSize(new Dimension(360, 48));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            dialog.getBody().add(row);
            dialog.getBody().add(Box.createVerticalStrut(8));
        }
        dialog.addActions(dialog.actionButton("Закрити", true, dialog::dispose));
        dialog.showDialog();
    }

    public static Optional<GameTheme> settings(JFrame owner, GameTheme theme) {
        StyledDialog dialog = new StyledDialog(owner, "Налаштування", "Оберіть оформлення гри", theme);
        GameTheme[] themes = {new TwilightTheme(), new PastelLilacTheme()};
        JComboBox<String> themesBox = new JComboBox<>(new String[]{themes[0].getName(), themes[1].getName()});
        themesBox.setSelectedItem(theme.getName());
        themesBox.setForeground(theme.getTextPrimary());
        themesBox.setBackground(theme.getHiddenCellBg());
        themesBox.setFont(new Font("SansSerif", Font.BOLD, 14));
        themesBox.setPreferredSize(new Dimension(360, 40));
        themesBox.setMaximumSize(new Dimension(360, 40));
        themesBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        dialog.getBody().add(themesBox);
        GameTheme[] result = new GameTheme[1];
        RoundedButton apply = dialog.actionButton("Застосувати", true, () -> {
            result[0] = themes[themesBox.getSelectedIndex()];
            dialog.dispose();
        });
        dialog.addActions(dialog.actionButton("Скасувати", false, dialog::dispose), apply);
        dialog.getRootPane().setDefaultButton(apply);
        dialog.showDialog();
        return Optional.ofNullable(result[0]);
    }

    private static JLabel leftLabel(String text, GameTheme theme) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(theme.getTextPrimary());
        return label;
    }

    private static JSpinner spinner(int value, int min, int max, GameTheme theme) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, 1));
        spinner.setPreferredSize(new Dimension(160, 34));
        JFormattedTextField field = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        field.setFont(new Font("SansSerif", Font.BOLD, 14));
        field.setBackground(theme.getHiddenCellBg());
        field.setForeground(theme.getTextPrimary());
        spinner.setBorder(BorderFactory.createLineBorder(theme.getPanelBorder()));
        return spinner;
    }
}
