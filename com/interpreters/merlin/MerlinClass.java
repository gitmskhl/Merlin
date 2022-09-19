package com.interpreters.merlin;

import java.util.List;
import java.util.Map;

public class MerlinClass implements MerlinCallable {

    private final String name;
    private final MerlinClass superclass;
    private final Map<String, MerlinFunction> methods;
    

    public MerlinClass(String name, MerlinClass superclass, Map<String, MerlinFunction> methods) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
    }

    @Override
    public int arity() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return new MerlinInstance(this);
    }

    public MerlinFunction findMethod(Token name) {
        if (methods.containsKey(name.lexeme)) return methods.get(name.lexeme);
        if (superclass != null) return superclass.findMethod(name);

        return null;
    }

    @Override
    public String toString() {
        return "<class '" + name + "'>";
    }

    public String getName() {
        return name;
    }
    
}
