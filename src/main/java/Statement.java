import javax.lang.model.SourceVersion;
import java.util.ArrayList;

public class Statement {

    protected int lineNumber;
    protected ArrayList<Token> tokens;

    public Statement(int lineNumber, ArrayList<Token> tokens) {
        this.lineNumber = lineNumber;
        this.tokens = tokens;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }

    public void setTokens(ArrayList<Token> tokens) {
        this.tokens = tokens;
    }

    @Override
    public String toString() {
        return "Statement( " + getTokenString() + " )";
    }

    public String getTokenString() {
        StringBuilder builder = new StringBuilder();
        for (Token token : tokens) {
            builder.append(token.toString());
            builder.append(" ");
        }
        return builder.substring(0, builder.lastIndexOf(" "));
    }

    public ArrayList<String> getVariables() {
        ArrayList<String> variables = new ArrayList<>();
        for (Token token : tokens) {
            String tokenString = token.getValue();
            if (tokenString.matches("[A-Za-z0-9]+") && !SourceVersion.isKeyword(tokenString) && !(tokenString.length() == 1 && tokenString.matches("[0-9]"))) {
                variables.add(tokenString);
            }
        }
        return variables;
    }
}
