package com.interpreters.merlin;

import com.interpreters.merlin.Expr.BinaryExpr;
import com.interpreters.merlin.Expr.GroupingExpr;
import com.interpreters.merlin.Expr.LiteralExpr;
import com.interpreters.merlin.Expr.UnaryExpr;

public class Interpreter implements Expr.Visitor<Object> {

    public Object interprete(Expr expr) {
        try {
            Object result = evaluate(expr);
            System.out.println(stringify(result));
            return result;
        }
        catch (RuntimeError error) {
            Merlin.runtimeError(error.token, error.message);
            return null;
        }
    }

    @Override
    public Object visitLiteralExpr(LiteralExpr expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr expr) {
        Object right = evaluate(expr.right);
        switch (expr.operation.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                if (right instanceof Double) return -(double) right;
                throw new RuntimeError(expr.operation, "Operand must be number.");
        }

        /// unreachable
        return null;
    }

    @Override
    public Object visitGroupingExpr(GroupingExpr expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr expr) {
        Object  left = evaluate(expr.left), 
                right = evaluate(expr.right);
        
        switch (expr.operation.type) {
            case PLUS:
                return sum(left, right, expr.operation);
            case STAR:
                if ((left instanceof Double) && (right instanceof Double)) {
                    return (double) left * (double) right;
                }
                throw new RuntimeError(expr.operation, "Operands must be two numbers.");
            case SLASH:
                if ((left instanceof Double) && (right instanceof Double)) {
                    if ((double) right == 0) 
                        throw new RuntimeError(expr.operation, "Divison by zero.");
                    
                    return (double) left / (double) right;
                }
                throw new RuntimeError(expr.operation, "Operands must be two numbers.");
            case MINUS:
                if ((left instanceof Double) && (right instanceof Double)) {
                    return (double) left - (double) right;
                }
                throw new RuntimeError(expr.operation, "Operands must be two numbers.");
        }
        return null;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) text = text.substring(0, text.length() - 2);
            return text;
        }
        return object.toString();
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        if (object instanceof Double) return (double) object != 0;
        if (object instanceof String) return !((String) object).isEmpty();
        return true;
    }

    private Object sum(Object left, Object right, Token operation) {
        if ((left instanceof Double) && (right instanceof Double)) {
            return (double) left + (double) right;
        }

        if (left instanceof String) {
            return (String) left + stringify(right);
        }
        else if (right instanceof String) {
            return stringify(left) + (String) right;
        }
        throw new RuntimeError(operation, "TypeError: Operands must be numbers or strings.");
    }
    
}
