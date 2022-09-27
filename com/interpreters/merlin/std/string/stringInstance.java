package com.interpreters.merlin.std.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.interpreters.merlin.Interpreter;
import com.interpreters.merlin.MerlinCallable;
import com.interpreters.merlin.MerlinClass;
import com.interpreters.merlin.MerlinInstance;
import com.interpreters.merlin.MerlinIterable;
import com.interpreters.merlin.MerlinLenable;
import com.interpreters.merlin.MerlinListInstance;
import com.interpreters.merlin.RuntimeError;
import com.interpreters.merlin.Token;

public class stringInstance extends MerlinInstance implements MerlinIterable, MerlinLenable{

    private int current;
    private final String str;

    public stringInstance(String str) {
        super(new MerlinClass("String", null, new HashMap<>(), null));
        this.str = str;
        initMethods();
    }
        

    public int length() {
        return str.length();
    }

    @Override
    public String toString() {
        return str;
    }

    @Override
    public Object next() {
        return str.charAt(current++);
    }


    @Override
    public boolean isAtEnd() {
        return current == str.length();
    }


    @Override
    public void reset() {
        current = 0;
    }


    @Override
    public int size() {
        return str.length();
    }


    private void initMethods() {
        set("length", new length());
        set("getAt", new getAt());
        set("substring", new substring());
        set("reverse", new reverse());
        set("split", new split());
    }

    class length implements MerlinCallable {

        @Override
        public int arity() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
            return length();
        }

    }

    class getAt implements MerlinCallable {

        private Token paren;

        @Override
        public int arity() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
            this.paren = paren;
            Object index = arguments.get(0);

            if (index instanceof Double) return getDouble((double) index);
            if (index instanceof MerlinListInstance) return getList((MerlinListInstance) index);

            throw new RuntimeError(paren, "Index must be an integer or List.");
        }

        private Object getDouble(double n) {
            if (n % 1 != 0) throw new RuntimeError(paren, "Index must be an integer.");
            int index = (int)n;
            return getIndex(index);
        }

        private Object getIndex(int index) {
            if (index >= length()) throw new RuntimeError(paren, "String index out of range: index is too big.");
            index = (index + length()) % length();
            if (index < 0) throw new RuntimeError(paren, "String index out of range: index is very negative.");
            return str.charAt(index) + "";
        }

        private Object getList(MerlinListInstance indexes) {
            List<Object> lst = new ArrayList<>();
            int i = 0;
            for (Object obj : indexes.getList()) {
                if (!checkInt(obj)) throw new RuntimeError(paren, "The list argument(position " + i + " in the list) must be an integer.");
                lst.add(getIndex((int)((double)obj)));
                i += 1;
            }
            return new MerlinListInstance(lst);
        }
    
        private boolean checkInt(Object obj) {
            if (!(obj instanceof Double)) return false;
            double n = (double) obj;
            if (n % 1 != 0) return false;
            return true;
        }

    }

    class substring implements MerlinCallable {

        @Override
        public int arity() {
            return 2;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
            Object arg1 = arguments.get(0), arg2 = arguments.get(1);
            if (!checkInt(arg1) || !checkInt(arg2)) throw new RuntimeError(paren, "Indexes must be numbers.");
            int start = (int)((double)arg1), end = (int)((double)arg2);
            if (start >= length()) throw new RuntimeError(paren, "String index out of range: start index is too big.");
            if (end > length()) throw new RuntimeError(paren, "String index out of range: end index is too big.");
            start = (start + length()) % length();
            if (end != length()) end = (end + length()) % length();
            if (start < 0) throw new RuntimeError(paren, "String index out of range: start index is very negative.");
            if (end < 0) throw new RuntimeError(paren, "String index out of range: end index is very negative.");
            return str.substring(start, end);
        }

        private boolean checkInt(Object obj) {
            if (!(obj instanceof Double)) return false;
            double n = (double) obj;
            if (n % 1 != 0) return false;
            return true;
        }

    }

    class reverse implements MerlinCallable {

        @Override
        public int arity() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
            return new StringBuilder(str).reverse().toString();
        }

    }

    class split implements MerlinCallable {

        @Override
        public int arity() {
            return -1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
            String regex = " ";
            if (arguments.size() == 1) {
                Object obj = arguments.get(0);
                if (!(obj instanceof String)) 
                    throw new RuntimeError(paren, "Argument regex must be string.");
                
                regex = (String) obj;
            }
            return new MerlinListInstance(new ArrayList<>(Arrays.asList(str.split(regex))));
        }

    }

}
