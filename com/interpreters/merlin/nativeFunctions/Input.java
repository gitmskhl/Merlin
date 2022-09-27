package com.interpreters.merlin.nativeFunctions;

import java.util.List;
import java.util.Scanner;

import com.interpreters.merlin.Interpreter;
import com.interpreters.merlin.MerlinCallable;
import com.interpreters.merlin.Token;

public class Input implements MerlinCallable {

    @Override
    public int arity() {
        return -1;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
        String message = "";
        if (arguments.size() == 1) message = arguments.get(0).toString();
        Scanner myObj = new Scanner(System.in);
        System.out.print(message);
        return myObj.nextLine();
    }

}
