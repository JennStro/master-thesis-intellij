import java.util.ArrayList;

public class Error {

    private int lineNumber;
    private ErrorType errorType;

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

    public Error onLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
        return this;
    }

    public Error type(ErrorType errorType) {
        this.errorType = errorType;
        return this;
    }

    public String toString() {
        return "HasError: " + errorType + " " + " On line: " + lineNumber;
    }

}
