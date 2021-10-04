package Statements;

import java.util.ArrayList;

public class Expression implements Node {

    private String expression;

    public Expression(String expression) {
        this.expression = expression;
    }

    @Override
    public ArrayList<Node> getChildren() {
        return null;
    }

    @Override
    public void setChildren(ArrayList<Node> children) {

    }

    @Override
    public void addChild(Node child) {
    }

    public String toString() {
        return this.expression;
    }
}
