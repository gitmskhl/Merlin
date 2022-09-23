package com.interpreters.merlin.STD.String;

import java.util.List;

import com.interpreters.merlin.Interpreter;
import com.interpreters.merlin.MerlinCallable;
import com.interpreters.merlin.Token;

class MerlinString implements MerlinCallable {

    @Override
    public int arity() {
        return -1;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
        
        String str = "";
        if (arguments.size() == 1) str = (String) arguments.get(0);
        
        return new MerlinStringInstance(str);
    }

    @Override
    public String toString() {
        return "<std native class 'String'>";
    }

}
