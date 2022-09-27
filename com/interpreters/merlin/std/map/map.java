package com.interpreters.merlin.std.map;

import java.util.List;

import com.interpreters.merlin.Interpreter;
import com.interpreters.merlin.MerlinCallable;
import com.interpreters.merlin.Token;

public class map implements MerlinCallable {

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
        return new mapInstance();
    }

    @Override
    public String toString() {
        return "<native class 'map' in std>";
    }
    
}
