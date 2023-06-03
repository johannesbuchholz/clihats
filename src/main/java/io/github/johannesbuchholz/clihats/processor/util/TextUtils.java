package io.github.johannesbuchholz.clihats.processor.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class TextUtils {

    /**
     * Indent every line with four spaces.
     */
    public static String indentEveryLine(String original) {
        return indentEveryLine(original, "    ");
    }

    /**
     * Puts indent before every line that is terminated by "\n".
     */
    public static String indentEveryLine(String original, String indent) {
        return Arrays.stream(original.split("\n"))
                .filter(line -> !line.isBlank())
                .map(line -> indent + line)
                .collect(Collectors.joining("\n"));
    }

    public static String quote(String input) {
        return "\"" + input + "\"";
    }

    /**
     * Removes consecutive whitespaces, removes tabs and newline and trims, escapes undesired characters.
     */
    public static String normalizeString(String original) {
        return original
                .replaceAll("[\\n\\t\\r]", "")
                .replaceAll("\\s{2,}", " ")
                .replace("\"", "\\\"")
                .trim();
    }

    /**
     * Camelcase to hyphen-separated.
     * <p>
     * Example: "myLongerWordWith1Number" becomes "my-longer-word-with-1-number"
     * </p>
     */
    public static String toHyphenString(String original) {
        Objects.requireNonNull(original);
        StringBuilder sb = new StringBuilder();
        original.chars()
                .filter(c -> !Character.isSpaceChar(c))
                .forEach(c -> {
                    if (Character.isDigit(c) || (Character.isAlphabetic(c) && Character.isUpperCase(c))) {
                        if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '-')
                            sb.append("-");
                    }
                    sb.appendCodePoint(Character.toLowerCase(c));
                });
        return sb.toString();
    }

    /**
     * Returns the original string but the first letter is changed to upper case.
     */
    public static String uppercaseFirst(String original) {
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

}