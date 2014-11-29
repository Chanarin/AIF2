package io.aif.language.word.comparator;


import io.aif.language.token.comparator.ITokenComparator;

import java.util.Set;

class OptimisedMeshComparator implements ISetComparator {

    private static final double MAX_AVERAGE_LENGTH_DISTANCE = .5;

    private final ISetComparator meshComparator;

    OptimisedMeshComparator(final ITokenComparator tokenComparator) {
        this.meshComparator = new MeshComparator(tokenComparator);
    }

    @Override
    public double compare(final Set<String> t1, final Set<String> t2) {
        if (t1.stream().filter(t2::contains).count() > 0) return 1.;
        final double averageT1Size = averageTokenLength(t1);
        final double averageT2Size = averageTokenLength(t2);
        final double distance = 1. - Math.min(averageT1Size, averageT2Size) / Math.max(averageT1Size, averageT2Size);
        if (distance > MAX_AVERAGE_LENGTH_DISTANCE) return .0;
        return meshComparator.compare(t1, t2);
    }

    private double averageTokenLength(final Set<String> tokens) {
        return tokens
                .stream()
                .mapToInt(String::length)
                .average()
                .getAsDouble();
    }

}
