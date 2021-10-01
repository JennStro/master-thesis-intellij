import com.jetbrains.rd.util.Maybe;

import java.util.ArrayList;
import java.util.Arrays;

public class Analyser {

    public ArrayList<String> getTokens(String fileContent) {
        return getTokens(fileContent, new ArrayList<>());
    }

    private ArrayList<String> getTokens(String fileContent, ArrayList<String> previousTokens) {
        if (fileContent.length() == 0) {
            return previousTokens;
        }
        if (fileContent.charAt(0) == '\n') {
            previousTokens.add("\n");
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
        if (fileContent.charAt(0) == '&') {
            if (fileContent.length() >= 2 && fileContent.charAt(1) == '&') {
                StringBuilder comparison = new StringBuilder();
                comparison.append(fileContent.charAt(0));
                comparison.append(fileContent.charAt(1));
                previousTokens.add(comparison.toString());
                return getTokens(fileContent.substring(2), previousTokens);
            }
            previousTokens.add(Character.toString(fileContent.charAt(0)));
            return getTokens(fileContent.substring(1), previousTokens);
        }
        if (fileContent.charAt(0) == '|') {
            if (fileContent.length() >= 2 && fileContent.charAt(1) == '|') {
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
        if (Character.isAlphabetic(fileContent.charAt(0))) {
            StringBuilder token = new StringBuilder();
            int i = 0;
            while (i < fileContent.length() && Character.isAlphabetic(fileContent.charAt(i))) {
                token.append(fileContent.charAt(i));
                i++;
            }
            previousTokens.add(token.toString());
            if(i == fileContent.length()) {
                return previousTokens;
            }
            return getTokens(fileContent.substring(i), previousTokens);
        } else {
            return getTokens(fileContent.substring(1), previousTokens);
        }
    }

    public MaybeError hasSemicolonAfterIf(String fileContents) {
        ArrayList<String> tokens = getTokens(fileContents);
        String[] dangerPattern = new String[]{"if", "(", ")", ";"};
        String[] actualPattern = new String[4];
        int matchedToken = 0;
        int lineNumber = 1;
        for (String token : tokens) {
            if (token.equals("if")) {
                actualPattern[matchedToken] = token;
                matchedToken += 1;
            }
            if (token.equals("(") && matchedToken == 1) {
                actualPattern[matchedToken] = token;
                matchedToken += 1;
            }
            if (token.equals(")") && matchedToken == 2) {
                actualPattern[matchedToken] = token;
                matchedToken += 1;
            }
            if (token.equals(";") && matchedToken == 3) {
                actualPattern[matchedToken] = token;
                matchedToken += 1;
            }
            boolean hasError = Arrays.equals(dangerPattern, actualPattern);
            if (hasError) {
                return new MaybeError().error(Arrays.equals(dangerPattern, actualPattern)).onLineNumber(lineNumber);
            }
            if (token.equals("\n")) {
                lineNumber += 1;
            }
        }
        return new MaybeError().error(Arrays.equals(dangerPattern, actualPattern)).onLineNumber(lineNumber);
    }
}
