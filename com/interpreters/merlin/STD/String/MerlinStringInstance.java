package com.interpreters.merlin.STD.String;

import java.util.HashMap;
import java.util.List;

import com.interpreters.merlin.Interpreter;
import com.interpreters.merlin.MerlinCallable;
import com.interpreters.merlin.MerlinClass;
import com.interpreters.merlin.MerlinInstance;
import com.interpreters.merlin.RuntimeError;
import com.interpreters.merlin.Token;

public class MerlinStringInstance extends MerlinInstance {

    private String str;

    public MerlinStringInstance(String str) {
        super(new MerlinClass("String", null, new HashMap<>(), null));
        this.str = str;

        initMethods();
    }
    
    private void initMethods() {
        set("get", new get(this));
        set("substring", new substring(this));
        set("length", new length(this));
    }

    public int size() {
        return str.length();
    }

    @Override
    public String toString() {
        return str;
    }

    private int getCorrectIndex(int index) {
        if (index >= 0) return index;
        return ((index + size()) % size());
    }

    private int convertToInt(Object obj) {
        return (int)((double)obj);
    }

    private boolean checkInt(Object obj) {
        if (!(obj instanceof Double)) return false;
        double n = (double) obj;
        if (n % 1 != 0) return false;
        return true;
    }

    private Object getAt(Object index, Token paren) {
        if (checkInt(index)) {
            int ind = getCorrectIndex(convertToInt(index));
            if (ind < 0 || ind > size()) 
                throw new RuntimeError(paren, "Index out of range");
            return str.charAt(ind);
        }
        throw new RuntimeError(paren, "Index must be an integer.");
    }

    class get implements MerlinCallable {

        private final MerlinStringInstance instance;

        get(MerlinStringInstance instance) {
            this.instance = instance;
        }

        @Override
        public int arity() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
            Object index = arguments.get(0);
            return instance.getAt(index, paren);
        }

    }

    class substring implements MerlinCallable {

        private final MerlinStringInstance instance;

        substring (MerlinStringInstance instance) {
            this.instance = instance;
        }

        @Override
        public int arity() {
            return 2;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
            Object begin = arguments.get(0), end = arguments.get(1);
            if (!instance.checkInt(begin) || !instance.checkInt(end))
                throw new RuntimeError(paren, "Indexes must be integers.");

            int b = instance.getCorrectIndex(instance.convertToInt(begin)),
                e = instance.getCorrectIndex(instance.convertToInt(end));
            
            
            return instance.str.subSequence(b, e);
        }

    }

    class length implements MerlinCallable {

        private final MerlinStringInstance instance;

        length (MerlinStringInstance instance) {
            this.instance = instance;
        }

        @Override
        public int arity() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
            return instance.str.length() * 1.0;
        }

    }

}
