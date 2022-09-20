package com.interpreters.merlin;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.interpreters.tools.ConsoleColor;

public class Merlin {

    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";
    
    private static Interpreter interpreter;

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
        interpreter = new Interpreter(path.split("\\.")[0]);
        run(Merlin.interpreter, getSource(path), path);
    }

    public static String getSource(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        return new String(bytes, Charset.defaultCharset());
    }

    private static void runPrompt() throws IOException {
        interpreter = new Interpreter("this");
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(Merlin.interpreter, line, "prompt");
        }
    }

    public static Environment run(Interpreter interpreter, String source, String fileName) {
        Scanner scanner = new Scanner(source, fileName);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        
        if (hadError) {
            if (interpreter.isMain()) return null;
            throw new RuntimeError();
        }

        boolean showWarnings = true; ///interpreter.isMain();
        Resolver resolver = new Resolver(showWarnings);
        interpreter.setDistance(resolver.resolveStatements(statements));

        if (hadError) {
            if (interpreter.isMain()) return null;
            throw new RuntimeError();
        }

        //System.out.println(new Printer().print(expr));
    
        return interpreter.interpreteAll(statements);
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
        System.err.println(ConsoleColor.RED + "Error: ");
        hadError = true;
        report(line, position, lexeme, message, file);
    }

    private static void report(int line, int position, String lexeme, String message, String file) {
        System.err.println("In file: " + file +
            "\n[line " + line + ", position " + position + ", at '" + lexeme + "']: " + message + ConsoleColor.RESET);
        System.out.println("\n");
    }

    public static void warning(Token token, String message) {
        System.out.print(ConsoleColor.PURPLE + "Warning: ");
        report(token.line, token.position, token.lexeme, message, token.file);
    }
    

}
