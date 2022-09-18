package com.interpreters.tools;

import com.interpreters.merlin.Expr;
import com.interpreters.merlin.Expr.BinaryExpr;
import com.interpreters.merlin.Expr.GroupingExpr;
import com.interpreters.merlin.Expr.LiteralExpr;
import com.interpreters.merlin.Expr.UnaryExpr;


public class Printer implements Expr.Visitor<String> {

    public String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitLiteralExpr(LiteralExpr expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(UnaryExpr expr) {
        return "(" + expr.operation.lexeme + " " + print(expr.right) + ")";
    }

    @Override
    public String visitGroupingExpr(GroupingExpr expr) {
        return "(group " + print(expr.expression) + " )";
    }

    @Override
    public String visitBinaryExpr(BinaryExpr expr) {
        return "(" + expr.operation.lexeme + " " + print(expr.left) +" " + print(expr.right) + ")";
    }
    
}
