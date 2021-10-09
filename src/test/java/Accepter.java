import com.intellij.psi.PsiJavaFile;

public class Accepter implements Runnable {

    private final PsiJavaFile file;
    private final Analyser analyser;

    public Accepter(PsiJavaFile file, Analyser analyser) {
        this.file = file;
        this.analyser = analyser;
    }

    @Override
    public void run() {
        file.accept(analyser);
    }

    public Analyser getAnalyser() {
        return this.analyser;
    }
}
