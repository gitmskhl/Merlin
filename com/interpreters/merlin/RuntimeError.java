package com.interpreters.merlin;

public class RuntimeError extends RuntimeException {
    final Token token;
    final String message;
    final boolean dummy;

    public RuntimeError(Token token, String message, boolean dummy) {
        this.token = token;
        this.message = message;
        this.dummy = dummy;
    }

    public RuntimeError(Token token, String message) {
        this(token, message, false);
    }

    public RuntimeError() {
        this(null, null, true);
    }

    @Override
    public String getMessage() {
        return "Runtime Error\n[line " + token.line + ", position " + token.position
            + ", at '" + token.lexeme + "']: " + message;
    }
    
}
