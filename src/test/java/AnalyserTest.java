
import com.intellij.lang.Language;
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

    Analyser analyser = new Analyser();

    @BeforeAll
    public void setUp() {
        try {
            super.setUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public void simpleIfTest() {
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


}
