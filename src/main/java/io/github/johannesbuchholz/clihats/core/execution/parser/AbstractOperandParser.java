package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.AbstractArgumentParser;
import io.github.johannesbuchholz.clihats.core.execution.parser.exception.ValueMappingException;

import java.util.Optional;

public abstract class AbstractOperandParser<T> extends AbstractArgumentParser<T> {

    public abstract int getPosition();

    @Override
    public String getId() {
        return "<" + getPosition() + ">";
    }

    @Override
    public String toString() {
        return "Operand " + getPosition();
    }

    @Override
    public Optional<String> getConflictMessage(AbstractArgumentParser<?> other) {
        if (!(other instanceof AbstractOperandParser))
            return Optional.empty();
        if (getPosition() == ((AbstractOperandParser<?>) other).getPosition())
            return Optional.of("Operands conflict on position " + getPosition());
        return Optional.empty();
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

}
