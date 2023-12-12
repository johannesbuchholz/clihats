package io.github.johannesbuchholz.clihats.processor.model;

import io.github.johannesbuchholz.clihats.processor.generators.ArgumentParserCodeGenerator;

import javax.lang.model.element.VariableElement;

public class ParameterCodeGeneratorPair {

    private final VariableElement targetParameter;
    private final ArgumentParserCodeGenerator argumentParserCodeGenerator;

    public static ParameterCodeGeneratorPair pair(VariableElement variableElement, ArgumentParserCodeGenerator argumentParserCodeGenerator) {
        return new ParameterCodeGeneratorPair(variableElement, argumentParserCodeGenerator);
    }

    public static ParameterCodeGeneratorPair unmanagedParameter(VariableElement variableElement) {
        return new ParameterCodeGeneratorPair(variableElement, null);
    }

    private ParameterCodeGeneratorPair(VariableElement targetParameter, ArgumentParserCodeGenerator argumentParserCodeGenerator) {
        this.targetParameter = targetParameter;
        this.argumentParserCodeGenerator = argumentParserCodeGenerator;
    }

    public VariableElement getTargetParameter() {
        return targetParameter;
    }

    public ArgumentParserCodeGenerator getArgumentParserCodeGenerator() {
        return argumentParserCodeGenerator;
    }

    public boolean isHasCodeGenerator() {
        return argumentParserCodeGenerator != null;
    }

}
