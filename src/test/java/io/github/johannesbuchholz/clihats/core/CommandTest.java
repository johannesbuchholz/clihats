package io.github.johannesbuchholz.clihats.core;

import io.github.johannesbuchholz.clihats.core.execution.Command;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CommandTest {

    @Test
    public void creationFailure_illegalName() {
        List<String> illegalNames = List.of("n ame", " ", "", "name ", "one and two", " name");

        List<IllegalArgumentException> exceptions = new ArrayList<>();
        for (String name : illegalNames) {
            try {
                Command.forName(name);
            } catch (IllegalArgumentException e) {
                exceptions.add(e);
            }
        }

        assertEquals(illegalNames.size(), exceptions.size());
    }

}
