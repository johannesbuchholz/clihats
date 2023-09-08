package io.github.johannesbuchholz.clihats.core.execution.parser;

import java.util.Optional;

public abstract class AbstractOperandParser<T> extends AbstractParser<T> {

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
    public int getParsingPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Optional<String> getConflictMessage(AbstractParser<?> other) {
        if (!(other instanceof AbstractOperandParser))
            return Optional.empty();
        if (getPosition() == ((AbstractOperandParser<?>) other).getPosition())
            return Optional.of("Operands conflict on position " + getPosition());
        return Optional.empty();
    }

}
