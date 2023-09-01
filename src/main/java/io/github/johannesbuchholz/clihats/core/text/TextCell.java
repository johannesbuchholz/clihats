package io.github.johannesbuchholz.clihats.core.text;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a text block of specific character-width.
 */
public class TextCell {

    private final List<String> paragraphs;
    private int width;
    private Alignment alignment;

    /**
     * Performs side effects on submitted cells.
     */
    static void reduceWidthUniformly(List<TextCell> cells) {
        // determine maximum actual width among all cell paragraphs
        int maxActualWidth = 0;
        for (TextCell c : cells) {
            if (c == null)
                continue;
            int maxParagraphWidth = c.paragraphs.stream()
                    .mapToInt(p -> Math.min(c.width, p.length()))
                    .max().orElse(0);
            if (maxParagraphWidth > maxActualWidth)
                maxActualWidth = maxParagraphWidth;
        }
        // set cell widths to upper bound
        for (TextCell c : cells)
             if (c != null)
                 c.width = maxActualWidth;
    }

    public static TextCell getNew(Collection<String> lines) {
        return getNew(lines.toArray(String[]::new));
    }

    /**
     * Adds the specified Strings as lines to a new cell and sets its width to the maximum String length.
     */
    public static TextCell getNew(String... lines) {
        return new TextCell(
                Arrays.stream(lines).filter(Objects::nonNull).collect(Collectors.toList()),
                Arrays.stream(lines).filter(Objects::nonNull).mapToInt(String::length).max().orElse(0),
                Alignment.LEFT
        );
    }

    /**
     * Adds the given line to this cell and sets the width of this to the length of the given line.
     */
    public static TextCell getNew(String line) {
        if (line == null)
            return TextCell.getNew();
        return new TextCell(List.of(line), line.length(), Alignment.LEFT);
    }

    public static TextCell getNew(int width) {
        return new TextCell(List.of(), width, Alignment.LEFT);
    }

    /**
     * New cell with width {@code 0}.
     */
    public static TextCell getNew() {
        return getNew(0);
    }

    private TextCell(List<String> paragraphs, int width, Alignment alignment) {
        this.paragraphs = Objects.requireNonNull(paragraphs).stream().map(String::trim).collect(Collectors.toList());
        this.width = width;
        this.alignment = alignment;
    }

    public TextCell copy() {
        return new TextCell(paragraphs, width, alignment);
    }

    /**
     * Ignores negative values.
     */
    public TextCell width(int width) {
        if (width >= 0) {
            this.width = width;
        }
        return this;
    }

    public TextCell align(Alignment alignment) {
        this.alignment = Objects.requireNonNull(alignment);
        return this;
    }

    public TextCell paragraph(List<String> texts) {
        if (texts != null)
            texts.stream()
                    .filter(Objects::nonNull)
                    .forEach(paragraphs::add);
        return this;
    }

    public TextCell paragraph(String... texts) {
        if (texts == null)
            return this;
        return paragraph(Arrays.asList(texts));
    }

    /**
     * Iterates over stored paragraphs and puts them into lines of text according to the width and alignment of this Cell.
     * Will always contain at least one line.
     */
    public List<String> getAllLines() {
        if (width < 1 )
            return new LinkedList<>(List.of(""));
        if (paragraphs.isEmpty())
            return new LinkedList<>(List.of(alignment.align("", width)));
        return paragraphs.stream()
                .flatMap(p -> generateLinesFromText(p).stream())
                .map(l -> alignment.align(l, width))
                .collect(Collectors.toList());
    }

    /**
     * Get lines up to given number exclusively.
     */
    public List<String> getLines(int numberOfRows) {
        if (numberOfRows < 0) {
            throw new IllegalArgumentException("number of rows must not be negative");
        }
        List<String> outLines = getAllLines();
        if (numberOfRows > outLines.size()) {
            for (int i = outLines.size(); i < numberOfRows; i++) {
                outLines.add(" ".repeat(width));
            }
        }
        return outLines.subList(0, numberOfRows);
    }

    /**
     * true iff this contains at least one non-empty paragraph.
     */
    public boolean hasContent() {
        return paragraphs.stream().anyMatch(p -> !p.isEmpty());
    }

    public int getLineCount() {
        return getAllLines().size();
    }

    public int getWidth() {
        return width;
    }

    @Override
    public String toString() {
        return String.join("\n", getAllLines());
    }

    private List<String> generateLinesFromText(String text) {
        if (width < 1)
            return new LinkedList<>();
        if (text.isEmpty())
            return new LinkedList<>(List.of(""));
        List<String> lines = new LinkedList<>();
        int offset = 0;
        int endOfNewLine;
        while (offset < text.length()) {
            // increase offset until non-space char
            while (offset < text.length() && text.charAt(offset) == ' ') {
                offset += 1;
            }
            endOfNewLine = Math.min(text.length(), offset + width);
            // move endOfNewLine backwards until space
            if (endOfNewLine < text.length() && text.charAt(endOfNewLine) != ' ') {
                int indexOfLastSpace = text.substring(0, endOfNewLine).lastIndexOf(" ");
                endOfNewLine = indexOfLastSpace > offset ? indexOfLastSpace : endOfNewLine;
            }
            lines.add(text.substring(offset, endOfNewLine));
            offset = endOfNewLine;
        }
        return lines;
    }

}
