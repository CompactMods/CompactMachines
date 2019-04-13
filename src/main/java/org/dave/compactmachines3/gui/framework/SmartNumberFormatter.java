package org.dave.compactmachines3.gui.framework;

public class SmartNumberFormatter {

    public static String formatNumber(double value) {
        double decimalRange = 100;
        double normalRange = 100000;

        if(value > -decimalRange && value < decimalRange) {
            if(value == Math.round(value)) {
                return String.format("%d", Math.round(value));
            }

            return String.format("%.2f", value);
        }

        if(value > -normalRange && value < normalRange) {
            return String.format("%.0f", value);
        }

        return String.format("%.2e", value).toLowerCase();
    }
}
