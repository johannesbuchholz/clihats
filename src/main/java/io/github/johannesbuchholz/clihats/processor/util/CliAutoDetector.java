package io.github.johannesbuchholz.clihats.processor.util;

import io.github.johannesbuchholz.clihats.processor.exceptions.ConfigurationException;
import io.github.johannesbuchholz.clihats.processor.model.CommandDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.*;
import java.util.stream.Collectors;

public class CliAutoDetector {

    private static final Logger log = LoggerFactory.getLogger(CliAutoDetector.class);

    private final Collection<TypeElement> annotatedInterfaces;

    public static CliAutoDetector getNewFor(Collection<TypeElement> annotatedInterfaces) {
        return new CliAutoDetector(annotatedInterfaces);
    }

    private CliAutoDetector(Collection<TypeElement> annotatedInterfaces) {
        this.annotatedInterfaces = annotatedInterfaces;
    }

    /**
     * Tries to match commands that do not explicitly declare their cli.
     * AutoDetection consists of choosing that cli lying further up in their respective package hierarchy.
     * This will throw an exception if there are multiple command-line interfaces in the "upwards" package-tree
     * of a command requiring auto-detection.
     * <p>Starts searching from the package of the respective command.</p>
     */
    public Map<TypeElement, List<CommandDto>> groupByCli(List<CommandDto> commandDtosForAutoDetection, ProcessingEnvironment processingEnv) {
        if (commandDtosForAutoDetection.isEmpty())
            return Map.of();
        Map<PackageElement, List<TypeElement>> cliByPackage = annotatedInterfaces.stream()
                .collect(Collectors.groupingBy(te -> processingEnv.getElementUtils().getPackageOf(te)));
        log.debug("Cli elements by package: {}", cliByPackage);

        Map<TypeElement, List<CommandDto>> commandDtoByCli = new HashMap<>(annotatedInterfaces.size());
        for (CommandDto commandDto : commandDtosForAutoDetection) {
            String[] packageElements = processingEnv.getElementUtils().getPackageOf(commandDto.getAnnotatedMethod()).toString().split("\\.");
            final List<TypeElement> matchingClis = new ArrayList<>();
            PackageElement currentPackage;
            while (matchingClis.isEmpty() && packageElements.length > 0) {
                currentPackage = processingEnv.getElementUtils().getPackageElement(String.join(".", packageElements));
                matchingClis.addAll(cliByPackage.getOrDefault(currentPackage, List.of()));
                packageElements = Arrays.copyOf(packageElements, packageElements.length - 1);
            }
            TypeElement matchingCli;
            if (matchingClis.isEmpty()) {
                continue;
            } else if (matchingClis.size() == 1) {
                matchingCli = matchingClis.get(0);
            } else {
                /*
                found package with more than one potential parent-cli.
                This can only be resolved, if the current command is directly enclosed by one of these cli-elements.
                 */
                Element annotatedMethodEnclosingElement = commandDto.getAnnotatedMethod().getEnclosingElement();
                matchingCli = matchingClis.stream()
                        .filter(cli -> cli.getSimpleName().equals(annotatedMethodEnclosingElement.getSimpleName()))
                        .findFirst()
                        .orElseThrow(() -> new ConfigurationException("Can not infer cli from command %s: found multiple candidates in package hierarchy: %s, consider explicitly declaring a cli for this command", commandDto, matchingClis));
            }
            if (commandDtoByCli.containsKey(matchingCli))
                commandDtoByCli.get(matchingCli).add(commandDto);
            else
                commandDtoByCli.put(matchingCli, new LinkedList<>(List.of(commandDto)));
            log.debug("Command automatically assigned to cli: {} -> {}", commandDto.getName(), matchingCli);
        }
        return commandDtoByCli;
    }

}
