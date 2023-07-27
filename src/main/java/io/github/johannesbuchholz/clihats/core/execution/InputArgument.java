package io.github.johannesbuchholz.clihats.core.execution;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class InputArgument {

    public static final char OPTION_PREFIX = '-';
    public static final String BREAK_SEQUENCE =  String.valueOf(new char[] {OPTION_PREFIX, OPTION_PREFIX});

    private final String value;
    private final Set<Character> chars;

    private final boolean isPOSIXConform;
    private final boolean isOption;

    public static InputArgument empty() {
        return InputArgument.of("");
    }

    public static InputArgument of(String value) {
        return new InputArgument(value);
    }

    private InputArgument(String value) {
        this.value = Objects.requireNonNull(value);
        chars = value.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());

        isOption = value.length() > 1
                && value.charAt(0) == OPTION_PREFIX
                && !value.equals(BREAK_SEQUENCE)
                && value.chars().skip(1).noneMatch(Character::isSpaceChar);
        isPOSIXConform = isOption
                && value.charAt(1) != OPTION_PREFIX
                && value.chars().skip(1).allMatch(Character::isLetterOrDigit);
    }

    public boolean isPOSIXConform() {
        return isPOSIXConform;
    }

    public boolean isOption() {
        return isOption;
    }

    public boolean isBreakSequence() {
        return BREAK_SEQUENCE.equals(value);
    }

    public boolean contains(char name) {
        return chars.contains(name);
    }

    public boolean containsAtEnd(char name) {
        if (value.isEmpty())
            return false;
        return value.charAt(value.length() - 1) == name;
    }

    public boolean equalsAny(Collection<String> names) {
        return names.contains(value);
    }

    public String getValue() {
        return value;
    }

    public InputArgument newWithout(char c) {
        String newValue = value.chars()
                .mapToObj(i -> (char) i)
                .filter(i -> i != c)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return InputArgument.of(newValue);
    }

    @Override
    public String toString() {
        return value;
    }

}
