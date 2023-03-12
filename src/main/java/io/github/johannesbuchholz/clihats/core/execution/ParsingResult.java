package io.github.johannesbuchholz.clihats.core.execution;

import java.util.ArrayList;
import java.util.List;

public class ParsingResult {

    private final Object[] values;
    private final List<Throwable> errors;
    private final List<AbstractOptionParser<?>> missing;
    private final List<String> unknown;

    public static ParsingResult.Builder builder(int capacity) {
        return new Builder(capacity);
    }

    public ParsingResult(Object[] values, List<Throwable> errors, List<AbstractOptionParser<?>> missing, List<String> unknown) {
        this.values = values;
        this.errors = errors;
        this.missing = missing;
        this.unknown = unknown;
    }

    public boolean isValid() {
        return errors.isEmpty() && missing.isEmpty() && unknown.isEmpty();
    }

    public Object[] getValues() {
        return values;
    }

    public List<Throwable> getErrors() {
        return errors;
    }
    public List<AbstractOptionParser<?>> getMissing() {
        return missing;
    }

    public List<String> getUnknown() {
        return unknown;
    }

    public static class Builder {

        private final Object[] values;
        private final List<Throwable> errors;
        private final List<AbstractOptionParser<?>> missing;
        private final List<String> unknown;

        private Builder(int capacity) {
            this.values = new Object[capacity];
            this.missing = new ArrayList<>(capacity);
            this.unknown = new ArrayList<>(capacity);
            this.errors = new ArrayList<>(capacity);
        }

        public void putArg(int pos, Object arg) {
            values[pos] = arg;
        }

        public void putMissing(AbstractOptionParser<?> missing) {
            this.missing.add(missing);
        }

        public void putUnknown(String arg) {
            unknown.add(arg);
        }

        public void putError(Throwable parsingError) {
            errors.add(parsingError);
        }

        public ParsingResult build() {
            return new ParsingResult(values, errors, missing, unknown);
        }

    }

}
