package io.github.johannesbuchholz.clihats.processor.util.visitors;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Designed for accepting AnnotationValue Objects that are expected to contain an array values considered as "simple":
 * <li>primitives</li>
 * <li>{@link String}</li>
 * <p>Visiting such an AnnotationValue returns the contained array as a list of the expected type.</p>
 * @param <R> the expected type contained in the array
 */
public class ArrayOfSimpleAnnotationValueVisitor<R> extends SimpleAnnotationValueVisitor9<List<R>, Void> {

    private static final Set<Class<?>> SUPPORTED_TYPES = Set.of(
            String.class,
            Boolean.class,
            Integer.class,
            Double.class,
            Float.class,
            Character.class,
            Long.class,
            Byte.class,
            Short.class
    );

    private final Class<R> type;

    public ArrayOfSimpleAnnotationValueVisitor(Class<R> type) {
        if (!SUPPORTED_TYPES.contains(type))
            throw new IllegalArgumentException(String.format("Can not create simple type visitor: %s is not in supported types %s",
                    type.getCanonicalName(),
                    SUPPORTED_TYPES.stream().map(Class::getCanonicalName).collect(Collectors.toList())
            ));
        this.type = type;
    }

    @Override
    public List<R> visitArray(List<? extends AnnotationValue> vals, Void p) {
        return vals.stream()
                .map(AnnotationValue::getValue)
                .map(type::cast)
                .collect(Collectors.toList());
    }

    @Override
    protected List<R> defaultAction(Object o, Void p) {
        throw new UnsupportedOperationException(String.format("Trying to visit AnnotationValue of not implemented type: %s of type %s", o, o.getClass()));
    }

}
