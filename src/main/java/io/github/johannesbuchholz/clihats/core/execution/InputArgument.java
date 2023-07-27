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
        boolean isOption = value.length() > 1
                && value.charAt(0) == OPTION_PREFIX
                && !value.equals(OPERAND_DELIMITER)
                && value.chars().skip(1).noneMatch(Character::isSpaceChar);
         boolean isPOSIXConform = isOption
                && value.charAt(1) != OPTION_PREFIX
                && value.chars().skip(1).allMatch(Character::isLetterOrDigit);
        return new InputArgument(value, value.chars().mapToObj(c -> (char) c).collect(Collectors.toSet()), isOption, isPOSIXConform);
    }

    private InputArgument(String value, Set<Character> chars, boolean isOption, boolean isPOSIXConform) {
        this.value = Objects.requireNonNull(value);
        this.chars = chars;
        this.isOption = isOption;
        this.isPOSIXConform = isPOSIXConform;
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
        int i = value.indexOf(c);
        if (i < 0)
            return new InputArgument(value, chars, isOption, isPOSIXConform);
        return InputArgument.of(value.substring(0, i) + value.substring(Math.min(i + 1, value.length())));
    }

    @Override
    public String toString() {
        return value;
    }

}
