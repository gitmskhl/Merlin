package com.interpreters.merlin.nativeFunctions;

import java.util.ArrayList;
import java.util.List;

import com.interpreters.merlin.Interpreter;
import com.interpreters.merlin.MerlinCallable;
import com.interpreters.merlin.MerlinLib;
import com.interpreters.merlin.MerlinListInstance;
import com.interpreters.merlin.RuntimeError;
import com.interpreters.merlin.Token;
import com.interpreters.merlin.TokenType;

public class Range implements MerlinCallable {

    private Token paren;

    @Override
    public int arity() {
        return -3;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
        this.paren = paren;
        List<Integer> lst = new ArrayList<>();
        for (int i = 0; i < arguments.size(); ++i) {
            checkInt(arguments.get(i), i);
            int value = (int)((double) arguments.get(i));
            lst.add(value);
        }

        switch (lst.size()) {
            case 1:
                return range(0, lst.get(0), 1);
            case 2:
                return range(lst.get(0), lst.get(1), 1);
            case 3:
                return range(lst.get(0), lst.get(1), lst.get(2));
        }

        throw new RuntimeError(paren,"Must be at least 1 argument.");
    }

    private MerlinListInstance range(int start, int end, int step) {
        if (step == 0) throw new RuntimeError(paren,
            "argument 3 must not be 0.");
        
        List<Object> res = new ArrayList<>();
        if (step > 0) {
            for (int i = start; i < end; i += step) res.add(i * 1.0);
        }
        else {
            for (int i = start; i > end; i += step) res.add(i * 1.0);
        }

        return new MerlinListInstance(res);
    }

    private void checkInt(Object obj, int index) {
        if (!(obj instanceof Double) || (((double)obj) % 1 != 0)) 
            throw new RuntimeError(paren, "Argument must be an integer: position " + index + ".");
    }
    
}
