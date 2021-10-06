public class Token {

    private int lineNumber;
    private String value;

    public Token(int lineNumber, String value) {
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

    public String toString(){
        return this.value;
    }

}
