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
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
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

                    try {
                        URL url = new URL("https://master-thesis-web-backend-prod.herokuapp.com/analyse");
                        URLConnection con = url.openConnection();
                        HttpURLConnection http = (HttpURLConnection) con;
                        http.setRequestMethod("POST");
                        http.setDoOutput(true);
                        http.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
                        http.connect();

                        try(OutputStream os = http.getOutputStream()) {
                            os.write(file.getInputStream().readAllBytes());
                        }
                        String response = new String(http.getInputStream().readAllBytes());
                        JSONObject obj = new JSONObject(response);

                        Optional<ConsoleView> console = getConsole(e);

                        if (console.isPresent()) {
                            if (obj.get("status").equals("errors")) {
                                String result = "In class " + obj.getString("containingClass");
                                if (obj.getInt("lineNumber") != -1) {
                                    result += ", on line number " + obj.get("lineNumber") + "\n";
                                }
                                result += obj.getString("explanation");
                                if (obj.has("suggestion")) {
                                    result += obj.getString("suggestion");
                                }
                                console.get().print(result, ConsoleViewContentType.NORMAL_OUTPUT);
                            } else {
                                console.get().print("No errors found!", ConsoleViewContentType.NORMAL_OUTPUT);
                            }
                        }



                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                }
            }
        }
    }
}
