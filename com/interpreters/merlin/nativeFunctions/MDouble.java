package com.interpreters.merlin.nativeFunctions;

import java.util.List;

import com.interpreters.merlin.Interpreter;
import com.interpreters.merlin.MerlinCallable;
import com.interpreters.merlin.RuntimeError;
import com.interpreters.merlin.Token;

public class MDouble implements MerlinCallable {

    @Override
    public int arity() {
        return 1;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
        Object arg = arguments.get(0);
        if (arg instanceof Double) return (double) arg;
        try {
            return Double.parseDouble(arg.toString());
        }
        catch (NumberFormatException exception) {
            throw new RuntimeError(paren, "Can't convert to double.");
        }
    }
    
}
