package io.github.johannesbuchholz.clihats.readme.examples;

import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.execution.CliHats;

/**
 * The Quickstart example of CliHats.
 */
@CommandLineInterface
public class CliHatsQuickstart {

    public static void main(String[] args) {
        CliHats.get(CliHatsQuickstart.class).execute(args);
    }

    /**
     * For some name, prints "Hello, {name}!" multiple times.
     * @param name The name to greet.
     * @param count The number of greetings.
     */
    @Command
    public static void sayHello(@Argument String name, @Argument(type = Argument.Type.OPERAND, necessity = Argument.Necessity.REQUIRED) Integer count) {
        for (int i = 0; i < count; i++)
            System.out.println("Hello, " + name + "!");
    }

}