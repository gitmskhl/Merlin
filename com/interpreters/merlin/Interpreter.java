package com.interpreters.merlin;

import java.io.IOException;
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
import com.interpreters.merlin.Expr.ListExpr;
import com.interpreters.merlin.Expr.ListGetExpr;
import com.interpreters.merlin.Expr.ListSetExpr;
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
import com.interpreters.merlin.Stmt.ForEachStmt;
import com.interpreters.merlin.Stmt.FunDeclStmt;
import com.interpreters.merlin.Stmt.IFStmt;
import com.interpreters.merlin.Stmt.ImportStmt;
import com.interpreters.merlin.Stmt.RETURNStmt;
import com.interpreters.merlin.Stmt.VarDeclStmt;
import com.interpreters.merlin.Stmt.WHILEStmt;
import com.interpreters.merlin.nativeFunctions.*;
import com.interpreters.merlin.std.map.map;
import com.interpreters.merlin.std.string.string;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private final String module;
    private final boolean mainThread;
    private final Map<String, MerlinLib> libs;

    private final Environment global = new Environment(null);
    private Environment environment = global;

    private Map<Expr, Integer> distances;

    Interpreter(String module, Map<String, MerlinLib> libs, boolean mainThread) {
        this.module = module;
        this.libs = libs;
        this.mainThread = mainThread;
        initNativeFunctions();
        this.libs.put(module, new MerlinLib(module, module, global));
    }

    Interpreter(String module) {
        this(module, new HashMap<>(), true);
    }

    private void initNativeFunctions() {
        global.define("print", new Printf(""));
        global.define("println", new Printf("\n"));
        global.define("len", new Len());
        global.define("range", new Range());
        global.define("int", new Int());
        global.define("input", new Input());
        global.define("filter", new Filter());
    }

    public void addDistances(Map<Expr, Integer> anotherDistances) {
        anotherDistances.putAll(distances);
    }

    public boolean isMain() {
        return this.mainThread;
    }

    public void setDistance(Map<Expr, Integer> distances) {
        this.distances = distances;
    }

    public Environment interpreteAll(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) 
                execute(statement);
            
            return global;
        }
        catch(RuntimeError error) {
            if (!mainThread) throw error;
            if (!error.dummy) Merlin.runtimeError(error.token, error.message);

            return null;
        }
    }

    private void interprete(List<Stmt> statements) {
        for (Stmt statement : statements) execute(statement);
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
        return environment.get(expr.name.lexeme, distances.get(expr));
    }

    @Override
    public Object visitAssignExpr(AssignExpr expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.object.name.lexeme, value, distances.get(expr.object));
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

        if (callee.arity() != -256) {
            if (callee.arity() < 0) {
                if (expr.arguments.size() > -callee.arity()) {
                    throw new RuntimeError(expr.paren, 
                    "Expected no more than " + (-callee.arity()) +" arguments but got " + expr.arguments.size() + ".");
                }
            }
            else if (callee.arity() != expr.arguments.size()) {
                throw new RuntimeError(expr.paren, 
                    "Expected " + callee.arity() + " arguments but got " + expr.arguments.size() + ".");
            }
        }
        
        List<Object> arguments = new ArrayList<>();
        for (Expr arg : expr.arguments) arguments.add(evaluate(arg));

        return callee.call(this, arguments, expr.paren);
    }

    @Override
    public Object visitGetExpr(GetExpr expr) {
        Object object = evaluate(expr.object);
        if (object instanceof MerlinInstance) {
            MerlinInstance instance = (MerlinInstance) object;
            return instance.get(expr.property);
        }
        else if (object instanceof MerlinLib) {
            return ((MerlinLib) object).get(expr.property);
        }

        throw new RuntimeError(expr.property, "Only instances and modules have properties.");
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
    public Object visitListExpr(ListExpr expr) {
        List<Object> elements = new ArrayList<>();
        for (Expr element : expr.elements) elements.add(evaluate(element));

        return new MerlinListInstance(elements);
    }   

    @Override
    public Object visitListGetExpr(ListGetExpr expr) {
        Object object = evaluate(expr.object);
        if (!(object instanceof MerlinListInstance)) {
            throw new RuntimeError(expr.bracket, "Can't take index from non-list object.");
        }
        Object index = evaluate(expr.index);
        return ((MerlinListInstance) object).get(index, expr.bracket);
    }

    @Override
    public Object visitListSetExpr(ListSetExpr expr) {
        Object object = evaluate(expr.getter.object);
        if (!(object instanceof MerlinListInstance)) {
            throw new RuntimeError(expr.getter.bracket, "Can't take index from non-list object.");
        }
        Object index = evaluate(expr.getter.index);
        Object value = evaluate(expr.value);
        ((MerlinListInstance) object).set(index, value, expr.getter.bracket);
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
    public Object visitSuperCallExpr(SuperCallExpr expr) {
        int distance = distances.get(expr);
        MerlinClass superclass = (MerlinClass) environment.get(expr.keyword.lexeme, distance);
        MerlinFunction constructor = superclass.getConstructor();
        if (constructor == null) return null;
        if (constructor.arity() != expr.arguments.size()) {
            throw new RuntimeError(expr.keyword, 
                "Expected " + constructor.arity() + " arguments but got " + expr.arguments.size()+ ".");
        }

        List<Object> arguments = new ArrayList<>();
        for (Expr arg : expr.arguments) arguments.add(evaluate(arg));


        MerlinInstance instance = (MerlinInstance) environment.get("this", distance - 1);
        superclass.getConstructor().bind("this", instance).call(this, arguments, expr.keyword);
        return null;
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

    @Override
    public Void visitImportStmt(ImportStmt stmt) {
        boolean isSTD = false;
        String moduleName = stmt.libname.lexeme;
        String aliasName = stmt.alias.lexeme;
        if (stmt.from != null) {
            if (stmt.dirs.size() == 1 && stmt.dirs.get(0).equals("std")) isSTD = true;
            moduleName = String.join("/", stmt.dirs) + "/" + moduleName;
        }

        if (moduleName.equals(module)) return null;

        String fileName = moduleName + ".merlin";

        if (isSTD) {
            Environment libEnvironment = new Environment(null);
            String objectName = "";
            Object object = null;

            switch (stmt.libname.lexeme) {
                case "string":
                    objectName = "string";
                    object = new string();
                    break;
                case "map":
                    objectName = "map";
                    object = new map();
                    break;
                default:
                    throw new RuntimeError(stmt.libname, 
                        "Undefined library '" + stmt.libname.lexeme + "' in std.");
            }

            libEnvironment.define(objectName, object);
            MerlinLib lib = new MerlinLib(moduleName, aliasName, libEnvironment);
            libs.put(lib.name, lib);
        }
        else if (!libs.containsKey(moduleName)) {
            String source;
            try {
                source = Merlin.getSource(fileName);
            }
            catch (IOException exception) {
                throw new RuntimeError(stmt.keyword, "No such file '" + fileName + "'.");
            }
            Interpreter newInterpreter = new Interpreter(moduleName, libs, false);
            Environment libEnvironment =  Merlin.run(newInterpreter, source, fileName);
            newInterpreter.addDistances(distances);
            MerlinLib lib = new MerlinLib(moduleName, aliasName, libEnvironment);
            libs.put(lib.name, lib);
        }

        MerlinLib lib = libs.get(moduleName);
        environment.define(lib.alias, lib);

        return null;
    }

    @Override
    public Void visitForEachStmt(ForEachStmt stmt) {
        Environment newEnv = new Environment(environment);
        Environment tmp = environment;

        try {
            environment = newEnv;
            Object expr = evaluate(stmt.iterable);
            if (!(expr instanceof MerlinIterable)) {
                throw new RuntimeError(stmt.in, "Expression after 'in' must be iterable.");
            }

            MerlinIterable iterable = (MerlinIterable) expr;

            for (; !iterable.isAtEnd(); ) {
                environment.assign(stmt.iter.name.lexeme, iterable.next(), distances.get(stmt.iter));
                execute(stmt.body);
            }

            iterable.reset();
            return null;
        }
        finally {
            environment = tmp;
        }
    }

}
