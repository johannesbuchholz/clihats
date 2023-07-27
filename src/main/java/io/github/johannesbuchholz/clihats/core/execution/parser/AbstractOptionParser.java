package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.AbstractParser;
import io.github.johannesbuchholz.clihats.core.execution.InputArgument;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class AbstractOptionParser extends AbstractParser {

    abstract Collection<OptionName> getNames();

    @Override
    protected int getParsingPriority() {
        return 0;
    }

    @Override
    public String toString() {
        return "Option " + getNames().stream().sorted().map(OptionName::getValue).collect(Collectors.joining(", "));
    }

    static Set<OptionName> asOptionNames(String name, String... names) {
        Set<OptionName> optionOptionNames = new HashSet<>();
        Set<OptionName> posixConformNames = new HashSet<>();
        Stream.concat(Stream.of(name), Stream.of(names))
                .map(OptionName::of)
                .forEach(optionOptionName -> {
                    optionOptionNames.add(optionOptionName);
                    if (optionOptionName.isPOSIXConformOptionName())
                        posixConformNames.add(optionOptionName);
                });
        if (posixConformNames.size() > 1) {
            throw new IllegalArgumentException("Encountered more than one POSIX conform option name: " + posixConformNames);
        }
        return optionOptionNames;
    }

    static class OptionName {

        private final String value;
        private final boolean isPOSIXConformOptionName;

        public static OptionName of(String value) {
            return new OptionName(value);
        }

        private OptionName(String value) {
            if (value == null
                    || value.length() < 2
                    || value.charAt(0) != InputArgument.OPTION_PREFIX
                    || value.equals(InputArgument.BREAK_SEQUENCE)
                    || value.chars().skip(1).anyMatch(Character::isSpaceChar))
                throw new IllegalArgumentException("Value is not a valid option name: " + value);
            this.value = value;
            isPOSIXConformOptionName = value.length() == 2
                    && Character.isLetterOrDigit(value.charAt(1));
        }

        public boolean isPOSIXConformOptionName() {
            return isPOSIXConformOptionName;
        }

        public String getValue() {
            return value;
        }

        public boolean matches(InputArgument arg) {
            if (arg == null ) {
                return false;
            }
            if (arg.isPOSIXConform() && isPOSIXConformOptionName()) {
                return arg.contains(value.charAt(1));
            }
            return arg.getValue().equals(value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return value;
        }

    }
}
