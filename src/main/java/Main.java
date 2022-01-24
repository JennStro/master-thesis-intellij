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
        // and https://plugins.jetbrains.com/docs/intellij/tool-windows.html
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
                        HttpURLConnection http = (HttpURLConnection) url.openConnection();
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
                            if (obj.has("hasException")) {
                                String result = obj.getString("hasException");
                                console.get().print(result, ConsoleViewContentType.NORMAL_OUTPUT);
                            } else {
                                if (obj.get("status").equals("errors")) {
                                    String result = "In class " + obj.getString("containingClass");
                                    if (obj.getInt("lineNumber") != -1) {
                                        result += ", on line number " + obj.get("lineNumber");
                                    }
                                    result += "\n\n" + obj.getString("explanation");
                                    if (obj.has("suggestion")) {
                                        result += "\n \nYou should try \n" + obj.getString("suggestion");
                                    }
                                    if (obj.has("moreInfoLink")) {
                                        result += "\n\nMore info? Check out " + obj.get("moreInfoLink");
                                    }
                                    if (obj.has("tip")) {
                                        result += "\n\n" + obj.get("tip");
                                    }
                                    console.get().print(result, ConsoleViewContentType.NORMAL_OUTPUT);
                                } else {
                                    console.get().print("No errors found!", ConsoleViewContentType.NORMAL_OUTPUT);
                                }
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
