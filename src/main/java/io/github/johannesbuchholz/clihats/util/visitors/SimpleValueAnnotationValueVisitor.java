    package io.github.johannesbuchholz.clihats.util.visitors;

    import javax.lang.model.util.SimpleAnnotationValueVisitor9;
    import java.util.Set;
    import java.util.stream.Collectors;

/**
 * Designed for accepting AnnotationValue Objects that are expected to contain an array values considered as "simple":
 * <li>primitives</li>
 * <li>{@link String}</li>
 * <p>Visiting such an AnnotationValue returns the contained array as a list of the expected type.</p>
 * @param <R> the expected type contained in the array
 */
public class SimpleValueAnnotationValueVisitor<R> extends SimpleAnnotationValueVisitor9<R, Void> {

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

    public SimpleValueAnnotationValueVisitor(Class<R> type) {
        if (!SUPPORTED_TYPES.contains(type))
            throw new IllegalArgumentException(String.format("Using visitor for unsupported type: %s is not from %s",
                    type.getCanonicalName(), SUPPORTED_TYPES.stream().map(Class::getCanonicalName).collect(Collectors.toList())
            ));
        this.type = type;
    }

    @Override
    protected R defaultAction(Object o, Void p) {
        if (!SUPPORTED_TYPES.contains(o.getClass()))
            throw new IllegalArgumentException(String.format("Using visitor for unsupported type: %s of type %s is not from %s",
                    o, type.getCanonicalName(), SUPPORTED_TYPES.stream().map(Class::getCanonicalName).collect(Collectors.toList())
            ));
        return type.cast(o);
    }

}
