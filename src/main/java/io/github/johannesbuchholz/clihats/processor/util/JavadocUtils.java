package io.github.johannesbuchholz.clihats.processor.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JavadocUtils {

    /**
     * @return All lines not starting with '@' as String.
     */
    public static String getText(String javadoc) {
        String[] lines = javadoc.split("\n");
        return Arrays.stream(lines)
                .takeWhile(line -> !line.stripLeading().startsWith("@"))
                .collect(Collectors.joining("\n"));
    }

    public static Map<String, String> extractParamDoc(String methodJavadoc) {
        String startMarker = "@param";
        String endMarker = "@";
        List<String> collectedParamDocs = new ArrayList<>();
        int start = methodJavadoc.indexOf(startMarker);
        int end;
        while (start > -1) {
            end = getIndexOfNextLinestartingPrefix(methodJavadoc, endMarker, start + startMarker.length());
            end = end < 0 ? methodJavadoc.length() : end;
            collectedParamDocs.add(methodJavadoc.substring(start + startMarker.length(), end));
            start = methodJavadoc.indexOf(startMarker, end);
        }
        return collectedParamDocs.stream()
                .map(TextUtils::normalizeString)
                .map(line -> line.split("\\s", 2))
                .filter(split -> split.length > 1)
                .collect(Collectors.toMap(split -> split[0], split -> split[1]));
    }

    /**
     * Example:
     * """
     * I am a longer text\n
     *     PREFIX  bli bla blubb
     * """
     *     ^-- This Position.
     * @return the index of the first occurrence of the prefix after a new line character, starting from offset
     */
    static int getIndexOfNextLinestartingPrefix(String original, String prefix, int offset) {
        int currentOffset = offset;
        while (currentOffset > -1) {
            if (original.startsWith(prefix, currentOffset))
                return currentOffset;
            currentOffset = original.indexOf("\n", currentOffset);
            while (currentOffset > -1 && currentOffset < original.length() && Character.isWhitespace(original.charAt(currentOffset)))
                currentOffset++;
        }
        return -1;
    }
}
