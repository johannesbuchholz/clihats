package io.github.johannesbuchholz.clihats.processor.generators;

import io.github.johannesbuchholz.clihats.processor.CommandLineInterfaceProcessor;
import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.exceptions.ArgumentConfigurationException;
import io.github.johannesbuchholz.clihats.processor.model.ArgumentDto;
import io.github.johannesbuchholz.clihats.processor.model.ParameterArgumentPair;
import io.github.johannesbuchholz.clihats.processor.model.ParameterCodeGeneratorPair;
import io.github.johannesbuchholz.clihats.processor.model.TargetParameter;
import io.github.johannesbuchholz.clihats.processor.util.ProcessingUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

public class ArgumentParserCodeGeneratorFactory {

    private final ProcessingEnvironment processingEnvironment;

    public static ArgumentParserCodeGeneratorFactory getNew(ProcessingEnvironment processingEnvironment) {
        return new ArgumentParserCodeGeneratorFactory(processingEnvironment);
    }

    private ArgumentParserCodeGeneratorFactory(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    public List<ParameterCodeGeneratorPair> createParserCodeGenerators(List<ParameterArgumentPair> parameterArgumentPairs) throws ArgumentConfigurationException {
        List<ParameterCodeGeneratorPair> parameterCodeGeneratorPairs = new ArrayList<>(parameterArgumentPairs.size());
        int operandsEncountered = 0;
        for (ParameterArgumentPair pair : parameterArgumentPairs) {
            if (pair.isUnmanaged()) {
                parameterCodeGeneratorPairs.add(ParameterCodeGeneratorPair.unmanagedParameter(pair.getTargetParameter()));
                continue;
            }
            ArgumentDto argumentDto = pair.getArgumentDto();
            VariableElement targetElement = pair.getTargetParameter();
            Argument.Type parserType = argumentDto.getType();
            ArgumentParserCodeGenerator parserCodeGenerator;
            switch (parserType) {
                case OPTION:
                    parserCodeGenerator = createOptionParser(argumentDto, targetElement);
                    break;
                case OPERAND:
                    parserCodeGenerator = createOperandParser(argumentDto, targetElement, operandsEncountered++);
                    break;
                case ARRAY_OPERAND:
                    parserCodeGenerator = createArrayOperandParser(argumentDto, targetElement, operandsEncountered++);
                    break;
                default:
                    throw new IllegalStateException("Unknown argument parserType " + parserType);
            }
            parameterCodeGeneratorPairs.add(ParameterCodeGeneratorPair.pair(targetElement, parserCodeGenerator));
        }
        return parameterCodeGeneratorPairs;
    }

    private ArgumentParserCodeGenerator createOptionParser(ArgumentDto argumentDto, VariableElement targetElement) throws ArgumentConfigurationException {
        verifyMapperType(targetElement.asType(), argumentDto.getMapper());
        TargetParameter targetParameter = extractTargetParameter(targetElement);
        if (argumentDto.getFlagValue().isEmpty()) {
            return new ValuedOptionParserCodeGenerator(argumentDto, targetParameter);
        }
        return new FlagOptionParserCodeGenerator(argumentDto, targetParameter);
    }

    private ArgumentParserCodeGenerator createOperandParser(ArgumentDto argumentDto, VariableElement targetElement, int operandsEncountered) throws ArgumentConfigurationException {
        verifyMapperType(targetElement.asType(), argumentDto.getMapper());
        return new OperandParserCodeGenerator(argumentDto, extractTargetParameter(targetElement), operandsEncountered);
    }

    private ArgumentParserCodeGenerator createArrayOperandParser(ArgumentDto argumentDto, VariableElement targetElement, int operandsEncountered) throws ArgumentConfigurationException {
        DeclaredType arrayComponentType = deduceArrayComponentTypeAndVerify(targetElement);
        verifyMapperType(arrayComponentType, argumentDto.getMapper());
        return new ArrayOperandParserCodeGenerator(argumentDto, extractTargetParameter(targetElement), operandsEncountered, arrayComponentType);
    }

    private void verifyMapperType(TypeMirror targetType, TypeElement mapperInputTypeElement) throws ArgumentConfigurationException{
        if (processingEnvironment.getTypeUtils().isSameType(mapperInputTypeElement.asType(), CommandLineInterfaceProcessor.identityMapperType.asType())) {
            // here if automatic mapper type deduction applies which does not need verification
            return;
        }
        // types match
        DeclaredType declaredMapperType = ProcessingUtils.getMatchingSuperClass(mapperInputTypeElement, CommandLineInterfaceProcessor.abstractValueMapperType, processingEnvironment)
                .orElseThrow(() -> new IllegalStateException("Mapper type should implement ValueMapper but is " + mapperInputTypeElement));
        TypeMirror mapperTargetType = declaredMapperType.getTypeArguments().get(0);
        if (!processingEnvironment.getTypeUtils().isAssignable(mapperTargetType, targetType))
            throw new ArgumentConfigurationException(String.format("Mapper type %s is not assignable to target parameter type %s", mapperTargetType, targetType));
        // ----- verify
        // mapper type is public
        if (!mapperInputTypeElement.getModifiers().contains(Modifier.PUBLIC)) {
            throw new ArgumentConfigurationException(String.format("Mapper %s is not public", mapperInputTypeElement));
        }
        // no-args constructor exists
        if (ProcessingUtils.getPublicNoArgsConstructor(mapperInputTypeElement).isEmpty()) {
            throw new ArgumentConfigurationException(String.format("Mapper %s does not possess a public no-args constructor", mapperInputTypeElement));
        }
    }

    private DeclaredType deduceArrayComponentTypeAndVerify(VariableElement targetElement) throws ArgumentConfigurationException {
        TypeMirror targetType = targetElement.asType();
        if (targetType.getKind() != TypeKind.ARRAY)
            throw new ArgumentConfigurationException(String.format("Target type is not of type array: %s", targetElement));

        TypeMirror componentType = ((ArrayType) targetType).getComponentType();
        if (componentType.getKind() != TypeKind.DECLARED) {
            throw new ArgumentConfigurationException(String.format("Target type is an array that does not contain a declared type: %s", targetElement));
        } else if (!((DeclaredType) componentType).getTypeArguments().isEmpty()) {
            throw new ArgumentConfigurationException(String.format("Arrays with component types possessing type arguments are not supported: %s", targetElement));
        }
        return (DeclaredType) componentType;
    }

    private TargetParameter extractTargetParameter(VariableElement targetVariableElement) {
        return new TargetParameter(
                targetVariableElement.getSimpleName().toString(),
                (TypeElement) processingEnvironment.getTypeUtils().asElement(targetVariableElement.asType())
        );
    }

}
