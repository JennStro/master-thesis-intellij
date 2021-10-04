package Statements;
import java.util.ArrayList;

public class IfStatement implements Node {

    private Expression conditionalExpression;
    private ArrayList<Node> children;

    public IfStatement(ArrayList<Node> children, Expression conditionalExpression) {
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
    public void addChild(Node child) {
        if (this.children != null) {
            this.children.add(child);
        }
    }

    public Expression getExpression() {
        return this.conditionalExpression;
    }
}
