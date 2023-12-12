package io.github.johannesbuchholz.clihats.processor.model;

import io.github.johannesbuchholz.clihats.processor.annotations.Argument;

import javax.lang.model.element.TypeElement;
import java.util.List;

/**
 * Holds values extracted from an {@link Argument} annotation.
 */
public class ArgumentDto {

    public static final String TYPE_FIELD_NAME = "type";
    public static final String NAME_FIELD_NAME = "name";
    public static final String FLAG_FIELD_NAME = "flagValue";
    public static final String DEFAULT_FIELD_NAME = "defaultValue";
    public static final String MAPPER_FIELD_NAME = "mapper";
    public static final String NECESSITY_FIELD_NAME = "necessity";
    public static final String DESCRIPTION_FIELD_NAME = "description";

    private final Argument.Type type;
    private final List<String> name;
    private final String flagValue;
    private final String defaultValue;
    private final TypeElement mapper;
    private final Argument.Necessity necessity;
    private final String description;

    public ArgumentDto(Argument.Type type, List<String> name, String flagValue, String defaultValue, TypeElement mapper, Argument.Necessity necessity, String description) {
        this.type = type;
        this.name = name;
        this.flagValue = flagValue;
        this.defaultValue = defaultValue;
        this.mapper = mapper;
        this.necessity = necessity;
        this.description = description;
    }

    public Argument.Type getType() {
        return type;
    }

    public List<String> getName() {
        return name;
    }

    public String  getFlagValue() {
        return flagValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public TypeElement getMapper() {
        return mapper;
    }

    public Argument.Necessity getNecessity() {
        return necessity;
    }

    public String getDescription() {
        return description;
    }

}
