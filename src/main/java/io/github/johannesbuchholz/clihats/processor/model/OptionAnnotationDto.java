package io.github.johannesbuchholz.clihats.processor.model;

import io.github.johannesbuchholz.clihats.processor.annotations.Option;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

/**
 * Holds values extracted from an {@link Option} annotation.
 */
public class OptionAnnotationDto {

    public static final String POSITION_FIELD_NAME = "position";
    public static final String NAME_FIELD_NAME = "name";
    public static final String FLAG_FIELD_NAME = "flagValue";
    public static final String DELIMITER_FIELD_NAME = "delimiter";
    public static final String DEFAULT_FIELD_NAME = "defaultValue";
    public static final String MAPPER_FIELD_NAME = "mapper";
    public static final String NECESSITY_FIELD_NAME = "necessity";
    public static final String DESCRIPTION_FIELD_NAME = "description";

    private final Integer position;
    private final List<String> name;
    private final String flagValue;
    private final String delimiter;
    private final String defaultValue;
    private final TypeElement mapper;
    private final VariableElement necessity;
    private final String description;

    public OptionAnnotationDto(Integer position, List<String> name, String flagValue, String delimiter, String defaultValue, TypeElement mapper, VariableElement necessity, String description) {
        this.position = position;
        this.name = name;
        this.flagValue = flagValue;
        this.delimiter = delimiter;
        this.defaultValue = defaultValue;
        this.mapper = mapper;
        this.necessity = necessity;
        this.description = description;
    }

    public Integer getPosition() {
        return position;
    }

    public List<String> getName() {
        return name;
    }

    public String  getFlagValue() {
        return flagValue;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public TypeElement getMapper() {
        return mapper;
    }

    public VariableElement getNecessity() {
        return necessity;
    }

    public String getDescription() {
        return description;
    }

}
