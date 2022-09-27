package com.interpreters.merlin.std.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Iterator;

import com.interpreters.merlin.Interpreter;
import com.interpreters.merlin.MerlinCallable;
import com.interpreters.merlin.MerlinClass;
import com.interpreters.merlin.MerlinInstance;
import com.interpreters.merlin.MerlinIterable;
import com.interpreters.merlin.MerlinListInstance;
import com.interpreters.merlin.RuntimeError;
import com.interpreters.merlin.Token;

public class mapInstance extends MerlinInstance {

    private Map<String, Object> map = new HashMap<>();

    public mapInstance() {
        super(new MerlinClass("Map", null, new HashMap<>(), null));
        initMethods();
    }

    @Override
    public String toString() {
        String res = "{";
        int i = 0, len = map.size() - 1;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            res += "\"" + entry.getKey() + "\": " + Interpreter.stringify(entry.getValue()) 
                    + (i < len ? ", " : "");
            ++i;
        }
        return res + "}";
    }

    private void initMethods() {
        set("put", new put(this));
        set("get", new get());
        set("constainsKey", new constainsKey());
        set("size", new size());
        set("values", new values());
        set("keys", new keys());
    }

    private String getKey(Object arg, Token paren) {
        if (!(arg instanceof String)) 
            throw new RuntimeError(paren, "Key must be string.");
        
        return (String) arg;
    }
    
    class put implements MerlinCallable {

        private final mapInstance instance;
        
        put(mapInstance instance) {
            this.instance = instance;
        }

        @Override
        public int arity() {
            return 2;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
            Object value = arguments.get(1);
            String key = getKey(arguments.get(0), paren);
            map.put(key, value);
            return instance;
        }

    }

    class get implements MerlinCallable {

        @Override
        public int arity() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
            String key = getKey(arguments.get(0), paren);

            return map.get(key);
        }

    }

    class constainsKey implements MerlinCallable {

        @Override
        public int arity() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
            String key = getKey(arguments.get(0), paren);

            return map.containsKey(key);
        }

    }

    class size implements MerlinCallable {

        @Override
        public int arity() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
            return map.size() * 1.0;
        }

    }

    class values implements MerlinCallable {

        @Override
        public int arity() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
            return new MerlinListInstance(new ArrayList<>(map.values()));
        }
    }

    class keys implements MerlinCallable {

        @Override
        public int arity() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
            return new MerlinListInstance(new ArrayList<>(map.keySet()));
        }
    }

}
