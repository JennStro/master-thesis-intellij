
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.Arrays;

public class AnalyserTest {

    Analyser analyser;

    @BeforeEach
    public void setUp() {
        this.analyser = new Analyser();
    }

    @Test
    public void simpleTest() {
        ArrayList<String> expectedTokens = new ArrayList<>(Arrays.asList("Hei", "p", "deg"));
        Assertions.assertEquals(expectedTokens, this.analyser.getTokens("Hei p deg"));
    }

    @Test
    public void semicolonIsOwnToken() {
        ArrayList<String> expectedTokens = new ArrayList<>(Arrays.asList("Hei", "p", "deg", ";"));
        Assertions.assertEquals(expectedTokens, this.analyser.getTokens("Hei p deg;"));
    }

    @Test
    public void semiColonInMiddle() {
        ArrayList<String> expectedTokens = new ArrayList<>(Arrays.asList("Hei", "p", ";", "deg", ";"));
        Assertions.assertEquals(expectedTokens, this.analyser.getTokens("Hei p; deg;"));
    }

    @Test
    public void semicolonAfterIf() {
        ArrayList<String> expectedTokens = new ArrayList<>(Arrays.asList("if", "(", "true", ")", ";"));
        Assertions.assertEquals(expectedTokens, this.analyser.getTokens("if(true);"));
    }

    @Test
    public void squareBrackets() {
        ArrayList<String> expectedTokens = new ArrayList<>(Arrays.asList("if", "(", "true", ")", "{", "}"));
        Assertions.assertEquals(expectedTokens, this.analyser.getTokens("if(true) {}"));
    }

    @Test
    public void assignment() {
        ArrayList<String> expectedTokens = new ArrayList<>(Arrays.asList("if", "(", "true", ")", "{", "a", "=", "b", ";", "}"));
        Assertions.assertEquals(expectedTokens, this.analyser.getTokens("if(true) { a = b; }"));
    }

    @Test
    public void comparison() {
        ArrayList<String> expectedTokens = new ArrayList<>(Arrays.asList("if", "(", "a", "==", "b", ")", "{", "a", "=", "b", ";", "}"));
        Assertions.assertEquals(expectedTokens, this.analyser.getTokens("if(a==b) { a = b; }"));
    }

    @Test
    public void methodCall() {
        ArrayList<String> expectedTokens = new ArrayList<>(Arrays.asList("something", ".", "toString", "(", ")", ";"));
        Assertions.assertEquals(expectedTokens, this.analyser.getTokens("something.toString();"));
    }

    @Test
    public void shouldFindSemicolonAfterIf() {
        String codeWithError = "if(true);";
        Assertions.assertTrue(analyser.hasSemicolonAfterIf(codeWithError).isHasError());
    }

    @Test
    public void shouldFindSemiColorAfterIfNestedParanthesis() {
        String codeWithError = "if((true && false) || true);";
        Assertions.assertTrue(analyser.hasSemicolonAfterIf(codeWithError).isHasError());
    }

    @Test
    public void shouldNotFindSemicolonAfterIf() {
        String codeWithNoError = "if((true && false) || true)";
        Assertions.assertFalse(analyser.hasSemicolonAfterIf(codeWithNoError).isHasError());
    }

    @Test
    public void shouldFindErrorOnLineThree() {
        String codeWithError = "\n \n if(true);";
        Assertions.assertEquals(3, analyser.hasSemicolonAfterIf(codeWithError).getLineNumber());
    }

    @Test
    public void shouldFindErrorOnLineThreeNotLastLine() {
        String codeWithError = "\n \n if(true); \n";
        Assertions.assertEquals(3, analyser.hasSemicolonAfterIf(codeWithError).getLineNumber());
    }
}
