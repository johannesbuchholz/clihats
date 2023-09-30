package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.InputArgument;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class OptionParserNameTest {

    @Test
    public void optionNameSorting_posixConformFirst() {
        AbstractOptionParser.OptionParserName posixConform = AbstractOptionParser.OptionParserName.of("-z");
        AbstractOptionParser.OptionParserName other = AbstractOptionParser.OptionParserName.of("--abc");
        assertTrue(posixConform.compareTo(other) < 0);
    }

    @Test
    public void optionNameSorting_bothNonConformByValue() {
        AbstractOptionParser.OptionParserName name1 = AbstractOptionParser.OptionParserName.of("--abc");
        AbstractOptionParser.OptionParserName name2 = AbstractOptionParser.OptionParserName.of("--xyz");
        assertTrue(name1.compareTo(name2) < 0);
    }

    @Test
    public void optionNameSorting_bothPosixConformValue() {
        AbstractOptionParser.OptionParserName name1 = AbstractOptionParser.OptionParserName.of("-a");
        AbstractOptionParser.OptionParserName name2 = AbstractOptionParser.OptionParserName.of("-z");
        assertTrue(name1.compareTo(name2) < 0);
    }

    @Test
    public void optionNameSorting_nullLast() {
        AbstractOptionParser.OptionParserName posixConform = AbstractOptionParser.OptionParserName.of("-a");
        AbstractOptionParser.OptionParserName nonConform = AbstractOptionParser.OptionParserName.of("--abc");
        assertTrue(posixConform.compareTo(null) < 0);
        assertTrue(nonConform.compareTo(null) < 0);
    }

    @Test
    public void createOptionName_positive() {
        List<String> names = List.of("-a", "--abcd", "-a-b-c", "--blubb", "--k", "---");
        List<Exception> exceptions = new ArrayList<>();
        for (String s : names) {
            try {
                AbstractOptionParser.OptionParserName.of(s);
            } catch (IllegalArgumentException e) {
                exceptions.add(e);
            }
        }
        assertTrue(exceptions.isEmpty());
    }

    @Test
    public void createOptionName_negative() {
        List<String> names = List.of("-", "", "--", "- -", "- ", "--a b-c",
                "--blubb ", " --k", "*", "a", "a-", "a  b", "abc");
        List<Exception> exceptions = new ArrayList<>();
        for (String s : names) {
            assertThrows(IllegalArgumentException.class, () -> AbstractOptionParser.OptionParserName.of(s));
        }
    }

    @Test
    public void createOptionName_IsPOSIXConform_positive() {
        List<String> names = List.of("-a", "-b", "-ö");
        List<AbstractOptionParser.OptionParserName> optionParserNames = names.stream().map(AbstractOptionParser.OptionParserName::of).collect(Collectors.toList());
        assertTrue(optionParserNames.stream().allMatch(AbstractOptionParser.OptionParserName::isPOSIXConformOptionName));
    }

    @Test
    public void createOptionName_IsPOSIXConform_negative() {
        List<String> names = List.of("-ab", "--a", "--a-b", "---",
                "-*", "-!", "-*", "-#", "-'", "-!", "-?", "-\"", "-§", "-$", "-%", "-&", "-/", "-(", "-)", "-=");
        List<AbstractOptionParser.OptionParserName> optionParserNames = names.stream().map(AbstractOptionParser.OptionParserName::of).collect(Collectors.toList());
        assertTrue(optionParserNames.stream().noneMatch(AbstractOptionParser.OptionParserName::isPOSIXConformOptionName));
    }

    @Test
    public void matchesInputArg_POSIX_POSIX_positive() {
        AbstractOptionParser.OptionParserName name = AbstractOptionParser.OptionParserName.of("-a");
        List<InputArgument> args = List.of(
                InputArgument.of("-a"),
                InputArgument.of("-abc"),
                InputArgument.of("-paq"),
                InputArgument.of("-rsa")
        );

        assertTrue(args.stream().allMatch(name::matches));
    }

    @Test
    public void matchesInputArg_POSIX_POSIX_negative() {
        AbstractOptionParser.OptionParserName name = AbstractOptionParser.OptionParserName.of("-a");
        List<InputArgument> args = List.of(
                InputArgument.of("-b"),
                InputArgument.of("-cde")
        );

        assertTrue(args.stream().noneMatch(name::matches));
    }

    @Test
    public void matchesInputArg_NONPOSIX_POSIX_negative() {
        AbstractOptionParser.OptionParserName name = AbstractOptionParser.OptionParserName.of("--a");
        List<InputArgument> args = List.of(
                InputArgument.of("-a"),
                InputArgument.of("-abc"),
                InputArgument.of("-paq"),
                InputArgument.of("-rsa")
        );

        assertTrue(args.stream().noneMatch(name::matches));
    }

    @Test
    public void matchesInputArg_POSIX_NONPOSIX_negative() {
        AbstractOptionParser.OptionParserName name = AbstractOptionParser.OptionParserName.of("-a");
        List<InputArgument> args = List.of(
                InputArgument.of("--a"),
                InputArgument.of("--abc"),
                InputArgument.of("--a-bc"),
                InputArgument.of("--paq"),
                InputArgument.of("---rsa")
        );

        assertTrue(args.stream().noneMatch(name::matches));
    }

    @Test
    public void matchesInputArg_NONPOSIX_NONPOSIX_positive() {
        AbstractOptionParser.OptionParserName name = AbstractOptionParser.OptionParserName.of("--long");
        List<InputArgument> args = List.of(
                InputArgument.of("--long")
        );

        assertTrue(args.stream().allMatch(name::matches));
    }

    @Test
    public void matchesInputArg_NONPOSIX_NONPOSIX_negative() {
        AbstractOptionParser.OptionParserName name = AbstractOptionParser.OptionParserName.of("--long");
        List<InputArgument> args = List.of(
                InputArgument.of("--long-long"),
                InputArgument.of("--l"),
                InputArgument.of("--abcdefg")
        );

        assertTrue(args.stream().noneMatch(name::matches));
    }

    @Test
    public void matchesInputArg_POSIX_misc_negative() {
        AbstractOptionParser.OptionParserName name = AbstractOptionParser.OptionParserName.of("-a");
        List<InputArgument> args = List.of(
                InputArgument.of(""),
                InputArgument.of(""),
                InputArgument.of("a"),
                InputArgument.of("abc"),
                InputArgument.of("--"),
                InputArgument.of("a-"),
                InputArgument.of("aa-a a")
        );

        assertTrue(args.stream().noneMatch(name::matches));
    }

    @Test
    public void matchesInputArg_NONPOSIX_misc_negative() {
        AbstractOptionParser.OptionParserName name = AbstractOptionParser.OptionParserName.of("--a");
        List<InputArgument> args = List.of(
                InputArgument.of(""),
                InputArgument.of(""),
                InputArgument.of("a"),
                InputArgument.of("abc"),
                InputArgument.of("--"),
                InputArgument.of("a-"),
                InputArgument.of("aa-a a"),
                InputArgument.of("- a"),
                InputArgument.of("- -a"),
                InputArgument.of("-- a"),
                InputArgument.of(" --a"),
                InputArgument.of(" -a ")
        );

        assertTrue(args.stream().noneMatch(name::matches));
    }

    @Test
    public void getValueWithoutPrefix() {
        List<String> names = List.of("--a", "-a", "--abcd-xyz", "---", "-q");
        List<String> expected = List.of("a", "a", "abcd-xyz", "-", "q");

        List<String> actual = names.stream()
                .map(AbstractOptionParser.OptionParserName::of)
                .map(AbstractOptionParser.OptionParserName::getValueWithoutPrefix)
                .collect(Collectors.toList());

        assertEquals(expected, actual);
    }

}
