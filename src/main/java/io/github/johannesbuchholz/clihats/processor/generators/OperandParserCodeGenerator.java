package io.github.johannesbuchholz.clihats.processor.generators;

import io.github.johannesbuchholz.clihats.core.execution.parser.ArgumentParsers;
import io.github.johannesbuchholz.clihats.processor.exceptions.ArgumentConfigurationException;
import io.github.johannesbuchholz.clihats.processor.exceptions.ConfigurationException;
import io.github.johannesbuchholz.clihats.processor.model.ArgumentDto;
import io.github.johannesbuchholz.clihats.processor.model.SnippetCodeData;
import io.github.johannesbuchholz.clihats.processor.util.ProcessingUtils;
import io.github.johannesbuchholz.clihats.processor.util.TextUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import java.util.HashSet;
import java.util.Set;

public class OperandParserCodeGenerator extends AbstractArgumentParserCodeGenerator {

    final int operandIndex;

    public OperandParserCodeGenerator(ArgumentDto argumentInputs, VariableElement targetVariableElement, int operandIndex, ProcessingEnvironment processingEnvironment) throws ConfigurationException, ArgumentConfigurationException {
        super(
                argumentInputs,
                targetVariableElement,
                deduceMapperTypeAndVerify(argumentInputs.getMapper(), targetVariableElement.asType(), processingEnvironment),
                processingEnvironment);
        this.operandIndex = operandIndex;
    }

    @Override
    public SnippetCodeData generateParserCode() {
        SnippetCodeData mapperSnippetCodeData = generateMapperCode();
        SnippetCodeData promptSnippetCodeData = generatePromptCode();
        Set<String> imports = new HashSet<>(ProcessingUtils.getPackageStrings(ArgumentParsers.class));
        imports.addAll(promptSnippetCodeData.getImportPackages());
        imports.addAll(mapperSnippetCodeData.getImportPackages());
        return SnippetCodeData.from(
                String.format(
                        "%s.operand(%s)%s%s%s%s%s%s",
                        ArgumentParsers.class.getSimpleName(),
                        operandIndex,
                        generateName(),
                        generateRequiredCode(),
                        generateDefaultValueCode(),
                        mapperSnippetCodeData.getCodeSnippet(),
                        promptSnippetCodeData.getCodeSnippet(),
                        generateDescriptionCode()
                ),
                imports
        );
    }

    String generateName() {
        String name;
        if (!argumentInputs.getName().isEmpty()) {
            name = argumentInputs.getName().get(0);
        } else {
            name = targetVariableElement.getSimpleName().toString();
        }
        return ".withDisplayName(" + TextUtils.quote(TextUtils.toUpperCaseString(name)) + ")";
    }

}
