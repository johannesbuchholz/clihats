package io.github.johannesbuchholz.clihats.processor;

import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.exceptions.ProcessingException;
import io.github.johannesbuchholz.clihats.processor.generators.CommanderProviderCodeGenerator;
import io.github.johannesbuchholz.clihats.processor.logging.Logging;
import io.github.johannesbuchholz.clihats.processor.mapper.AbstractValueMapper;
import io.github.johannesbuchholz.clihats.processor.model.CommandDto;
import io.github.johannesbuchholz.clihats.processor.model.CommanderDto;
import io.github.johannesbuchholz.clihats.processor.model.ProgramCodeData;
import io.github.johannesbuchholz.clihats.processor.util.CliAutoDetector;
import io.github.johannesbuchholz.clihats.processor.util.ProcessingUtils;
import io.github.johannesbuchholz.clihats.processor.util.visitors.ArrayOfTypeAnnotationValueVisitor;
import io.github.johannesbuchholz.clihats.processor.util.visitors.SimpleValueAnnotationValueVisitor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SupportedAnnotationTypes("io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class CommandLineInterfaceProcessor extends AbstractProcessor {

    public static final String IMPLEMENTATION_VERSION = CommandLineInterfaceProcessor.class.getPackage().getImplementationVersion();
    public static final String JAVA_VERSION = System.getProperty("java.version");
    public static final String JAVA_VENDOR = System.getProperty("java.vendor");

    public static TypeElement cliAnnotationType;
    public static TypeElement commandAnnotationType;
    public static TypeElement optionAnnotationType;
    public static TypeElement abstractValueMapperType;
    public static TypeElement identityMapperType;

    private void initStaticFields() {
        cliAnnotationType = processingEnv.getElementUtils().getTypeElement(CommandLineInterface.class.getCanonicalName());
        commandAnnotationType = processingEnv.getElementUtils().getTypeElement(Command.class.getCanonicalName());
        optionAnnotationType = processingEnv.getElementUtils().getTypeElement(Argument.class.getCanonicalName());
        abstractValueMapperType = processingEnv.getElementUtils().getTypeElement(AbstractValueMapper.class.getCanonicalName());
        identityMapperType = processingEnv.getElementUtils().getTypeElement(AbstractValueMapper.IdentityMapper.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        try {
            TypeElement cliType = annotations.stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Expected exactly one entry of type " + cliAnnotationType.getQualifiedName()));
            initStaticFields();
            writeAllCliSourceFiles(cliType, roundEnv);
        } catch (Exception e) {
            System.err.println(CommandLineInterfaceProcessor.class.getCanonicalName() + " Unexpected error while processing: " + e.getMessage());
            e.printStackTrace(System.err);
            throw e;
        }
        return true;
    }

    private void writeAllCliSourceFiles(TypeElement cliAnnotation, RoundEnvironment roundEnvironment) {
        List<CommanderDto> commanderDtos = collectProgramInfo(roundEnvironment, cliAnnotation);
        if (commanderDtos.isEmpty())
            return;
        ProgramCodeData programCodeData = new CommanderProviderCodeGenerator(processingEnv, commanderDtos)
                .generateCommanderProviderCode();
        writeClassFile(programCodeData);
    }

    private void writeClassFile(ProgramCodeData programCodeData) {
        JavaFileObject builderFile;
        try {
            builderFile = processingEnv.getFiler().createSourceFile(programCodeData.getQualifiedClassName());
        } catch (IOException e) {
            throw new ProcessingException(e, "Could not create source {}: {}", programCodeData.getQualifiedClassName(), e);
        }

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            out.write(programCodeData.getClassFileContent());
        } catch (IOException e) {
            throw new ProcessingException(e, "Could not write to source file {}: {}", programCodeData.getQualifiedClassName(), e);
        }
        Logging.getCliHatsLogger().info("Wrote generated classes to {}", builderFile.toUri());
    }

    /**
     * Groups {@link Command} to their {@link CommandLineInterface} objects and creates one {@link CommanderDto} per
     * such command-line interface.
     */
    private List<CommanderDto> collectProgramInfo(RoundEnvironment roundEnvironment, TypeElement cliAnnotation) {
        // init map with empty lists per declared cli
        Map<TypeElement, List<CommandDto>> commandDtosByAnnotatedCli = roundEnvironment.getElementsAnnotatedWith(cliAnnotation).stream()
                .collect(Collectors.toMap(element -> (TypeElement) element, element -> new ArrayList<>()));

        if (Logging.getCliHatsLogger().isDebugEnabled())
            commandDtosByAnnotatedCli.keySet().forEach(annotatedCli -> Logging.getCliHatsLogger().debug("Found command-line interface: {}", annotatedCli));

        // match annotated command-methods with declared cli or mark for auto-detection
        List<CommandDto> commandDtosForAutoDetection = new ArrayList<>();
        Set<? extends Element> commandAnnotatedMethods = roundEnvironment.getElementsAnnotatedWith(Command.class);
        commandAnnotatedMethods.stream()
                .map(element -> ((ExecutableElement) element))
                .map(this::mapToCommandDto)
                .peek(commandDto -> Logging.getCliHatsLogger().debug("Found command: {}", commandDto))
                .forEach(commandDto -> {
                    List<TypeElement> clisOfCommand = commandDto.getCli();
                    if (clisOfCommand.isEmpty())
                        commandDtosForAutoDetection.add(commandDto);
                    else
                        new HashSet<>(clisOfCommand)
                                .forEach(cliOfCommand -> Optional.ofNullable(commandDtosByAnnotatedCli.get(cliOfCommand))
                                        .ifPresentOrElse(
                                                listOfCommands -> listOfCommands.add(commandDto),
                                                () -> Logging.getCliHatsLogger().warn("Could not match command {} to desired command-line interface: {} is not a command-line interface", commandDto, cliOfCommand.getQualifiedName())));
                });

        // auto detection
        Map<TypeElement, List<CommandDto>> commandDtosByAutodetectedCli
                = CliAutoDetector.getNewFor(commandDtosByAnnotatedCli.keySet()).groupByCli(commandDtosForAutoDetection, processingEnv);

        // merge maps
        commandDtosByAutodetectedCli.forEach((type, dtos) ->
                commandDtosByAnnotatedCli.merge(type, dtos, (l1, l2) -> Stream.of(l1, l2).flatMap(List::stream).collect(Collectors.toList())));

        if (Logging.getCliHatsLogger().isDebugEnabled())
            commandDtosByAnnotatedCli.forEach((key, value) -> Logging.getCliHatsLogger().debug("Collected commands for cli {}: {}", key, value));

        // log unmatched command-methods
        commandAnnotatedMethods.stream()
                .filter(annotatedMethod -> commandDtosByAnnotatedCli.values().stream()
                        .flatMap(List::stream)
                        .noneMatch(dtoWithCli -> dtoWithCli.getAnnotatedMethod().equals(annotatedMethod)))
                .forEach(unmatchedCommandDto -> Logging.getCliHatsLogger().warn("Found command not associated to any cli: {} enclosed by {}", unmatchedCommandDto, unmatchedCommandDto.getEnclosingElement()));

        // create commander dtos
        return commandDtosByAnnotatedCli.entrySet().stream()
                .map(entry -> mapToCommanderDto(entry.getKey(), entry.getValue()))
                .peek(commanderDto -> {
                    if (commanderDto.getCommandDtoList().isEmpty())
                        Logging.getCliHatsLogger().warn("Interface {} does not possess any commands", commanderDto.getAnnotatedInterface().getQualifiedName());
                })
                .collect(Collectors.toList());
    }

    private CommanderDto mapToCommanderDto(TypeElement cliInterfaceType, List<CommandDto> matchingCommandDto) {
        Map<String, ? extends AnnotationValue> cliAnnotationValuesBySimpleName
                = ProcessingUtils.getAnnotationValuesBySimpleName(cliInterfaceType, cliAnnotationType, processingEnv)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(cliAnnotationType.getQualifiedName() + " should appear exactly once on the given executable"));
        return new CommanderDto(
                Objects.requireNonNull(cliAnnotationValuesBySimpleName.get(CommanderDto.NAME_FIELD_NAME)).accept(new SimpleValueAnnotationValueVisitor<>(String.class), null).trim(),
                Objects.requireNonNull(cliAnnotationValuesBySimpleName.get(CommanderDto.DESCRIPTION_FIELD_NAME)).accept(new SimpleValueAnnotationValueVisitor<>(String.class), null).trim(),
                cliInterfaceType,
                matchingCommandDto
        );
    }

    private CommandDto mapToCommandDto(ExecutableElement commandAnnotatedElement) {
        Map<String, ? extends AnnotationValue> commandAnnotationValuesBySimpleName
                = ProcessingUtils.getAnnotationValuesBySimpleName(commandAnnotatedElement, commandAnnotationType, processingEnv)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(commandAnnotationType.getQualifiedName() + " should appear exactly once on the given executable"));
        return new CommandDto(
                Objects.requireNonNull(commandAnnotationValuesBySimpleName.get(CommandDto.NAME_FIELD_NAME)).accept(new SimpleValueAnnotationValueVisitor<>(String.class), null).trim(),
                Objects.requireNonNull(commandAnnotationValuesBySimpleName.get(CommandDto.DESCRIPTION_FIELD_NAME)).accept(new SimpleValueAnnotationValueVisitor<>(String.class), null).trim(),
                Objects.requireNonNull(commandAnnotationValuesBySimpleName.get(CommandDto.CLI_FIELD_NAME)).accept(new ArrayOfTypeAnnotationValueVisitor(), processingEnv.getTypeUtils()),
                commandAnnotatedElement
        );
    }

}
