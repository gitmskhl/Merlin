package com.interpreters.merlin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.interpreters.tools.Printer;

public class Merlin {
    
    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;


    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: merlin [script]");
            System.exit(64);
        }
        if (args.length == 1) runFile(args[0]);
        else runPrompt();
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        Expr expr = parser.parse();
        
        if (hadError) return;

        //System.out.println(new Printer().print(expr));
    
        Interpreter interpreter = new Interpreter();
        interpreter.interprete(expr);
    }

    public static void runtimeError(Token token, String message) {
        hadRuntimeError = true;
        report(token.line, token.position, token.lexeme, message);
    }

    public static void error(Token token, String message) {
        error(token.line, token.position, token.lexeme, message);
    }

    public static void  error(int line, int position, String lexeme, String message) {
        hadError = true;
        report(line, position, lexeme, message);
    }

    private static void report(int line, int position, String lexeme, String message) {
        System.err.println(
            "[line " + line + ", position " + position + ", at '" + lexeme + "']: " + message);
    }

    

}
