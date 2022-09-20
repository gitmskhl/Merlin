package com.interpreters.merlin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.interpreters.merlin.Expr.AssignExpr;
import com.interpreters.merlin.Expr.BinaryExpr;
import com.interpreters.merlin.Expr.CallExpr;
import com.interpreters.merlin.Expr.FunctionExpr;
import com.interpreters.merlin.Expr.GetExpr;
import com.interpreters.merlin.Expr.GroupingExpr;
import com.interpreters.merlin.Expr.LiteralExpr;
import com.interpreters.merlin.Expr.LogicExpr;
import com.interpreters.merlin.Expr.SetExpr;
import com.interpreters.merlin.Expr.SuperCallExpr;
import com.interpreters.merlin.Expr.SuperExpr;
import com.interpreters.merlin.Expr.ThisExpr;
import com.interpreters.merlin.Expr.UnaryExpr;
import com.interpreters.merlin.Expr.VariableExpr;
import com.interpreters.merlin.Stmt.BlockStmt;
import com.interpreters.merlin.Stmt.ClassDeclStmt;
import com.interpreters.merlin.Stmt.ExpressionStmt;
import com.interpreters.merlin.Stmt.FORStmt;
import com.interpreters.merlin.Stmt.FunDeclStmt;
import com.interpreters.merlin.Stmt.IFStmt;
import com.interpreters.merlin.Stmt.ImportStmt;
import com.interpreters.merlin.Stmt.RETURNStmt;
import com.interpreters.merlin.Stmt.VarDeclStmt;
import com.interpreters.merlin.Stmt.WHILEStmt;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private final boolean showWarnings;

    public Resolver(boolean showWarnings) {
        this.showWarnings = showWarnings;
    }

    public Resolver() {
        this(true);
    }

    class Scope {
        class State {
            public boolean defined = false;
            public boolean used = false;
            public boolean initialized = false;
        }
        private final Map<String, State> variables = new HashMap<>();
        private final Map<String, Token> tokens = new HashMap<>();

        public boolean contains(String name) {
            return variables.containsKey(name);
        }

        public void defineNative(String name) {
            State state = new State();
            state.defined = state.used = state.initialized = true;
            variables.put(name, state);
        }

        public void declare(Token name) {
            if (variables.containsKey(name.lexeme)) {
                Merlin.error(name, "Redefinition of variable.");
            }
            variables.put(name.lexeme, new State());
            tokens.put(name.lexeme, name);
        }

        public void define(String name) {
            variables.get(name).defined = true;
        }

        public boolean isDefined(String name) {
            return variables.get(name).defined;
        }

        public void initialize(String name) {
            variables.get(name).initialized = true;
        }

        public boolean isInitialized(String name) {
            return variables.get(name).initialized;
        }

        public void use(String name) {
            variables.get(name).used = true;
        }

        public List<Token> getUnused() {
            List<Token> result = new ArrayList<>();
            for (Map.Entry<String, State> entry : variables.entrySet()) {
                if (!entry.getValue().used) result.add(tokens.get(entry.getKey()));
            }

            return result;
        }

    }

    enum FunctionType {
        NONE,
        FUNCTION
    }

    enum ClassType {
        NONE,
        CLASS,
        SUBCLASS
    }

    private boolean isConstructor = false;

    /// List -> [defined, used, initialized]
    private final Stack<Scope> scopes = new Stack<>();
    private final Map<Expr, Integer> distances = new HashMap<>();

    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;



    public Map<Expr, Integer> resolveStatements(List<Stmt> statements) {
        beginScope();
        initNative();
        resolve(statements);
        endScope();
        return distances;
    }

    private void initNative() {
        Scope scope = scopes.peek();
        scope.defineNative("print");
        scope.defineNative("println");
    }

    private void resolve(List<Stmt> statements) {
        for (Stmt stmt : statements) resolve(stmt);
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void resolveLocal(Token name, Expr expr, boolean initialize) {
        for (int i = scopes.size() - 1; i >= 0; --i) {
            if (scopes.get(i).contains(name.lexeme)) {
                scopes.get(i).use(name.lexeme);
                if (initialize) {
                    scopes.get(i).initialize(name.lexeme);
                }
                if (!scopes.get(i).isDefined(name.lexeme)) {
                    Merlin.error(name, "Can't read local variable in its own initializer.");
                }

                if (i != 0) {
                    distances.put(expr, scopes.size() - 1 - i);
                }

                if (!scopes.get(i).isInitialized(name.lexeme)) {
                    if (!showWarnings) return;
                    Merlin.warning(name, "Using an uninitialized variable.");
                }
                return;
            }
        }
        Merlin.error(name, "Undefined variable.");
    }

    @Override
    public Void visitExpressionStmt(ExpressionStmt stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitBlockStmt(BlockStmt stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitIFStmt(IFStmt stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitWHILEStmt(WHILEStmt stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitFORStmt(FORStmt stmt) {
        if (stmt.initializer != null) resolve(stmt.initializer);
        resolve(stmt.condition);
        if (stmt.increment != null) resolve(stmt.increment);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitRETURNStmt(RETURNStmt stmt) {
        if (currentFunction == FunctionType.NONE) {
            Merlin.error(stmt.keyword, "Can't return from top-level code.");
        }
        if (isConstructor && stmt.value != null) {
            Merlin.error(stmt.keyword, "Can't return a value in the constructor.");
        }
        if (stmt.value != null) resolve(stmt.value);
        return null;
    }

    @Override
    public Void visitVarDeclStmt(VarDeclStmt stmt) {
        for (int i = 0; i < stmt.names.size(); ++i) {
            declare(stmt.names.get(i));
            if (stmt.initializers.get(i) != null) {
                initialize(stmt.names.get(i).lexeme);
                resolve(stmt.initializers.get(i));
            }
            define(stmt.names.get(i).lexeme);
        }

        return null;
    }

    @Override
    public Void visitFunDeclStmt(FunDeclStmt stmt) {
        declare(stmt.name);
        initialize(stmt.name.lexeme);
        resolve(stmt.description);
        define(stmt.name.lexeme);
        return null;
    }

    @Override
    public Void visitLiteralExpr(LiteralExpr expr) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(UnaryExpr expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitGroupingExpr(GroupingExpr expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitBinaryExpr(BinaryExpr expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(VariableExpr expr) {
        resolveLocal(expr.name, expr, false);
        return null;
    }

    @Override
    public Void visitAssignExpr(AssignExpr expr) {
        resolveLocal(expr.object.name, expr.object, true);
        resolve(expr.value);
        return null;
    }

    @Override
    public Void visitLogicExpr(LogicExpr expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(CallExpr expr) {
        resolve(expr.callee);
        for (Expr arg : expr.arguments) resolve(arg);
        return null;
    }

    @Override
    public Void visitGetExpr(GetExpr expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitSetExpr(SetExpr expr) {
        resolve(expr.object);
        resolve(expr.value);
        return null;
    }

    @Override
    public Void visitThisExpr(ThisExpr expr) {
        if (currentClass == ClassType.NONE) {
            Merlin.error(expr.keyword, "Can't use 'this' outside of a class.");
        }

        resolveLocal(expr.keyword, expr, false);
        return null;
    }

    @Override
    public Void visitSuperExpr(SuperExpr expr) {
        if (currentClass == ClassType.NONE) {
            Merlin.error(expr.keyword, "Can't use 'super' outside of a class.");
        }
        else if (currentClass != ClassType.SUBCLASS) {
            Merlin.error(expr.keyword, "Can't use 'super' in a class with no superclass.");
        }
        resolveLocal(expr.keyword, expr, false);
        return null;
    }

    @Override
    public Void visitSuperCallExpr(SuperCallExpr expr) {
        if (currentClass == ClassType.NONE) {
            Merlin.error(expr.keyword, "Can't use 'super' outside of a class.");
        }
        else if (currentClass != ClassType.SUBCLASS) {
            Merlin.error(expr.keyword, "Can't use 'super' in a class with no superclass.");
        }

        if (!isConstructor) {
            Merlin.error(expr.keyword, "can't call a superclass constructor 'super(args)' outside of a constructor.");
        }

        resolveLocal(expr.keyword, expr, false);
        for (Expr arg : expr.arguments) resolve(arg);

        return null;
    }

    @Override
    public Void visitFunctionExpr(FunctionExpr expr) {
        FunctionType tmp = currentFunction;
        currentFunction = FunctionType.FUNCTION;
        beginScope();
        for (Token parameter : expr.parameters) {
            declare(parameter);
            define(parameter.lexeme);
            initialize(parameter.lexeme);
        }
        resolve(expr.body);
        endScope();
        currentFunction = tmp;
        return null;
    }

    private void declare(Token name) {
        scopes.peek().declare(name);
    }

    private void define(String name) {
        scopes.peek().define(name);
    }

    private void initialize(String name) {
        scopes.peek().initialize(name);
    }

    private void use(String name) {
        scopes.peek().use(name);
    }

    private void beginScope() {
        scopes.push(new Scope());
    }

    private void endScope() {
        for (Token token : scopes.peek().getUnused()) {
            if (!showWarnings) return;
            Merlin.warning(token, "Variable is not used.");
        }
        scopes.pop();
    }

    @Override
    public Void visitClassDeclStmt(ClassDeclStmt stmt) {
        declare(stmt.name);
        define(stmt.name.lexeme);
        initialize(stmt.name.lexeme);
        if (stmt.superclass != null) {
            resolve(stmt.superclass);
            beginScope();
            scopes.peek().defineNative("super");
        }
        beginScope();
        scopes.peek().defineNative("this");
        ClassType tmp = currentClass;
        currentClass = stmt.superclass != null ? ClassType.SUBCLASS : ClassType.CLASS;
        boolean previous = isConstructor;
        for (Stmt.FunDeclStmt method : stmt.methods) {
            isConstructor = method.name.lexeme.equals("init");
            resolve(method);
            use(method.name.lexeme);
        }
        isConstructor = previous;
        currentClass = tmp;
        endScope();
        if (stmt.superclass != null) {
            endScope();
        }
        
        return null;
    }

    @Override
    public Void visitImportStmt(ImportStmt stmt) {
        declare(stmt.alias);
        define(stmt.alias.lexeme);
        initialize(stmt.alias.lexeme);

        return null;
    }
}
