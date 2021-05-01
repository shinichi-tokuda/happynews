package io.github.pureza.happynews.util;

import java.util.Locale;

/**
 * String utility methods
 */
public class Strings {

    private Strings() {
        // This class can't be instantiated
    }


    /**
     * Converts a piece of text to sentence case
     */
    public static String toSentenceCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return text.substring(0, 1).toUpperCase(Locale.US) + text.substring(1).toLowerCase(Locale.US);
    }
}
