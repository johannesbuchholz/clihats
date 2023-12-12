package io.github.johannesbuchholz.clihats.processor.mapper;

import io.github.johannesbuchholz.clihats.processor.model.SnippetCodeData;
import io.github.johannesbuchholz.clihats.processor.util.ProcessingUtils;

import javax.lang.model.element.TypeElement;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultMapperRegistry {

    private static final Map<String, SnippetCodeData> DEFAULT_MAPPER_CODE_BY_TARGET_TYPE_NAME = Map.ofEntries(
            Map.entry(Path.class.getCanonicalName(), SnippetCodeData.from("Path::of", ProcessingUtils.getPackageStrings(Path.class))),
            Map.entry(Boolean.class.getCanonicalName(),  SnippetCodeData.from("Boolean::parseBoolean")),
            Map.entry(Integer.class.getCanonicalName(), SnippetCodeData.from("Integer::parseInt")),
            Map.entry(Double.class.getCanonicalName(), SnippetCodeData.from("Double::parseDouble")),
            Map.entry(Float.class.getCanonicalName(), SnippetCodeData.from("Float::parseFloat")),
            Map.entry(LocalDate.class.getCanonicalName(), SnippetCodeData.from("LocalDate::parse", ProcessingUtils.getPackageStrings(LocalDate.class))),
            Map.entry(LocalDateTime.class.getCanonicalName(), SnippetCodeData.from("LocalDateTime::parse", ProcessingUtils.getPackageStrings(LocalDateTime.class))),
            Map.entry(BigDecimal.class.getCanonicalName(), SnippetCodeData.from("str -> BigDecimal.valueOf(Double.parseDouble(str))", ProcessingUtils.getPackageStrings(BigDecimal.class)))
    );

    public static final List<String> SUPPORTED_TARGET_TYPES = DEFAULT_MAPPER_CODE_BY_TARGET_TYPE_NAME.keySet().stream().sorted().collect(Collectors.toList());

    public static Optional<SnippetCodeData> getMapperCodeFor(TypeElement targetType) {
        if (targetType == null)
            throw new IllegalArgumentException("TypeElement must not be null");
        return Optional.ofNullable(DEFAULT_MAPPER_CODE_BY_TARGET_TYPE_NAME.get(targetType.getQualifiedName().toString()));
    }

    public static SnippetCodeData getEnumMapperCodeFor(TypeElement targetType) {
        return SnippetCodeData.from(
                String.format("str -> Enum.valueOf(%s.class, str)", targetType.getSimpleName()),
                ProcessingUtils.getPackageStrings(targetType));
    }

}
