package io.github.johannesbuchholz.clihats.processor;

import java.util.Arrays;
import java.util.Objects;

public class ReusableTestResult {

    public static class Result {
        private final String id;
        private final Object[] args;

        private Result(String id, Object[] args) {
            this.id = id;
            this.args = args;
        }

        public Object[] getArgs() {
            return args;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Result result = (Result) o;
            return Objects.equals(id, result.id) && Arrays.deepEquals(args, result.args);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(id);
            result = 31 * result + Arrays.deepHashCode(args);
            return result;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "id='" + id + '\'' +
                    ", args=" + Arrays.deepToString(args) +
                    '}';
        }
    }

    public static Result getExpected(String id, Object... args) {
        return new Result(id, args);
    }

    private final Result[] resultStore = new Result[1];

    public void put(String id, Object... args) {
        if (hasResult())
            throw new IllegalStateException("Already received a result: " + resultStore[0]);
        resultStore[0] = new Result(id, args);
    }

    public boolean hasResult() {
        return resultStore[0] != null;
    }

    public void clear() {
        resultStore[0] = null;
    }

    public Result getAndClear() {
        if (!hasResult())
            throw new IllegalStateException("Never received a result");
        Result result = resultStore[0];
        clear();
        return result;
    }

}
