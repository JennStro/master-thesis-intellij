import java.util.ArrayList;

public class IfStatement extends Statement {

    private ArrayList<Statement> body;

    public IfStatement(int lineNumber, ArrayList<Token> expression, ArrayList<Statement> body) {
        super(lineNumber, expression);
        this.body = body;
    }

    public ArrayList<Statement> getBody() {
        return this.body;
    }

    public void setBody(ArrayList<Statement> body) {
        this.body = body;
    }

    public ArrayList<Token> getExpression() {
        return this.tokens;
    }

    public void setExpression(ArrayList<Token> expression) {
        this.tokens = expression;
    }

    @Override
    public String toString() {
        StringBuilder statements = new StringBuilder();
        for (Statement statement : body) {
            statements.append(statement.toString());
            statements.append(", ");
        }
        return "IfStatement( Body ( " + statements.substring(0, statements.lastIndexOf(",")) + " ))";
    }

    public String getExpressionString() {
        StringBuilder builder = new StringBuilder();
        for (Token token : tokens) {
            builder.append(token.toString());
            builder.append(" ");
        }
        return builder.substring(0, builder.lastIndexOf(" "));
    }

}
