
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.util.lang.JavaVersion;
import org.junit.jupiter.api.*;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.ArrayList;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AnalyserTest extends BasePlatformTestCase {

    Analyser analyser;

    @BeforeAll
    public void setUp() {
        try {
            super.setUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    public void setUpAnalyser() {
        this.analyser = new Analyser();
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new LightProjectDescriptor() {
            @Override
            public Sdk getSdk() {
                return IdeaTestUtil.getMockJdk(JavaVersion.current());
            }
        };
    }

    @Test
    public void simpleTest() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",  "public static void main(String[] arg) {}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
    }

    @Test
    public void shouldGetSemicolonAfterIfErrorTest() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                "public class Test { " +
                            "public void method() {" +
                                "if (true); {}" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        Assertions.assertEquals(ErrorType.SEMICOLON_AFTER_IF, accepter.getAnalyser().getErrors().get(0).getErrorType());
    }

    @Test
    public void shouldNotGetSemicolonAfterIfErrorTest() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                "public class Test { " +
                            "public void method() {" +
                                "if (true) {}" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        Assertions.assertTrue(accepter.getAnalyser().getErrors().isEmpty());
    }

    @Test
    public void bitwiseAndOperatorErrorTest() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                "public class Test { " +
                            "public void method() {" +
                                "if (true & false) {}" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        Assertions.assertEquals(ErrorType.BITWISE_OPERATOR, accepter.getAnalyser().getErrors().get(0).getErrorType());
    }

    @Test
    public void bitwiseOrOperatorErrorTest() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                "public class Test { " +
                            "public void method() {" +
                                "if (true | false) {}" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        Assertions.assertEquals(ErrorType.BITWISE_OPERATOR, accepter.getAnalyser().getErrors().get(0).getErrorType());
    }

    @Test
    public void bitwiseOrOperatorAndSemicolonAfterIfErrorTest() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                "public class Test { " +
                            "public void method() {" +
                                "if (true | false); {}" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        Assertions.assertEquals(2, accepter.getAnalyser().getErrors().size());
        Assertions.assertEquals(ErrorType.SEMICOLON_AFTER_IF, accepter.getAnalyser().getErrors().get(0).getErrorType());
        Assertions.assertEquals(ErrorType.BITWISE_OPERATOR, accepter.getAnalyser().getErrors().get(1).getErrorType());
    }

    @Test
    public void usesCorrectOperatorsTest() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                "public class Test { " +
                            "public void method() {" +
                                "if ((true || false) && true) {}" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        Assertions.assertTrue( accepter.getAnalyser().getErrors().isEmpty());
    }

    @Test
    public void useOfEqualSignInsteadOfEquals() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                "public class Test { " +
                            "public void method() {" +
                                "String myString = \"Hello\";" +
                                "String myString2 = \"Hello\";" +
                                "if (myString == myString2) {}" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        Assertions.assertFalse( accepter.getAnalyser().getErrors().isEmpty());
        Assertions.assertEquals(ErrorType.NOT_USING_EQUALS, accepter.getAnalyser().getErrors().get(0).getErrorType());
    }

    @Test
    public void notIgnoringReturnValue() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                    "public class Test { " +
                            "public void method() {" +
                                "String myString = \"Hello\";" +
                                "String m = myString.toUpperCase();" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        Assertions.assertTrue( accepter.getAnalyser().getErrors().isEmpty());
    }

    @Test
    public void ignoringReturnValue() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                "public class Test { " +
                            "public void method() {" +
                                "java.lang.String myString = \"Hello\";" +
                                "myString.toUpperCase();" +
                             "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        Assertions.assertFalse( accepter.getAnalyser().getErrors().isEmpty());
        Assertions.assertEquals( 1, accepter.getAnalyser().getErrors().size());
        Assertions.assertEquals( ErrorType.IGNORING_RETURN_VALUE, accepter.getAnalyser().getErrors().get(0).getErrorType());
    }

    @Test
    public void ignoringReturnValueButCallingOnOftenUsedAsVoidMethod() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                "public class Test { " +
                            "public void method() {" +
                                "ArrayList<Integer> ints = new ArrayList();" +
                                "ints.add(1);" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        Assertions.assertTrue( accepter.getAnalyser().getErrors().isEmpty());
    }

    @Test
    public void ignoringReturnValueWhenCallingArrayListMethod() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                        "public class Test { " +
                            "public void method() {" +
                                "java.util.ArrayList<Integer> ints = new ArrayList();" +
                                "ints.toString();" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        Assertions.assertFalse( accepter.getAnalyser().getErrors().isEmpty());
        Assertions.assertEquals( 1, accepter.getAnalyser().getErrors().size());
        Assertions.assertEquals( ErrorType.IGNORING_RETURN_VALUE, accepter.getAnalyser().getErrors().get(0).getErrorType());
    }

    @Test
    public void findsErrorOnLineNumberFive() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                "public class Test { " +
                            "public void method() {" +
                                "String myString = \"Hello\";" +
                                "String myString2 = \"Hello\";" +
                                "if (myString == myString2) {}" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        Assertions.assertEquals(99, accepter.getAnalyser().getErrors().get(0).getOffset());

    }

    @Test
    public void findsIfErrorAndIgnoringReturnValueError() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                "public class Test { " +
                            "public void method() {" +
                                "java.lang.String myString = \"Hello\";" +
                                "java.lang.String myString2 = \"Hello\";" +
                                "myString2.toUpperCase();" +
                                "if (myString.charAt(0) == myString2.charAt(0)); {}" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        ArrayList<Error> errors = accepter.getAnalyser().getErrors();
        Assertions.assertEquals(2, errors.size());
        Assertions.assertEquals(ErrorType.IGNORING_RETURN_VALUE, errors.get(0).getErrorType());
        Assertions.assertEquals(ErrorType.SEMICOLON_AFTER_IF, errors.get(1).getErrorType());
    }

    @Test
    public void ignoringReturnWithArgument() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                "public class Test { " +
                            "public void method() {" +
                                "java.lang.String myString = \"Hello\";" +
                                "java.lang.String myString2 = \"Hello\";" +
                                "myString2.concat(myString);" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        ArrayList<Error> errors = accepter.getAnalyser().getErrors();
        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(ErrorType.IGNORING_RETURN_VALUE, errors.get(0).getErrorType());
    }

    @Test
    public void ignoringReturnValueWhenCallingRemoveOnArrayList() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                        "public class Test { " +
                            "public void method() {" +
                                "java.util.ArrayList<Integer> ints = new ArrayList();" +
                                "ints.remove(0);" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        Assertions.assertFalse( accepter.getAnalyser().getErrors().isEmpty());
        Assertions.assertEquals( 1, accepter.getAnalyser().getErrors().size());
        Assertions.assertEquals( ErrorType.IGNORING_RETURN_VALUE, accepter.getAnalyser().getErrors().get(0).getErrorType());
    }

    @Test
    public void notIgnoringReturnValueWhenCallingOnArrayList() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                "import java.util.ArrayList;" +
                        "public class Test { " +
                            "public void method() {" +
                                "ArrayList<Integer> ints = new ArrayList();" +
                                "ints.sort(Comparator.naturalOrder());" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        Assertions.assertTrue( accepter.getAnalyser().getErrors().isEmpty());
    }

    @Test
    public void shouldGetOriginalCodeForSemicolonAfterIfError() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                "public class Test { " +
                            "public void method() {" +
                                "if (true); {}" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        Error error = accepter.getAnalyser().getErrors().get(0);
        Assertions.assertEquals(ErrorType.SEMICOLON_AFTER_IF, error.getErrorType());
        Assertions.assertEquals("if (true);", error.getCodeThatCausedTheError());
    }

    @Test
    public void shouldGetOriginalCodeForBitwiseOperationError() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                "public class Test { " +
                            "public void method() {" +
                                "if (true | false) {" +
                                    "System.out.println(\"Helo\")" +
                                "}" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        Error error = accepter.getAnalyser().getErrors().get(0);
        Assertions.assertEquals(ErrorType.BITWISE_OPERATOR, error.getErrorType());
        Assertions.assertEquals("true | false", error.getCodeThatCausedTheError());
    }

    @Test
    public void callingLocalMethod() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                "import java.util.ArrayList;" +
                        "public class Test { " +
                            "public void method() {" +
                                "ArrayList<Integer> ints = new ArrayList();" +
                            "}" +

                            "public void methodCaller() {" +
                                "method();" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        Assertions.assertTrue( accepter.getAnalyser().getErrors().isEmpty());
    }

    @Test
    public void findsIfErrorWhenUsingInlineStringInIfCondition() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                "public class Test { " +
                            "public void method() {" +
                                "if (\"hei\" == \"hei2\") {}" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        ArrayList<Error> errors = accepter.getAnalyser().getErrors();
        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(ErrorType.NOT_USING_EQUALS, errors.get(0).getErrorType());
    }

    @Test
    public void systemOutPrintShouldNotBeChecked() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                "public class Test { " +
                            "public void method() {" +
                                "System.out.println()" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        ArrayList<Error> errors = accepter.getAnalyser().getErrors();
        Assertions.assertEquals(0, errors.size());
    }

    @Test
    public void shouldGetExplanationForSemicolonIfError() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test",
                "public class Test { " +
                            "public void method() {" +
                                "if (true); {}" +
                            "}" +
                        "}");
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        Error error = accepter.getAnalyser().getErrors().get(0);
        Assertions.assertEquals(ErrorType.SEMICOLON_AFTER_IF, error.getErrorType());
        Assertions.assertEquals("A semicolon should not be after if statement!", error.getExplanation());
    }

    @Test
    public void shouldGetErrorWhenIgnoringReturnValueInsideIfStatement() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test", Programs.IGNORING_RETURN_VALUE_INSIDE_IF_STATEMENT);
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        Error error = accepter.getAnalyser().getErrors().get(0);
        Assertions.assertEquals(ErrorType.IGNORING_RETURN_VALUE, error.getErrorType());
    }

    @Test
    public void shouldGetErrorWhenIgnoringReturnValueInsideIfStatementExampleProgram() {
        MockPSIFile mockPSIFile = new MockPSIFile(this, "test", Programs.EXAMPLE_PROGRAM_SIMPLE_APP);
        ApplicationManager.getApplication().runReadAction(mockPSIFile);
        PsiJavaFile file = mockPSIFile.getFile();
        Assertions.assertEquals("Java", file.getLanguage().getDisplayName());
        Accepter accepter = new Accepter(file, analyser);
        ApplicationManager.getApplication().runReadAction(accepter);
        Error error = accepter.getAnalyser().getErrors().get(0);
        Assertions.assertEquals(ErrorType.IGNORING_RETURN_VALUE, error.getErrorType());
    }
}
