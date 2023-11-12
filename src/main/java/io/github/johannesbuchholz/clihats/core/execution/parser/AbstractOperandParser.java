package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.ArgumentParser;
import io.github.johannesbuchholz.clihats.core.execution.ParserId;
import io.github.johannesbuchholz.clihats.core.execution.parser.exception.ValueMappingException;

import java.util.Objects;
import java.util.Optional;

public abstract class AbstractOperandParser<T> implements ArgumentParser<T> {

    final OperandParserId id;
    final int index;

    protected AbstractOperandParser(int index) {
        this.index = index;
        id = new OperandParserId(index);
    }

    public abstract int getIndex();

    @Override
    public ParserId getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Operand " + index;
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

    public static class OperandParserId implements ParserId {

        private final int value;
        
        OperandParserId(int value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return String.valueOf(value);
        }

        @Override
        public Optional<String> hasCommonParts(ParserId other) {
            if (other instanceof OperandParserId && value == ((OperandParserId) other).value)
                return Optional.of("Equal on position " + value);
            return Optional.empty();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OperandParserId that = (OperandParserId) o;
            return value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public int compareTo(ParserId o) {
            if (!(o instanceof OperandParserId))
                return 1;
            return Integer.compare(value, ((OperandParserId) o).value);
        }

        @Override
        public String toString() {
            throw new IllegalStateException();
        }

    }

}
