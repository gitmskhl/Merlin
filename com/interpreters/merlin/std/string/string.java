package com.interpreters.merlin.std.string;

import java.util.List;

import com.interpreters.merlin.Interpreter;
import com.interpreters.merlin.MerlinCallable;
import com.interpreters.merlin.Token;

public class string implements MerlinCallable {

    @Override
    public int arity() {
        return -1;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
        String str = "";
        if (arguments.size() == 1) str = arguments.get(0).toString();

        return new stringInstance(str);
    }

    @Override
    public String toString() {
        return "<native class 'std::string'>";
    }
    
}
