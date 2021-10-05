import java.util.ArrayList;

public class Program {

    private ArrayList<Statement> statements;

    public Program(ArrayList<Statement> statements) {
        this.statements = statements;
    }

    public ArrayList<Statement> getStatements() {
        return statements;
    }

    public void setStatements(ArrayList<Statement> statements) {
        this.statements = statements;
    }

    public void add(Statement statement) {
        statements.add(statement);
    }

    @Override
    public String toString() {
        StringBuilder statementString = new StringBuilder();
        for (Statement statement : statements) {
            statementString.append(statement.toString());
            statementString.append(", ");
        }
        return "Program( "+ statementString.substring(0, statementString.lastIndexOf(",")) +  " )";
    }
}
