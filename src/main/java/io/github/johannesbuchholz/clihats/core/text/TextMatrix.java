package io.github.johannesbuchholz.clihats.core.text;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Stores text cells row-wise. Not Threadsafe.
 */
public class TextMatrix {

    private final static String DEFAULT_HDELIM = " ";

    /*
     In a row, only null values follow a null value.
     */
    private TextCell[][] cells;
    private String hDelim;
    private int rowN;
    private int colN;

    public static TextMatrix empty() {
        return new TextMatrix(new TextCell[][]{}, DEFAULT_HDELIM);
    }

    private TextMatrix(TextCell[][] cells, String hDelim) {
        this.cells = cells;
        this.hDelim = hDelim;
        rowN = cells.length;
        colN = Arrays.stream(cells).mapToInt(a -> a.length).max().orElse(0);
    }

    public TextMatrix hDelim(String hDelim) {
        this.hDelim = hDelim;
        return this;
    }

    /**
     * Adds an empty row.
     */
    public TextMatrix row() {
        return rowInternal(new int[0], TextCell.getNew());
    }

    /**
     * Adds a new row placing each content string in a new cell and applies the given width to each added cell.
     */
    public TextMatrix row(int width, String... cellContents) {
        return row(new int[]{width}, cellContents);
    }

    /**
     * Adds a new row placing each content string in a new cell of width equal to the length of the respective content.
     */
    public TextMatrix row(String... cellContents) {
        return row(new int[0], cellContents);
    }

    /**
     * Adds a new row placing each content string in a new cell and applies the given widths to each added cell in
     * a cyclic manner.
     */
    public TextMatrix row(int[] widths, String... cellContents) {
        return rowInternal(
                widths,
                Arrays.stream(Objects.requireNonNull(cellContents))
                        .filter(Objects::nonNull)
                        .map(TextCell::getNew)
                        .toArray(TextCell[]::new)
        );
    }

    /**
     * Adds a new row containing the given cells.
     */
    public TextMatrix row(TextCell... rowEntries) {
        return rowInternal(new int[0], rowEntries);
    }

    public TextMatrix removeEmptyCols() {
        // collect empty col indexes
        Set<Integer> nonEmptyCols = new HashSet<>();
        for (int row = 0; row < rowN; row++)
            for (int col = 0; col < colN; col++)
                if (cells[row][col] != null && cells[row][col].hasContent())
                    nonEmptyCols.add(col);
        if (nonEmptyCols.size() == colN)
            // nothing to remove
            return this;
        // remove empty cols
        int colNOld = colN;
        colN = nonEmptyCols.size();
        TextCell[][] newCells = new TextCell[rowN][colN];
        for (int row = 0; row < rowN; row++) {
            int skippedCols = 0;
            for (int col = 0; col < colNOld; col++)
                if (nonEmptyCols.contains(col))
                    newCells[row][col - skippedCols] = cells[row][col];
                else
                    skippedCols++;
        }
        cells = newCells;
        return this;
    }

    /**
     * Shrinks every column by trimming every line of every cell.
     */
    public TextMatrix resizeColumnWidths() {
        for (int j = 0; j < colN; j++) {
            int finalJ = j;
            TextCell.reduceWidthUniformly(
                    IntStream.range(0, rowN)
                            .mapToObj(i -> cells[i][finalJ])
                            .collect(Collectors.toList())
            );
        }
        return this;
    }

    public List<String> getAllLines() {
        return Arrays.stream(cells)
                .flatMap(row -> getLinesOfRow(row).stream())
                .collect(Collectors.toList());
    }

    /**
     * Null entries not allowed.
     */
    private TextMatrix rowInternal(int[] widths, TextCell... rowEntries) {
        Objects.requireNonNull(widths);
        return rowInternal(
                IntStream.range(0, rowEntries.length)
                        .mapToObj(i -> rowEntries[i].copy().width(widths.length == 0 ? -1 : widths[i % widths.length]))
                        .toArray(TextCell[]::new)
        );
    }

    private TextMatrix rowInternal(TextCell[] rowEntries) {
        if (rowEntries == null)
            return this;
        // update row and col count
        int rowNOld = rowN;
        rowN = rowN + 1;
        colN = Math.max(colN, rowEntries.length);
        // copy, do not bother with heap space
        TextCell[][] newCells = new TextCell[rowN][colN];
        for (int row = 0; row < rowNOld; row++)
            newCells[row] = Arrays.copyOf(cells[row], colN);
        // add new row
        for (int col = 0; col < colN; col++)
            if (col < rowEntries.length)
                newCells[rowN - 1][col] =  rowEntries[col];
        cells = newCells;
        return this;
    }

    private List<String> getLinesOfRow(TextCell[] row) {
        if (row.length == 0)
            return new ArrayList<>();
        // determine total line count
        int rowLineCount = Arrays.stream(row)
                .filter(Objects::nonNull)
                .mapToInt(TextCell::getLineCount)
                .max().orElse(0);
        // get lines of row
        String[] lines = row[0].getLines(rowLineCount).toArray(String[]::new);
        int cellIndex = 1;
        TextCell cell;
        while (cellIndex < row.length && (cell = row[cellIndex]) != null) {
            List<String> currentCellLines = cell.getLines(rowLineCount);
            IntStream.range(0, rowLineCount)
                    .forEach(i -> lines[i] += hDelim + currentCellLines.get(i));
            cellIndex++;
        }
        return new ArrayList<>(List.of(lines));
    }

    @Override
    public String toString() {
        return String.join("\n", getAllLines());
    }

}
