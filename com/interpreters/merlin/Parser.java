package com.interpreters.merlin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.interpreters.merlin.Stmt.FunDeclStmt;

import static com.interpreters.merlin.TokenType.*;

public class Parser {
    static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    private final int max_arguments = 255;

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
        if (match(CLASS)) return classDeclarationStatement();
        if (match(IMPORT)) return importStatement();
        if (check(DEF) && checkNext(IDENTIFIER)) {
            advance();
            return funDeclarationStatement("function");
        }

        return statement();
    }

    private Stmt statement() {
        if (match(LEFT_BRACE)) return blockStatement();
        if (match(IF)) return ifStatement();
        if (match(WHILE)) return whileStatement();
        if (match(FOR)) return forStatement();
        if (match(RETURN)) return returnStatement();

        return expressionStatement();
    }

    private Stmt importStatement() {
        Token keyword = previous();
        Token libname = consume(IDENTIFIER, "Expect module name.");
        Token alias = libname;
        if (match(AS)) {
            alias = consume(IDENTIFIER, "Expect alias.");
        }
        consume(SEMICOLON, "Expect ';' after module name.");
        return new Stmt.ImportStmt(keyword, libname, alias);
    }

    private Stmt classDeclarationStatement() {
        Token name = consume(IDENTIFIER, "Expect class name.");
        Expr.VariableExpr superclass = null;
        if (match(COLON)) {
            superclass = new Expr.VariableExpr(consume(IDENTIFIER, "Expect superclass name."));
        }
        consume(LEFT_BRACE, "Expect '{' before class body.");
        List<FunDeclStmt> methods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            methods.add(funDeclarationStatement("method"));
        }
        consume(RIGHT_BRACE, "Expect '}' after class body.");
        return new Stmt.ClassDeclStmt(name, superclass, methods);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) value = expression();
        consume(SEMICOLON, "Expect ';' after return statement.");
        return new Stmt.RETURNStmt(keyword, value);
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        Stmt initializer = null;
        if (!match(SEMICOLON)) {
            if (match(VAR)) {
                initializer = varDeclarationStatement();
            }
            else initializer = expressionStatement();
        }

        Expr condition = new Expr.LiteralExpr(true);
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after condition.");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after clauses.");

        Stmt body = statement();
        Stmt forStmt =  new Stmt.FORStmt(initializer, condition, increment, body);
        return new Stmt.BlockStmt(Arrays.asList(forStmt));
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Stmt body = statement();
        return new Stmt.WHILEStmt(condition, body);
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.IFStmt(condition, thenBranch, elseBranch);
    }

    private Stmt blockStatement() {
        return new Stmt.BlockStmt(block("block"));
    }

    private List<Stmt> block(String where) {
        List<Stmt> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, "Expect '}' after " + where + ".");
        return statements;
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

    private Stmt.FunDeclStmt funDeclarationStatement(String type) {
        Token name = consume(IDENTIFIER, "Expect " + type + " name.");
        Expr.FunctionExpr description = parseAnonymusFunction();
        return new Stmt.FunDeclStmt(name, description);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.ExpressionStmt(expr);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();
        if (match(EQUAL, PLUS_EQUAL, MINUS_EQUAL, STAR_EQUAL, SLASH_EQUAL, PERCENT_EQUAL)) {
            if (expr instanceof Expr.VariableExpr) {
                Token operator = previous();
                Expr value = assignment();
                
                if (operator.type != EQUAL) value = replaceOperator(operator, expr, value);
                
                return new Expr.AssignExpr((Expr.VariableExpr) expr, value);
            }
            else if (expr instanceof Expr.GetExpr) {
                Token operator = previous();
                Expr value = assignment();

                if (operator.type != EQUAL) value = replaceOperator(operator, expr, value);

                Expr.GetExpr get = (Expr.GetExpr) expr;
                return new Expr.SetExpr(get.object, get.property, value);
            }
            else if (expr instanceof Expr.ListGetExpr) {
                Token operator = previous();
                Expr value = assignment();

                if (operator.type != EQUAL) value = replaceOperator(operator, expr, value);
                
                Expr.ListGetExpr getter = (Expr.ListGetExpr) expr;
                return new Expr.ListSetExpr(getter, value);
            }

            throw error("Can't assign a non-variable type value.");
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();
        while (match(OR)) {
            Token operation = previous();
            Expr right = and();
            expr = new Expr.LogicExpr(expr, operation, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();
        while (match(AND)) {
            Token operation = previous();
            Expr right = equality();
            expr = new Expr.LogicExpr(expr, operation, right);
        }

        return expr;
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
        Expr expr = mod();
        while (match(STAR, SLASH)) {
            Token operation = previous();
            Expr right = mod();
            expr = new Expr.BinaryExpr(expr, operation, right);
        }

        return expr;
    }

    private Expr mod() {
        Expr expr = unary();
        while (match(PERCENT)) {
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
        return call();
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if(match(LEFT_PAREN)) {
                Token paren = previous();
                List<Expr> arguments = parseArguments();
                expr = new Expr.CallExpr(expr, paren, arguments);
            }
            else if (match(DOT)) {
                Token property = consume(IDENTIFIER, "Expect property.");
                expr = new Expr.GetExpr(expr, property);
            }
            else if (match(LEFT_BRACKET)) {
                Token bracket = previous();
                Expr index = expression();
                consume(RIGHT_BRACKET, "Expect ']' after index.");
                expr = new Expr.ListGetExpr(expr, bracket, index);
            }
            else break;
        }

        return expr;
    }

    private Expr primary() {
        if (match(NIL)) return new Expr.LiteralExpr(null);
        if (match(TRUE)) return new Expr.LiteralExpr(true);
        if (match(FALSE)) return new Expr.LiteralExpr(false);
        if (match(STRING, NUMBER)) return new Expr.LiteralExpr(previous().literal);
        if (match(IDENTIFIER)) return new Expr.VariableExpr(previous());
        if (match(DEF)) return parseAnonymusFunction();
        if (match(THIS)) return new Expr.ThisExpr(previous());
        if (match(SUPER)) {
            Token keyword = previous();
            if (match(DOT)) {
                //consume(DOT, "Expect '.' after 'super'.");
                Token property = consume(IDENTIFIER, "Expect property.");
                return new Expr.SuperExpr(keyword, property);
            }
            else {
                consume(LEFT_PAREN, "Expect '.' or '(' after 'super'.");
                List<Expr> arguments = parseArguments();
                return new Expr.SuperCallExpr(keyword, arguments);
            }
        }

        if (match(LEFT_BRACKET)) return parseList();

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.GroupingExpr(expr);
        }

        throw error("Unexpected expression.");
    }


    private Expr.ListExpr parseList() {
        Token bracket = previous();
        List<Expr> elements = new ArrayList<>();
        if (!check(RIGHT_BRACKET)) {
            do {
                elements.add(expression());
            } while(match(COMMA));
        }

        consume(RIGHT_BRACKET, "Expect ']' after list elements.");
        return new Expr.ListExpr(bracket, elements);
    }

    private Expr.FunctionExpr parseAnonymusFunction() {
        Token paren = consume(LEFT_PAREN, "Expect '(' before parameters.");
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while(match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        consume(LEFT_BRACE, "Expect '{' before body.");
        List<Stmt> body = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            body.add(declaration());
        }
        consume(RIGHT_BRACE, "Expect '}' after body.");
        return new Expr.FunctionExpr(paren, parameters, body);
    }

    private List<Expr> parseArguments() {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() > max_arguments) {
                    throw new RuntimeError(peek(), 
                        "The maximum number of arguments must not exceed 255");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after arguments.");
        return arguments;
    }

    private Expr replaceOperator(Token operator, Expr expr, Expr value) {
        
        TokenType type = null;

        switch (operator.type) {
            case PLUS_EQUAL: type = PLUS; break;
            case MINUS_EQUAL: type = MINUS; break;
            case STAR_EQUAL: type = STAR; break;
            case SLASH_EQUAL: type = SLASH; break;
            case PERCENT_EQUAL: type = PERCENT; break;
        }
        Token operation = new Token(
                type, 
                operator.lexeme, 
                operator.literal, 
                operator.line, 
                operator.position,
                operator.file);
            
        return new Expr.BinaryExpr(expr, operation, value);
    }

    private boolean checkNext(TokenType type) {
        if (isAtEnd()) return false;
        ++current;
        boolean result = check(type);
        --current;
        return result;
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

    private boolean check(TokenType type) {
        return peek().type == type;
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
