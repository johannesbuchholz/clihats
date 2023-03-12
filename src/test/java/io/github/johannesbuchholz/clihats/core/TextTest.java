package io.github.johannesbuchholz.clihats.core;

import io.github.johannesbuchholz.clihats.core.execution.text.Alignment;
import io.github.johannesbuchholz.clihats.core.execution.text.TextCell;
import io.github.johannesbuchholz.clihats.core.execution.text.TextMatrix;
import io.github.johannesbuchholz.clihats.util.TextUtils;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TextTest {

    public static final String ORIGINAL = "this is a text and here comes a completely new Line.";
    public static final int LINE_WIDTH_18 = 18;
    public static final int LINE_WIDTH_4 = 4;

    @Test
    public void testCell_empty1() {
        TextCell textCell = TextCell.getNew();
        String expected = "";
        assertEquals(expected, textCell.toString());
    }

    @Test
    public void testCell_empty4() {
        TextCell textCell = TextCell.getNew(0);
        String expected = "";
        assertEquals(expected, textCell.toString());
    }

    @Test
    public void testMatrix_empty() {
        TextMatrix textMatrix = TextMatrix.empty();
        String expected = "";
        assertEquals(expected, textMatrix.toString());
    }

    @Test
    public void testCell_getAllLines() {
        List<String> expected = List.of("this is a text and", "here comes a      ", "completely new    ", "Line.             ");
        TextCell textCell = TextCell.getNew().width(LINE_WIDTH_18).paragraph(ORIGINAL);
        assertEquals(expected, textCell.getAllLines());
    }

    @Test
    public void testCell_Width0_expectNoContent() {
        TextCell textCell = TextCell.getNew().width(0);
        textCell.paragraph("some text never displayed")
                        .paragraph("another paragraph");

        List<String> actual = textCell.getAllLines();

        List<String> expected = List.of("");
        assertEquals(expected, actual);
    }

    @Test
    public void testCell_getAllLines_width4() {
        String text1 = "this is a text";
        List<String> expected = List.of("this", "is a", "text");
        TextCell textCell = TextCell.getNew().width(LINE_WIDTH_4).paragraph(text1);
        assertEquals(expected, textCell.getAllLines());
    }

    @Test
    public void testCellRows_firstTwoRows() {
        List<String> expected = List.of("this is a text and", "here comes a      ");
        TextCell textCell = TextCell.getNew(LINE_WIDTH_18).paragraph(ORIGINAL);
        assertEquals(expected, textCell.getLines(2));
    }

    @Test
    public void testCellAlignment_RIGHT() {
        List<String> expected = List.of(
                "this is a text and",
                "      here comes a",
                "    completely new",
                "             Line."
        );
        TextCell textCell = TextCell.getNew(LINE_WIDTH_18).align(Alignment.RIGHT).paragraph(ORIGINAL);
        assertEquals(expected, textCell.getAllLines());
    }

    @Test
    public void testCellAlignment_RIGHT_withEmptyLines() {
        List<String> expected = List.of(
                "this is a text and",
                "      here comes a",
                "    completely new",
                "             Line.",
                "                  ",
                "                  "
        );
        TextCell textCell = TextCell.getNew(LINE_WIDTH_18).align(Alignment.RIGHT).paragraph(ORIGINAL);
        assertEquals(expected, textCell.getLines(6));
    }

    @Test
    public void testCellAlignment_withBlankInput() {
        List<String> expected = List.of(
                "                  "
        );
        String text = "   ";
        TextCell textCellL = TextCell.getNew(LINE_WIDTH_18).align(Alignment.LEFT).paragraph(text);
        TextCell textCellR = TextCell.getNew(LINE_WIDTH_18).align(Alignment.RIGHT).paragraph(text);
        TextCell textCellC = TextCell.getNew(LINE_WIDTH_18).align(Alignment.CENTER).paragraph(text);

        assertEquals(expected, textCellL.getAllLines());
        assertEquals(expected, textCellR.getAllLines());
        assertEquals(expected, textCellC.getAllLines());
    }

    @Test
    public void testCellAlignment_CENTER() {
        List<String> expected = List.of(
                "this is a text and",
                "   here comes a   ",
                "  completely new  ",
                "      Line.       "
        );
        TextCell textCell = TextCell.getNew(LINE_WIDTH_18).align(Alignment.CENTER).paragraph(ORIGINAL);
        assertEquals(expected, textCell.getAllLines());
    }

    @Test
    public void testCell_tooNarrow() {
        int width = 5;
        String text = "too-much";
        List<String> expected = List.of(
                "too-m",
                "uch  "
        );

        TextCell textCell = TextCell.getNew(width).paragraph(text);
        assertEquals(expected, textCell.getAllLines());
    }

    @Test
    public void testRow() {
        int width = 4;
        String text1 = "this is a text";
        String text2 = "and this is another text";

        TextMatrix textRow = TextMatrix.empty()
                .hDelim("---")
                .row(TextCell.getNew(width).paragraph(text1), TextCell.getNew(width).paragraph(text2));

        List<String> expected = List.of(
                "this---and ",
                "is a---this",
                "text---is  ",
                "    ---anot",
                "    ---her ",
                "    ---text"
        );

        assertEquals(expected, textRow.getAllLines());
    }

    @Test
    public void testRow_alignments() {
        int width = 4;
        String text1 = "this is a text";
        String text2 = "and this is another text";

        TextMatrix textRow = TextMatrix.empty()
                .hDelim("---")
                .row(
                        TextCell.getNew(width).paragraph(text1),
                        TextCell.getNew(width).paragraph(text2),
                        TextCell.getNew(width).paragraph(text1),
                        TextCell.getNew(width).align(Alignment.RIGHT).paragraph("", "-r", "-r"),
                        TextCell.getNew(width).align(Alignment.CENTER).paragraph("", null, "", "4", "", "6", "7", "8", "9"));

        List<String> expected = List.of(
                "this---and ---this---    ---    ",
                "is a---this---is a---  -r---    ",
                "text---is  ---text---  -r--- 4  ",
                "    ---anot---    ---    ---    ",
                "    ---her ---    ---    --- 6  ",
                "    ---text---    ---    --- 7  ",
                "    ---    ---    ---    --- 8  ",
                "    ---    ---    ---    --- 9  "
        );
        assertEquals(expected, textRow.getAllLines());
    }

    @Test
    public void indentTest() {
        String s = String.join("\n", List.of(
                "public static void main(String[] args) {",
                "%s",
                "}",
                "Integer i = 25;",
                "String s = \"some string;\";",
                "Double d = 25.25;"
        ));
        String expected =
                "----public static void main(String[] args) {\n" +
                "----%s\n" +
                "----}\n" +
                "----Integer i = 25;\n" +
                "----String s = \"some string;\";\n" +
                "----Double d = 25.25;";

        String result = TextUtils.indentEveryLine(s, "----");
        assertEquals(expected, result);
    }

    @Test
    public void testRow_stripTrailingColumns_lowerLeftTriangle() {
        // given
        TextMatrix m = TextMatrix.empty()
                .hDelim("|")
                .row("1A")
                .row("2A", "2B");

        // when
        String actual = m.toString();

        //then
        String expected =
                "1A\n" +
                "2A|2B";
        assertEquals(expected, actual);
    }

    @Test
    public void testRow_stripTrailingColumns_upperLeftTriangle() {
        // given
        TextMatrix m = TextMatrix.empty()
                .hDelim("|")
                .row("1A", "1B")
                .row("2A");
        // when
        String actual = m.toString();

        //then
        String expected =
                "1A|1B\n" +
                "2A";
        assertEquals(expected, actual);
    }

    @Test
    public void testRow_stripIntermediateNulls() {
        // given
        TextMatrix m = TextMatrix.empty()
                .hDelim("|")
                .row("1A", "1B", null, "1C");
        // when
        String actual = m.toString();

        //then
        String expected =
                "1A|1B|1C";
        assertEquals(expected, actual);
    }

    @Test
    public void testRow_handleEmptyCells() {
        // given
        TextMatrix m = TextMatrix.empty()
                .hDelim("|")
                .row("1A", "1B", "", "1C");
        // when
        String actual = m.toString();

        //then
        String expected =
                "1A|1B||1C";
        assertEquals(expected, actual);
    }

    @Test
    public void testRow_applyUniformCellWidthPerRow() {
        // given
        TextMatrix m = TextMatrix.empty()
                .hDelim("|")
                .row(2, "", "", "")
                .row(1, "", "", "", "", "", "");
        // when
        String actual = m.removeEmptyCols().toString();

        //then
        String expected = "";
        assertEquals(expected, actual);
    }

    @Test
    public void testRow_removeEmptyCols_resultEmpty() {
        // given
        TextMatrix m = TextMatrix.empty()
                .hDelim("|")
                .row("", "", "")
                .row("", "", "", "", "", "");
        // when
        String actual = m.toString();

        //then
        String expected =
                "||" + "\n" +
                "|||||";
        assertEquals(expected, actual);
    }

    @Test
    public void testRow_removeEmptyCols() {
        // given
        TextMatrix m = TextMatrix.empty()
                .hDelim("|")
                .row(2, "", "1B", "", "1D", "", "", "")
                .row(2, "", "", "", "", "", "", "")
                .row(2, "", "", "", "", "", "", "1G");
        // when
        String actual = m.removeEmptyCols().toString();

        //then
        String expected =
                "1B|1D|  " + "\n" +
                "  |  |  " + "\n" +
                "  |  |1G";
        assertEquals(expected, actual);
    }

    @Test
    public void testMatrix_resizeColumnsToMaxParagraphWidth() {
        TextMatrix tm = TextMatrix.empty()
                .hDelim("|")
                .row(TextCell.getNew("One").width(6).align(Alignment.RIGHT), TextCell.getNew("Two").width(6).align(Alignment.LEFT), TextCell.getNew("Three").width(6).align(Alignment.RIGHT))
                .row(TextCell.getNew("   A").width(6).align(Alignment.RIGHT), TextCell.getNew("B").width(6).align(Alignment.LEFT), TextCell.getNew("C").width(6).align(Alignment.LEFT));
        String expectedOriginal =
                "   One|Two   | Three" + "\n" +
                "     A|B     |C     ";
        String expectedResized =
                "One|Two|Three" + "\n" +
                "  A|B  |C    ";

        assertEquals(expectedOriginal, tm.toString());
        assertEquals(expectedResized, tm.resizeColumnWidths().toString());
    }

    @Test
    public void testMatrix_autoAdjustColumnWidth_paragraphWiderThanCellWidth() {
        TextMatrix tm = TextMatrix.empty()
                .hDelim("|")
                .row(TextCell.getNew("One").width(4).align(Alignment.RIGHT), TextCell.getNew("Two").width(4).align(Alignment.LEFT), TextCell.getNew("Three").width(4).align(Alignment.RIGHT))
                .row(TextCell.getNew("   A").width(4).align(Alignment.RIGHT), TextCell.getNew("B").width(4).align(Alignment.LEFT), TextCell.getNew("C").width(4).align(Alignment.LEFT));
        String expectedOriginal =
                " One|Two |Thre" + "\n" +
                "    |    |   e" + "\n" +
                "   A|B   |C   ";
        String expectedResized =
                "One|Two|Thre" + "\n" +
                "   |   |   e" + "\n" +
                "  A|B  |C   ";
        assertEquals(expectedOriginal, tm.toString());
        assertEquals(expectedResized, tm.resizeColumnWidths().toString());
    }

}
