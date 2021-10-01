import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
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
        ArrayList<SelectedFile> files = getFiles(project);

        for (SelectedFile file : files) {
            System.out.println(file.getName());
            System.out.println(file.getContent());
        }
    }
}
