import com.intellij.psi.*;

import java.lang.reflect.Method;
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

        if (statement.getThenBranch() instanceof PsiEmptyStatement) {
            this.errors.add(new Error().type(ErrorType.SEMICOLON_AFTER_IF));
        }
        String conditionalText = statement.getCondition().getText();
        if (hasBitwiseOperator(conditionalText, '|') || hasBitwiseOperator(conditionalText, '&')) {
            this.errors.add(new Error().type(ErrorType.BITWISE_OPERATOR));
        }
    }

    @Override
    public void visitBinaryExpression(PsiBinaryExpression expression) {
        super.visitBinaryExpression(expression);
        System.out.println(expression.getText());
        System.out.println(expression.getOperationSign());
        if (expression.getOperationSign().getTokenType().equals(JavaTokenType.EQEQ)) {
            PsiExpression leftExpression = expression.getLOperand();
            PsiExpression rightExpression = expression.getROperand();
            if (leftExpression.getType().equalsToText("String") && rightExpression.getType().equalsToText("String")) {
                errors.add(new Error().type(ErrorType.NOT_USING_EQUALS));
            }
        }
    }

    //TODO: Store variable with type in visitVariable. Lookup variable to get type.
    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        super.visitMethodCallExpression(expression);
        System.out.println(expression.getMethodExpression().getText());
        System.out.println(expression.getMethodExpression().getType());
        Class clazz = String.class;
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            System.out.println(method.getReturnType());
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
