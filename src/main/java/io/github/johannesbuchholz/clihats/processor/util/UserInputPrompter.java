package io.github.johannesbuchholz.clihats.processor.util;

import java.io.Console;
import java.util.Objects;

public class UserInputPrompter {

    public static UserInputPrompter getNew() {
        return new UserInputPrompter();
    }

    private UserInputPrompter() {
    }

    public String prompt(String promptText) {
        Console console = obtainConsole();
        return console.readLine(promptText);
    }

    public String promptMasked(String promptText) {
        Console console = obtainConsole();
        return String.valueOf(console.readPassword(promptText));
    }

    private Console obtainConsole() {
        return Objects.requireNonNull(System.console(), "Tried to obtain system console but received null");
    }

}
