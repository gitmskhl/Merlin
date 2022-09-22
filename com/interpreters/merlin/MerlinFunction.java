package com.interpreters.merlin;

import java.util.List;

import com.interpreters.merlin.Expr.FunctionExpr;

public class MerlinFunction implements MerlinCallable {

    private final String name;
    private final Expr.FunctionExpr description;
    private final Environment closure;

    public MerlinFunction(String name, FunctionExpr description, Environment closure) {
        this.name = name;
        this.description = description;
        this.closure = closure;
    }

    @Override
    public int arity() {
        return description.parameters.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < arguments.size(); ++i) {
            environment.define(description.parameters.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(description.body, environment);
            return null;
        }
        catch(Return ret) {
            return ret.value;
        }
    }

    public MerlinFunction bind(String name, Object value) {
        Environment environment = new Environment(closure);
        environment.define(name, value);
        MerlinFunction function = new MerlinFunction(name, description, environment);
        return function;
    }

    @Override
    public String toString() {
        if (name != null) return "<fn '" + name + "'>";
        return "<anonymus fn>";
    }
    
}
