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
import master.thesis.backend.errors.BaseError;
import master.thesis.backend.errors.BugReport;
import master.thesis.backend.analyser.Analyser;
import org.jetbrains.annotations.NotNull;


import java.awt.*;
import java.io.IOException;
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

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            if (projectIsOpen(project)) {
                FileEditorManager manager = FileEditorManager.getInstance(project);
                VirtualFile[] files = manager.getSelectedFiles();

                for (VirtualFile file : files) {
                    String filecontent = "";
                    try {
                        filecontent = new String(file.getInputStream().readAllBytes());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    BugReport report = new Analyser().analyse(filecontent);
                    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

                    if (!report.getBugs().isEmpty()) {
                        BaseError error = report.getBugs().get(0);
                        //int lineNumber = PsiDocumentManager.getInstance(project).getDocument(file).getLineNumber(error.getOffset());

                        editor.getMarkupModel().removeAllHighlighters();
                        CaretModel caretModel = editor.getCaretModel();
                        // caretModel.moveToLogicalPosition(new LogicalPosition(lineNumber, 0));
                        ScrollingModel scrollingModel = editor.getScrollingModel();
                        scrollingModel.scrollToCaret(ScrollType.CENTER);
                        editor.getSelectionModel().selectLineAtCaret();
                        editor.getSelectionModel().setSelection(error.getOffset(), error.getOffset() + error.getLength());
                        //editor.getMarkupModel().addLineHighlighter(lineNumber, HighlighterLayer.FIRST, new TextAttributes(null, JBColor.YELLOW.darker(), null, null, Font.BOLD));

                        // From https://stackoverflow.com/questions/51972122/intellij-plugin-development-print-in-console-window
                        ToolWindow toolWindow = ToolWindowManager.getInstance(e.getProject()).getToolWindow("MyPlugin");
                        toolWindow.getContentManager().removeAllContents(true);
                        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(e.getProject()).getConsole();
                        Content content = toolWindow.getContentManager().getFactory().createContent(consoleView.getComponent(), "MyPlugin Output", false);
                        toolWindow.getContentManager().addContent(content);
                        toolWindow.activate(null);
                        consoleView.print(error.getWhat(), ConsoleViewContentType.NORMAL_OUTPUT);
                        if (error.hasSuggestion()) {
                            consoleView.print(error.getSuggestion(), ConsoleViewContentType.NORMAL_OUTPUT);
                        }
                    } else {
                        editor.getMarkupModel().removeAllHighlighters();
                        ToolWindow toolWindow = ToolWindowManager.getInstance(e.getProject()).getToolWindow("MyPlugin");
                        toolWindow.getContentManager().removeAllContents(true);
                        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(e.getProject()).getConsole();
                        Content content = toolWindow.getContentManager().getFactory().createContent(consoleView.getComponent(), "MyPlugin Output", false);
                        toolWindow.getContentManager().addContent(content);
                        toolWindow.activate(null);
                        //consoleView.print(Formatter.infoMessage(master.thesis.formatter.Editor.INTELLIJ), ConsoleViewContentType.NORMAL_OUTPUT);
                        consoleView.print("No errors found!", ConsoleViewContentType.NORMAL_OUTPUT);
                    }
                }
            }
        }
    }
}
