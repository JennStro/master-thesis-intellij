import java.util.*;
import java.util.stream.Collectors;

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
        int lineNumber = 0;
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
        int lineNumber = 0;
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

    public ArrayList<MaybeError> attachAffectedLinesToErrors(ArrayList<MaybeError> errors, ArrayList<String> tokens) {
        ArrayList<MaybeError> errorsWithAffectedLinesAttached = new ArrayList<>();
        for (MaybeError error : errors)
            if (error.isError()) {
                error.setAffectedStatements(getAffectedStatementsFromError(error, tokens));
                errorsWithAffectedLinesAttached.add(error);
            }
        return errorsWithAffectedLinesAttached;
    }

    private ArrayList<Statement> getAllStatementsContainingVariable(String variable, ArrayList<Statement> statements, ArrayList<Statement> foundStatements) {
        if (statements.size() == 0) {
            return foundStatements;
        }
        Statement statement = statements.get(0);
        if (statement instanceof IfStatement) {
            if (((IfStatement) statement).getVariablesFromConditionalExpression().contains(variable)) {
                foundStatements.add(statement);
            }
            foundStatements.addAll(getAllStatementsContainingVariable(variable, ((IfStatement) statement).getBody(), new ArrayList<>()));
            ArrayList<Statement> rest = new ArrayList<>(statements.subList(1, statements.size()));
            return getAllStatementsContainingVariable(variable, rest, foundStatements);
        }
        if (statement.getVariables().contains(variable)) {
            foundStatements.add(statement);
            ArrayList<Statement> rest = new ArrayList<>(statements.subList(1, statements.size()));
            return getAllStatementsContainingVariable(variable, rest, foundStatements);
        }
        ArrayList<Statement> rest = new ArrayList<>(statements.subList(1, statements.size()));
        return getAllStatementsContainingVariable(variable, rest, foundStatements);
    }

    private ArrayList<Statement> getAffectedStatementsFromError(MaybeError error, ArrayList<String> tokens) {
        ArrayList<Statement> statements = getStatements(tokens).getStatements();
        if (error.getErrorType().equals(ErrorType.SEMICOLON_AFTER_IF)) {

            for (Statement statement : statements) {
                if (error.getLineNumber() == statement.getLineNumber() && statement instanceof IfStatement) {

                    ArrayList<Statement> body = ((IfStatement) statement).getBody();

                    HashSet<String> variablesToLookFor = new HashSet<>();
                    for (Statement bodyStatement : body) {
                        variablesToLookFor.addAll(bodyStatement.getVariables());
                    }

                    ArrayList<Statement> statementsWithUseOfEffectedVariables = new ArrayList<>();
                    ArrayList<Statement> statementsAfterIf = statements.stream().filter(st -> st.getLineNumber()>=error.getLineNumber()).collect(Collectors.toCollection(ArrayList::new));
                    for (String variable : variablesToLookFor) {
                        statementsWithUseOfEffectedVariables.addAll(getAllStatementsContainingVariable(variable, statementsAfterIf, new ArrayList<>()));
                    }
                    return statementsWithUseOfEffectedVariables;
                }
            }
            return getStatements(tokens).getStatements();
        }
        return null;
    }

    private ArrayList<Token> getTokenWithLineNumber(ArrayList<String> tokens) {
        int lineNumber = 0;
        ArrayList<Token> tokensWithLineNumber = new ArrayList<>();
        for(String token : tokens) {
            if (token.equals("\n")) {
                lineNumber += 1;
            } else {
                tokensWithLineNumber.add(new Token(lineNumber, token));
            }
        }
        return tokensWithLineNumber;
    }

    public Program getStatements(ArrayList<String> tokens) {
        return getStatements(getTokenWithLineNumber(tokens), new Program(new ArrayList<>()), new ArrayList<>());
    }

    private Program getStatements(ArrayList<Token> tokens, Program statements, ArrayList<Token> seenTokens) {
        if (tokens.isEmpty()) {
            return statements;
        }
        Token token = tokens.get(0);

        if (token.getValue().equals("if")) {
            ArrayList<Token> rest = tokens.stream().dropWhile(t -> !t.getValue().equals("}")).collect(Collectors.toCollection(ArrayList::new));
            if (!rest.isEmpty()) {
                rest.remove(0);
            }

            ArrayList<Token> thisStatement =  tokens.stream().takeWhile(t -> !t.getValue().equals("}")).collect(Collectors.toCollection(ArrayList::new));

            ArrayList<Token> body = tokens.stream().dropWhile(t -> !t.getValue().equals("{")).takeWhile(t -> !t.getValue().equals("}")).collect(Collectors.toCollection(ArrayList::new));
            ArrayList<Token> expression = getConditionalExpression(tokens);

            seenTokens.add(token);
            Program bodyStatements = getStatements(body, new Program(new ArrayList<>()), seenTokens);
            IfStatement statement = new IfStatement(token.getLineNumber(), thisStatement)
                    .withBody(bodyStatements.getStatements()).and.withConditionalExpression(expression);
            statements.add(statement);
            return getStatements(rest, statements, seenTokens);
        }
        if (token.getValue().equals(";")) {
            Collections.reverse(seenTokens);
            ArrayList<Token> thisStatement = seenTokens.stream().takeWhile(t -> !(t.getValue().equals(";")||t.getValue().equals("{"))).collect(Collectors.toCollection(ArrayList::new));
            Collections.reverse(seenTokens);
            Collections.reverse(thisStatement);
            seenTokens.addAll(thisStatement);
            seenTokens.add(token);

            tokens.remove(0);

            statements.add(new Statement(token.getLineNumber(), thisStatement));
            return getStatements(tokens, statements, seenTokens);

        }

        ArrayList<Token> rest = new ArrayList<>(tokens.subList(1, tokens.size()));
        seenTokens.add(token);
        return getStatements(rest, statements, seenTokens);
    }

    private ArrayList<Token> getConditionalExpression(ArrayList<Token> tokens) {
        boolean hasMatchingParanthesis = hasMatchingParanthesis(tokens, "(", ")");

        if (hasMatchingParanthesis) {

            ArrayList<Token> expression = tokens.stream()
                    .takeWhile(t -> !t.getValue().equals("{"))
                    .dropWhile(t -> !t.getValue().equals("("))
                    .collect(Collectors.toCollection(ArrayList::new));
            expression.remove(0);

            Collections.reverse(expression);
            expression = expression.stream()
                    .dropWhile(t -> !t.getValue().equals(")"))
                    .collect(Collectors.toCollection(ArrayList::new));
            expression.remove(0);

            Collections.reverse(expression);

            return expression;
        }
        return new ArrayList<>();
    }

    private boolean hasMatchingParanthesis(ArrayList<Token> tokens, String open, String close) {
        ArrayList<Token> closingParanthesis = tokens.stream()
                .filter(token -> token.getValue().equals(close))
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Token> openParanthesis = tokens.stream()
                .filter(token -> token.getValue().equals(open))
                .collect(Collectors.toCollection(ArrayList::new));
        return closingParanthesis.size() == openParanthesis.size();
    }

}
