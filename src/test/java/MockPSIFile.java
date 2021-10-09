import com.intellij.lang.Language;
import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class MockPSIFile implements Runnable{

    private String fileName;
    private String fileText;
    private BasePlatformTestCase testCase;
    private PsiJavaFile file;


    public MockPSIFile(BasePlatformTestCase testCase, String fileName, String fileText) {
        this.testCase = testCase;
        this.fileName = fileName;
        this.fileText = fileText;
    }

    @Override
    public void run() {
        this.file = (PsiJavaFile) testCase.createLightFile(this.fileName, Language.findLanguageByID("JAVA"), this.fileText);
    }

    public PsiJavaFile getFile() {
        return file;
    }
}
