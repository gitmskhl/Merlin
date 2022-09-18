package com.interpreters.merlin;

import java.util.List;

import com.interpreters.merlin.Expr.AssignExpr;
import com.interpreters.merlin.Expr.BinaryExpr;
import com.interpreters.merlin.Expr.GroupingExpr;
import com.interpreters.merlin.Expr.LiteralExpr;
import com.interpreters.merlin.Expr.UnaryExpr;
import com.interpreters.merlin.Expr.VariableExpr;
import com.interpreters.merlin.Stmt.BlockStmt;
import com.interpreters.merlin.Stmt.ExpressionStmt;
import com.interpreters.merlin.Stmt.PrintStmt;
import com.interpreters.merlin.Stmt.VarDeclStmt;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private final Environment global = new Environment(null);
    private Environment environment = global;


    public void interprete(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) 
                interprete(statement);
        }
        catch(RuntimeError error) {
            Merlin.runtimeError(error.token, error.message);
        }
    }

    public void interprete(Stmt stmt) {
        stmt.accept(this);
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

            case LESS: return less(left, right, expr.operation);
            case LESS_EQUAL: return !less(right, left, expr.operation);
            case GREATER_EQUAL: return !less(left, right, expr.operation);
            case GREATER: return less(right, left, expr.operation);
            case EQUAL_EQUAL: return equals(left, right);
            case BANG_EQUAL: return !equals(left, right);
        }
        return null;
    }

    @Override
    public Object visitVariableExpr(VariableExpr expr) {
        return environment.get(expr.name);
    }

    @Override
    public Object visitAssignExpr(AssignExpr expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.object.name, value);
        return value;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean equals(Object left, Object right) {
        if (left == null) return false;
        return left.equals(right);
    }

    private boolean less(Object left, Object right, Token operation) {
        if ((left instanceof Double) && (right instanceof Double)) return (double) left < (double) right;
        if ((left instanceof String) && (right instanceof Double)) 
            return ((String) left).compareTo((String) right) < 0;
        
        throw new RuntimeError(operation, "Operands must be two numbers or two strings.");
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

    @Override
    public Void visitExpressionStmt(ExpressionStmt stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(PrintStmt stmt) {
        System.out.println(stringify(evaluate(stmt.expression)));
        return null;
    }

    @Override
    public Void visitVarDeclStmt(VarDeclStmt stmt) {
        for (int i = 0, end = stmt.names.size(); i < end; ++i) {
            Object value = evaluate(stmt.initializers.get(i));
            environment.define(stmt.names.get(i), value);
        }
        return null;
    }

    @Override
    public Void visitBlockStmt(BlockStmt stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    public void executeBlock(List<Stmt> statements, Environment blockEnvironment) {
        Environment tmp = this.environment;
        this.environment = blockEnvironment;
        try {
            interprete(statements);
        }
        finally {
            this.environment = tmp;
        }
    }
}
