package io.github.johannesbuchholz.clihats.processor.model;

import javax.lang.model.element.TypeElement;

public class TargetParameter {
    private final String name;
    private final TypeElement typeElement;

    public TargetParameter(String name, TypeElement typeElement) {
        this.name = name;
        this.typeElement = typeElement;
    }

    public String getName() {
        return name;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }
}
