
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.Arrays;
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
        Assertions.assertEquals(ErrorType.SEMICOLON_AFTER_IF, analyser.getPossibleErrorsOf(analyser.getStatements(analyser.getTokens(codeWithError)).getStatements()).get(0).getErrorType());
    }

    @Test
    public void shouldFindSemiColorAfterIfNestedParanthesis() {
        String codeWithError = "if((true && false) || true);";
        Assertions.assertEquals(ErrorType.SEMICOLON_AFTER_IF, analyser.getPossibleErrorsOf(analyser.getStatements(analyser.getTokens(codeWithError)).getStatements()).get(0).getErrorType());
    }

    @Test
    public void shouldNotFindSemicolonErrorIfNone() {
        String codeWithNoError = "if(true){ int b = 5; }";
        Assertions.assertTrue(analyser.getPossibleErrorsOf(analyser.getStatements(analyser.getTokens(codeWithNoError)).getStatements()).isEmpty());
    }

    @Test
    public void shouldNotFindSemicolonAfterIf() {
        String codeWithNoError = "if((true && false) || true)";
        Assertions.assertTrue(analyser.getPossibleErrorsOf(analyser.getStatements(analyser.getTokens(codeWithNoError)).getStatements()).isEmpty());
    }

    @Test
    public void shouldFindErrorOnLineThree() {
        String codeWithError = "\n \n if(true);";
        Error error = analyser.getPossibleErrorsOf(analyser.getStatements(analyser.getTokens(codeWithError)).getStatements()).get(0);
        Assertions.assertEquals(ErrorType.SEMICOLON_AFTER_IF, error.getErrorType());
        Assertions.assertEquals(2, error.getLineNumber());
    }

    @Test
    public void shouldFindErrorOnLineThreeNotLastLine() {
        String codeWithError = "\n \n if(true); \n";
        Error error = analyser.getPossibleErrorsOf(analyser.getStatements(analyser.getTokens(codeWithError)).getStatements()).get(0);
        Assertions.assertEquals(ErrorType.SEMICOLON_AFTER_IF, error.getErrorType());
        Assertions.assertEquals(2, error.getLineNumber());
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
        Error error = analyser.getPossibleErrorsOf(analyser.getStatements(analyser.getTokens(errorString)).getStatements()).get(0);
        Assertions.assertEquals(ErrorType.BITWISE_OPERATOR, error.getErrorType());
    }

    @Test
    public void usesAndOperator() {
        String errorString = "if(true && false)";
        Assertions.assertTrue(analyser.getPossibleErrorsOf(analyser.getStatements(analyser.getTokens(errorString)).getStatements()).isEmpty());
    }

    @Test
    public void notMatchingParanthesisGivesNoError() {
        String errorString = "if((true && false)";
        Assertions.assertTrue(analyser.getPossibleErrorsOf(analyser.getStatements(analyser.getTokens(errorString)).getStatements()).isEmpty());
    }

    @Test
    public void shouldOnlyGetSemicolonError() {
        ArrayList<Error> errors = analyser.getPossibleErrorsOf(analyser.getStatements(analyser.getTokens("if(true && false); {}")).getStatements());
        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(ErrorType.SEMICOLON_AFTER_IF, errors.get(0).getErrorType());
    }

    @Test
    public void shouldOnlyGetBitwiseOperatorError() {
        ArrayList<Error> errors = analyser.getPossibleErrorsOf(analyser.getStatements(analyser.getTokens("if(true & false) {}")).getStatements());
        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(ErrorType.BITWISE_OPERATOR, errors.get(0).getErrorType());
    }

    @Test
    public void shouldHaveBothSemicolonAndBitwiseOperatorError() {
        ArrayList<Error> errors = analyser.getPossibleErrorsOf(analyser.getStatements(analyser.getTokens("if(true & false); {}")).getStatements());
        System.out.println(errors);
        Assertions.assertEquals(2, errors.size());

    }

    @Test
    public void getIfStatement() {
        String program = "if(true); \n { int a = 5; \n int b = 6;}";
        String expectedString = "IfStatement( Body ( Statement( int a = 5 ), Statement( int b = 6 ) ))";
        IfStatement ifStatement = (IfStatement) analyser.getStatements(analyser.getTokens(program)).getStatements().get(0);
        Assertions.assertEquals(expectedString, ifStatement.toString());
        Assertions.assertEquals(1, ifStatement.getBody().get(0).getLineNumber());

        String nestedProgram =  "if(true); \n { int a = 5; \n if (false) { \n int b = 6; \n } \n }";
        String expectedNestedString = "IfStatement( Body ( Statement( int a = 5 ), IfStatement( Body ( Statement( int b = 6 ) )) ))";
        IfStatement nestedIfStatement = (IfStatement) analyser.getStatements(analyser.getTokens(nestedProgram)).getStatements().get(0);
        Assertions.assertEquals(expectedNestedString, nestedIfStatement.toString());
        Assertions.assertEquals(2, nestedIfStatement.getBody().get(1).getLineNumber());
    }

    @Test
    public void getAssignmentStatementAndIfStatement() {
        String program = "int a = 5; \n if(true); \n { int a = 5; \n int b = 6;}";
        String expectedString = "Program( Statement( int a = 5 ), IfStatement( Body ( Statement( int a = 5 ), Statement( int b = 6 ) )) )";
        Program statements = analyser.getStatements(analyser.getTokens(program));
        Assertions.assertEquals(expectedString, statements.toString());

        String program2 = "int a = 5; \n if(true); \n { int a = 5; \n int b = 6; \n } \n int x = 5;";
        String expectedString2 = "Program( Statement( int a = 5 ), IfStatement( Body ( Statement( int a = 5 ), Statement( int b = 6 ) )), Statement( int x = 5 ) )";
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
        String program = "int a = 2; \n if(true); \n { a = 5; \n int b = 6;}";
        ArrayList<Error> errors = analyser.attachAffectedLinesToErrors(analyser.getPossibleErrorsOf(analyser.getStatements(analyser.getTokens(program)).getStatements()), analyser.getTokens(program));
        System.out.println(errors.get(0));
        Assertions.assertEquals(2, errors.get(0).getAffectedLines().size());
        Assertions.assertEquals(2, errors.get(0).getAffectedLines().get(0));
        Assertions.assertEquals(3, errors.get(0).getAffectedLines().get(1));

        String program2 = "int a = 2; \n if(true); \n { a = 5; \n int b = 6;} assert a == 5;";
        ArrayList<Error> errors2 = analyser.attachAffectedLinesToErrors(analyser.getPossibleErrorsOf(analyser.getStatements(analyser.getTokens(program2)).getStatements()), analyser.getTokens(program2));
        System.out.println(errors2.get(0));
        Assertions.assertEquals(3, errors2.get(0).getAffectedLines().size());
        Assertions.assertEquals(2, errors2.get(0).getAffectedLines().get(0));
        Assertions.assertEquals(3, errors2.get(0).getAffectedLines().get(2));

    }

    @Test
    public void getAssignmentStatement() {
        String program = "int a = 5;";
        Statement statement = analyser.getStatements(analyser.getTokens(program)).getStatements().get(0);
        Assertions.assertTrue(statement.getVariables().contains("a"));

        String program2 = "int a = 5; b = 2;";
        Statement statement2 = analyser.getStatements(analyser.getTokens(program2)).getStatements().get(1);
        Assertions.assertTrue(statement2.getVariables().contains("b"));
    }

    @Test
    public void getStatement() {
        String program = "assert a == b;";
        String expected = "assert a == b";
        ArrayList<String> expectedVariables = new ArrayList<>(List.of("a", "b"));

        Program statements = analyser.getStatements(analyser.getTokens(program));
        Statement statement = statements.getStatements().get(0);

        Assertions.assertEquals(expected, statement.getTokenString());
        Assertions.assertEquals(expectedVariables, statement.getVariables());
        Assertions.assertEquals("Program( Statement( assert a == b ) )", statements.toString());
    }

    @Test
    public void threeIfStatementsTest() {
        String program = "if (true); {} if (false) {} if (true && false)";
        Program statements = analyser.getStatements(analyser.getTokens(program));
        System.out.println(statements);
        System.out.println(statements.getStatements().get(0).getTokenString());
        ArrayList<Error> errors = analyser.getPossibleErrorsOf(statements.getStatements());
        Assertions.assertEquals(1, errors.size());
    }

}
