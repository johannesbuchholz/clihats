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
// tag::example-class-slim[]
// tag::annotation-cli[]
@CommandLineInterface
// end::annotation-cli[]
public class HelloWorldCliHats {
    // tag::example-class-content[]
    // tag::example-class-main[]

    public static void main(String[] args) {
        CliHats.get(HelloWorldCliHats.class).execute(args);
    }
    // end::example-class-main[]

    /**
     * Simply prints "Hello, World!"
     */
    // tag::example-class-say-hello[]
    // tag::annotation-command[]
    @Command
    // end::annotation-command[]
    public static void sayHello() {
        System.out.println("Hello, World!");
    }
    // end::example-class-say-hello[]

    /**
     * Prints a greeting to the specified name.
     * @param name the name to greet.
     * @param polite if true, additionally prints "Nice to meet you!".
     */
    // tag::example-class-say-hello-to-person[]
    // tag::annotation-command-2[]
    @Command
    // end::annotation-command-2[]
    public static void sayHelloToPerson(
            // tag::annotation-options-name[]
            @Option(necessity = OptionNecessity.REQUIRED)
            // end::annotation-options-name[]
            String name,
            // tag::annotation-options-polite[]
            @Option(flagValue = "true", defaultValue = "false")
            // end::annotation-options-polite[]
            Boolean polite
    ) {
        System.out.printf("Hello, %s!\n", name);
        if (polite)
            System.out.println("Nice to meet you!");
    }
    // end::example-class-say-hello-to-person[]
    // end::example-class-content[]

}
// end::example-class-slim[]
// end::example-class[]
