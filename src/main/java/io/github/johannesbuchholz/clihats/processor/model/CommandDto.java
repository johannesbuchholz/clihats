package io.github.johannesbuchholz.clihats.processor.model;

import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.util.ProcessingUtils;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Objects;

/**
 * Holds values extracted from an {@link Command} annotation.
 */
public class CommandDto {

    public static final String NAME_FIELD_NAME = "name";
    public static final String DESCRIPTION_FIELD_NAME = "description";
    public static final String CLI_FIELD_NAME = "cli";

    private final String name;
    private final String description;
    private final List<TypeElement> cli;

    private final ExecutableElement annotatedMethod;

    public CommandDto(String name, String description, List<TypeElement> cli, ExecutableElement annotatedMethod) {
        this.name = name;
        this.description = description;
        this.cli = cli;
        this.annotatedMethod = annotatedMethod;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<TypeElement> getCli() {
        return cli;
    }

    public ExecutableElement getAnnotatedMethod() {
        return annotatedMethod;
    }

    @Override
    public String toString() {
        return ProcessingUtils.generateOriginIdentifier(annotatedMethod);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandDto that = (CommandDto) o;
        return name.equals(that.name) && annotatedMethod.equals(that.annotatedMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, annotatedMethod);
    }

}
