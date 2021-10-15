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
        context.put(variable.getName(), variable.getType());
        super.visitLocalVariable(variable);
    }

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
        if (statement.getThenBranch() instanceof PsiEmptyStatement) {
            int offset = statement.getTextOffset();
            this.errors.add(new Error()
                    .type(ErrorType.SEMICOLON_AFTER_IF)
                    .onOffset(offset)
                    .causedBy(statement.getText())
                    .withExplanation(Explanations.SEMICOLON_AFTER_IF));
        }
        String conditionalText = statement.getCondition().getText();
        if (hasBitwiseOperator(conditionalText, '|') || hasBitwiseOperator(conditionalText, '&')) {
            int offset = statement.getTextOffset();
            this.errors.add(new Error()
                    .type(ErrorType.BITWISE_OPERATOR)
                    .onOffset(offset)
                    .causedBy(statement.getCondition().getText())
                    .withExplanation(Explanations.BITWISE_OPERATOR));
        }
        super.visitIfStatement(statement);
    }

    @Override
    public void visitBinaryExpression(PsiBinaryExpression expression) {
        if (usesDoubleEqualSign(expression)) {
            PsiExpression leftExpression = expression.getLOperand();
            PsiExpression rightExpression = expression.getROperand();
            if (bothExpressionsAreStrings(leftExpression, rightExpression)) {
                int offset = expression.getTextOffset();
                errors.add(new Error()
                        .type(ErrorType.NOT_USING_EQUALS)
                        .onOffset(offset)
                        .causedBy(expression.getText())
                        .withExplanation(Explanations.NOT_USING_EQUALS));
            }
        }
        super.visitBinaryExpression(expression);
    }

    private boolean usesDoubleEqualSign(PsiBinaryExpression expression) {
        return expression.getOperationSign().getTokenType().equals(JavaTokenType.EQEQ);
    }

    private boolean bothExpressionsAreStrings(PsiExpression left, PsiExpression right) {
        if (left != null && right != null && left.getType() != null && right.getType() != null) {
            return left.getType().equalsToText(getTypeOfString(left)) && right.getType().equalsToText(getTypeOfString(right));
        }
        return false;
    }

    private String getTypeOfString(PsiExpression expr) {
        if (expr instanceof PsiLiteralExpression) {
            return "java.lang.String";
        }
        return "String";
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        super.visitMethodCallExpression(expression);

        System.out.println("CALL: " + expression.getText());

        boolean isSystemCall = expression.getMethodExpression().getText().chars().mapToObj(it -> (char) it)
                .takeWhile(it -> it != '(').map(Object::toString).collect(Collectors.joining())
                .equals("System.out.println");
        boolean containsDot = expression.getText().contains(".");

        if (!isSystemCall && containsDot && !parentUses(expression)) {
            String methodName = expression.getMethodExpression().getText().chars().mapToObj(it -> (char) it)
                    .dropWhile(it -> it != '.').map(Object::toString).collect(Collectors.joining()).substring(1);
            String nameOfObjectBeingCalledOn = expression.getMethodExpression().getText().chars().mapToObj(it -> (char) it)
                    .takeWhile(it -> it != '.').map(Object::toString).collect(Collectors.joining());

            System.out.println("Containing class: " + nameOfObjectBeingCalledOn);

            PsiType typeOfObjectBeingCalledOn = this.context.get(nameOfObjectBeingCalledOn);
            System.out.println(typeOfObjectBeingCalledOn);
            System.out.println(fullyQualifiedName);

            String typeOfContainingClass = typeOfObjectBeingCalledOn
                    .getCanonicalText().chars().mapToObj(it -> (char) it)
                    .takeWhile(it -> it != '<')
                    .map(Object::toString).collect(Collectors.joining());;
            System.out.println(typeOfContainingClass);

            try {
                Class<?> containingClass = Class.forName(typeOfContainingClass);

                if (expression.getArgumentList().isEmpty()) {
                    try {
                        Method method = containingClass.getMethod(methodName, (Class<?>[]) null);
                        boolean methodReturnsVoid = method.getReturnType().getName().equals("void");
                        if (!methodReturnsVoid) {
                            int offset = expression.getTextOffset();
                            errors.add(new Error().type(ErrorType.IGNORING_RETURN_VALUE).onOffset(offset));
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                } else {
                    for (Method candidate : containingClass.getDeclaredMethods()) {
                        if (candidate.getName().equals(methodName) && !candidate.getReturnType().getName().equals("void") && !candidate.getReturnType().getName().equals("boolean")) {
                            int offset = expression.getTextOffset();
                            errors.add(new Error()
                                    .type(ErrorType.IGNORING_RETURN_VALUE)
                                    .onOffset(offset)
                                    .causedBy(expression.getText())
                                    .withExplanation(Explanations.IGNORING_RETURN_VALUE));
                        }
                    }
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        }
    }

    private boolean parentUses(PsiMethodCallExpression expression) {
        String parentString = expression.getText() + ";";
        return !parentString.equals(expression.getParent().getText());
    }

    private String fullyQualifiedNameOf(PsiType type) {
        String typeName = type.getCanonicalText();
        System.out.println(typeName);
        String typetypeMName = type.getCanonicalText()
                .chars().mapToObj(it -> (char) it)
                .takeWhile(it -> it != '<')
                .map(Object::toString).collect(Collectors.joining());
        System.out.println(typetypeMName);
        System.out.println(this.fullyQualifiedName);
        return this.fullyQualifiedName.get(
                type.getCanonicalText()
                .chars().mapToObj(it -> (char) it)
                .takeWhile(it -> it != '<')
                .map(Object::toString).collect(Collectors.joining())
            );
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
