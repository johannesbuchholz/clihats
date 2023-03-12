package io.github.johannesbuchholz.clihats.util;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Groups a list of {@link javax.lang.model.element.TypeElement} by their uppermost shared parent package.
 */
public class PackageGrouper {

    private final Collection<TypeElement> typeElements;

    public static PackageGrouper createFor(Collection<TypeElement> typeElements) {
        return new PackageGrouper(typeElements);
    }

    private PackageGrouper(Collection<TypeElement> typeElements) {
        this.typeElements = typeElements;
    }

    public Map<PackageElement, List<TypeElement>> group(Elements elements) {
        Map<PackageElement, List<TypeElement>> typeElementsByPackage = new HashMap<>(typeElements.size());
        for (TypeElement typeElement : typeElements) {
            PackageElement currentPackageElement = elements.getPackageOf(typeElement);

            if (currentPackageElement.isUnnamed())
                throw new IllegalStateException("Can not process type in unnamed package: " + typeElement);

            List<PackageRelation> packageRelations = typeElementsByPackage.keySet()
                    .stream()
                    .map(packageElement -> PackageRelation.of(currentPackageElement, packageElement))
                    .filter(PackageRelation::isRelated)
                    .collect(Collectors.toList());

            if (packageRelations.size() > 1)
                throw new IllegalStateException("expected at most one relation put found " + packageRelations.size());

            if (packageRelations.isEmpty()) {
                // create new entry
                LinkedList<TypeElement> newTypeElement = new LinkedList<>();
                newTypeElement.add(typeElement);
                typeElementsByPackage.put(currentPackageElement, newTypeElement);
            } else {
                PackageRelation relationOfCurrentPackage = packageRelations.get(0);
                if (relationOfCurrentPackage.relationType == RelationType.CHILD) {
                    // append current type to other type elements of package
                    typeElementsByPackage.get(relationOfCurrentPackage.packageElementToRelateTo).add(typeElement);
                } else if (relationOfCurrentPackage.relationType == RelationType.PARENT) {
                    // replace old package with current package and update type elements
                    List<TypeElement> typeElementsOfPackage = typeElementsByPackage.remove(relationOfCurrentPackage.packageElementToRelateTo);
                    typeElementsOfPackage.add(typeElement);
                    typeElementsByPackage.put(relationOfCurrentPackage.packageElement, typeElementsOfPackage);
                }
            }
        }
        return typeElementsByPackage;
    }

    private static class PackageRelation {
        private final PackageElement packageElement;
        private final PackageElement packageElementToRelateTo;
        private final RelationType relationType;

        private static PackageRelation of(PackageElement packageElement, PackageElement packageElementToRelateTo) {
            return new PackageRelation(packageElement, packageElementToRelateTo, RelationType.determine(packageElement, packageElementToRelateTo));
        }

        private PackageRelation(PackageElement packageElement, PackageElement packageElementToRelateTo, RelationType relationType) {
            this.packageElement = packageElement;
            this.packageElementToRelateTo = packageElementToRelateTo;
            this.relationType = relationType;
        }

        private boolean isRelated() {
            return relationType != RelationType.UNRELATED;
        }
    }

    private enum RelationType {
        CHILD, PARENT, UNRELATED;

        /**
         * Determines the family type of the given package in relation to the other package element.
         * <p>
         *    Example: my.own.package.yeah starts with my.own.package => my.own.package is parent of my.own.package.yeah
         * </p>
         */
        public static RelationType determine(PackageElement packageElement, PackageElement packageElementToRelateTo) {
            if (packageElementToRelateTo.getQualifiedName().toString().startsWith(packageElement.getQualifiedName().toString()))
                return PARENT;
            else if (packageElement.getQualifiedName().toString().startsWith(packageElementToRelateTo.getQualifiedName().toString()))
                return CHILD;
            else
                return UNRELATED;
        }

    }

}
