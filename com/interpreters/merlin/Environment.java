package com.interpreters.merlin;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public Object get(String name) {
        return values.get(name);
    }

    public Object get(String name, int depth) {
        return ancestor(depth).get(name);
    }

    public void assign(String name, Object value) {
        values.put(name, value);
    }

    public void assign(String name, Object value, int depth) {
        ancestor(depth).assign(name, value);
    }

    private Environment ancestor(int depth) {
        Environment current = this;
        for (int i = 0; i < depth; ++i) current = current.enclosing;
        return current;
    }

    public Map<String, Object> getValues() {
        return this.values;
    }
}
