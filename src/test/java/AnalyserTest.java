
import com.intellij.lang.Language;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.JavaPsiTestCase;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.util.lang.JavaVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.Objects;

public class AnalyserTest extends BasePlatformTestCase {

    Analyser analyser = new Analyser();

    @BeforeEach
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
        Language lan = Language.findLanguageByID("JAVA");
        System.out.println(lan.getDisplayName());
        Tester tester = new Tester(this);
        ApplicationManager.getApplication().runReadAction(tester);
        PsiJavaFile file = tester.getFile();
        file.getLanguage();
       // this.createLightFile("test", Language.findLanguageByID("JAVA"), "public static void main(String[] arg) {}");
    }



}
