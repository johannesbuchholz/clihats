package io.github.johannesbuchholz.clihats.readme.examples;

import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.execution.CliHats;

import static io.github.johannesbuchholz.clihats.processor.annotations.Argument.Necessity.REQUIRED;

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
     * Simply prints "Hello, World!".
     */
    @Command // <3>
    public static void sayHello() {
        System.out.println("Hello, World!");
    }

    /**
     * Prints a greeting to the specified name.
     * @param name The name to greet.
     * @param polite If true, additionally prints "Nice to meet you!".
     */
    @Command
    public static void sayHelloToPerson(
            @Argument(necessity = REQUIRED) String name, // <4>
            @Argument(flagValue = "true", defaultValue = "false") Boolean polite // <5>
    ) {
        System.out.printf("Hello, %s!\n", name);
        if (polite)
            System.out.println("Nice to meet you!");
    }

}
// end::example-class[]
