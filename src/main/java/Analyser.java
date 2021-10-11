import com.intellij.psi.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class Analyser extends JavaRecursiveElementVisitor {

    private ArrayList<Error> errors = new ArrayList<>();
    private HashMap<String, PsiType> context = new HashMap<>();
    private HashMap<String, String> fullyQualifiedName = new HashMap<>(Map.of(
            "ArrayList", "java.util.ArrayList",
            "String", "java.lang.String"
    ));

    public ArrayList<Error> getErrors() {
        return this.errors;
    }

    @Override
    public void visitLocalVariable(PsiLocalVariable variable) {
        super.visitLocalVariable(variable);
        context.put(variable.getName(), variable.getType());
    }

    @Override
    public void visitImportList(PsiImportList list) {
        System.out.println(list.getText());
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
            String containingClassString = expression.getMethodExpression().getText().chars().mapToObj(it -> (char) it)
                    .takeWhile(it -> it != '.').map(Object::toString).collect(Collectors.joining());

            String typeOfContainingClass = fullyQualifiedName.get(context.get(containingClassString).getCanonicalText()
                    .chars().mapToObj(it -> (char) it)
                    .takeWhile(it -> it != '<')
                    .map(Object::toString).collect(Collectors.joining()));

            try {
                Class<?> containingClass = Class.forName(typeOfContainingClass);

                if (expression.getArgumentList().isEmpty()) {
                    try {
                        Method method = containingClass.getMethod(methodName, (Class<?>[]) null);
                        boolean methodReturnsVoid = method.getReturnType().getName().equals("Void");
                        if (!methodReturnsVoid) {
                            errors.add(new Error().type(ErrorType.IGNORING_RETURN_VALUE));
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


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
