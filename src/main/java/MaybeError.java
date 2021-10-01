public class MaybeError {

    private boolean hasError;
    private int lineNumber;

    public boolean isHasError() {
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

    public MaybeError error(boolean hasError) {
        this.hasError = hasError;
        return this;
    }

    public MaybeError onLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
        return this;
    }

}
