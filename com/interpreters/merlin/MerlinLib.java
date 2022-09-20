package com.interpreters.merlin;

import java.util.Map;

public class MerlinLib {
    public final String name;

    private final Map<String, Object> values;

    public MerlinLib(String name, Environment environment) {
        this.name = name;
        this.values = environment.getValues();
    }

    public Object get(Token name) {
        if (values.containsKey(name.lexeme)) return values.get(name.lexeme);

        throw new RuntimeError(name, "Undefined object '" + name.lexeme + "' in module.");
    }

    @Override
    public String toString() {
        return "<lib '" + name + "'>";
    }

}
