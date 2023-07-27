package io.github.johannesbuchholz.clihats.readme.examples;

import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.annotations.Option;
import io.github.johannesbuchholz.clihats.processor.annotations.OptionNecessity;
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
     * Prints "Hello {name}" multiple times.
     * @param name The name to greet.
     * @param times The number of greetings.
     */
    @Command
    public static void sayHello(@Option String name, @Option(position = 0, necessity = OptionNecessity.REQUIRED) Integer times) {
        for (int i = 0; i < times; i++)
            System.out.println("Hello, " + name + "!");
    }

}
