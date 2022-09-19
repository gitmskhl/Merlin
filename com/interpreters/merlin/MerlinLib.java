package com.interpreters.merlin;

import java.util.Map;

public class MerlinLib {

    private final Map<String, Object> values;

    public MerlinLib(Environment environment) {
        this.values = environment.getValues();
    }

    public Object get(Token name) {
        if (values.containsKey(name.lexeme)) return values.get(name.lexeme);

        throw new RuntimeError(name, "Undefined object '" + name.lexeme + "' in module.");
    }

}
