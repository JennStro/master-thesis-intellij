import com.intellij.psi.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import master.thesis.errors.ErrorType;
import master.thesis.errors.Error;

public class Analyser extends JavaRecursiveElementVisitor {

    private ArrayList<Error> errors = new ArrayList<>();
    private HashMap<String, PsiType> context = new HashMap<>();

    public ArrayList<Error> getErrors() {
        return this.errors;
    }

    @Override
    public void visitLocalVariable(PsiLocalVariable variable) {
        context.put(variable.getName(), variable.getType());
        super.visitLocalVariable(variable);
    }

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
        if (statement.getThenBranch() instanceof PsiEmptyStatement) {
            this.errors.add(new Error(ErrorType.SEMICOLON_AFTER_IF, statement.getTextOffset(), statement.getTextLength()));
        }
        String conditionalText = statement.getCondition().getText();
        if (hasBitwiseOperator(conditionalText, '|') || hasBitwiseOperator(conditionalText, '&')) {
            this.errors.add(new Error(ErrorType.BITWISE_OPERATOR, statement.getTextOffset(), statement.getTextLength()));
        }
        super.visitIfStatement(statement);
    }

    @Override
    public void visitBinaryExpression(PsiBinaryExpression expression) {
        if (usesDoubleEqualSign(expression)) {
            PsiExpression leftExpression = expression.getLOperand();
            PsiExpression rightExpression = expression.getROperand();
            if (!isPrimitive(leftExpression.getType()) || rightExpression != null && !isPrimitive(rightExpression.getType())) {
                errors.add(new Error(ErrorType.NOT_USING_EQUALS, expression.getTextOffset(), expression.getTextLength()));
            }
        }
        super.visitBinaryExpression(expression);
    }

    private boolean isPrimitive(PsiType type) {
        return type != null && (
                type.equalsToText("int") ||
                type.equalsToText("byte") ||
                type.equalsToText("short") ||
                type.equalsToText("boolean") ||
                type.equalsToText("char") ||
                type.equalsToText("float"))
                ;
    }

    private boolean usesDoubleEqualSign(PsiBinaryExpression expression) {
        return expression.getOperationSign().getTokenType().equals(JavaTokenType.EQEQ);
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        super.visitMethodCallExpression(expression);

        boolean isSystemCall = expression.getMethodExpression().getText().chars().mapToObj(it -> (char) it)
                .takeWhile(it -> it != '(').map(Object::toString).collect(Collectors.joining())
                .equals("System.out.println");
        boolean containsDot = expression.getText().contains(".");

        if (!isSystemCall && containsDot && !parentUses(expression)) {
            String methodName = expression.getMethodExpression().getText().chars().mapToObj(it -> (char) it)
                    .dropWhile(it -> it != '.').map(Object::toString).collect(Collectors.joining()).substring(1);
            String nameOfObjectBeingCalledOn = expression.getMethodExpression().getText().chars().mapToObj(it -> (char) it)
                    .takeWhile(it -> it != '.').map(Object::toString).collect(Collectors.joining());

            PsiType typeOfObjectBeingCalledOn = this.context.get(nameOfObjectBeingCalledOn);

            String typeOfContainingClass = typeOfObjectBeingCalledOn
                    .getCanonicalText().chars().mapToObj(it -> (char) it)
                    .takeWhile(it -> it != '<')
                    .map(Object::toString).collect(Collectors.joining());

            try {
                Class<?> containingClass = Class.forName(typeOfContainingClass);

                if (expression.getArgumentList().isEmpty()) {
                    try {
                        Method method = containingClass.getMethod(methodName, (Class<?>[]) null);
                        boolean methodReturnsVoid = method.getReturnType().getName().equals("void");
                        if (!methodReturnsVoid) {
                            errors.add(new Error(ErrorType.IGNORING_RETURN_VALUE, expression.getTextOffset(), expression.getTextLength()));
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                } else {
                    for (Method candidate : containingClass.getDeclaredMethods()) {
                        if (candidate.getName().equals(methodName) && !candidate.getReturnType().getName().equals("void") && !candidate.getReturnType().getName().equals("boolean")) {
                            errors.add(new Error(ErrorType.IGNORING_RETURN_VALUE, expression.getTextOffset(), expression.getTextLength()));
                        }
                    }
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        }
    }

    private PsiType getReturnTypeFrom(PsiMethodCallExpression expression) {
        PsiMethod method = expression.resolveMethod();
        if (method != null) {
            return method.getReturnType();
        }
        return null;
    }

    private boolean parentUses(PsiMethodCallExpression expression) {
        String parentString = expression.getText() + ";";
        return !parentString.equals(expression.getParent().getText());
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
