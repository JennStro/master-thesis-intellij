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
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

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

    private ArrayList<SelectedFile> getFiles(Project project) {
        ArrayList<SelectedFile> selectedFiles = new ArrayList<>();
        if(projectIsOpen(project)) {
            FileEditorManager manager = FileEditorManager.getInstance(project);
            VirtualFile[] files = manager.getSelectedFiles();
            for (VirtualFile file : files) {
                try {
                    SelectedFile selectedFile = new SelectedFile().withName(file.getName()).withContent(new String(file.contentsToByteArray()));
                    selectedFiles.add(selectedFile);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return selectedFiles;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if(project != null) {
            ArrayList<SelectedFile> files = getFiles(project);
            Analyser analyser = new Analyser();

            for (SelectedFile file : files) {
                System.out.println(file.getName());
                System.out.println(file.getContent());
                System.out.println(analyser.getTokens(file.getContent()));
                ArrayList<Error> errors = analyser.getPossibleErrorsOf(analyser.getStatements(analyser.getTokens(file.getContent())).getStatements());
                ArrayList<Error> errorsWithAffectedLines = analyser.attachAffectedLinesToErrors(errors, analyser.getTokens(file.getContent()));
                for (Error error : errorsWithAffectedLines) {
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
        }
    }
}
