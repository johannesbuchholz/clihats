package io.github.johannesbuchholz.clihats.core.text;

import java.util.function.BiFunction;

public enum Alignment {
    LEFT((s, i) -> {
        int padTotal = Math.max(0, i - s.length());
        return s + " ".repeat(padTotal);
    }),
    RIGHT((s, i) -> {
        int padTotal = Math.max(0, i - s.length());
        return " ".repeat(padTotal) + s;
    }),
    CENTER((s, i) -> {
        int padTotal = Math.max(0, i - s.length());
        int padLeft = padTotal/2;
        return " ".repeat(padLeft) + s + " ".repeat(padTotal - padLeft);
    });

    private final BiFunction<String, Integer, String> alignmentFunction;

    Alignment(BiFunction<String, Integer, String> alignmentFunction) {
        this.alignmentFunction = alignmentFunction;
    }

    /**
     * Aligns the given string within the given width.
     * If the given string is null or longer than the given width this method returns the string as is.
     */
    public String align(String original, int width) {
        if (width < 0) {
            throw  new IllegalArgumentException("Width must not be negative: " + width);
        }
        if (original == null || original.length() >= width) {
            return original;
        }
        return alignmentFunction.apply(original, width);
    }

}
