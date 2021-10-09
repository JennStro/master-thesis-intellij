import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiEmptyStatement;
import com.intellij.psi.PsiIfStatement;
import com.intellij.psi.PsiLocalVariable;

import java.util.*;

public class Analyser extends JavaRecursiveElementVisitor {

    private ArrayList<Error> errors = new ArrayList<>();

    public ArrayList<Error> getErrors() {
        return this.errors;
    }

    @Override
    public void visitLocalVariable(PsiLocalVariable variable) {
        super.visitLocalVariable(variable);
        System.out.println("Found a variable at offset " + variable.getTextRange().getStartOffset());
        System.out.println("Variable: " + variable.getName());
    }

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
        super.visitIfStatement(statement);

        System.out.println("If-cond: " + Objects.requireNonNull(statement.getCondition()).getText());
        System.out.println("If-then: " + statement.getThenBranch());
        System.out.println("If-else:" + statement.getElseBranch());

        if (statement.getThenBranch() instanceof PsiEmptyStatement) {
            System.out.println("Found an empty statement :(");
            System.out.println(statement.getText());
            this.errors.add(new Error().type(ErrorType.SEMICOLON_AFTER_IF));
        }
        String conditionalText = statement.getCondition().getText();
        if (hasBitwiseOperator(conditionalText, '|') || hasBitwiseOperator(conditionalText, '&')) {
            this.errors.add(new Error().type(ErrorType.BITWISE_OPERATOR));
        }
    }

    private boolean hasBitwiseOperator(String text, char bitwiseOperator) {
        int firstOperator = text.indexOf(bitwiseOperator);
        if (textContains(firstOperator)) {
            int secondOperator = text.substring(firstOperator+1).indexOf(bitwiseOperator);
            return !textContains(secondOperator);
        }
        return false;
    }

    private boolean textContains(int operatorIndex) {
        return operatorIndex != -1;
    }

}
