package Statements;

import java.util.ArrayList;

public interface Node {

    ArrayList<Node> getChildren();

    void setChildren(ArrayList<Node> children);

    String getExpression();

    String toString();
}
