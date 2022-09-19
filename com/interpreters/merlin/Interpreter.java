package com.interpreters.merlin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.interpreters.merlin.Expr.AssignExpr;
import com.interpreters.merlin.Expr.BinaryExpr;
import com.interpreters.merlin.Expr.CallExpr;
import com.interpreters.merlin.Expr.FunctionExpr;
import com.interpreters.merlin.Expr.GetExpr;
import com.interpreters.merlin.Expr.GroupingExpr;
import com.interpreters.merlin.Expr.LiteralExpr;
import com.interpreters.merlin.Expr.LogicExpr;
import com.interpreters.merlin.Expr.SetExpr;
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
import com.interpreters.merlin.Stmt.RETURNStmt;
import com.interpreters.merlin.Stmt.VarDeclStmt;
import com.interpreters.merlin.Stmt.WHILEStmt;
import com.interpreters.merlin.nativeFunctions.Printf;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private final boolean mainThread;
    private final Map<String, MerlinLib> libs;

    private final Environment global = new Environment(null);
    private Environment environment = global;

    private Map<Expr, Integer> distances;

    Interpreter(Map<String, MerlinLib> libs, boolean mainThread) {
        this.libs = libs;
        this.mainThread = mainThread;
        global.define("print", new Printf(""));
        global.define("println", new Printf("\n"));
    }

    Interpreter() {
        this(new HashMap<>(), true);
    }

    public boolean isMain() {
        return this.mainThread;
    }

    public void setDistance(Map<Expr, Integer> distances) {
        this.distances = distances;
    }

    public void interprete(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) 
                execute(statement);
        }
        catch(RuntimeError error) {
            Merlin.runtimeError(error.token, error.message);
        }
    }

    public void execute(Stmt stmt) {
        stmt.accept(this);
    }

    @Override
    public Object visitLiteralExpr(LiteralExpr expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr expr) {
        Object right = evaluate(expr.right);
        switch (expr.operation.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                if (right instanceof Double) return -(double) right;
                throw new RuntimeError(expr.operation, "Operand must be number.");
        }

        /// unreachable
        return null;
    }

    @Override
    public Object visitGroupingExpr(GroupingExpr expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr expr) {
        Object  left = evaluate(expr.left), 
                right = evaluate(expr.right);
        
        switch (expr.operation.type) {
            case PLUS:
                return sum(left, right, expr.operation);
            case STAR:
                if ((left instanceof Double) && (right instanceof Double)) {
                    return (double) left * (double) right;
                }
                throw new RuntimeError(expr.operation, "Operands must be two numbers.");
            case SLASH:
                if ((left instanceof Double) && (right instanceof Double)) {
                    if ((double) right == 0) 
                        throw new RuntimeError(expr.operation, "Divison by zero.");
                    
                    return (double) left / (double) right;
                }
                throw new RuntimeError(expr.operation, "Operands must be two numbers.");
            case MINUS:
                if ((left instanceof Double) && (right instanceof Double)) {
                    return (double) left - (double) right;
                }
                throw new RuntimeError(expr.operation, "Operands must be two numbers.");

            case PERCENT:
                if ((left instanceof Double) && (right instanceof Double)) {
                    if ((double) right == 0) 
                        throw new RuntimeError(expr.operation, "Divison by zero.");
                    
                    return Math.floor((double)left) % Math.floor((double)right);
                }
                throw new RuntimeError(expr.operation, "Operands must be two numbers.");

            case LESS: return less(left, right, expr.operation);
            case LESS_EQUAL: return !less(right, left, expr.operation);
            case GREATER_EQUAL: return !less(left, right, expr.operation);
            case GREATER: return less(right, left, expr.operation);
            case EQUAL_EQUAL: return equals(left, right);
            case BANG_EQUAL: return !equals(left, right);
        }
        return null;
    }

    @Override
    public Object visitLogicExpr(LogicExpr expr) {
        Object left = evaluate(expr.left);
        if (expr.operation.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        }
        else {
            if (!isTruthy(left)) return left;
        }
        return evaluate(expr.right);
    }

    @Override
    public Object visitVariableExpr(VariableExpr expr) {
        if (distances.containsKey(expr)) {
            return environment.get(expr.name.lexeme, distances.get(expr));
        }
        return global.get(expr.name.lexeme);
    }

    @Override
    public Object visitAssignExpr(AssignExpr expr) {
        Object value = evaluate(expr.value);
        if (distances.containsKey(expr.object)) {
            environment.assign(expr.object.name.lexeme, value, distances.get(expr.object));
        } else global.assign(expr.object.name.lexeme, value);
        return value;
    }

    @Override
    public Object visitCallExpr(CallExpr expr) {
        Object object = evaluate(expr.callee);
        if (!(object instanceof MerlinCallable)) {
            throw new RuntimeError(expr.paren, 
                "The call operator can only be used on function and class objects");
        }

        MerlinCallable callee = (MerlinCallable) object;

        if (callee.arity() != -1) {
            if (callee.arity() != expr.arguments.size()) {
                throw new RuntimeError(expr.paren, 
                    "Expected " + callee.arity() + " arguments but got" + expr.arguments.size() + ".");
            }
        }
        
        List<Object> arguments = new ArrayList<>();
        for (Expr arg : expr.arguments) arguments.add(evaluate(arg));

        return callee.call(this, arguments);
    }

    @Override
    public Object visitGetExpr(GetExpr expr) {
        Object object = evaluate(expr.object);
        if (!(object instanceof MerlinInstance)) {
            throw new RuntimeError(expr.property, "Only instances have properties.");
        }
        MerlinInstance instance = (MerlinInstance) object;
        return instance.get(expr.property);
    }

    @Override
    public Object visitSetExpr(SetExpr expr) {
        Object object = evaluate(expr.object);
        if (!(object instanceof MerlinInstance)) {
            throw new RuntimeError(expr.property, "Only instances have properties.");
        }
        MerlinInstance instance = (MerlinInstance) object;
        
        Object value = evaluate(expr.value);
        instance.set(expr.property.lexeme, value);

        return value;
    }

    @Override
    public Object visitThisExpr(ThisExpr expr) {
        int distance = distances.get(expr);
        return environment.get("this", distance);
    }

    @Override
    public Object visitSuperExpr(SuperExpr expr) {
        int distance = distances.get(expr);
        MerlinClass superclass = (MerlinClass) environment.get(expr.keyword.lexeme, distance);
        MerlinInstance instance = (MerlinInstance) environment.get("this", distance - 1);
        return superclass.findMethod(expr.property).bind("this", instance);
    }

    @Override
    public Object visitFunctionExpr(FunctionExpr expr) {
        return new MerlinFunction(null, expr, environment);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean equals(Object left, Object right) {
        if (left == null) return false;
        return left.equals(right);
    }

    private boolean less(Object left, Object right, Token operation) {
        if ((left instanceof Double) && (right instanceof Double)) return (double) left < (double) right;
        if ((left instanceof String) && (right instanceof Double)) 
            return ((String) left).compareTo((String) right) < 0;
        
        throw new RuntimeError(operation, "Operands must be two numbers or two strings.");
    }

    public static String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) text = text.substring(0, text.length() - 2);
            return text;
        }
        return object.toString();
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        if (object instanceof Double) return (double) object != 0;
        if (object instanceof String) return !((String) object).isEmpty();
        return true;
    }

    private Object sum(Object left, Object right, Token operation) {
        if ((left instanceof Double) && (right instanceof Double)) {
            return (double) left + (double) right;
        }

        if (left instanceof String) {
            return (String) left + stringify(right);
        }
        else if (right instanceof String) {
            return stringify(left) + (String) right;
        }
        throw new RuntimeError(operation, "TypeError: Operands must be numbers or strings.");
    }

    @Override
    public Void visitExpressionStmt(ExpressionStmt stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitVarDeclStmt(VarDeclStmt stmt) {
        for (int i = 0, end = stmt.names.size(); i < end; ++i) {
            Object value = null;
            if (stmt.initializers.get(i) != null)
                value = evaluate(stmt.initializers.get(i));
            environment.define(stmt.names.get(i).lexeme, value);
        }
        return null;
    }

    @Override
    public Void visitBlockStmt(BlockStmt stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    public void executeBlock(List<Stmt> statements, Environment blockEnvironment) {
        Environment tmp = this.environment;
        this.environment = blockEnvironment;
        try {
            interprete(statements);
        }
        finally {
            this.environment = tmp;
        }
    }

    @Override
    public Void visitIFStmt(IFStmt stmt) {
        Object condition = evaluate(stmt.condition);
        if (isTruthy(condition)) execute(stmt.thenBranch);
        else if (stmt.elseBranch != null) execute(stmt.elseBranch);
        
        return null;
    }

    @Override
    public Void visitWHILEStmt(WHILEStmt stmt) {
        while (isTruthy(evaluate(stmt.condition))) execute(stmt.body);
        return null;
    }

    @Override
    public Void visitFORStmt(FORStmt stmt) {

        if (stmt.initializer != null) {
            if (stmt.increment != null) {
                for (execute(stmt.initializer); isTruthy(evaluate(stmt.condition)); evaluate(stmt.increment)) {
                    execute(stmt.body);
                }
            }
            else {
                for (execute(stmt.initializer); isTruthy(evaluate(stmt.condition));) {
                    execute(stmt.body);
                }
            }
        }
        else {
            if (stmt.increment != null) {
                for (;isTruthy(evaluate(stmt.condition)); evaluate(stmt.increment)) {
                    execute(stmt.body);
                }
            }
            else {
                for (;isTruthy(evaluate(stmt.condition));) {
                    execute(stmt.body);
                }
            }
        }
        

        return null;
    }

    @Override
    public Void visitFunDeclStmt(FunDeclStmt stmt) {
        MerlinFunction function = new MerlinFunction(stmt.name.lexeme, stmt.description, environment);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitRETURNStmt(RETURNStmt stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);
        throw new Return(value);
    }

    @Override
    public Void visitClassDeclStmt(ClassDeclStmt stmt) {
        MerlinClass superclass = null;
        if (stmt.superclass != null) {
            Object object = evaluate(stmt.superclass);
            if (object instanceof MerlinClass) superclass = (MerlinClass) object;
            else throw new RuntimeError(stmt.superclass.name, "Superclass must be class.");
        }

        environment.define(stmt.name.lexeme, null);

        Environment closure = environment;
        if (superclass != null) {
            closure = new Environment(environment);
            closure.define("super", superclass);
        }
        Map<String, MerlinFunction> methods = new HashMap<>();
        MerlinFunction constructor = null;
        for (Stmt.FunDeclStmt function : stmt.methods) {
            MerlinFunction method = 
                    new MerlinFunction(function.name.lexeme, function.description, closure);
            if (function.name.lexeme.equals("init")) constructor = method;
            else methods.put(function.name.lexeme, method);
        }

        MerlinClass mc = new MerlinClass(stmt.name.lexeme, superclass, methods, constructor);
        environment.assign(stmt.name.lexeme, mc);

        return null;
    }    
}
