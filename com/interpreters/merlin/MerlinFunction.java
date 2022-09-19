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
    public Object call(Interpreter interpreter, List<Object> arguments) {
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
    
}
