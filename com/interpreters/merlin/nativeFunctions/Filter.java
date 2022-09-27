package com.interpreters.merlin.nativeFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.interpreters.merlin.Interpreter;
import com.interpreters.merlin.MerlinCallable;
import com.interpreters.merlin.MerlinListInstance;
import com.interpreters.merlin.RuntimeError;
import com.interpreters.merlin.Token;

public class Filter implements MerlinCallable {

    @Override
    public int arity() {
        return 2;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
        if (!(arguments.get(0) instanceof MerlinCallable)) 
            throw new RuntimeError(paren, "First argument must be MerlinCallable.");
        
        MerlinCallable callback = (MerlinCallable) arguments.get(0);
        if (callback.arity() != 1) 
            throw new RuntimeError(paren, "Callback must take exactly 1 argument");
        
        if (!(arguments.get(1) instanceof MerlinListInstance)) 
            throw new RuntimeError(paren, "Second argument must be MerlinListInstance.");
        
        MerlinListInstance lst = (MerlinListInstance) arguments.get(1);
        
        List<Object> resultList = new ArrayList<>();
        for (Object arg : lst.getList()) 
            resultList.add(callback.call(interpreter, Arrays.asList(arg), paren));
        
        return new MerlinListInstance(resultList);
    }
    
}
