package com.interpreters.merlin;

import java.util.HashMap;
import java.util.Map;

public class MerlinInstance {
    private final MerlinClass description;
    private final Map<String, Object> properties = new HashMap<>();
    
    public MerlinInstance(MerlinClass description) {
        this.description = description;
    }


    public Object get(Token property) {
        if (properties.containsKey(property.lexeme)) return properties.get(property.lexeme);

        MerlinFunction method = description.findMethod(property);
        if (method != null) return method;

        throw new RuntimeError(property, "Undefined property '" + property.lexeme + "'.");
    }

    public void set(String property, Object value) {
        properties.put(property, value);
    }

    @Override
    public String toString() {
        return "<instance of class '" + description.getName() + "'>";
    }

}
