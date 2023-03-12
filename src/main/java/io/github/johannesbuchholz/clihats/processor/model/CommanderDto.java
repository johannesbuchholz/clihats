package io.github.johannesbuchholz.clihats.processor.model;

import javax.lang.model.element.TypeElement;
import java.util.List;

public class CommanderDto {

    public static final String NAME_FIELD_NAME = "name";
    public static final String DESCRIPTION_FIELD_NAME = "description";

    private final String name;
    private final String description;
    private final TypeElement annotatedInterface;
    private final List<CommandDto> commandDtoList;

    public CommanderDto(String name, String description, TypeElement annotatedInterface, List<CommandDto> commandDtoList) {
        this.description = description;
        this.name = name;
        this.annotatedInterface = annotatedInterface;
        this.commandDtoList = commandDtoList;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TypeElement getAnnotatedInterface() {
        return annotatedInterface;
    }

    public List<CommandDto> getCommandDtoList() {
        return commandDtoList;
    }

}
