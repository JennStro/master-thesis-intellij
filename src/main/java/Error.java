public class Error {

    private int offset = -1;
    private ErrorType errorType;

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

    public String toString() {
        return "HasError: " + errorType + " " + " On line: " + offset;
    }

}
