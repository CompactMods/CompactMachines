package org.dave.cm2.utility;

import net.minecraft.util.text.TextFormatting;

public class TextFormattingHelper {

    public static String colorizeKeyValue(String input) {
        return colorizeKeyValue(input, TextFormatting.DARK_GREEN, TextFormatting.GRAY);
    }

    public static String colorizeKeyValue(String input, TextFormatting key, TextFormatting value) {
        if(!input.contains(":")) {
            return input;
        }

        String[] parts = input.split(":", 2);
        return key + parts[0] + ":" + TextFormatting.RESET + value + parts[1] + TextFormatting.RESET;
    }
}
