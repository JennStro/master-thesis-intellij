import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import master.thesis.backend.errors.BaseError;
import master.thesis.backend.errors.BugReport;
import master.thesis.backend.analyser.Analyser;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class Main extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        Presentation presentation = event.getPresentation();
        if (!projectIsOpen(project)) {
            presentation.setEnabledAndVisible(false);
            return;
        }
        presentation.setEnabledAndVisible(true);
    }

    private boolean projectIsOpen(Project project) {
        return project != null;
    }

    private Optional<ConsoleView> getConsole(AnActionEvent e) {
        // From https://stackoverflow.com/questions/51972122/intellij-plugin-development-print-in-console-window
        String consoleName = "Master thesis plugin";
        ToolWindow toolWindow = ToolWindowManager.getInstance(e.getProject()).getToolWindow(consoleName);
        if (toolWindow != null) {
            ContentManager toolWindowContentManager = toolWindow.getContentManager();
            toolWindowContentManager.removeAllContents(true);
            ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(e.getProject()).getConsole();
            Content content = toolWindowContentManager.getFactory().createContent(consoleView.getComponent(), consoleName, false);
            toolWindowContentManager.addContent(content);
            toolWindow.activate(null);
            return Optional.of(consoleView);
        }
        return Optional.empty();
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


                    Optional<ConsoleView> console = getConsole(e);
                    if (console.isPresent()) {
                        if (!report.getBugs().isEmpty()) {
                            BaseError error = report.getBugs().get(0);
                            console.get().print("In file " + file.getName() + ", on line " + error.getLineNumber() + ", in class " + error.getContainingClass() + ": \n" + error.getWhat(), ConsoleViewContentType.NORMAL_OUTPUT);
                            if (error.getSuggestion().isPresent()) {
                                console.get().print("\n"+error.getSuggestion().get(), ConsoleViewContentType.NORMAL_OUTPUT);
                            }
                        } else {
                            console.get().print("No errors found!", ConsoleViewContentType.NORMAL_OUTPUT);
                        }
                    }

                }
            }
        }
    }
}
