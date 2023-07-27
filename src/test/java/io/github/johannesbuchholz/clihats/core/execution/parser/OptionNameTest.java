package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.InputArgument;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OptionNameTest {

    @Test
    public void createOptionName_positive() {
        List<String> names = List.of("-a", "--abcd", "-a-b-c", "--blubb", "--k");
        List<Exception> exceptions = new ArrayList<>();
        for (String s : names) {
            try {
                AbstractOptionParser.OptionName.of(s);
            } catch (IllegalArgumentException e ) {
                exceptions.add(e);
            }
        }

        assertTrue(exceptions.isEmpty());
    }

    @Test
    public void createOptionName_negative() {
        List<String> names = List.of("-", "", "--", "- -", "- ", "--a b-c", "--blubb ", " --k", "*", "a", "a-", "a  b", "abc");
        List<Exception> exceptions = new ArrayList<>();
        for (String s : names) {
            try {
                System.out.println(AbstractOptionParser.OptionName.of(s));
            } catch (IllegalArgumentException e ) {
                exceptions.add(e);
            }
        }

        assertEquals(names.size(), exceptions.size());
    }

    @Test
    public void createOptionName_IsPOSIXConform_positive() {
        List<String> names = List.of("-a", "-b", "-ö");
        List<AbstractOptionParser.OptionName> optionNames = names.stream().map(AbstractOptionParser.OptionName::of).collect(Collectors.toList());
        assertTrue(optionNames.stream().allMatch(AbstractOptionParser.OptionName::isPOSIXConformOptionName));
    }

    @Test
    public void createOptionName_IsPOSIXConform_negative() {
        List<String> names = List.of("-ab", "--a", "--a-b");
        List<AbstractOptionParser.OptionName> optionNames = names.stream().map(AbstractOptionParser.OptionName::of).collect(Collectors.toList());
        assertTrue(optionNames.stream().noneMatch(AbstractOptionParser.OptionName::isPOSIXConformOptionName));
    }

    @Test
    public void matchesInputArg_POSIX_POSIX_positive() {
        AbstractOptionParser.OptionName name = AbstractOptionParser.OptionName.of("-a");
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
        AbstractOptionParser.OptionName name = AbstractOptionParser.OptionName.of("-a");
        List<InputArgument> args = List.of(
                InputArgument.of("-b"),
                InputArgument.of("-cde")
        );

        assertTrue(args.stream().noneMatch(name::matches));
    }

    @Test
    public void matchesInputArg_NONPOSIX_POSIX_negative() {
        AbstractOptionParser.OptionName name = AbstractOptionParser.OptionName.of("--a");
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
        AbstractOptionParser.OptionName name = AbstractOptionParser.OptionName.of("-a");
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
        AbstractOptionParser.OptionName name = AbstractOptionParser.OptionName.of("--long");
        List<InputArgument> args = List.of(
                InputArgument.of("--long")
        );

        assertTrue(args.stream().allMatch(name::matches));
    }

    @Test
    public void matchesInputArg_NONPOSIX_NONPOSIX_negative() {
        AbstractOptionParser.OptionName name = AbstractOptionParser.OptionName.of("--long");
        List<InputArgument> args = List.of(
                InputArgument.of("--long-long"),
                InputArgument.of("--l"),
                InputArgument.of("--abcdefg")
        );

        assertTrue(args.stream().noneMatch(name::matches));
    }

    @Test
    public void matchesInputArg_POSIX_misc_negative() {
        AbstractOptionParser.OptionName name = AbstractOptionParser.OptionName.of("-a");
        List<InputArgument> args = List.of(
                InputArgument.empty(),
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
        AbstractOptionParser.OptionName name = AbstractOptionParser.OptionName.of("--a");
        List<InputArgument> args = List.of(
                InputArgument.empty(),
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

}
