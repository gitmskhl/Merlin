package com.interpreters.merlin;

import java.util.ArrayList;
import java.util.List;

import static com.interpreters.merlin.TokenType.*;

public class Parser {
    static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd()) {
            try {
                statements.add(declaration());
            }
            catch (ParseError error) {
                // synchronize
                return null;
            }
        }
        
        return statements;
    }

    private Stmt declaration() {
        if (match(VAR)) return varDeclarationStatement();

        return statement();
    }

    private Stmt statement() {
        if (match(PRINT)) return printStatement();

        return expressionStatement();
    }

    private Stmt varDeclarationStatement() {
        List<Token> names = new ArrayList<>();
        List<Expr> initializers = new ArrayList<>();

        do {
            Token name = consume(IDENTIFIER, "Expect a variable name.");
            Expr initializer = null;
            if (match(EQUAL)) {
                initializer = expression();
            }
            names.add(name);
            initializers.add(initializer);
        } while(match(COMMA));

        consume(SEMICOLON, "Expect ';' after variable declaration.");

        return new Stmt.VarDeclStmt(names, initializers);
    }

    private Stmt printStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.PrintStmt(expr);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.ExpressionStmt(expr);
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operation = previous();
            Expr right = comparison();
            expr = new Expr.BinaryExpr(expr, operation, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)) {
            Token operation = previous();
            Expr right = term();
            expr = new Expr.BinaryExpr(expr, operation, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(PLUS, MINUS)) {
            Token operation = previous();
            Expr right = factor();
            expr = new Expr.BinaryExpr(expr, operation, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(STAR, SLASH)) {
            Token operation = previous();
            Expr right = unary();
            expr = new Expr.BinaryExpr(expr, operation, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operation = previous();
            Expr right = unary();
            return new Expr.UnaryExpr(operation, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(NIL)) return new Expr.LiteralExpr(null);
        if (match(TRUE)) return new Expr.LiteralExpr(true);
        if (match(FALSE)) return new Expr.LiteralExpr(false);
        if (match(STRING, NUMBER)) return new Expr.LiteralExpr(previous().literal);
        if (match(IDENTIFIER)) return new Expr.VariableExpr(previous());

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.GroupingExpr(expr);
        }

        throw error("Unexpected expression.");
    }

    private Token consume(TokenType type, String message) {
        if (match(type)) return previous();
        throw error(message);
    }

    private boolean match(TokenType... types) {
        TokenType currenType = peek().type;
        for (TokenType type : types) {
            if (currenType == type) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token advance() {
        if (isAtEnd()) return peek();
        return tokens.get(current++);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private ParseError error(Token token, String message) {
        Merlin.error(token, message);
        return new ParseError();
    }

    private ParseError error(String message) {
        return error(peek(), message);
    }

}
