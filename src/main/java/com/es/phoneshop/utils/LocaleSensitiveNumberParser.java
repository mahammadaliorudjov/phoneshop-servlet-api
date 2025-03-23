package com.es.phoneshop.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class LocaleSensitiveNumberParser implements NumberParser {
    private final Locale locale;

    public LocaleSensitiveNumberParser(Locale locale) {
        this.locale = locale;
    }

    @Override
    public int parseInt(String intput) {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
        String cleanedString = intput.replace(String.valueOf(symbols.getGroupingSeparator()), "")
                .replace(String.valueOf(symbols.getDecimalSeparator()), ".");
        return (int) Double.parseDouble(cleanedString);
    }

    public boolean isIntegerAndPositive(String quantityString, Locale locale) {
        if (quantityString == null || quantityString.trim().isEmpty()) {
            return false;
        }
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
        char groupingSeparator = symbols.getGroupingSeparator();
        char decimalSeparator = symbols.getDecimalSeparator();
        String regex = "^\\d{1,3}(" +
                (groupingSeparator == '.' ? "\\." : String.valueOf(groupingSeparator)) +
                "?\\d{3})*" +
                (decimalSeparator == '.' ? "\\." : String.valueOf(decimalSeparator)) +
                "?\\d{0,2}$";
        if (!quantityString.trim().matches(regex)) {
            return false;
        }
        try {
            String cleanedString = quantityString.replace(String.valueOf(groupingSeparator), "")
                    .replace(String.valueOf(decimalSeparator), ".");
            double parsedValue = Double.parseDouble(cleanedString);
            return parsedValue >= 0 && parsedValue == Math.floor(parsedValue);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
