package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.ArgumentParser;
import io.github.johannesbuchholz.clihats.core.execution.InputArgument;
import io.github.johannesbuchholz.clihats.core.execution.ParserId;
import io.github.johannesbuchholz.clihats.core.execution.parser.exception.ValueMappingException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractOptionParser<T> implements ArgumentParser<T> {

    final Set<OptionParserName> names;
    final OptionParserId id;

    protected AbstractOptionParser(Set<OptionParserName> names) {
        this.names = names;
        id = new OptionParserId(names);
    }

    public Set<OptionParserName> getNames() {
        return names;
    }

    @Override
    public OptionParserId getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Option " + id.value;
    }

    T mapWithThrows(ValueMapper<T> mapper, String stringValue) throws ValueMappingException {
        if (stringValue == null)
            return null;
        try {
            return mapper.map(stringValue);
        } catch (Exception e) {
            throw new ValueMappingException(this, e);
        }
    }

    public static class OptionParserName implements Comparable<OptionParserName> {

        private final String value;
        private final boolean isPOSIXConformOptionName;

        static OptionParserName of(String value) {
            if (value == null
                    || value.length() < 2
                    || value.charAt(0) != InputArgument.OPTION_PREFIX
                    || value.equals(InputArgument.OPERAND_DELIMITER)
                    || value.chars().skip(1).anyMatch(Character::isSpaceChar))
                throw new IllegalArgumentException(String.format("Invalid option name: %s. %s",
                        value, "A valid name satisfies the following: Have length greater than one. Start with '-'. Be not equal to '--'. Do not contain any whitespaces."));
            return new OptionParserName(value);
        }

        private OptionParserName(String value) {
            this.value = value;
            isPOSIXConformOptionName = value.length() == 2 && Character.isLetterOrDigit(value.charAt(1));
        }

        public boolean isPOSIXConformOptionName() {
            return isPOSIXConformOptionName;
        }

        public String getValue() {
            return value;
        }

        public String getValueWithoutPrefix() {
            if (isPOSIXConformOptionName)
                return value.substring(1);
            return value.substring(2);
        }

        boolean matches(InputArgument arg) {
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
            OptionParserName that = (OptionParserName) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public String toString() {
            return "OptionParserName{value=" + value + "}";
        }

        @Override
        public int compareTo(OptionParserName o) {
            if (o == null)
                return -1;
            int compare = Boolean.compare(o.isPOSIXConformOptionName, isPOSIXConformOptionName);
            if (compare == 0)
                return value.compareTo(o.getValue());
            return compare;
        }

    }

    public static class OptionParserId implements ParserId {

        private final Set<OptionParserName> names;
        private final String value;

        OptionParserId(Set<OptionParserName> names) {
            this.names = new HashSet<>(Objects.requireNonNull(names));
            value = names.stream().sorted().map(OptionParserName::getValue).collect(Collectors.joining(","));
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public Optional<String> hasCommonParts(ParserId other) {
            if (!(other instanceof OptionParserId))
                return Optional.empty();
            String commonNames = names.stream()
                    .filter(((OptionParserId) other).names::contains)
                    .map(OptionParserName::getValue)
                    .collect(Collectors.joining(", "));
            if (commonNames.isEmpty())
                return Optional.empty();
            return Optional.of("Equal on names: " + commonNames);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OptionParserId that = (OptionParserId) o;
            return Objects.equals(names, that.names);
        }

        @Override
        public int hashCode() {
            return Objects.hash(names);
        }

        @Override
        public int compareTo(ParserId o) {
            if(!(o instanceof OptionParserId))
                return -1;
            String posixNames = names.stream()
                    .filter(OptionParserName::isPOSIXConformOptionName)
                    .map(OptionParserName::getValue)
                    .collect(Collectors.joining());
            String otherPosixNames = ((OptionParserId) o).names.stream()
                    .filter(OptionParserName::isPOSIXConformOptionName)
                    .map(OptionParserName::getValue)
                    .collect(Collectors.joining());
            if (posixNames.isEmpty() && otherPosixNames.isEmpty()) {
                return value.compareTo(o.getValue());
            } else if (!posixNames.isEmpty() && !otherPosixNames.isEmpty()) {
                return posixNames.compareTo(otherPosixNames);
            } else if (otherPosixNames.isEmpty()) {
                return -1;
            } else {
                return 1;
            }
        }

        @Override
        public String toString() {
            throw new IllegalStateException();
        }

    }

}
