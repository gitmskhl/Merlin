package com.interpreters.merlin.nativeFunctions;

import java.util.List;

import com.interpreters.merlin.Interpreter;
import com.interpreters.merlin.MerlinCallable;
import com.interpreters.merlin.MerlinLenable;
import com.interpreters.merlin.RuntimeError;
import com.interpreters.merlin.Token;
import com.interpreters.merlin.TokenType;

public class Len implements MerlinCallable {

    @Override
    public int arity() {
        return 1;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Object obj = arguments.get(0);
        if (obj instanceof MerlinLenable) return ((MerlinLenable) obj).size() * 1.0;
        
        throw new RuntimeError(new Token(TokenType.IDENTIFIER, "len", null, -1, -1, ""),
              "object type has not len()");
    }
    
}
