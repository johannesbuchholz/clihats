package io.github.johannesbuchholz.clihats.processor.model;

import javax.lang.model.element.VariableElement;

public class ParameterArgumentPair {

    private final VariableElement targetParameter;
    private final ArgumentDto argumentDto;

    public static ParameterArgumentPair pair(VariableElement variableElement, ArgumentDto argumentDto) {
        return new ParameterArgumentPair(variableElement, argumentDto);
    }

    public static ParameterArgumentPair unmanagedParameter(VariableElement variableElement) {
        return new ParameterArgumentPair(variableElement, null);
    }

    private ParameterArgumentPair(VariableElement targetParameter, ArgumentDto argumentDto) {
        this.targetParameter = targetParameter;
        this.argumentDto = argumentDto;
    }

    public VariableElement getTargetParameter() {
        return targetParameter;
    }

    public ArgumentDto getArgumentDto() {
        return argumentDto;
    }

    public boolean isUnmanaged() {
        return argumentDto == null;
    }

}
