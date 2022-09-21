package com.interpreters.merlin;

import java.util.List;

public class MFContainer implements MerlinCallable {

    private final Container instance;
    private final String method;

    public MFContainer(Container instance, String method) {
        this.instance = instance;
        this.method = method;
    } 

    @Override
    public int arity() {
        switch (method) {
            case "length":
            case "pop":
            case "isEmpty":
                return 0;
            case "add":
            case "get":
                return 1;
        }
        return -1;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        switch (method) {
            case "add": 
                instance.add(arguments.get(0));
                return null;
            case "get":
                return instance.get(arguments.get(0));
            case "length":
                return instance.length();
            case "pop":
                return instance.pop();
            case "isEmpty":
                return instance.isEmpty();
        }
        return null;
    }

    @Override
    public String toString() {
        return "<'add' method for '" + instance + "''>";
    }
    
}
