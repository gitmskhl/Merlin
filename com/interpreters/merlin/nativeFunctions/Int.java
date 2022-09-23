package com.interpreters.merlin.nativeFunctions;

import java.util.List;

import com.interpreters.merlin.Interpreter;
import com.interpreters.merlin.MerlinCallable;
import com.interpreters.merlin.RuntimeError;
import com.interpreters.merlin.Token;

public class Int implements MerlinCallable {

    @Override
    public int arity() {
        return 1;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
        Object obj = arguments.get(0);
        if (obj instanceof Double) return Math.floor(((double) obj));
        
        throw new RuntimeError(paren, "Native function 'int': Can't convert argument to int.");
    }

}
