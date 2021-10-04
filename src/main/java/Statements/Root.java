package Statements;

import java.util.ArrayList;

public class Root implements Node {

    private ArrayList<Node> children;

    public Root(ArrayList<Node> children) {
        this.children = children;
    }

    public Root() {

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
}
