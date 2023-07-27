package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.AbstractParser;

abstract class AbstractOperandParser extends AbstractParser {

    abstract int getPosition();

    @Override
    protected int getParsingPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public String toString() {
        return "Operand " + getPosition();
    }

}
