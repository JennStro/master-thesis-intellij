
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
        Assertions.assertEquals(2, analyser.hasSemicolonAfterIf(codeWithError).getLineNumber());
    }

    @Test
    public void shouldFindErrorOnLineThreeNotLastLine() {
        String codeWithError = "\n \n if(true); \n";
        Assertions.assertEquals(2, analyser.hasSemicolonAfterIf(codeWithError).getLineNumber());
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
    public void getIfStatement() {
        String program = "if(true); \n { int a = 5; \n int b = 6;}";
        String expectedString = "IfStatement( Body ( AssignmentStatement(), AssignmentStatement() ))";
        IfStatement ifStatement = (IfStatement) analyser.getStatements(analyser.getTokens(program)).getStatements().get(0);
        Assertions.assertEquals(expectedString, ifStatement.toString());
        Assertions.assertEquals(1, ifStatement.getBody().get(0).getLineNumber());

        String nestedProgram =  "if(true); \n { int a = 5; \n if (false) { \n int b = 6; \n } \n }";
        String expectedNestedString = "IfStatement( Body ( AssignmentStatement(), IfStatement( Body ( AssignmentStatement() )) ))";
        IfStatement nestedIfStatement = (IfStatement) analyser.getStatements(analyser.getTokens(nestedProgram)).getStatements().get(0);
        Assertions.assertEquals(expectedNestedString, nestedIfStatement.toString());
        Assertions.assertEquals(2, nestedIfStatement.getBody().get(1).getLineNumber());
    }

    @Test
    public void getAssignmentStatementAndIfStatement() {
        String program = "int a = 5; \n if(true); \n { int a = 5; \n int b = 6;}";
        String expectedString = "Program( AssignmentStatement(), IfStatement( Body ( AssignmentStatement(), AssignmentStatement() )) )";
        Program statements = analyser.getStatements(analyser.getTokens(program));
        Assertions.assertEquals(expectedString, statements.toString());

        String program2 = "int a = 5; \n if(true); \n { int a = 5; \n int b = 6; \n } \n int x = 5;";
        String expectedString2 = "Program( AssignmentStatement(), IfStatement( Body ( AssignmentStatement(), AssignmentStatement() )), AssignmentStatement() )";
        Program statements2 = analyser.getStatements(analyser.getTokens(program2));
        Assertions.assertEquals(expectedString2, statements2.toString());

        String program3 = "if((true && false) || false); \n { int a = 5; \n int b = 6;}";
        String expectedExpression = "( true && false ) || false";
        Program statements3 = analyser.getStatements(analyser.getTokens(program3));
        IfStatement statement = (IfStatement) statements3.getStatements().get(0);
        Assertions.assertEquals(expectedExpression, statement.getExpressionString());
    }

    @Test
    public void getAffectedLinesFromIfError() {
        String program = "int a = 5; \n if(true); \n { int a = 5; \n int b = 6;}";
        ArrayList<MaybeError> errors = analyser.attachAffectedLinesToErrors(analyser.getPossibleErrorsOf(program), analyser.getTokens(program));
        System.out.println(errors.get(0));
        Assertions.assertEquals(2, errors.get(0).getAffectedLines().get(0));
    }

    @Test
    public void getAssignmentStatement() {
        String program = "int a = 5;";
        AssignmentStatement statement = (AssignmentStatement) analyser.getStatements(analyser.getTokens(program)).getStatements().get(0);
        Assertions.assertEquals("int", statement.getVariableType());
        Assertions.assertEquals("a", statement.getVariableName());
        Assertions.assertEquals("5", statement.getVariableValue());
    }

}
