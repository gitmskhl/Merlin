package com.interpreters.merlin;


public enum TokenType {
    // single character
    BANG, MINUS, PLUS, STAR, SLASH,
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, SEMICOLON, LESS, GREATER, EQUAL, LEFT_BRACKET, RIGHT_BRACKET,
    COLON, QUESTION,

    // two-character
    BANG_EQUAL, EQUAL_EQUAL, LESS_EQUAL, GREATER_EQUAL, 

    // primary
    NUMBER, STRING, IDENTIFIER,

    // keywords
    TRUE, FALSE, NIL, FOR, WHILE, IF, ELSE, DEF, CLASS, AND, OR,
    RETURN, THIS, SUPER, VAR, PRINT,

    /// END OF FILE
    EOF
}