package io.github.johannesbuchholz.clihats.util.visitors;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;
import javax.lang.model.util.Types;

/**
 * Designed for accepting AnnotationValue Objects that are expected to contain a class type value.
 * <p>Visiting such an AnnotationValue returns the contained value as {@link javax.lang.model.element.TypeElement}.</p>
 */
public class TypeAnnotationValueVisitor extends SimpleAnnotationValueVisitor9<TypeElement, Types> {

    @Override
    public TypeElement visitType(TypeMirror t, Types types) {
        return (TypeElement) types.asElement(t);
    }

    @Override
    protected TypeElement defaultAction(Object o, Types types) {
        throw new UnsupportedOperationException(String.format("Trying to visit AnnotationValue of not implemented type: %s of type %s", o, o.getClass()));
    }

}
