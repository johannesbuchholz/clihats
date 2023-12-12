package io.github.johannesbuchholz.clihats.processor.util;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProcessingUtils {

    /**
     * {@code classToCompareTo} must not be an array type.
     */
    public static boolean isSameType(TypeMirror typeMirror, Class<?> classToCompareTo, ProcessingEnvironment processingEnvironment) {
        return processingEnvironment.getTypeUtils().isSameType(
                typeMirror,
                processingEnvironment.getElementUtils().getTypeElement(classToCompareTo.getCanonicalName()).asType());
    }

    /**
     * Generates a String representation of the location of the annotated method.
     * Also validates, that the enclosing element of the method is of Kind {@link ElementKind#CLASS}.
     */
    public static String generateOriginIdentifier(ExecutableElement annotatedMethod) {
        Element parentElement = annotatedMethod.getEnclosingElement();
        String parentOriginString = ((TypeElement) parentElement).getQualifiedName().toString();
        return String.format("%s#%s(%s)",
                parentOriginString,
                annotatedMethod.getSimpleName(),
                annotatedMethod.getParameters().stream()
                        .map(variableElement -> String.format("%s %s", variableElement.asType().toString(), variableElement.getSimpleName()))
                        .collect(Collectors.joining(", "))
        );
    }

    public static Set<String> getPackageStrings(TypeElement... typeElements) {
        if (typeElements == null)
            throw new IllegalArgumentException("Argument must not be null");
        return Arrays.stream(typeElements)
                .map(TypeElement::getQualifiedName)
                .map(Name::toString)
                .filter(ProcessingUtils::importRequired)
                .collect(Collectors.toSet());
    }

    public static Set<String> getPackageStrings(Class<?>... types) {
        return Stream.of(types)
                .map(Class::getCanonicalName)
                .filter(ProcessingUtils::importRequired)
                .collect(Collectors.toSet());
    }

    private static boolean importRequired(String canonicalClassName) {
        return !canonicalClassName.startsWith("java.lang");
    }

    public static List<? extends AnnotationMirror> getAnnotationInstances(Element annotatedElement, Element requestedElement, ProcessingEnvironment processingEnvironment) {
        return  processingEnvironment.getElementUtils().getAllAnnotationMirrors(annotatedElement).stream()
                .filter(am -> processingEnvironment.getTypeUtils().isSameType(am.getAnnotationType(), requestedElement.asType()))
                .collect(Collectors.toList());
    }

    public static List<Map<String, ? extends AnnotationValue>> getAnnotationValuesBySimpleName(Element annotatedType, Element requestedElement, ProcessingEnvironment processingEnvironment) {
        return getAnnotationInstances(annotatedType, requestedElement, processingEnvironment).stream()
                .map(am -> processingEnvironment.getElementUtils().getElementValuesWithDefaults(am))
                .map(map -> map.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().getSimpleName().toString(), Map.Entry::getValue)))
                .collect(Collectors.toList());
    }

    public static Optional<DeclaredType> getMatchingSuperClass(TypeElement typeElement, TypeElement requestedSuperClass, ProcessingEnvironment processingEnvironment) {
        List<DeclaredType> superTypes = Stream.concat(Stream.of(typeElement.getSuperclass()), typeElement.getInterfaces().stream())
                .map(tm -> (DeclaredType) tm)
                .collect(Collectors.toList());

        if (superTypes.isEmpty())
            return Optional.empty();

        Optional<DeclaredType> matchingSupertype = superTypes.stream()
                .filter(superType -> processingEnvironment.getTypeUtils().isSameType(superType.asElement().asType(), requestedSuperClass.asType()))
                .findFirst();

        if (matchingSupertype.isPresent())
            return matchingSupertype;
        else
            return superTypes.stream()
                    .map(superType -> getMatchingSuperClass((TypeElement) superType.asElement(), requestedSuperClass, processingEnvironment))
                    .filter(Optional::isPresent)
                    .findFirst()
                    .orElse(Optional.empty());
    }

    public static Optional<ExecutableElement> getPublicNoArgsConstructor(TypeElement typeElement) {
        List<ExecutableElement> noArgConstructor = typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
                .map(ExecutableElement.class::cast)
                .filter(ex -> ex.getParameters().isEmpty() && ex.getModifiers().contains(Modifier.PUBLIC))
                .collect(Collectors.toList());
        if (noArgConstructor.isEmpty())
            return Optional.empty();
        else if (noArgConstructor.size() == 1)
            return Optional.of(noArgConstructor.get(0));
        else
            throw new IllegalStateException("Found more than one no-arg constructor for type " + typeElement);
    }

    public static <T extends Enum<T>> T getEnumFromTypeElement(Class<T> enumType, VariableElement enumElement, ProcessingEnvironment processingEnvironment) {
        EnumSet<T> enumTypes = EnumSet.allOf(enumType);
        if (enumElement.getKind() != ElementKind.ENUM_CONSTANT)
            throw new IllegalArgumentException("Given type element does not belong to an enum constant");
        return enumTypes.stream()
                // ensure that class types of the enum defining class matches
                .filter(enumCandidate -> isSameType(enumElement.asType(), enumCandidate.getClass(), processingEnvironment))
                // ensure that names match
                .filter(enumCandidate -> enumCandidate.name().equals(enumElement.getSimpleName().toString()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Given type element %s does not belong to any value of %s", enumElement, enumTypes)));
    }

    /**
     * Works for array types and declared types.
     */
    public static boolean hasGenericTypeParameter(TypeMirror type) {
        if (type.getKind() == TypeKind.DECLARED) {
            return !((DeclaredType) type).getTypeArguments().isEmpty();
        } else if (type.getKind() == TypeKind.ARRAY) {
            return hasGenericTypeParameter(((ArrayType) type).getComponentType());
        }
        return false;
    }
}
