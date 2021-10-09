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
import java.util.Objects;

public class Main extends AnAction {

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
                file.accept(new JavaRecursiveElementVisitor() {
                    @Override
                    public void visitLocalVariable(PsiLocalVariable variable) {
                        super.visitLocalVariable(variable);
                        System.out.println("Found a variable at offset " + variable.getTextRange().getStartOffset());
                        System.out.println("Variable: " + variable.getName());
                    }

                    @Override
                    public void visitIfStatement(PsiIfStatement statement) {
                        super.visitIfStatement(statement);

                        System.out.println("If-cond: " + Objects.requireNonNull(statement.getCondition()).getText());
                        System.out.println("If-then: " + statement.getThenBranch());
                        System.out.println("If-else:" + statement.getElseBranch());

                        if (statement.getThenBranch() instanceof PsiEmptyStatement) {
                            System.out.println("Found an empty statement :(");
                            System.out.println(statement.getText());
                        }
                    }
                });
            }
        }
    }

    private void HandleError(Error error, Project project) {
        Messages.showMessageDialog(project, "OPS: found error on line " + error.getLineNumber(), "An error: " + error.getErrorType(), Messages.getInformationIcon());

        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        CaretModel caretModel = editor.getCaretModel();
        caretModel.moveToLogicalPosition(new LogicalPosition(error.getLineNumber(), 0));
        ScrollingModel scrollingModel = editor.getScrollingModel();
        scrollingModel.scrollToCaret(ScrollType.CENTER);
        editor.getSelectionModel().selectLineAtCaret();
        editor.getMarkupModel().addLineHighlighter(error.getLineNumber() , HighlighterLayer.FIRST, new TextAttributes(null, JBColor.YELLOW.darker(), null, null, Font.BOLD));

        for (Integer line : error.getAffectedLines()) {
            editor.getMarkupModel().addLineHighlighter(line , HighlighterLayer.FIRST, new TextAttributes(null, JBColor.YELLOW.darker(), null, null, Font.BOLD));
        }
        Messages.showMessageDialog(project, "Remove lines", "Errors", Messages.getInformationIcon());
        editor.getMarkupModel().removeAllHighlighters();
    }
}
