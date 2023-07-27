package io.github.johannesbuchholz.clihats.readme.examples;

import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.annotations.Option;
import io.github.johannesbuchholz.clihats.processor.annotations.OptionNecessity;
import io.github.johannesbuchholz.clihats.processor.execution.CliHats;

// tag::example-class[]
/**
 * A simple "Hello, World!" program to demonstrate the capabilities of CliHats.
 */
@CommandLineInterface //<1>
public class HelloWorldCliHats {

    public static void main(String[] args) {
        CliHats.get(HelloWorldCliHats.class).execute(args); // <2>
    }

    /**
     * Simply prints "Hello, World!"
     */
    @Command // <3>
    public static void sayHello() {
        System.out.println("Hello, World!");
    }

    /**
     * Prints a greeting to the specified name.
     * @param name the name to greet.
     * @param polite if true, additionally prints "Nice to meet you!".
     */
    @Command
    public static void sayHelloToPerson(
            @Option(necessity = OptionNecessity.REQUIRED) // <4>
            String name,
            @Option(flagValue = "true", defaultValue = "false") // <5>
            Boolean polite
    ) {
        System.out.printf("Hello, %s!\n", name);
        if (polite)
            System.out.println("Nice to meet you!");
    }

}
// end::example-class[]
