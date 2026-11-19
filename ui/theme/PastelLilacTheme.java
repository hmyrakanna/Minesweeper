package ui.theme;

import java.awt.Color;

public class PastelLilacTheme implements GameTheme {
    @Override
    public String getName() {
        return "Пастельна бузкова";
    }

    @Override
    public Color getWindowBackground() {
        return new Color(216, 194, 238);
    }

    @Override
    public Color getPanelBackground() {
        return new Color(236, 220, 248);
    }

    @Override
    public Color getPanelBorder() {
        return new Color(179, 142, 214);
    }

    @Override
    public Color getHiddenCellBg() {
        return new Color(168, 145, 198);
    }

    @Override
    public Color getHiddenCellHover() {
        return new Color(186, 164, 214);
    }

    @Override
    public Color getHiddenCellBorder() {
        return new Color(212, 192, 236);
    }

    @Override
    public Color getRevealedCellBg() {
        return new Color(250, 244, 255);
    }

    @Override
    public Color getRevealedCellBorder() {
        return new Color(214, 195, 232);
    }

    @Override
    public Color getAccentPrimary() {
        return new Color(224, 102, 166);
    }

    @Override
    public Color getAccentSecondary() {
        return new Color(152, 112, 204);
    }

    @Override
    public Color getTextPrimary() {
        return new Color(62, 38, 86);
    }

    @Override
    public Color getTextMuted() {
        return new Color(118, 92, 145);
    }

    @Override
    public Color getFlagColor() {
        return new Color(219, 76, 152);
    }

    @Override
    public Color getMineColor() {
        return new Color(190, 42, 96);
    }

    @Override
    public Color getNumberColor(int number) {
        return switch (number) {
            case 1 -> new Color(56, 130, 214);
            case 2 -> new Color(34, 148, 92);
            case 3 -> new Color(204, 58, 128);
            case 4 -> new Color(118, 66, 196);
            case 5 -> new Color(194, 92, 32);
            case 6 -> new Color(24, 142, 168);
            case 7 -> new Color(156, 42, 118);
            default -> new Color(62, 38, 86);
        };
    }
}