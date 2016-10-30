package io.aif.language.sentence.separators.extractors;

import com.google.common.collect.ImmutableList;

import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import io.aif.language.common.IExtractor;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class StatDataExtractorTest {

  @Test(groups = "unit-tests")
  public void testParseStat() throws Exception {
    // input arguments
    final List<String> tokens = ImmutableList.of("12", "token");

    // mocks

    // expected results
    final String expectedToken = "token";

    // creating test instance
    final StatDataExtractor statDataExtractor = new StatDataExtractor(
      str -> Optional.of(str.charAt(0)),
      str -> Optional.of(str.charAt(0))) {

      @Override
      void parsToken(final String token, final StatData statData) {
        assertEquals(token, expectedToken);
      }

    };

    // execution test
    final StatData statData = statDataExtractor.parseStat(tokens);

    // result assert
    assertNotNull(statData);

    // mocks verify
  }

  @Test(groups = "unit-tests")
  public void testParsToken() throws Exception {
    // input arguments
    final String inputToken = "token";

    // mocks
    final StatData mockStatData = mock(StatData.class);
    final IExtractor mockFirstExtractor = mock(IExtractor.class);
    final IExtractor mockSecondExtractor = mock(IExtractor.class);

    when(mockFirstExtractor.extract(eq(inputToken))).thenReturn(Optional.of('t'));
    when(mockSecondExtractor.extract(eq(inputToken))).thenReturn(Optional.of('o'));

    // expected results

    // creating test instance
    final StatDataExtractor statDataExtractor =
      new StatDataExtractor(mockFirstExtractor, mockSecondExtractor);

    // execution test
    statDataExtractor.parsToken(inputToken, mockStatData);

    // result assert

    // mocks verify
    verify(mockStatData, times(1)).addCharacter('t');
    verify(mockStatData, times(1)).addCharacter('o');
    verify(mockStatData, times(1)).addCharacter('k');
    verify(mockStatData, times(1)).addCharacter('e');
    verify(mockStatData, times(1)).addCharacter('n');

    verify(mockStatData, times(1)).addEdgeCharacter('o', 't');
  }

}