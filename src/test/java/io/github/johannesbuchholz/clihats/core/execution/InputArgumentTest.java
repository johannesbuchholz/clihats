package io.github.johannesbuchholz.clihats.core.execution;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class InputArgumentTest {

    @Test
    public void testInput_positive() {
        List<String> argValues = List.of("a", "", "-", "--abc-d", "-a1", "--A2", "a-b-c", "--a-b-c", "-a-b-c");
        List<InputArgument> args = argValues.stream().map(InputArgument::of).collect(Collectors.toList());

        assertEquals(argValues.size(), args.size());
    }

    @Test
    public void testDeterminePosixOption_positive() {
        List<String> argValues = List.of("-a", "-ab", "-xyz", "-A", "-AbCd");
        List<InputArgument> args = argValues.stream().map(InputArgument::of).collect(Collectors.toList());

        assertTrue(args.stream().allMatch(InputArgument::isPOSIXConform));
    }

    @Test
    public void testDeterminePosixOption_negative() {
        List<String> argValues = List.of("--a", "-", "--a", "--", "", " ", "--some-option", "a", "abc", "-d abc", "- ");
        List<InputArgument> args = argValues.stream().map(InputArgument::of).collect(Collectors.toList());

        assertTrue(args.stream().noneMatch(InputArgument::isPOSIXConform));
    }

    @Test
    public void testCreateOption_positive() {
        List<String> argValues = List.of("--a", "--ab-c", "---", "--ABCd", "--abc--def", "--abc-#*~łĸ‚Æ¥÷×‹‘º");

        List<Exception> exceptions = new ArrayList<>();
        for (String s : argValues) {
            try {
                InputArgument.of(s);
            } catch (IllegalArgumentException e) {
                exceptions.add(e);
            }
        }
        assertTrue(exceptions.isEmpty());
    }

    @Test
    public void testDetermineOption_negative() {
        List<String> argValues = List.of("-a ", "--", "-", "", "- abc", "- --ABCd", "a-b-c", "--abc def", "-- ");
        List<InputArgument> args = argValues.stream().map(InputArgument::of).collect(Collectors.toList());

        assertTrue(args.stream().noneMatch(InputArgument::isOption));
    }

    @Test
    public void testDetermineBreakSequence_positive() {
        List<String> argValues = List.of("--");
        List<InputArgument> args = argValues.stream().map(InputArgument::of).collect(Collectors.toList());

        assertTrue(args.stream().allMatch(InputArgument::isBreakSequence));
    }

    @Test
    public void testDetermineBreakSequence_negative() {
        List<String> argValues = List.of("-", "---", "", "-a", "--abc-d", "--a--b");
        List<InputArgument> args = argValues.stream().map(InputArgument::of).collect(Collectors.toList());

        assertTrue(args.stream().noneMatch(InputArgument::isBreakSequence));
    }

    @Test
    public void testContains_positive() {
        String argValue = "--AbCde1-XyZ99-";
        InputArgument arg = InputArgument.of(argValue);

        List<Character> expectedToBeContained = argValue.chars().mapToObj(c -> (char) c).collect(Collectors.toList());

        assertTrue(expectedToBeContained.stream().allMatch(arg::contains));
    }

    @Test
    public void testContains_negative() {
        String argValue = "--AbCde1-XyZ99-";
        InputArgument arg = InputArgument.of(argValue);

        List<Character> expectedToBeNotContained = List.of('x', 'Y', 'z', '#', '.', '2', 'a', 'B', 'c', 'D');

        assertTrue(expectedToBeNotContained.stream().noneMatch(arg::contains));
    }

    @Test
    public void testContainsAtEnd_positive() {
        String argValue = "-abcd";
        InputArgument arg = InputArgument.of(argValue);

        char expectedToBeContained = 'd';

        assertTrue(arg.containsAtEnd(expectedToBeContained));
    }

    @Test
    public void testContainsAtEnd_negative() {
        String argValue = "-abcd";
        InputArgument arg = InputArgument.of(argValue);

        List<Character> expectedToBeNotContained = List.of('x', 'Y', 'z', '#', '.', '2', 'a', 'b', 'c', 'B', 'c', 'D');

        assertTrue(expectedToBeNotContained.stream().noneMatch(arg::containsAtEnd));
    }

    @Test
    public void testEqualsAnyIfOption_positive() {
        String argValue = "--my-arg";
        InputArgument arg = InputArgument.of(argValue);

        List<String> valueList = List.of("", "blubb", argValue, "my-arg", "-my-arg", "something-different");

        assertTrue(arg.equalsAny(valueList));
    }

    @Test
    public void testEqualsAnyIfOption_negative() {
        String argValue = "--my-arg";
        InputArgument arg = InputArgument.of(argValue);

        List<String> valueList = List.of("", "blubb", "my-arg", "-my-arg", "something-different");

        assertFalse(arg.equalsAny(valueList));
    }

}
