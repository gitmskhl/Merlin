package com.interpreters.merlin;

import java.util.List;

public interface MerlinCallable {
    public int arity();
    public Object call(Interpreter interpreter, List<Object> arguments);
}
