package io.github.johannesbuchholz.clihats.processor.subjects;

import io.github.johannesbuchholz.clihats.processor.GlobalTestResult;
import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.execution.CliHats;
import io.github.johannesbuchholz.clihats.processor.mapper.AbstractValueMapper;

import java.nio.file.Path;

@CommandLineInterface
public class Cli4 {

    public static void main(String[] args) {
        CliHats.get(Cli4.class).execute(args);
    }

    @Command
    public static void arrayOperand(
            @Argument(type = Argument.Type.OPERAND) Integer number,
            @Argument(type = Argument.Type.ARRAY_OPERAND) Path[] paths
    ) {
        GlobalTestResult.setSuccess("array-operand", number, paths);
    }

    @Command
    public static void arrayOperandDefault(
            @Argument(type = Argument.Type.OPERAND) Integer number,
            @Argument(type = Argument.Type.ARRAY_OPERAND, defaultValue = "/the/default/way") Path[] paths
    ) {
        GlobalTestResult.setSuccess("array-operand-default", number, paths);
    }

    @Command
    public static void arrayOperandMapper(
            @Argument(type = Argument.Type.ARRAY_OPERAND, mapper = UppercaseMapper.class) String[] strings
    ) {
        Object[] args = {strings};
        GlobalTestResult.setSuccess("array-operand-mapper", args);
    }

    public static class UppercaseMapper extends AbstractValueMapper<String> {
        @Override
        public String map(String stringValue) {
            return stringValue.toUpperCase();
        }
    }

    @Command
    public static void arrayOperandMulti(
            @Argument(type = Argument.Type.OPERAND) String string,
            @Argument(type = Argument.Type.ARRAY_OPERAND) String[] strings,
            @Argument(type = Argument.Type.OPERAND) Path path,
            @Argument(type = Argument.Type.ARRAY_OPERAND) Path[] paths
    ) {
        Object[] args = {string, strings, path, paths};
        GlobalTestResult.setSuccess("array-operand-multi", args);
    }

}
