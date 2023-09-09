package io.github.johannesbuchholz.clihats.core.execution;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class InputArgument {

    public static final char OPTION_PREFIX = '-';
    public static final String OPERAND_DELIMITER =  String.valueOf(new char[] {OPTION_PREFIX, OPTION_PREFIX});
    private static final Set<String> HELP_ARGUMENT_VALUES = Set.of("--help");

    private final String value;
    private final Set<Character> chars;

    private final boolean isPOSIXConform;
    private final boolean isOption;

    public static boolean isHelpArgument(String value) {
        return HELP_ARGUMENT_VALUES.contains(value);
    }

    public static InputArgument of(String value) {
        return new InputArgument(value);
    }

    private InputArgument(String value) {
        this.value = Objects.requireNonNull(value);
        chars = value.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());

        isOption = value.length() > 1
                && value.charAt(0) == OPTION_PREFIX
                && !value.equals(OPERAND_DELIMITER)
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
        return OPERAND_DELIMITER.equals(value);
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
