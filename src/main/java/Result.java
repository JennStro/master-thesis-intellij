import Statements.Node;

import java.util.ArrayList;

public class Result {

    private Node tree;
    private ArrayList<String> restOfTokens;

    public Result(ArrayList<String> restOfTokens, Node tree) {
        this.tree = tree;
        this.restOfTokens = restOfTokens;
    }

    public Node getTree() {
        return tree;
    }

    public void setTree(Node tree) {
        this.tree = tree;
    }

    public ArrayList<String> getRestOfTokens() {
        return restOfTokens;
    }

    public void setRestOfTokens(ArrayList<String> restOfTokens) {
        this.restOfTokens = restOfTokens;
    }
}
