package com.interpreters.tools;

import java.io.IOError;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class Generate {

    private static String commonSeparator = "\t";
    private static String insiderSeparator = commonSeparator + "\t";
    private static String doubleInsiderSeparator = insiderSeparator + "\t";

    private static final String pack = "com.interpreters.merlin";
    
    

    public static void main(String[] args) throws IOException{
        if (args.length != 1) {
            System.err.println("Usage: Generate <output_directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        
        defineAST(outputDir, "Expr",  Arrays.asList(
            "Literal    : Object value",
            "Unary      : Token operation, Expr right",
            "Grouping   : Expr expression",
            "Binary     : Expr left, Token operation, Expr right",
            "Variable   : Token name",
            "Assign     : Expr.VariableExpr object, Expr value",
            "Logic      : Expr left, Token operation, Expr right"
        ));
        
        defineAST(outputDir, "Stmt",  Arrays.asList(
            "Expression : Expr expression",
            "Print      : Expr expression",
            "Block      : List<Stmt> statements",
            "IF         : Expr condition, Stmt thenBranch, Stmt elseBranch",

            "VarDecl    : List<Token> names, List<Expr> initializers"
        ));
    }

    private static void defineAST(String outputDir, String baseName, List<String> types) throws IOException {
        defineAST(outputDir, baseName, types, pack);
    }

    private static void defineAST(
        String outputDir, String baseName, List<String> types, String pack)
        throws IOException {
            String path = outputDir + "/" + baseName + ".java";
            PrintWriter writer = new PrintWriter(path, "UTF-8");
            writer.println("package " + pack + ";");
            /// libraries
            libraries(writer, Arrays.asList(
                "java.util.List"
            ));
            /// 
            writer.println("public abstract class " + baseName + " {");
            writer.println("\n");
            defineVisitor(writer, baseName, types);
            writer.println("");
            for (String type : types) {
                String className = type.split(":")[0].trim() + baseName;
                String fields = type.split(":")[1].trim();
                defineType(writer, baseName, className, fields);
                writer.println("\n\n");
            }
            writer.println("");
            writer.println(commonSeparator + "public abstract <R> R accept(Visitor<R> visitor);");
            writer.println("");
            writer.println("}");

            writer.close();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fields) {
        writer.println(commonSeparator + "public static class " + className + " extends " + baseName + "{");
        writer.println(insiderSeparator + "public " + className + "(" + fields + ") {");
        for (String field : fields.split(",")) {
            String var = field.trim().split(" ")[1];
            writer.println(doubleInsiderSeparator + "this." + var + " = " + var + ";");
        }
        writer.println(insiderSeparator + "}");

        writer.println("");

        writer.println(insiderSeparator + "@Override");
        writer.println(insiderSeparator + "public <R> R accept(Visitor<R> visitor) {");
        writer.println(doubleInsiderSeparator + "return visitor.visit" + className + "(this);");
        writer.println(insiderSeparator + "}");

        for (String field : fields.split(",")) {
            writer.println(insiderSeparator + "public final " + field + ";");
        }

        writer.println(commonSeparator + "}");
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println(commonSeparator + "public interface Visitor<R> {");
        for (String type : types) {
            String typeName = type.split(":")[0].trim() + baseName;
            writer.println(
                insiderSeparator + "R visit" + typeName + 
                "(" + typeName + " " + baseName.toLowerCase() + ");");
        }
        writer.println(commonSeparator + "}");
        writer.println();
    }

    private static void libraries(PrintWriter writer, List<String> libs) {
        writer.println();
        for (String lib : libs) {
            writer.println("import " + lib + ";");
        }
        writer.println("\n");
    }
}