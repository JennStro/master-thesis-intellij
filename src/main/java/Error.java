public class Error {

    private int offset = -1;
    private ErrorType errorType;
    private String codeThatHasCausedTheError;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public Error onOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public Error type(ErrorType errorType) {
        this.errorType = errorType;
        return this;
    }

    public Error causedBy(String code) {
        this.codeThatHasCausedTheError = code;
        return this;
    }

    public String toString() {
        return "HasError: " + errorType + " " + " On line: " + offset;
    }

    public String getCodeThatCausedTheError() {
        return this.codeThatHasCausedTheError;
    }

}
