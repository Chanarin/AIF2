package io.aif.language.word.dict;

import com.google.gson.Gson;
import io.aif.common.FileHelper;
import io.aif.language.common.settings.ISettings;
import io.aif.language.sentence.SimpleSentenceSplitterCharactersExtractorQualityTest;
import io.aif.language.sentence.splitters.AbstractSentenceSplitter;
import io.aif.language.token.TokenSplitter;
import io.aif.language.token.comparator.ITokenComparator;
import io.aif.language.word.IDict;
import io.aif.language.word.IWord;
import io.aif.language.word.comparator.ISetComparator;
import opennlp.tools.formats.ad.ADSentenceStream;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by b0noI on 31/10/14.
 */
public class DictBuilderTest {
    
    private static final Gson GSON = new Gson();

    @Test(groups = "experimental")
    public void testQuality() throws Exception {
        String text;
        long before = System.nanoTime();
        try(InputStream modelResource = SimpleSentenceSplitterCharactersExtractorQualityTest.class.getResourceAsStream("aif_article.txt")) {
            text = FileHelper.readAllText(modelResource);
        }

        final TokenSplitter tokenSplitter = new TokenSplitter();
        final AbstractSentenceSplitter sentenceSplitter = AbstractSentenceSplitter.Type.HEURISTIC.getInstance();
        final List<String> tokens = tokenSplitter.split(text);
        final List<List<String>> sentences = sentenceSplitter.split(tokens);
        final List<String> filteredTokens = sentences.stream().flatMap(List::stream).collect(Collectors.toList());

        final ITokenComparator tokenComparator = ITokenComparator.defaultComparator();
        final ISetComparator setComparator = ISetComparator.createDefaultInstance(tokenComparator);
        final DictBuilder dictBuilder = new DictBuilder(setComparator, tokenComparator);
        final IDict dict = dictBuilder.build(filteredTokens);

        long after = System.nanoTime();
        long delta = (after - before) / 1000_000_000;
        
        final IdealDict idealDict = loadIdealDict();
        
        final int rootTokenErrors = (int)dict.getWords().stream().filter(word -> 
            rootTokenError(word, idealDict)
        ).count();
        final int tokensErrors = dict.getWords().stream().mapToInt(word ->
            tokensErrors(word, idealDict)
        ).sum();


        dict.getWords().stream().filter(word ->
                        tokensErrors(word, idealDict) > 0
        ).forEach(System.out::println);
        System.out.println(dict);
        // 180 sec
        // 122 best
    }
    
    private static IdealDict loadIdealDict() throws IOException {
        try(final InputStream modelResource = DictBuilderTest.class.getResourceAsStream("ideal_dict.json")) {
            final String json = FileHelper.readAllText(modelResource);
            return GSON.fromJson(json, IdealDict.class);
        }
    }
    
    private static class IdealDict {
        
        private Map<String, List<String>> words;
        
        public Optional<Map.Entry<String, List<String>>> findTarget(final String targetToken) {
            return words.entrySet().stream().filter(entry ->
                            entry.getKey().equals(targetToken) || entry.getValue().contains(targetToken)
            ).findFirst();
        }
        
    }
    
    private static boolean rootTokenError(final IWord word, final IdealDict idealDict) {
        final Optional<Map.Entry<String, List<String>>> idealResult = idealDict.findTarget(word.getRootToken());
        if (!idealResult.isPresent()) {
            return true;
        }
        return !idealResult.get().getKey().toLowerCase().equals(word.getRootToken().toLowerCase());
    }
    
    private static int tokensErrors(final IWord word, final IdealDict idealDict) {
        final Optional<Map.Entry<String, List<String>>> idealResult = idealDict.findTarget(word.getRootToken());  
        if (!idealResult.isPresent()) {
            return word.getAllTokens().size();
        }
        return (int)word.getAllTokens().stream().filter(token -> !idealResult.get().getValue().contains(token)).count();
    }

}
