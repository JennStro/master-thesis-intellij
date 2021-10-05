
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
        Assertions.assertTrue(analyser.hasSemicolonAfterIf(codeWithError).isError());
    }

    @Test
    public void shouldFindSemiColorAfterIfNestedParanthesis() {
        String codeWithError = "if((true && false) || true);";
        Assertions.assertTrue(analyser.hasSemicolonAfterIf(codeWithError).isError());
    }

    @Test
    public void shouldNotFindSemicolonErrorIfNone() {
        String codeWithNoError = "if(true){ int b = 5; }";
        Assertions.assertFalse(analyser.hasSemicolonAfterIf(codeWithNoError).isError());
    }

    @Test
    public void shouldNotFindSemicolonAfterIf() {
        String codeWithNoError = "if((true && false) || true)";
        Assertions.assertFalse(analyser.hasSemicolonAfterIf(codeWithNoError).isError());
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

    @Test
    public void bitwiseAndOperator() {
        ArrayList<String> expectedTokens = new ArrayList<>(Arrays.asList("if", "(", "true", "&", "false", ")"));
        Assertions.assertEquals(expectedTokens, this.analyser.getTokens("if(true & false)"));
    }

    @Test
    public void bitwiseOrOperator() {
        ArrayList<String> expectedTokens = new ArrayList<>(Arrays.asList("if", "(", "true", "|", "false", ")"));
        Assertions.assertEquals(expectedTokens, this.analyser.getTokens("if(true | false)"));
    }

    @Test
    public void usesBitwiseAndOperatorBug() {
        String errorString = "if(true & false)";
        Assertions.assertTrue(this.analyser.usesBitwiseOperator(errorString).isError());
    }

    @Test
    public void usesAndOperator() {
        String errorString = "if(true && false)";
        Assertions.assertFalse(this.analyser.usesBitwiseOperator(errorString).isError());
    }

    @Test
    public void notMatchingParanthesisGivesNoError() {
        String errorString = "if((true && false)";
        Assertions.assertFalse(this.analyser.usesBitwiseOperator(errorString).isError());
    }

    @Test
    public void shouldGetListOfErrorsButOnlyHaseSemicolonError() {
        ArrayList<MaybeError> errors = analyser.getPossibleErrorsOf("if(true && false); {}");
        HashMap<ErrorType, Boolean> hasErrorOfTypeOf = new HashMap<>();
        for (MaybeError error : errors) {
            hasErrorOfTypeOf.put(error.getErrorType(), error.isError());
        }
        Assertions.assertTrue(hasErrorOfTypeOf.get(ErrorType.SEMICOLON_AFTER_IF));
        Assertions.assertFalse(hasErrorOfTypeOf.get(ErrorType.BITWISE_OPERATOR));
    }

    @Test
    public void shouldOnlyGetBitwiseOperatorError() {
        ArrayList<MaybeError> errors = analyser.getPossibleErrorsOf("if(true & false) {}");
        HashMap<ErrorType, Boolean> hasErrorOfTypeOf = new HashMap<>();
        for (MaybeError error : errors) {
            hasErrorOfTypeOf.put(error.getErrorType(), error.isError());
        }
        Assertions.assertFalse(hasErrorOfTypeOf.get(ErrorType.SEMICOLON_AFTER_IF));
        Assertions.assertTrue(hasErrorOfTypeOf.get(ErrorType.BITWISE_OPERATOR));
    }

    @Test
    public void shouldHaveBothSemicolonAndBitwiseOperatorError() {
        ArrayList<MaybeError> errors = analyser.getPossibleErrorsOf("if(true & false); {}");
        HashMap<ErrorType, Boolean> hasErrorOfTypeOf = new HashMap<>();
        for (MaybeError error : errors) {
            hasErrorOfTypeOf.put(error.getErrorType(), error.isError());
        }
        Assertions.assertTrue(hasErrorOfTypeOf.get(ErrorType.SEMICOLON_AFTER_IF));
        Assertions.assertTrue(hasErrorOfTypeOf.get(ErrorType.BITWISE_OPERATOR));
    }

    @Test
    public void getEffectedLinesFromIfError() {
        String program = "if(true); \n { int a = 5; \n int b = 6;}";
        ArrayList<MaybeError> errors = analyser.getPossibleErrorsOf(program);
        ArrayList<String> body = analyser.getBodyFromIfStatment(errors.get(0).getLineNumber(), analyser.getTokens(program));
        Assertions.assertEquals(new ArrayList<>(List.of("int", "a", "=", "5", ";", "\n", "int", "b", "=", "6", ";")), body);
    }

    @Test
    public void getIfStatement() {
        String program = "if(true); \n { int a = 5; \n int b = 6;}";
        ArrayList<Statement> statements = analyser.getStatements(analyser.getTokens(program));
        System.out.println(statements);
    }

}
