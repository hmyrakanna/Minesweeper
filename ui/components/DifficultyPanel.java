package ui.components;

import model.Difficulty;
import ui.theme.GameTheme;

import javax.swing.*;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class DifficultyPanel extends RoundedPanel {
    private final Map<Difficulty, RoundedButton> buttons = new EnumMap<>(Difficulty.class);
    private final JLabel title = new JLabel("СКЛАДНІСТЬ");
    private final JLabel controlsTitle = new JLabel("КЕРУВАННЯ");
    private final JLabel[] hints = {
            new JLabel("ЛКМ — відкрити клітинку"),
            new JLabel("ПКМ — поставити прапорець"),
            new JLabel("Shift + ЛКМ — відкрити сусідів")
    };

    public DifficultyPanel(GameTheme theme, Consumer<Difficulty> onSelect) {
        super(18, theme);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(18, 14, 18, 14));
        setPreferredSize(new Dimension(244, 0));

        title.setFont(new Font("SansSerif", Font.BOLD, 12));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(title);
        add(Box.createVerticalStrut(14));

        for (Difficulty difficulty : Difficulty.values()) {
            String label = difficulty == Difficulty.CUSTOM ? "Власне поле…"
                    : difficulty.getTitle() + " · " + difficulty.getWidth() + "×" + difficulty.getHeight();
            RoundedButton button = new RoundedButton(label, 12, theme);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setPreferredSize(new Dimension(210, 44));
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            button.setMinimumSize(new Dimension(160, 44));
            button.addActionListener(e -> onSelect.accept(difficulty));
            buttons.put(difficulty, button);
            add(button);
            add(Box.createVerticalStrut(8));
        }

        add(Box.createVerticalGlue());
        controlsTitle.setFont(title.getFont());
        controlsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(controlsTitle);
        add(Box.createVerticalStrut(10));
        for (JLabel hint : hints) {
            hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
            hint.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(hint);
            add(Box.createVerticalStrut(8));
        }
        setTheme(theme);
    }

    public void setSelected(Difficulty selected) {
        buttons.forEach((difficulty, button) -> button.setActive(difficulty == selected));
    }

    @Override
    public void setTheme(GameTheme theme) {
        super.setTheme(theme);
        title.setForeground(theme.getTextMuted());
        controlsTitle.setForeground(theme.getTextMuted());
        for (JLabel hint : hints) hint.setForeground(theme.getTextPrimary());
        buttons.values().forEach(button -> button.setTheme(theme));
    }
}
