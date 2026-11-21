package ui.dialogs;

import ui.components.*;
import ui.theme.*;
import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends StyledDialog {
    private final GameTheme[] themes = { new TwilightTheme(), new PastelLilacTheme() };
    private GameTheme selected;
    public SettingsDialog(JFrame owner, GameTheme theme) {
        super(owner, "Налаштування", "Оформлення гри", theme);
        JComboBox<String> box = new JComboBox<>(new String[]{themes[0].getName(), themes[1].getName()});
        box.setSelectedItem(theme.getName());
        box.setFont(new Font("SansSerif", Font.PLAIN, 14));
        box.setBackground(theme.getHiddenCellBg());
        box.setForeground(theme.getTextPrimary());
        box.setMaximumSize(new Dimension(380, 38));
        box.setPreferredSize(new Dimension(380, 38));
        body.add(box);
        body.add(Box.createVerticalStrut(16));
        body.add(label("ПОПЕРЕДНІЙ ПЕРЕГЛЯД", 11, Font.BOLD, theme.getTextMuted()));
        body.add(Box.createVerticalStrut(8));
        ThemePreviewPanel preview = new ThemePreviewPanel(theme);
        preview.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(preview);
        box.addActionListener(e -> preview.setTheme(themes[box.getSelectedIndex()]));
        RoundedButton apply = actionButton("Застосувати", true, () -> {
            selected = themes[box.getSelectedIndex()];
            dispose();
        });
        addActions(actionButton("Скасувати", false, this::dispose), apply);
        getRootPane().setDefaultButton(apply);
    }
    public GameTheme getSelectedTheme() {
        return selected;
    }
}
