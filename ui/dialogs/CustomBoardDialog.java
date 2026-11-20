package ui.dialogs;

import model.*;
import ui.components.*;
import ui.theme.GameTheme;
import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class CustomBoardDialog extends StyledDialog {
    private int width = 12, height = 12;
    private final JLabel sizeLabel, minesLabel;
    private final JSlider mines;
    private final RoundedButton create;
    private GameConfig result;

    public CustomBoardDialog(JFrame owner, GameTheme theme, GameConfig previous) {
        super(owner, "Власне поле", "Клацніть або перетягніть, щоб вибрати розмір", theme);
        sizeLabel = label("", 19, Font.BOLD, theme.getAccentPrimary());
        body.add(sizeLabel);
        body.add(Box.createVerticalStrut(12));
        BoardSizeSelector selector = new BoardSizeSelector(theme, this::sizeChanged);
        selector.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(selector);
        body.add(Box.createVerticalStrut(14));
        minesLabel = label("", 14, Font.BOLD, theme.getTextPrimary());
        body.add(minesLabel);
        mines = new JSlider(1, 135, 21);
        mines.setOpaque(false);
        mines.setForeground(theme.getAccentPrimary());
        mines.setPreferredSize(new Dimension(480, 34));
        mines.addChangeListener(e -> updateMineLabel());
        body.add(mines);
        create = actionButton("Створити", true, () -> {
            if (width >= 5 && height >= 5) {
                result = new GameConfig(Difficulty.CUSTOM, width, height, mines.getValue());
                dispose();
            }
        });
        addActions(actionButton("Скасувати", false, this::dispose), create);
        getRootPane().setDefaultButton(create);
        if (previous != null && previous.difficulty() == Difficulty.CUSTOM) {
            selector.select(previous.width(), previous.height());
            mines.setValue(previous.mines());
        } else selector.select(12, 12);
    }
    private void sizeChanged(int w, int h) {
        width = w;
        height = h;
        boolean valid = w >= 5 && h >= 5;
        sizeLabel.setForeground(valid ? theme.getAccentPrimary() : new Color(224, 74, 102));
        sizeLabel.setText(w + " × " + h + (valid ? "  •  " + (w*h) + " клітинок" : "  •  Мінімум 5 × 5"));
        create.setEnabled(valid);
        mines.setEnabled(valid);
        if (valid) {
            mines.setMaximum(w * h - 9);
            mines.setValue(Math.max(1, Math.round(w * h * 0.15f)));
        }
        updateMineLabel();
    }
    private void updateMineLabel() {
        minesLabel.setText(mines.isEnabled() ? String.format("Міни: %d  (%.0f%% поля)", mines.getValue(),
                100.0 * mines.getValue() / (width * height)) : "Спочатку виберіть допустимий розмір");
    }
    public Optional<GameConfig> choose() {
        showDialog();
        return Optional.ofNullable(result);
    }
}
