package io.github.johannesbuchholz.clihats.core.execution;

import java.util.Optional;

public interface ParserId extends Comparable<ParserId> {

    String getValue();

    Optional<String> hasCommonParts(ParserId other);

}
