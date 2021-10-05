import java.util.ArrayList;

public class IfStatement extends Statement {

    private ArrayList<Statement> body;
    private String expression;


    public IfStatement(int lineNumber, String value, ArrayList<Statement> body) {
        super(lineNumber, value);
        this.body = body;
    }

    public ArrayList<Statement> getBody() {
        return body;
    }

    public void setBody(ArrayList<Statement> body) {
        this.body = body;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        StringBuilder statements = new StringBuilder();
        for (Statement statement : body) {
            statements.append(statement.toString());
        }
        return "IfStatement(" + statements.toString() + ")";
    }
}
