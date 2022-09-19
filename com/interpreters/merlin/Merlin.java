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
    
    private static Interpreter interpreter = new Interpreter();

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
        run(Merlin.interpreter, new String(bytes, Charset.defaultCharset()), path);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(Merlin.interpreter, line, "prompt");
        }
    }

    private static void run(Interpreter interpreter, String source, String fileName) {
        Scanner scanner = new Scanner(source, fileName);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        
        if (hadError) return;

        Resolver resolver = new Resolver();
        interpreter.setDistance(resolver.resolveStatements(statements));

        if (hadError) return;

        //System.out.println(new Printer().print(expr));
    
        interpreter.interprete(statements);
    }

    public static void runtimeError(Token token, String message) {
        hadRuntimeError = true;
        System.err.println("Runtime Error: ");
        report(token.line, token.position, token.lexeme, message, token.file);
    }

    public static void error(Token token, String message) {
        error(token.line, token.position, token.lexeme, message, token.file);
    }

    public static void  error(int line, int position, String lexeme, String message, String file) {
        System.err.println("Error: ");
        hadError = true;
        report(line, position, lexeme, message, file);
    }

    private static void report(int line, int position, String lexeme, String message, String file) {
        System.err.println("In file: " + file +
            "\n[line " + line + ", position " + position + ", at '" + lexeme + "']: " + message);
    }

    public static void warning(Token token, String message) {
        System.out.print("Warning: ");
        report(token.line, token.position, token.lexeme, message, token.file);
    }
    

}
