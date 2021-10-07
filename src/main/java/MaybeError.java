import java.util.ArrayList;

//TODO: No longer need for MaybeError, can convert to pure error.
public class MaybeError {

    private boolean hasError;
    private int lineNumber;
    private ErrorType errorType;
    private ArrayList<Statement> affectedStatementsFromError;

    public boolean isError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public MaybeError error(boolean hasError) {
        this.hasError = hasError;
        return this;
    }

    public MaybeError onLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
        return this;
    }

    public MaybeError type(ErrorType errorType) {
        this.errorType = errorType;
        return this;
    }

    public void setAffectedStatements(ArrayList<Statement> affectedStatementsFromError) {
        this.affectedStatementsFromError = affectedStatementsFromError;
    }

    public ArrayList<Statement> getAffectedStatements() {
        return affectedStatementsFromError;
    }

    public String toString() {
        return "HasError: " + hasError + " Error: " + errorType + " " + " On line: " + lineNumber + " AffectedStatements: " + affectedStatementsFromError;
    }
    public ArrayList<Integer> getAffectedLines() {
        ArrayList<Integer> lines = new ArrayList<>();
        for (Statement statement : affectedStatementsFromError) {
            lines.add(statement.getLineNumber());
        }
        return lines;
    }

}
