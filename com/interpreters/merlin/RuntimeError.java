package com.interpreters.merlin;

public class RuntimeError extends RuntimeException {
    final Token token;
    final String message;

    public RuntimeError(Token token, String message) {
        this.token = token;
        this.message = message;
    }


    @Override
    public String getMessage() {
        return "Runtime Error\n[line " + token.line + ", position " + token.position
            + ", at '" + token.lexeme + "']: " + message;
    }
    
}
