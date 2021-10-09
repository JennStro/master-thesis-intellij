import com.intellij.lang.Language;
import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class Tester implements Runnable{

    private BasePlatformTestCase testCase;
    private PsiJavaFile file;

    public Tester(BasePlatformTestCase testCase) {
        this.testCase = testCase;
    }

    @Override
    public void run() {
        this.file = (PsiJavaFile) testCase.createLightFile("test", Language.findLanguageByID("JAVA"), "public static void main(String[] arg) {}");
    }

    public PsiJavaFile getFile() {
        return file;
    }
}
