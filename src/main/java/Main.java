import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
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
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.*;
import com.intellij.ui.JBColor;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;


import java.awt.*;
import java.util.ArrayList;
import master.thesis.Formatter;
import master.thesis.errors.BitwiseOperator;

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

                if (!this.analyser.getErrors().isEmpty()) {
                    Error error = this.analyser.getErrors().get(0);
                    int lineNumber = PsiDocumentManager.getInstance(project).getDocument(file).getLineNumber(error.getOffset());
                    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                    CaretModel caretModel = editor.getCaretModel();
                    caretModel.moveToLogicalPosition(new LogicalPosition(lineNumber, 0));
                    ScrollingModel scrollingModel = editor.getScrollingModel();
                    scrollingModel.scrollToCaret(ScrollType.CENTER);
                    editor.getSelectionModel().selectLineAtCaret();
                    editor.getMarkupModel().addLineHighlighter(lineNumber, HighlighterLayer.FIRST, new TextAttributes(null, JBColor.YELLOW.darker(), null, null, Font.BOLD));
                    // From https://stackoverflow.com/questions/51972122/intellij-plugin-development-print-in-console-window
                    ToolWindow toolWindow = ToolWindowManager.getInstance(e.getProject()).getToolWindow("MyPlugin");
                    toolWindow.getContentManager().removeAllContents(true);
                    ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(e.getProject()).getConsole();
                    Content content = toolWindow.getContentManager().getFactory().createContent(consoleView.getComponent(), "MyPlugin Output", false);
                    toolWindow.getContentManager().addContent(content);
                    toolWindow.activate(null);
                    consoleView.print(Formatter.exampleTextTemplate(BitwiseOperator.getExample()), ConsoleViewContentType.NORMAL_OUTPUT);
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
