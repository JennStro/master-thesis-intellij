package Statements;
import java.util.ArrayList;

public class IfStatement implements Node {

    private String conditionalExpression;
    private ArrayList<Node> children;

    public IfStatement(ArrayList<Node> children, String conditionalExpression) {
        this.children = children;
        this.conditionalExpression = conditionalExpression;
    }

    @Override
    public ArrayList<Node> getChildren() {
        return this.children;
    }

    @Override
    public void setChildren(ArrayList<Node> children) {
        this.children = children;
    }

    @Override
    public String getExpression() {
        return this.conditionalExpression;
    }

    @Override
    public String toString() {
        StringBuilder statements = new StringBuilder();
        for (Node child : children) {

        }
        return "IfStatement{" +
                "expression='" + conditionalExpression + '\'' +
                '}';
    }
}
