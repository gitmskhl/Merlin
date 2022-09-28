package com.interpreters.merlin.std.os;

import java.util.HashMap;
import java.util.List;

import com.interpreters.merlin.Interpreter;
import com.interpreters.merlin.MerlinCallable;
import com.interpreters.merlin.MerlinClass;
import com.interpreters.merlin.MerlinInstance;
import com.interpreters.merlin.RuntimeError;
import com.interpreters.merlin.Token;

public class osInstance extends MerlinInstance {

    public osInstance() {
        super(new MerlinClass("Os", null, new HashMap<>(), null));
        initMethods();
    }
    

    private void initMethods() {
        set("clear", new clear());
        set("name", new name());
        set("sleep", new sleep());
    }

    class clear implements MerlinCallable {

        @Override
        public int arity() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
            System.out.print("\033[H\033[2J");
            System.out.flush();
            return null;
        }

    }

    class name implements MerlinCallable {

        @Override
        public int arity() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
            return System.getProperty("os.name");
        }

    }

    class sleep implements MerlinCallable {

        @Override
        public int arity() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
            if (!(arguments.get(0) instanceof Double)) 
                throw new RuntimeError(paren, "Argument must be integer number.");

            int time = (int) ((double)arguments.get(0));
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeError(paren, "Internal exception");
            }
            return null;
        }
        
    }

}
