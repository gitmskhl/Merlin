package com.interpreters.merlin;

import java.util.List;
import java.util.Map;

public class MerlinClass implements MerlinCallable {

    private final String name;
    private final MerlinClass superclass;
    private final Map<String, MerlinFunction> methods;
    private final MerlinFunction constructor;
    private final int _arity;
    

    public MerlinClass(String name, MerlinClass superclass, Map<String, MerlinFunction> methods, MerlinFunction constructor) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
        this.constructor = constructor;
        this._arity = constructor != null ? constructor.arity() : 0;
    }

    @Override
    public int arity() {
        return this._arity;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
        MerlinInstance instance =  new MerlinInstance(this);
        if (constructor != null) constructor.bind("this", instance).call(interpreter, arguments, paren);
        return instance;
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

    public MerlinFunction getConstructor() {
        return constructor;
    }
    
}
