package io.github.johannesbuchholz.clihats.util;

import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

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
                "myHyphenString", "", "simple", "Big", "MyOtherString", "some-value", "HereWeAre-YeeHaa", "separated Words", "myLongerWordWith1Number"
        );
        List<String> expected = List.of(
                "my-hyphen-string", "", "simple", "big", "my-other-string", "some-value", "here-we-are-yee-haa", "separated-words", "my-longer-word-with-1-number"
        );

        List<String> results = originals.stream()
                .map(TextUtils::toHyphenString)
                .collect(Collectors.toList());

        assertEquals(expected, results);
    }

    @Test
    public void testEscaping() {
        String s = "some text with \"quotes\".";
        String actual = TextUtils.normalizeString(s);
        String expected = "some text with \\\"quotes\\\".";
        System.out.println(s);
        System.out.println(actual);
        System.out.println(expected);
        assertEquals(expected, actual);
    }

}
