package ui.theme;

import java.awt.Color;

public class TwilightTheme implements GameTheme {
    @Override
    public String getName() {
        return "Сутінкова бузкова";
    }

    @Override
    public Color getWindowBackground() {
        return new Color(23, 18, 34);
    }

    @Override
    public Color getPanelBackground() {
        return new Color(36, 27, 53);
    }

    @Override
    public Color getPanelBorder() {
        return new Color(66, 48, 86);
    }

    @Override
    public Color getHiddenCellBg() {
        return new Color(89, 68, 115);
    }

    @Override
    public Color getHiddenCellHover() {
        return new Color(112, 85, 158);
    }

    @Override
    public Color getHiddenCellBorder() {
        return new Color(109, 86, 141);
    }

    @Override
    public Color getRevealedCellBg() {
        return new Color(27, 21, 40);
    }

    @Override
    public Color getRevealedCellBorder() {
        return new Color(47, 35, 66);
    }

    @Override
    public Color getAccentPrimary() {
        return new Color(232, 143, 192);
    }

    @Override
    public Color getAccentSecondary() {
        return new Color(196, 173, 245);
    }

    @Override
    public Color getTextPrimary() {
        return new Color(248, 242, 255);
    }

    @Override
    public Color getTextMuted() {
        return new Color(185, 168, 214);
    }

    @Override
    public Color getFlagColor() {
        return new Color(255, 115, 185);
    }

    @Override
    public Color getMineColor() {
        return new Color(255, 82, 125);
    }

    @Override
    public Color getNumberColor(int number) {
        return switch (number) {
            case 1 -> new Color(125, 211, 252);
            case 2 -> new Color(134, 239, 172);
            case 3 -> new Color(251, 146, 196);
            case 4 -> new Color(196, 181, 253);
            case 5 -> new Color(253, 186, 116);
            case 6 -> new Color(103, 232, 249);
            case 7 -> new Color(244, 114, 182);
            default -> new Color(248, 250, 252);
        };
    }
}
