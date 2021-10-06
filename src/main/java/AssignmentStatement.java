import java.util.ArrayList;

public class AssignmentStatement extends Statement {

    private String type;
    private String variableName;
    private String value;

    public AssignmentStatement and = this;

    public AssignmentStatement(int lineNumber, ArrayList<Token> tokens) {
        super(lineNumber, tokens);
    }

    public String getVariableType() {
        return type;
    }

    public AssignmentStatement withVariableType(String type) {
        this.type = type;
        return this;
    }

    public String getVariableName() {
        return variableName;
    }

    public AssignmentStatement withVariableName(String variableName) {
        this.variableName = variableName;
        return this;
    }

    public String getVariableValue() {
        return value;
    }

    public AssignmentStatement withVariableValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return "AssignmentStatement( " + getTokenString() + " )";
    }
}
