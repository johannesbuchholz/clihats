package io.github.johannesbuchholz.clihats.core.execution;

// TODO: Simplify by replacing occurrences of this class by Object[].
public class ParsingResult {

    private final Object[] values;

    public static ParsingResult.Builder builder(int capacity) {
        return new Builder(capacity);
    }

    public ParsingResult(Object[] values) {
        this.values = values;
    }

    public Object[] getValues() {
        return values;
    }

    public static class Builder {

        private final Object[] values;

        private Builder(int capacity) {
            this.values = new Object[capacity];
        }

        public void putArg(int pos, Object arg) {
            values[pos] = arg;
        }

        public ParsingResult build() {
            return new ParsingResult(values);
        }

    }

}
