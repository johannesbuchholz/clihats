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

    public static final String JAVA_LANG = "java.lang";

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

    public static Set<String> getPackageStrings(Element element) {
        if (element == null)
            return Set.of();
        return getPackageStrings(List.of(element));
    }

    public static Set<String> getPackageStrings(List<? extends Element> elements) {
        Set<String> packageStrings = new HashSet<>();
        for (Element e : elements) {
            if (!e.getKind().isClass() && !e.getKind().isInterface())
                throw new IllegalArgumentException("Programming error: Trying to get a package of an element that is not an interface or a class: " + e + " (" + e.getKind() + ")");
            String canonicalName = ((TypeElement) e).getQualifiedName().toString();
            if (ProcessingUtils.importRequired(canonicalName))
                packageStrings.add(canonicalName);
        }
        return packageStrings;
    }

    public static Set<String> getPackageStrings(Class<?>... moreTypes) {
        return Stream.of(moreTypes)
                .map(Class::getCanonicalName)
                .filter(ProcessingUtils::importRequired)
                .collect(Collectors.toSet());
    }

    private static boolean importRequired(String canonicalClassName) {
        return !canonicalClassName.startsWith(JAVA_LANG);
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

    public static <T extends Enum<T>> T getEnumFromTypeElement(EnumSet<T> enumTypes, VariableElement enumElement, ProcessingEnvironment processingEnvironment) {
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
