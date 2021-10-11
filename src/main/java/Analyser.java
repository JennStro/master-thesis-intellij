import com.intellij.psi.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class Analyser extends JavaRecursiveElementVisitor {

    private ArrayList<Error> errors = new ArrayList<>();
    private HashMap<String, PsiType> context = new HashMap<>();

    public ArrayList<Error> getErrors() {
        return this.errors;
    }

    @Override
    public void visitLocalVariable(PsiLocalVariable variable) {
        super.visitLocalVariable(variable);
        context.put(variable.getName(), variable.getType());
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
        if (expression.getOperationSign().getTokenType().equals(JavaTokenType.EQEQ)) {
            PsiExpression leftExpression = expression.getLOperand();
            PsiExpression rightExpression = expression.getROperand();
            if (leftExpression.getType().equalsToText("String") && rightExpression.getType().equalsToText("String")) {
                errors.add(new Error().type(ErrorType.NOT_USING_EQUALS));
            }
        }
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        super.visitMethodCallExpression(expression);

        if (expression.getMethodExpression().getType() == null && !(expression.getParent() instanceof PsiLocalVariable)) {
            String methodName = expression.getMethodExpression().getText().chars().mapToObj(it -> (char) it)
                    .dropWhile(it -> it != '.').map(Object::toString).collect(Collectors.joining()).substring(1);
            String containingClass = expression.getMethodExpression().getText().chars().mapToObj(it -> (char) it)
                    .takeWhile(it -> it != '.').map(Object::toString).collect(Collectors.joining());

            if (context.containsKey(containingClass) && context.get(containingClass).equalsToText("String")) {
                try {
                    Method method = getStringMethod(methodName);
                    if (!method.getReturnType().getName().equals("Void")) {
                         errors.add(new Error().type(ErrorType.IGNORING_RETURN_VALUE));
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private Method getStringMethod(String methodName) throws NoSuchMethodException {
        Class<String> clazz = String.class;
        return clazz.getMethod(methodName, (Class<?>[]) null);
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
