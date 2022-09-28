package com.interpreters.merlin.std.os;

import java.util.List;

import com.interpreters.merlin.Interpreter;
import com.interpreters.merlin.MerlinCallable;
import com.interpreters.merlin.Token;

public class os implements MerlinCallable {

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
        return new osInstance();
    }
    
}
