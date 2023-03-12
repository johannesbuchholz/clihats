package io.github.johannesbuchholz.clihats.processor.generators;

import io.github.johannesbuchholz.clihats.core.execution.Commander;
import io.github.johannesbuchholz.clihats.processor.exceptions.ConfigurationException;
import io.github.johannesbuchholz.clihats.processor.model.CommandDto;
import io.github.johannesbuchholz.clihats.processor.model.CommanderDto;
import io.github.johannesbuchholz.clihats.processor.model.ExtendedSnippetCodeData;
import io.github.johannesbuchholz.clihats.processor.model.SnippetCodeData;
import io.github.johannesbuchholz.clihats.util.ProcessingUtils;
import io.github.johannesbuchholz.clihats.util.TextUtils;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.*;

public class CommanderCodeGenerator {

    private final ProcessingEnvironment processingEnvironment;
    private final CommanderDto commanderDto;

    public CommanderCodeGenerator(ProcessingEnvironment processingEnvironment, CommanderDto commanderDto) throws ConfigurationException {
        this.processingEnvironment = processingEnvironment;
        this.commanderDto = commanderDto;
    }

    /**
     * Commander.forName("myCommander)
     *     .withDescription("describing myCommander")
     *     .withPrintStream(...)
     *     .withCommands(...)))
     */
    public ExtendedSnippetCodeData generateCommanderCode() {
        StringBuilder commanderSb = new StringBuilder("Commander.forName(").append(TextUtils.quote(generateCommanderName())).append(")");
        Set<String> imports = ProcessingUtils.getPackageStrings(Commander.class);
        // additional setter
        String actualDescription = generateActualDescription();
        if (!actualDescription.isBlank())
            commanderSb.append(CommanderProviderCodeGenerator.NEW_LINE_INDENT_DOUBLE).append(".withDescription(").append(TextUtils.quote(actualDescription)).append(")");
        // commands
        List<CommandDto> commandInfoList = commanderDto.getCommandDtoList();
        List<String> commandCodeStrings = new ArrayList<>(commandInfoList.size());
        Set<SnippetCodeData> requestedMethodAnnotations = new HashSet<>();
        commandInfoList.stream()
                .sorted(Comparator.comparing(CommandDto::getName))
                .forEachOrdered(commandDto -> {
                    ExtendedSnippetCodeData extendedSnippetCodeData = new CommandCodeGenerator(processingEnvironment, commandDto).generateCommandCode();
                    imports.addAll(extendedSnippetCodeData.getImportPackages());
                    commandCodeStrings.add(extendedSnippetCodeData.getCodeSnippet());
                    requestedMethodAnnotations.addAll(extendedSnippetCodeData.getBaggage());
                });
        if (!commandCodeStrings.isEmpty()) {
            commanderSb.append(CommanderProviderCodeGenerator.NEW_LINE_INDENT_DOUBLE).append(".withCommands(");
            commanderSb.append("\n").append(TextUtils.indentEveryLine(String.join(",\n", commandCodeStrings), CommanderProviderCodeGenerator.LINE_INDENT_DOUBLE.repeat(2)));
            commanderSb.append(")");
        }
        return ExtendedSnippetCodeData.from(commanderSb.toString(), imports)
                .setBaggage(requestedMethodAnnotations);
    }

    private String generateCommanderName() {
        String commanderName = commanderDto.getName();
        if (commanderName.isBlank())
            commanderName = TextUtils.toHyphenString(commanderDto.getAnnotatedInterface().getSimpleName().toString());
        return TextUtils.normalizeString(commanderName);
    }

    private String generateActualDescription() {
        String actualDescription = commanderDto.getDescription();
        if (actualDescription.isBlank())
            actualDescription = Objects.requireNonNullElse(processingEnvironment.getElementUtils().getDocComment(commanderDto.getAnnotatedInterface()), "");
        return TextUtils.normalizeString(actualDescription);
    }

}
