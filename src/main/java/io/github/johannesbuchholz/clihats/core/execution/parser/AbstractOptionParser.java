package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.AbstractParser;
import io.github.johannesbuchholz.clihats.core.execution.InputArgument;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractOptionParser extends AbstractParser {

    protected static Set<OptionName> collectAsOptionNamesFrom(String name, String... names) {
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

    public abstract Collection<OptionName> getNames();

    @Override
    public String getDisplayName() {
        return getNames().stream().sorted().map(OptionName::getValue).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return "Option " + getDisplayName();
    }

    @Override
    protected int getParsingPriority() {
        return 0;
    }

    @Override
    protected Optional<String> getConflictMessage(AbstractParser other) {
       if (!(other instanceof AbstractOptionParser))
           return Optional.empty();
        HashSet<OptionName> duplicateNames = new HashSet<>(getNames());
        duplicateNames.retainAll(((AbstractOptionParser) other).getNames());
        if (!duplicateNames.isEmpty())
            return Optional.of(String.format("Parser %s conflicts with parser %s on names %s", this, other, duplicateNames));
        return Optional.empty();
    }

    public static class OptionName implements Comparable<OptionName> {

        private final String value;
        private final boolean isPOSIXConformOptionName;

        public static OptionName of(String value) {
            if (value == null
                    || value.length() < 2
                    || value.charAt(0) != InputArgument.OPTION_PREFIX
                    || value.equals(InputArgument.OPERAND_DELIMITER)
                    || value.chars().skip(1).anyMatch(Character::isSpaceChar))
                throw new IllegalArgumentException("Value is not a valid option name: " + value);
            return new OptionName(value);
        }

        private OptionName(String value) {
            this.value = value;
            isPOSIXConformOptionName = value.length() == 2 && Character.isLetterOrDigit(value.charAt(1));
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OptionName that = (OptionName) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public String toString() {
            return value;
        }

        @Override
        public int compareTo(OptionName o) {
            if (o == null)
                return -1;
            if (isPOSIXConformOptionName && !o.isPOSIXConformOptionName)
                return -1;
            else if (!isPOSIXConformOptionName && o.isPOSIXConformOptionName)
                return 1;
            return value.compareTo(o.value);
        }

    }
}
