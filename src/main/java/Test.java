import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;

public class Test extends AnAction {

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
                MaybeError maybeError = analyser.hasSemicolonAfterIf(file.getContent());
                if (maybeError.isHasError()) {
                    Messages.showMessageDialog(project, "OPS: found error on line " + maybeError.getLineNumber(), "An error", Messages.getInformationIcon());
                    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                    editor.getMarkupModel().addLineHighlighter(maybeError.getLineNumber() - 1, HighlighterLayer.FIRST, new TextAttributes(null, JBColor.YELLOW, JBColor.RED, EffectType.BOLD_LINE_UNDERSCORE, 1));
                }
            }
        }
    }
}
