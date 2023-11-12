package io.github.johannesbuchholz.clihats.processor.util;

import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TextUtilsTest {

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

}
