package io.github.johannesbuchholz.clihats.processor.util;

import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TextUtilsTest {

    @Test
    public void testLineIndent_correctIndentOnEveryLine() {
        String s = "abc\nxyz";
        String expected = "    abc\n    xyz";
        String actual = TextUtils.indentEveryLine(s);
        assertEquals(expected, actual);
    }

    @Test
    public void testLineIndent_doNotIndentEmpty() {
        String s = "";
        String expected = "";
        String actual = TextUtils.indentEveryLine(s);
        assertEquals(expected, actual);
    }

    @Test
    public void testNormalization() {
        String s = "this      is a \t str or \n string \t\t\n\t with many \t\r  \r\n\r   \n strange characters.\n    Indeed  . ";
        String e = "this is a str or string with many strange characters. Indeed .";
        String r = TextUtils.normalizeString(s);
        assertEquals(e, r);
    }

    @Test
    public void hyphenStringTest() {
        List<String> originals = List.of(
                "myHyphenString", "", "a b", "xY", "-", "---", "simple", "Big", "MyOtherString With Space", "some-value", "HereWeAre-YeeHaa", "Separated Words", "more separated words", "myLongerWordWith1Number"
        );
        List<String> expected = List.of(
                "my-hyphen-string", "", "ab", "x-y", "-", "---", "simple", "big", "my-other-string-with-space", "some-value", "here-we-are-yee-haa", "separated-words", "moreseparatedwords", "my-longer-word-with-1-number"
        );

        List<String> results = originals.stream()
                .map(TextUtils::toHyphenString)
                .collect(Collectors.toList());

        assertEquals(expected, results);
    }

    @Test
    public void toUppercaseStringTest() {
        List<String> originals = List.of(
                "myHyphenString", "", "a b", "xY", "-", "---", "simple", "Big", "MyOtherString With Space", "some-value", "HereWeAre-YeeHaa", "Separated Words", "more separated words", "myLongerWordWith1Number"
        );
        List<String> expected = List.of(
                "MY_HYPHEN_STRING", "", "AB", "X_Y", "-", "---", "SIMPLE", "BIG", "MY_OTHER_STRING_WITH_SPACE", "SOME-VALUE", "HERE_WE_ARE-_YEE_HAA", "SEPARATED_WORDS", "MORESEPARATEDWORDS", "MY_LONGER_WORD_WITH_1_NUMBER"
        );

        List<String> results = originals.stream()
                .map(TextUtils::toUpperCaseString)
                .collect(Collectors.toList());

        assertEquals(expected, results);
    }

    @Test
    public void hyphenStringTest_Null() {
        Throwable npe = null;
        try {
            TextUtils.toHyphenString(null);
        } catch (NullPointerException e) {
            npe = e;
        }
        assertNotNull(npe);
    }

    @Test
    public void testEscaping() {
        String s = "some text with \"quotes\".";
        String actual = TextUtils.normalizeString(s);
        String expected = "some text with \\\"quotes\\\".";
        assertEquals(expected, actual);
    }

    // testcase name, input string, args, expected outcome
    private static final Object[][] formattingCases = new Object[][] {
            {"simple replacement", "Hello {}", new Object[]{"World"}, "Hello World"},
            {"multiple placeholders", "{} + {} = {}", new Object[]{1, 2, 3}, "1 + 2 = 3"},
            {"extra args ignored", "Hello {}", new Object[]{"World", "ignored"}, "Hello World"},
            {"missing args", "Hello {} {}", new Object[]{"World"}, "Hello World {}"},
            {"no placeholders", "Hello World", new Object[]{"unused"}, "Hello World"},
            {"null args", "Hello {}", null, "Hello {}"},
            {"empty args", "Hello {}", new Object[]{}, "Hello {}"},
            {"adjacent placeholders", "{}{}{}", new Object[]{1, 2, 3}, "123"},
            {"partial fill", "{}{}{}", new Object[]{1}, "1{}{}"},
            {"non-placeholder braces", "{text}", new Object[]{"X"}, "{text}"},
            {"empty string", "", new Object[]{"x"}, ""},
            {"single open brace", "{", null, "{"},
            {"single close brace", "}", null, "}"},
            {"unclosed brace at end", "Hello {", null, "Hello {"},
            {"leading open brace", "{text", null, "{text"},
            {"closing brace only pattern", "text}", null, "text}"},
            {"standalone braces no placeholder", "{a}", new Object[]{"X"}, "{a}"},
            {"adjacent broken braces", "{}{", new Object[]{1}, "1{"},
            {"double open braces", "{{", null, "{{"},
            {"double close braces", "}}", null, "}}"}
    };

    @Test
    public void format_shouldHandleEdgeCases() {
        for (Object[] formattingCase : formattingCases) {
            // given
            String name = (String) formattingCase[0];
            String input = (String) formattingCase[1];
            Object[] args = (Object[]) formattingCase[2];
            String expected = (String) formattingCase[3];

            // when
            String actual = TextUtils.format(input, args);

            // then
            assertEquals("Failed: " + name, expected, actual);
        }
    }

}
