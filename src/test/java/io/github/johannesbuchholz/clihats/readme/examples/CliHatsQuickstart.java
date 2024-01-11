package io.github.johannesbuchholz.clihats.readme.examples;

import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.execution.CliHats;

import java.util.Optional;

import static io.github.johannesbuchholz.clihats.processor.annotations.Argument.Necessity.REQUIRED;
import static io.github.johannesbuchholz.clihats.processor.annotations.Argument.Type.OPERAND;

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
     * @param name The name to greet. Defaults to 'John'.
     * @param familyName The family name of the person to greet. If set, provides name 'Conner'.
     * @param count The number of greetings.
     */
    @Command
    public static void sayHello(
            @Argument(defaultValue = "John") String name,
            @Argument(flagValue = "Conner") String familyName,
            @Argument(type = OPERAND, necessity = REQUIRED) Integer count
    ) {
        for (int i = 0; i < count; i++)
            System.out.println("Hello, " + name + Optional.ofNullable(familyName).map(s -> " " + s).orElse("") + "!");
    }

}