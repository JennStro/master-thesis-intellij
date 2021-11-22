import com.intellij.psi.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import master.thesis.errors.*;

public class Analyser extends JavaRecursiveElementVisitor {

    private ArrayList<BaseError> errors = new ArrayList<>();
    private HashMap<String, PsiType> context = new HashMap<>();

    public ArrayList<BaseError> getErrors() {
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
            this.errors.add(new SemiColonAfterIfError(statement.getTextOffset(), statement.getTextLength()));
        }
        String conditionalText = statement.getCondition().getText();
        if (hasBitwiseOperator(conditionalText, '|') || hasBitwiseOperator(conditionalText, '&')) {
            this.errors.add(new BitwiseOperatorError(statement.getTextOffset(), statement.getTextLength()));
        }
        super.visitIfStatement(statement);
    }

    @Override
    public void visitBinaryExpression(PsiBinaryExpression expression) {
        if (usesDoubleEqualSign(expression)) {
            PsiExpression leftExpression = expression.getLOperand();
            PsiExpression rightExpression = expression.getROperand();
            // Tests do not resolve method return type, returns null.
            if (leftExpression.getType() != null && rightExpression != null && rightExpression.getType() != null) {
                if (!isPrimitive(leftExpression.getType()) || !isPrimitive(rightExpression.getType()))
                    errors.add(new EqualsOperatorError(expression.getTextOffset(), expression.getTextLength()));
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

        PsiMethod resolvedMethod = expression.resolveMethod();

        if (!isSystemCall && containsDot && !parentUses(expression)) {
            if(!resolvedMethod.getReturnType().equalsToText("void")) {
                errors.add(new IgnoringReturnError(expression.getTextOffset(), expression.getTextLength()));
            }
        }
        if (resolvedMethod.getModifierList().hasModifierProperty(PsiModifier.STATIC)) {
            if (!resolvedMethod.getContainingClass().getName().equals(expression.getMethodExpression().getQualifierExpression().getText())) {
                errors.add(new StaticAsNormalError(expression.getTextOffset(), expression.getTextLength()));
            }
        }
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
