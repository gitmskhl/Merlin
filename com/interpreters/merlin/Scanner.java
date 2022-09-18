package com.interpreters.merlin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.interpreters.merlin.TokenType.*;

public class Scanner {
    private static final Map<String, TokenType> keywords = new HashMap<>();

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;

    private int line = 1;
    private int position = 0;

    static {
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("def",    DEF);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }


    Scanner(String source) {
        this.source = source;
    }


    public List<Token> scanTokens() {

        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "at the end of the file", null, line, position + 1));

        return tokens;
    }

    private void scanToken() {
        char c = advance();

        switch (c) {
            case '\n':
                ++line;
                position = 0;
                break;
            case ' ':
            case '\t':
            case '\r':
                break;
            
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case '+': addToken(match('=') ? PLUS_EQUAL : PLUS); break;
            case '-': addToken(match('=') ? MINUS_EQUAL : MINUS); break;
            case '*': addToken(match('=') ? STAR_EQUAL : STAR); break;
            case '%': addToken(match('=') ? PERCENT_EQUAL : PERCENT); break;
            case ';': addToken(SEMICOLON); break;
            case '?': addToken(QUESTION); break;
            case ':': addToken(COLON); break;
            case ',': addToken(COMMA); break;
            case '.': 
                if (!isDigit(nextPeek())) addToken(DOT);
                else number(c);
                break;
            
            case '/':
                if (match('/')) comment();
                else addToken(match('=') ? SLASH_EQUAL : SLASH);
                break;
            case '<': addToken((match('=') ? LESS_EQUAL : LESS)); break;
            case '>': addToken((match('=') ? GREATER_EQUAL : GREATER)); break;
            case '=': addToken((match('=') ? EQUAL_EQUAL : EQUAL)); break;
            case '!': addToken((match('=') ? BANG_EQUAL : BANG)); break;

            case '"': string(); break;

            default:
                if (isDigit(c)) number(c);
                else if (isFirstIdentifierCharacter(c)) identifier();
                else error("Unexpected symbol '" + c +"'");
        }
    }

    private void identifier() {
        while (isIdentifierCharacter(peek())) advance();
        String lexeme = source.substring(start, current);
        TokenType type = keywords.get(lexeme);
        if (type == null) type = IDENTIFIER;
        tokens.add(new Token(type, lexeme, null, line, position));
    }

    private void number(char previous) {
        while (isDigit(peek())) advance();
        if (previous != '.' && peek() == '.') {
            advance();
            while (isDigit(peek())) advance();
        }

        String lexeme = source.substring(start, current);
        Double literal = Double.parseDouble(lexeme);
        tokens.add(new Token(NUMBER, lexeme, literal, line, position));
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isFirstIdentifierCharacter(char c) {
        return  c == '_'
                || (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z');
    }

    private boolean isIdentifierCharacter(char c) {
        return isFirstIdentifierCharacter(c) || isDigit(c);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                ++line;
                position = -1;
            }
            advance();
        }
        if (isAtEnd()) error("", "Unterminated string.");
        else {
            advance();
            String lexeme = source.substring(start + 1, current - 1);
            Token token = new Token(STRING, lexeme, lexeme, line, position);
            tokens.add(token);
        }
    }

    private void comment() {
        while (peek() != '\n' && !isAtEnd()) advance();
    }

    /// safe
    private boolean match(char expected) {
        if (peek() == expected) {
            advance();
            return true;
        }

        return false;
    }


    // safe
    private char nextPeek() {
        ++current;
        char next = peek();
        --current;
        return next;
    }

    /// safe
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    /// unsafe
    private char advance() {
        ++position;
        return source.charAt(current++);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void addToken(TokenType type) {
        tokens.add(new Token(type, source.substring(start, current), null, line, position));
    }

    private void error(String lexeme, String message) {
        Merlin.error(line, position, lexeme, message);
    }

    private void error(String message) {
        error(source.substring(start, current), message);
    }

}
