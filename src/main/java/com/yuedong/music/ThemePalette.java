package com.yuedong.music;

import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;

public final class ThemePalette {
    public static final String MODE_DARK = "dark";
    public static final String MODE_LIGHT = "light";
    public static final String MODE_COFFEE = "coffee";
    public static final String MODE_VSCODE = "vscode";
    public static final String MODE_CUSTOM = "custom";
    
    public static final String ANIM_NONE = "none";
    public static final String ANIM_FADE = "fade";
    public static final String ANIM_SCROLL = "scroll";
    public static final String ANIM_TYPEWRITER = "typewriter";

    public final int bg;
    public final int surface;
    public final int accent;
    public final int accentDim;
    public final int text;
    public final int textSecondary;
    public final int textTertiary;
    public final int textOnColor;
    public final int inputBg;
    public final int borderLight;

    private ThemePalette(int bg, int surface, int accent, int accentDim, int text, int textSecondary, int textTertiary, int textOnColor, int inputBg, int borderLight) {
        this.bg = bg;
        this.surface = surface;
        this.accent = accent;
        this.accentDim = accentDim;
        this.text = text;
        this.textSecondary = textSecondary;
        this.textTertiary = textTertiary;
        this.textOnColor = textOnColor;
        this.inputBg = inputBg;
        this.borderLight = borderLight;
    }

    public static ThemePalette forMode(String mode) {
         if (MODE_LIGHT.equals(mode)) return light();
         if (MODE_COFFEE.equals(mode)) return coffee();
         if (MODE_VSCODE.equals(mode)) return vscode();
         return dark();
    }

    public ThemePalette withCustomColors(Map<String, String> customColors) {
        if (customColors == null || customColors.isEmpty()) {
            return this;
        }
        int newBg = parseColor(customColors.get("bg"), this.bg);
        int newAccent = parseColor(customColors.get("accent"), this.accent);
        
        return new ThemePalette(
            newBg, this.surface, newAccent, this.accentDim, 
            this.text, this.textSecondary, this.textTertiary, this.textOnColor, 
            this.inputBg, this.borderLight
        );
    }

    private static int parseColor(String hex, int fallback) {
        if (hex == null || hex.isEmpty()) return fallback;
        try {
            return Color.parseColor(hex);
        } catch (Exception e) {
            return fallback;
        }
    }

    public static boolean isHexColor(String value) {
        return value != null && value.matches("#([0-9a-fA-F]{6}|[0-9a-fA-F]{8})");
    }

    private static ThemePalette dark() {
        return new ThemePalette(
            Color.parseColor("#000000"), // bg
            Color.parseColor("#0A0A0A"), // surface
            Color.parseColor("#30D158"), // accent
            Color.parseColor("#1A3A2A"), // accentDim
            Color.parseColor("#FFFFFF"), // text
            Color.parseColor("#8E8E93"), // textSecondary
            Color.parseColor("#636366"), // textTertiary
            Color.parseColor("#FFFFFF"), // textOnColor
            Color.parseColor("#1C1C1E"), // inputBg
            Color.parseColor("#2C2C2E")  // borderLight
        );
    }

    private static ThemePalette light() {
        return new ThemePalette(
            Color.parseColor("#F5F5F7"), Color.parseColor("#FFFFFF"), Color.parseColor("#007AFF"),
            Color.parseColor("#D1E8FF"), Color.parseColor("#000000"), Color.parseColor("#8E8E93"),
            Color.parseColor("#AEAEB2"), Color.parseColor("#FFFFFF"), Color.parseColor("#FFFFFF"),
            Color.parseColor("#E5E5EA")
        );
    }

    private static ThemePalette coffee() {
        return new ThemePalette(
            Color.parseColor("#F4EFE6"), Color.parseColor("#EEE5D8"), Color.parseColor("#D97757"),
            Color.parseColor("#F1D4C6"), Color.parseColor("#2B2118"), Color.parseColor("#6C5A49"),
            Color.parseColor("#9B8976"), Color.parseColor("#FFFFFF"), Color.parseColor("#FFFBF3"),
            Color.parseColor("#E8DDCF")
        );
    }

    private static ThemePalette vscode() {
        return new ThemePalette(
            Color.parseColor("#1E1E1E"), Color.parseColor("#252526"), Color.parseColor("#007ACC"),
            Color.parseColor("#073A5A"), Color.parseColor("#D4D4D4"), Color.parseColor("#A6A6A6"),
            Color.parseColor("#6A6A6A"), Color.parseColor("#FFFFFF"), Color.parseColor("#3C3C3C"),
            Color.parseColor("#454545")
        );
    }
}
