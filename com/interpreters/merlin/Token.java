package com.interpreters.merlin;

public class Token {

    public final TokenType type;
    public final String lexeme;
    public final Object literal;
    public final int line;
    public final int position;

    public Token(TokenType type, String lexeme, Object literal, int line, int position) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.position = position;
    }

    @Override
    public String toString() {
        return type + " " + lexeme + " " + (literal != null ? literal : "") 
            + " " + line + " " + position;
    }



}