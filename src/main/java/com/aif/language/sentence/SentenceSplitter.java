package com.aif.language.sentence;

import com.aif.language.common.ISplitter;
import com.aif.language.common.VisibilityReducedForTestPurposeOnly;

import java.util.*;
import java.util.stream.Collectors;

public class SentenceSplitter implements ISplitter<List<String>, List<String>> {

    private         final   ISentenceSeparatorExtractor sentenceSeparatorExtractor;

    public SentenceSplitter(final ISentenceSeparatorExtractor sentenceSeparatorExtractor) {
        this.sentenceSeparatorExtractor = sentenceSeparatorExtractor;
    }

    public SentenceSplitter() {
        this(ISentenceSeparatorExtractor.Type.PREDEFINED.getInstance());
    }

    @Override
    public List<List<String>> split(final List<String> tokens) {
        final Optional<List<Character>> optionalSeparators = sentenceSeparatorExtractor.extract(tokens);

        if (!optionalSeparators.isPresent()) {
            return new ArrayList<List<String>>(){{add(tokens);}};
        }

        final List<Character> separators = optionalSeparators.get();

        final List<Boolean> listOfPositions = SentenceSplitter.mapToBooleans(tokens, separators);

        final SentenceIterator sentenceIterator = new SentenceIterator(tokens, listOfPositions);

        final List<List<String>> sentences = new ArrayList<>();
        while (sentenceIterator.hasNext()) {
            sentences.add(sentenceIterator.next());
        }

        sentences.forEach(sentence -> prepareSentences(sentence, separators));

        return sentences
                .parallelStream()
                .map(sentence -> SentenceSplitter.prepareSentences(sentence, separators))
                .collect(Collectors.toList());
    }

    @VisibilityReducedForTestPurposeOnly
    static List<String> prepareSentences(final List<String> sentence, final List<Character> separators) {
        final List<String> preparedTokens = new ArrayList<>();

        for (String token: sentence) {
            preparedTokens.addAll(prepareToken(token, separators));
        }

        return preparedTokens;
    }

    @VisibilityReducedForTestPurposeOnly
    static List<String> prepareToken(final String token, final List<Character> separators) {
        final List<String> tokens = new ArrayList<>(3);
        final int lastPosition = lastNonSeparatorPosition(token, separators);
        final int firstPosition = firstNonSeparatorPosition(token, separators);

        if (firstPosition != 0) {
            tokens.add(token.substring(0, firstPosition));
        }

        tokens.add(token.substring(firstPosition, lastPosition));

        if (lastPosition != token.length()) {
            tokens.add(token.substring(lastPosition, token.length()));
        }

        return tokens;
    }

    static int firstNonSeparatorPosition(final String token, final List<Character> separarors) {
        if (!separarors.contains(token.charAt(0))) {
            return 0;
        }
        int i = 0;
        while (i < token.length() && separarors.contains(token.charAt(i))) {
            i++;
        }
        if (i == token.length()) {
            return 0;
        }
        return i;
    }

    static int lastNonSeparatorPosition(final String token, final List<Character> separators) {
        if (!separators.contains(token.charAt(token.length() - 1))) {
            return token.length();
        }
        int i = token.length() - 1;
        while (i > 0 && separators.contains(token.charAt(i))) {
            i--;
        }
        i++;
        if (i == 0) {
            return token.length();
        }
        return i;
    }

    @VisibilityReducedForTestPurposeOnly
    static List<Boolean> mapToBooleans(final List<String> tokens, final List<Character> separators) {
        final List<Boolean> result = new ArrayList<>(tokens.size());

        for (int i = 0; i < tokens.size(); i++) {
            final String token = tokens.get(i);
            if (separators.contains(token.charAt(token.length() - 1))) {
                result.add(true);
            } else if (i != tokens.size() - 1 && separators.contains(token.charAt(0))) {
                result.add(true);
            } else {
                result.add(false);
            }
        }

        return result;
    }

    @VisibilityReducedForTestPurposeOnly
    static class SentenceIterator implements Iterator<List<String>> {

        private final   List<String>    tokens;

        private final   List<Boolean>   endTokens;

        private         int             currentPosition = 0;

        public SentenceIterator(List<String> tokens, List<Boolean> endTokens) {
            assert tokens != null;
            assert endTokens != null;
            assert tokens.size() == endTokens.size();
            this.tokens = tokens;
            this.endTokens = endTokens;
        }

        @Override
        public boolean hasNext() {
            return currentPosition != tokens.size();
        }

        @Override
        public List<String> next() {
            final List<String> sentence = getNextSentence();

            return sentence;
        }

        private List<String> getNextSentence() {
            final int oldIndex = currentPosition;
            currentPosition = getNextTrueIndex();
            return this.tokens.subList(oldIndex, currentPosition);
        }

        private int getNextTrueIndex() {
            int startIndex = currentPosition;

            if (endTokens.size() == startIndex) {
                return startIndex;
            }

            do {
                if (endTokens.get(startIndex)) {
                    startIndex++;
                    return startIndex;
                }
                startIndex++;
            } while(startIndex < endTokens.size() - 1);
            return startIndex + 1;
        }

    }

}
