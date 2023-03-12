package io.github.johannesbuchholz.clihats.util.visitors;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;

/**
 * Designed for accepting AnnotationValue Objects that are expected to contain a class type value.
 * <p>Visiting such an AnnotationValue returns the contained value as {@link TypeElement}.</p>
 */
public class EnumAnnotationValueVisitor extends SimpleAnnotationValueVisitor9<VariableElement, Void> {

    @Override
    public VariableElement visitEnumConstant(VariableElement c, Void unused) {
        return c;
    }

    @Override
    protected VariableElement defaultAction(Object o, Void unused) {
        throw new UnsupportedOperationException(String.format("Trying to visit enum AnnotationValue of not implemented type: %s of type %s", o, o.getClass()));
    }

}
