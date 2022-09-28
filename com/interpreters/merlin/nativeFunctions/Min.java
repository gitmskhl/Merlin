package com.interpreters.merlin.nativeFunctions;

import java.util.Arrays;
import java.util.List;

import com.interpreters.merlin.Interpreter;
import com.interpreters.merlin.MerlinCallable;
import com.interpreters.merlin.MerlinIterable;
import com.interpreters.merlin.RuntimeError;
import com.interpreters.merlin.Token;

public class Min implements MerlinCallable{

    @Override
    public int arity() {
        return -2;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
        if (!(arguments.get(0) instanceof MerlinIterable)) 
            throw new RuntimeError(paren, "First argument must be MerlinIterable.");
        
        MerlinIterable iterable = (MerlinIterable) arguments.get(0);
        if (arguments.size() == 1) return defaultComparator(iterable, paren);
        if (!(arguments.get(1) instanceof MerlinCallable))
            throw new RuntimeError(paren, "Comparator(second argument) must be MerlinCallable.");
        
        MerlinCallable comparator = (MerlinCallable) arguments.get(1);
        return Comparator(interpreter, iterable, comparator, paren);
    }

    private Object defaultComparator(MerlinIterable iterable, Token paren) {
        boolean flag = false;
        Object min = null;
        for (; !iterable.isAtEnd();) {
            Object current = iterable.next();
            if (!flag || Interpreter.less(current, min, paren)) {
                min = current;
                flag = true;
            }
        }
        iterable.reset();
        return min;
    }

    private Object Comparator(Interpreter interpreter, MerlinIterable iterable, MerlinCallable comparator, Token paren) {
        boolean flag = false;
        Object min = null;
        for (; !iterable.isAtEnd();) {
            Object current = iterable.next();
            try {
                if (!flag || (boolean) comparator.call(interpreter, Arrays.asList(current, min), paren)) {
                    min = current;
                    flag = true;
                }
            }
            catch (ClassCastException exception) {
                throw new RuntimeError(paren, "Comparator must return 'boolean'.");
            }
        }
        iterable.reset();
        return min;
    }
    
}
