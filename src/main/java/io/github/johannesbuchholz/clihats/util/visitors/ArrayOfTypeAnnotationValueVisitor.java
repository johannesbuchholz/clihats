package io.github.johannesbuchholz.clihats.util.visitors;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Designed for accepting arrays of AnnotationValue Objects that are expected to contain class type values.
 * <p>Visiting such an AnnotationValue returns the contained value as {@link TypeElement}.</p>
 */
public class ArrayOfTypeAnnotationValueVisitor extends SimpleAnnotationValueVisitor9<List<TypeElement>, Types> {

    @Override
    public List<TypeElement> visitArray(List<? extends AnnotationValue> vals, Types types) {
        TypeAnnotationValueVisitor typeAnnotationValueVisitor = new TypeAnnotationValueVisitor();
        return vals.stream()
                .map(AnnotationValue::getValue)
                .map(TypeMirror.class::cast)
                .map(typeMirror -> typeAnnotationValueVisitor.visitType(typeMirror, types))
                .collect(Collectors.toList());
    }

    @Override
    protected List<TypeElement> defaultAction(Object o, Types types) {
        throw new UnsupportedOperationException(String.format("Trying to visit array of AnnotationValue of not implemented type: %s of type %s", o, o.getClass()));
    }

}
