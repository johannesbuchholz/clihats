package io.github.johannesbuchholz.clihats.processor.mapper;

import io.github.johannesbuchholz.clihats.core.execution.ValueMapper;
import io.github.johannesbuchholz.clihats.processor.exceptions.ConfigurationException;
import io.github.johannesbuchholz.clihats.processor.mapper.defaults.*;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class DefaultMapperRegistry {

    /**
     * Contains known implementations of {@link AbstractValueMapper} mapped by the canonical names of their respective type.
     */
    private static final Map<String, Class<? extends AbstractValueMapper<?>>> DEFAULT_MAPPERS_BY_TARGET_TYPE_NAME = Map.ofEntries(
            Map.entry(Path.class.getCanonicalName(), PathMapper.class),
            Map.entry(Boolean.class.getCanonicalName(), BooleanMapper.class),
            Map.entry(Integer.class.getCanonicalName(), IntegerMapper.class),
            Map.entry(Double.class.getCanonicalName(), DoubleMapper.class),
            Map.entry(Float.class.getCanonicalName(), FloatMapper.class),
            Map.entry(LocalDate.class.getCanonicalName(), LocalDateMapper.class),
            Map.entry(LocalDateTime.class.getCanonicalName(), LocalDateTimeMapper.class),
            Map.entry(BigDecimal.class.getCanonicalName(), BigDecimalMapper.class)
    );

    /**
     * Returns the type representing an implementation of {@link ValueMapper} mapping to the given type.
     */
    public static TypeElement getForType(TypeElement typeElement, Elements elements) {
        String key = typeElement.getQualifiedName().toString();
        if (!DEFAULT_MAPPERS_BY_TARGET_TYPE_NAME.containsKey(key))
            throw new ConfigurationException("Could not find default mapper for target class %s: available target classes are %s",
                    typeElement.getQualifiedName(), DEFAULT_MAPPERS_BY_TARGET_TYPE_NAME.keySet().stream().toString()
            );
        return elements.getTypeElement(DEFAULT_MAPPERS_BY_TARGET_TYPE_NAME.get(key).getCanonicalName());
    }

}
