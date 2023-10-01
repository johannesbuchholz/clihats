package io.github.johannesbuchholz.clihats.core;

import io.github.johannesbuchholz.clihats.core.execution.Instruction;

import java.util.Arrays;
import java.util.Objects;

public class TestResult {

    private Object[] receivedArgs = null;

    /**
     * Returns a TestResult with the given args in order. Use this to create expected results.
     */
    public static TestResult newExpected(Object... args) {
        TestResult tr = TestResult.newEmpty();
        tr.receiveArgs(args);
        return tr;
    }

    public static TestResult newEmpty() {
        return new TestResult();
    }

    private TestResult() {
    }

    private synchronized void receiveArgs(Object[] args) {
        if (receivedArgs != null)
            throw new IllegalStateException(String.format("Trying to receive args %s when args were already present: %s" , Arrays.toString(args), Arrays.toString(receivedArgs)));
        receivedArgs = Objects.requireNonNull(args);
    }

    public Instruction getTestInstruction() {
        return this::receiveArgs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestResult that = (TestResult) o;
        return Arrays.deepEquals(receivedArgs, that.receivedArgs);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(receivedArgs);
    }

    @Override
    public String toString() {
        return "TestResult{" +
                Arrays.toString(receivedArgs) +
                '}';
    }
}
