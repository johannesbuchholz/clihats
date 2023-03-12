package io.github.johannesbuchholz.clihats.util;

import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class JavadocUtilsTest {

    @Test
    public void javadocTextExtractionTest() {
        List<String> originals = List.of(
                "first line\n second line\nthird line and\nfourth line with text\n@param blubb paramtext\nsome trailing",
                "@param blubb paramtext\nsome trailing",
                "first line\n second line\nthird line and\n ---lul @param blubb paramtext\n    @param blubb paramtext\nsome trailing",
                "first line\n second line",
                ""
        );
        List<String> expected = List.of(
                "first line\n second line\nthird line and\nfourth line with text",
                "",
                "first line\n second line\nthird line and\n ---lul @param blubb paramtext",
                "first line\n second line",
                ""
        );
        List<String> actual = originals.stream()
                .map(JavadocUtils::getText)
                .collect(Collectors.toList());
        assertEquals(expected, actual);
    }

    @Test
    public void getIndexOfNextLinestartingPrefixTest() {
        List<String> originals = List.of(
                "first line\n second line\nthird line and\nfourth line with text\n@param blubb paramtext\nsome trailing",
                "\n@param blubb paramtext\nsome trailing",
                "@param blubb paramtext\nsome trailing",
                "first line\n second line\nthird line and\n ---lul @param blubb paramtext\n    @param blubb paramtext\nsome trailing",
                "first line\n second line",
                ""
        );
        List<Integer> expected = List.of(
                61,
                1,
                0,
                74,
                -1,
                -1
        );
        String prefix = "@param";
        List<Integer> actual = originals.stream()
                .map(original -> JavadocUtils.getIndexOfNextLinestartingPrefix(original, prefix, 0))
                .collect(Collectors.toList());
        assertEquals(expected, actual);
    }

    @Test
    public void getParamStrings() {
        // given
        String javaDocExample = "@param first first param in one line\n" +
                "@tag-not-known and some other text\n" +
                "@param a another param\n" +
                "@param @param param named @param with a \n" +
                "line break\n" +
                "    and some indent in yet another line\n" +
                "   \n" +
                "@see some link\n" +
                "@throws MyVerySpecialException some exception {@link MyClass#myMethod(String, Integer)\n" +
                "\n" +
                "\n" +
                "          \n     @param     xxX    @param some indented param\n" +
                "@param noDesc    \n" +
                "@param descInAnotherLine    \n" +
                "\n" +
                "  blubb\n" +
                "@param  \n" +
                "@param\n" +
                "@param final last entry";
        // when
        Map<String, String> actualParamMap = JavadocUtils.extractParamDoc(javaDocExample);

        // then
        Map<String, String> expectedParamMap = Map.of(
                "first", "first param in one line",
                "a", "another param",
                "@param", "param named @param with a line break and some indent in yet another line",
                "descInAnotherLine", "blubb",
                "xxX", "@param some indented param",
                "final", "last entry"
        );

        assertEquals(expectedParamMap, actualParamMap);
    }

}
