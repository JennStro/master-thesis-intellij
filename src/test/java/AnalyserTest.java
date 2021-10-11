
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.util.lang.JavaVersion;
import org.junit.jupiter.api.*;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

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
                                "String myString = \"Hello\";" +
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
    public void ignoringReturnValueButCallingOnVoidMethod() {
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
                "import java.util.ArrayList;" +
                        "public class Test { " +
                            "public void method() {" +
                                "ArrayList<Integer> ints = new ArrayList();" +
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

}
