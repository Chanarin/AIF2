package com.aif.language.sentence.separators.clasificators;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.*;
import java.util.stream.Collectors;

class StatSentenceSeparatorGroupsClassificatory implements ISentenceSeparatorGroupsClassificatory {

    @Override
    public Map<Group, Set<Character>> classify(final List<String> tokens, final List<Set<Character>> separatorsGroups) {
        final List<Integer> mapTokensToGroups = mapTokensToGroups(tokens, separatorsGroups);

        final Map<Integer, List<Integer>> groupsIntegrationFactor = new HashMap<>();
        final Map<Integer, Integer> tempGroupIntegrationFactorCalculator = new HashMap<>();
        final Map<Integer, Integer> lastPosition = new HashMap<>();
        final Map<Integer, List<Integer>> distances = new HashMap<>();

        for (int i = 1; i <= separatorsGroups.size(); i++) {
            groupsIntegrationFactor.put(i, new ArrayList<>());
            tempGroupIntegrationFactorCalculator.put(i, 0);
            lastPosition.put(i, 0);
            distances.put(i, new ArrayList<>());
        }

        for (int i = 0; i < mapTokensToGroups.size(); i++) {
            final Integer element = mapTokensToGroups.get(i);
            if (element != 0) {

                distances.get(element).add(i - lastPosition.get(element));
                lastPosition.put(element, i);
            }
        }

        distances.keySet().forEach(key -> {
            final List<Integer> dist = distances.get(key);
            final List<Integer> filteredDist = new ArrayList<>();
            Collections.sort(dist);
            final int limit = (int)((double)dist.size() * .8);
            int i = 0;
            while (filteredDist.size() < limit) {
                filteredDist.add(dist.get(i++));
            }
            distances.put(key, filteredDist);
        });

        final Map<Integer, Double> sum = new HashMap<>();
//        groupsIntegrationFactor.keySet().stream().forEach(
//                key -> sum.put(key, groupsIntegrationFactor.get(key).stream().filter(i -> i != 0).mapToDouble(i -> (double)i).average().getAsDouble())
//        );
        distances.keySet().forEach(
                key -> {
                    final SummaryStatistics stats = new SummaryStatistics();
                    distances.get(key).forEach(element -> stats.addValue(element));
                    sum.put(key, stats.getMean());
                }
        );

        final Map<Group, Set<Character>> result = new HashMap<>();
        result.put(Group.GROUP_2, new HashSet<>());
        final double max = sum.entrySet().stream().mapToDouble(Map.Entry::getValue).max().getAsDouble();
        sum.keySet().forEach(key -> {
            if (sum.get(key) ==  max) {
                result.put(Group.GROUP_1, separatorsGroups.get(key - 1));
            } else {
                result.get(Group.GROUP_2).addAll(separatorsGroups.get(key - 1));
            }
        });
        return result;
    }

//    @Override
//    public Map<Group, Set<Character>> classify(final List<String> tokens, final List<Set<Character>> separatorsGroups) {
//        final List<Integer> mapTokensToGroups = mapTokensToGroups(tokens, separatorsGroups);
//
//        final Map<Integer, List<Integer>> groupsIntegrationFactor = new HashMap<>();
//        final Map<Integer, Integer> tempGroupIntegrationFactorCalculator = new HashMap<>();
//        final Map<Integer, Integer> lastPosition = new HashMap<>();
//
//        for (int i = 1; i <= separatorsGroups.size(); i++) {
//            groupsIntegrationFactor.put(i, new ArrayList<>());
//            tempGroupIntegrationFactorCalculator.put(i, 0);
//            lastPosition.put(i, 0);
//        }
//
//        for (int i = 0; i < mapTokensToGroups.size(); i++) {
//            final Integer element = mapTokensToGroups.get(i);
//            if (element != 0) {
//                groupsIntegrationFactor.get(element).add(tempGroupIntegrationFactorCalculator.get(element));
//                tempGroupIntegrationFactorCalculator.put(element, 0);
//                lastPosition.put(element, i);
//                tempGroupIntegrationFactorCalculator
//                        .keySet()
//                        .stream()
//                        .filter(key -> key != element)
//                        .forEach(key ->
//                                tempGroupIntegrationFactorCalculator.put(key, tempGroupIntegrationFactorCalculator.get(key) + 1));
//            } else {
//                for (Integer key : lastPosition.keySet()) {
//                    if (i - lastPosition.get(key) > 20) {
//                        lastPosition.put(key, i);
//                        tempGroupIntegrationFactorCalculator.put(key, 0);
//                    }
//                }
//            }
//        }
//
//        final Map<Integer, Double> sum = new HashMap<>();
//        groupsIntegrationFactor.keySet().stream().forEach(
//                key -> sum.put(key, groupsIntegrationFactor.get(key).stream().filter(i -> i != 0).mapToDouble(i -> (double)i).average().getAsDouble())
//        );
//
//        final Map<Group, Set<Character>> result = new HashMap<>();
//        result.put(Group.GROUP_2, new HashSet<>());
//        final double max = sum.entrySet().stream().mapToDouble(Map.Entry::getValue).max().getAsDouble();
//        sum.keySet().forEach(key -> {
//            if (sum.get(key) ==  max) {
//                result.put(Group.GROUP_1, separatorsGroups.get(key - 1));
//            } else {
//                result.get(Group.GROUP_2).addAll(separatorsGroups.get(key - 1));
//            }
//        });
//        return result;
//    }

    private List<Integer> mapTokensToGroups(final List<String> tokens, final List<Set<Character>> separatorsGroups) {
        return tokens
                .stream()
                .map(
                        token -> {
                            if (token.isEmpty()) return 0;
                            for (Integer i = 0; i < separatorsGroups.size(); i++) {
                                final Set<Character> separatorGroup = separatorsGroups.get(i);
                                if (separatorGroup.contains(token.charAt(0))) {
                                    return i + 1;
                                }
                                if (separatorGroup.contains(token.charAt(token.length() - 1))) {
                                    return i + 1;
                                }
                            }
                            return 0;
                        }
                ).collect(Collectors.toList());
    }

}
