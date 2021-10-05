import java.util.*;

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
        }
        if (Character.isDigit(fileContent.charAt(0))) {
            StringBuilder token = new StringBuilder();
            int i = 0;
            while (i < fileContent.length() && Character.isDigit(fileContent.charAt(i))) {
                token.append(fileContent.charAt(i));
                i++;
            }
            previousTokens.add(token.toString());
            if(i == fileContent.length()) {
                return previousTokens;
            }
            return getTokens(fileContent.substring(i), previousTokens);
        }
        return getTokens(fileContent.substring(1), previousTokens);

    }

    public MaybeError hasSemicolonAfterIf(String fileContents) {
        ArrayList<String> tokens = getTokens(fileContents);
        String[] dangerPattern = new String[]{"if", "(", ")", ";"};
        String[] actualPattern = new String[4];
        Stack<String> paranthesis = new Stack<>();
        int matchedToken = 0;
        int lineNumber = 1;
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            String predecessor = "";
            if (i > 0) {
                predecessor = tokens.get(i-1);
            }
            if (token.equals("if")) {
                actualPattern[matchedToken] = token;
                matchedToken += 1;
            }
            if (token.equals("(") && matchedToken == 1) {
                actualPattern[matchedToken] = token;
                matchedToken += 1;
                paranthesis.push(token);
            }
            if (token.equals("(") && matchedToken > 1) {
                paranthesis.push(token);
            }
            if (token.equals(")") && matchedToken == 2) {
                actualPattern[matchedToken] = token;
                matchedToken += 1;
                paranthesis.pop();
            }
            if (token.equals(")") && matchedToken > 2) {
                paranthesis.pop();
            }
            if (token.equals(";") && matchedToken == 3 && paranthesis.empty() && predecessor.equals(")")) {
                actualPattern[matchedToken] = token;
                matchedToken += 1;
            }
            boolean hasError = Arrays.equals(dangerPattern, actualPattern);
            if (hasError) {
                return new MaybeError().error(Arrays.equals(dangerPattern, actualPattern)).onLineNumber(lineNumber).type(ErrorType.SEMICOLON_AFTER_IF);
            }
            if (token.equals("\n")) {
                lineNumber += 1;
            }
        }
        return new MaybeError().error(Arrays.equals(dangerPattern, actualPattern)).onLineNumber(lineNumber).type(ErrorType.SEMICOLON_AFTER_IF);
    }

    public MaybeError usesBitwiseOperator(String fileContents) {
        ArrayList<String> tokens = getTokens(fileContents);
        String[] dangerPattern = new String[]{"if", "(", "&", ")"};
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
            if (token.equals("&") && matchedToken == 2) {
                actualPattern[matchedToken] = token;
                matchedToken += 1;
            }
            if (token.equals(")") && matchedToken == 3) {
                actualPattern[matchedToken] = token;
                matchedToken += 1;
            }
            boolean hasError = Arrays.equals(dangerPattern, actualPattern);
            if (hasError) {
                return new MaybeError().error(Arrays.equals(dangerPattern, actualPattern)).onLineNumber(lineNumber).type(ErrorType.BITWISE_OPERATOR);
            }
            if (token.equals("\n")) {
                lineNumber += 1;
            }
        }
        return new MaybeError().error(Arrays.equals(dangerPattern, actualPattern)).onLineNumber(lineNumber).type(ErrorType.BITWISE_OPERATOR);
    }

    public ArrayList<MaybeError> getPossibleErrorsOf(String fileContents) {
        ArrayList<MaybeError> errors = new ArrayList<>();
        errors.add(hasSemicolonAfterIf(fileContents));
        errors.add(usesBitwiseOperator(fileContents));
        return errors;
    }

    public HashMap<String, Integer> getAffectedLinesFromError(MaybeError error, ArrayList<String> tokens) {
        if (error.isError()) {
            if (error.getErrorType().equals(ErrorType.SEMICOLON_AFTER_IF)) {
                ArrayList<String> body = getBodyFromIfStatment(error.getLineNumber(), tokens);
            }
        }
        return new HashMap<>();
    }

    public ArrayList<String> getBodyFromIfStatment(int lineOfError, ArrayList<String> tokens) {
        int i = 0;
        int lineNumber = 0;
        ArrayList<String> body = new ArrayList<>();
        while (i < tokens.size() && !tokens.get(i).equals("}")) {
            if (tokens.get(i).equals("\n")) {
                lineNumber += 1;
                if (lineNumber == lineOfError) {
                    body = new ArrayList<>(tokens.subList(i+2, tokens.indexOf("}")));
                }
            }
            i += 1;
        }
        return body;
    }

    public ArrayList<Statement> getStatements(ArrayList<String> tokens) {
        return getStatements(tokens, new ArrayList<>(), 0, new ArrayList<>());
    }

    private ArrayList<Statement> getStatements(ArrayList<String> tokens, ArrayList<Statement> statements, int lineNumber, ArrayList<String> seenTokens) {
        if (tokens.isEmpty()) {
            return statements;
        }
        String token = tokens.get(0);
        seenTokens.add(token);

        if (token.equals("\n")) {
            ArrayList<String> rest = new ArrayList<>(tokens.subList(1, tokens.size()));
            return getStatements(rest, statements, lineNumber+1, seenTokens);
        }
        if (token.equals("if")) {
            ArrayList<String> rest = new ArrayList<>(tokens.subList(tokens.indexOf("}"), tokens.size()));
            ArrayList<String> body = new ArrayList<>(tokens.subList(tokens.indexOf("{"), tokens.indexOf("}")));
            ArrayList<Statement> bodyStatements = getStatements(body, new ArrayList<>(), lineNumber, seenTokens);
            System.out.println(bodyStatements);
            statements.add(new IfStatement(lineNumber, body.toString(), bodyStatements));
            return statements;
        }
        if (token.equals("=")) {
            ArrayList<String> toNextStatement = new ArrayList<>(tokens.subList(0, tokens.indexOf(";")+1));
            ArrayList<String> rest = new ArrayList<>(tokens.subList(tokens.indexOf(";")+1, tokens.size()));
            statements.add(new Statement(lineNumber, toNextStatement.toString()));
            return getStatements(rest, statements, lineNumber, seenTokens);
        }

        ArrayList<String> rest = new ArrayList<>(tokens.subList(1, tokens.size()));
        return getStatements(rest, statements, lineNumber, seenTokens);
    }

}
