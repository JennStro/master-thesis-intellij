import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;


import java.awt.*;
import java.util.ArrayList;

public class Main extends AnAction {

    private Analyser analyser;

    @Override
    public void update(@NotNull AnActionEvent event) {
        System.out.println("Updated");
        Project project = event.getProject();
        Presentation presentation = event.getPresentation();
        if (!projectIsOpen(project)) {
            presentation.setEnabledAndVisible(false);
            return;
        }
        presentation.setEnabledAndVisible(true);
        this.analyser = new Analyser();
        System.out.println("Enabled");
    }

    private boolean projectIsOpen(Project project) {
        return project != null;
    }

    private ArrayList<PsiJavaFile> getParsedFiles(Project project) {
        ArrayList<PsiJavaFile> parsedFiles = new ArrayList<>();
        if(projectIsOpen(project)) {
            FileEditorManager manager = FileEditorManager.getInstance(project);
            VirtualFile[] files = manager.getSelectedFiles();
            for (VirtualFile file : files) {
                    PsiJavaFile parsedFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(file);
                    parsedFiles.add(parsedFile);
            }
        }
        return parsedFiles;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if(project != null) {
            ArrayList<PsiJavaFile> files = getParsedFiles(project);

            for (PsiJavaFile file : files) {
                file.accept(this.analyser);

                for (Error error : this.analyser.getErrors()) {
                    Messages.showMessageDialog(project, "OPS: found error! " + error.getErrorType(), "", Messages.getInformationIcon());

                    if (error.getOffset() != -1) {
                        int lineNumber = PsiDocumentManager.getInstance(project).getDocument(file).getLineNumber(error.getOffset());
                        Messages.showMessageDialog(project, "OPS: found error! " + error.getErrorType() + " On line " + lineNumber, "", Messages.getInformationIcon());
                    }
                }
            }
        }
    }

    private void HandleError(Error error, Project project) {
        Messages.showMessageDialog(project, "OPS: found error on line " + error.getOffset(), "An error: " + error.getErrorType(), Messages.getInformationIcon());

        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        CaretModel caretModel = editor.getCaretModel();
        caretModel.moveToLogicalPosition(new LogicalPosition(error.getOffset(), 0));
        ScrollingModel scrollingModel = editor.getScrollingModel();
        scrollingModel.scrollToCaret(ScrollType.CENTER);
        editor.getSelectionModel().selectLineAtCaret();
        editor.getMarkupModel().addLineHighlighter(error.getOffset() , HighlighterLayer.FIRST, new TextAttributes(null, JBColor.YELLOW.darker(), null, null, Font.BOLD));

        //for (Integer line : error.getAffectedLines()) {
        //    editor.getMarkupModel().addLineHighlighter(line , HighlighterLayer.FIRST, new TextAttributes(null, JBColor.YELLOW.darker(), null, null, Font.BOLD));
        //}
        Messages.showMessageDialog(project, "Remove lines", "Errors", Messages.getInformationIcon());
        editor.getMarkupModel().removeAllHighlighters();
    }
}
