import java.util.ArrayList;

public class MaybeError {

    private boolean hasError;
    private int lineNumber;
    private ErrorType errorType;
    private ArrayList<Statement> affectedLinesFromError;

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

    public void setAffectedLines(ArrayList<Statement> affectedLinesFromError) {
        this.affectedLinesFromError = affectedLinesFromError;
    }

    public ArrayList<Statement> getAffectedLines() {
        return affectedLinesFromError;
    }

    public String toString() {
        return "HasError: " + hasError + " Error: " + errorType + " " + " On line: " + lineNumber + " AffectedStatements: " + affectedLinesFromError;
    }
}
