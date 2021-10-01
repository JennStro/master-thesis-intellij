import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Analyser {

    public ArrayList<String> getTokens(String fileContent) {
        return getTokens(fileContent, new ArrayList<>());
    }

    private ArrayList<String> getTokens(String fileContent, ArrayList<String> previousTokens) {
        if (fileContent.length() == 0) {
            return previousTokens;
        }
        if (fileContent.charAt(0) == '\n') {
            return getTokens(fileContent.substring(1), previousTokens);
        }
        if (fileContent.charAt(0) == ' ') {
            return getTokens(fileContent.substring(1), previousTokens);
        }
        if (fileContent.charAt(0) == ';') {
            previousTokens.add(Character.toString(fileContent.charAt(0)));
            return getTokens(fileContent.substring(1), previousTokens);
        }
        if (fileContent.charAt(0) == '(' || fileContent.charAt(0) == ')') {
            previousTokens.add(Character.toString(fileContent.charAt(0)));
            return getTokens(fileContent.substring(1), previousTokens);
        }
        if (fileContent.charAt(0) == '{' || fileContent.charAt(0) == '}') {
            previousTokens.add(Character.toString(fileContent.charAt(0)));
            return getTokens(fileContent.substring(1), previousTokens);
        }
        if (fileContent.charAt(0) == '[' || fileContent.charAt(0) == ']') {
            previousTokens.add(Character.toString(fileContent.charAt(0)));
            return getTokens(fileContent.substring(1), previousTokens);
        }
        if (fileContent.charAt(0) == '=') {
            if (fileContent.length() >= 2 && fileContent.charAt(1) == '=') {
                StringBuilder comparison = new StringBuilder();
                comparison.append(fileContent.charAt(0));
                comparison.append(fileContent.charAt(1));
                previousTokens.add(comparison.toString());
                return getTokens(fileContent.substring(2), previousTokens);
            }
            previousTokens.add(Character.toString(fileContent.charAt(0)));
            return getTokens(fileContent.substring(1), previousTokens);
        }
        if (fileContent.charAt(0) == '.') {
            previousTokens.add(Character.toString(fileContent.charAt(0)));
            return getTokens(fileContent.substring(1), previousTokens);
        }
        StringBuilder token = new StringBuilder();
        int i =0;
        while(i < fileContent.length() && Character.isAlphabetic(fileContent.charAt(i))) {
            token.append(fileContent.charAt(i));
            i++;
        }
        previousTokens.add(token.toString());
        if(i == fileContent.length()) {
            return previousTokens;
        }
        return getTokens(fileContent.substring(i), previousTokens);
    }

    public boolean hasError(String fileContents) {
        return false;
    }
}
