package com.interpreters.merlin.nativeFunctions;

import java.util.List;

import com.interpreters.merlin.Interpreter;
import com.interpreters.merlin.MerlinCallable;

public class Printf implements MerlinCallable {

    private final String end;

    public Printf(String end) {
        this.end = end;
    }

    @Override
    public int arity() {
        return -256;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        for (Object object : arguments.subList(0, arguments.size() - 1)) {
            System.out.print(Interpreter.stringify(object).replace("\\n", "\n"));
            System.out.print(" ");
        }

        System.out.print(
            Interpreter.stringify(arguments.get(arguments.size() - 1)).replace("\\n", "\n"));

        System.out.print(end);
        return null;
    }
    
}
