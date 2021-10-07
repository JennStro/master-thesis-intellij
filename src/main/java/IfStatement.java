import javax.lang.model.SourceVersion;
import java.util.ArrayList;

public class IfStatement extends Statement {

    private ArrayList<Statement> body;
    private ArrayList<Token> expression;

    public IfStatement and = this;

    public IfStatement(int lineNumber, ArrayList<Token> tokens) {
        super(lineNumber, tokens);
    }

    public IfStatement withConditionalExpression(ArrayList<Token> expression) {
        this.expression = expression;
        return this;
    }

    public IfStatement withBody(ArrayList<Statement> body) {
        this.body = body;
        return this;
    }

    public ArrayList<Statement> getBody() {
        return this.body;
    }

    public void setBody(ArrayList<Statement> body) {
        this.body = body;
    }

    public ArrayList<Token> getConditionalExpressionTokens() {
        return this.expression;
    }

    @Override
    public String toString() {
        if (this.body.isEmpty()) {
            return "IfStatement( Body ())";
        }
        StringBuilder statements = new StringBuilder();
        for (Statement statement : this.body) {
            statements.append(statement.toString());
            statements.append(", ");
        }
        return "IfStatement( Body ( " + statements.substring(0, statements.lastIndexOf(",")) + " ))";
    }

    public String getExpressionString() {
        StringBuilder builder = new StringBuilder();
        for (Token token : this.expression) {
            builder.append(token.toString());
            builder.append(" ");
        }
        return builder.substring(0, builder.lastIndexOf(" "));
    }

    public ArrayList<String> getVariablesFromConditionalExpression() {
        ArrayList<String> variables = new ArrayList<>();
        for (Token token : this.expression) {
            String tokenString = token.getValue();
            if (tokenString.matches("[A-Za-z0-9]+") && !SourceVersion.isKeyword(tokenString) && !(tokenString.length() == 1 && tokenString.matches("[0-9]"))) {
                variables.add(tokenString);
            }
        }
        return variables;
    }

}
