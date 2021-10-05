public class Statement {

    protected int lineNumber;
    protected String value;

    public Statement(int lineNumber, String value) {
        this.lineNumber = lineNumber;
        this.value = value;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Statement(" +
                "lineNumber=" + lineNumber +
                ", value='" + value + '\'' +
                ')';
    }
}
