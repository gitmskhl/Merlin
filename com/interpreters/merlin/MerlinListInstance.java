package com.interpreters.merlin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MerlinListInstance extends MerlinInstance implements MerlinIterable, MerlinLenable {

    private final List<Object> list;
    private int current;

    public MerlinListInstance(List<Object> list) {
        super(new MerlinClass("List", null, new HashMap<>(), null));
        this.list = list;
        this.current = 0;
        initListMethods();
    }

    public MerlinListInstance() {
        this(new ArrayList<>());
    }

    public int size() {
        return list.size();
    }

    public Object get(Object index, Token bracket) {
        Token getToken = new Token(TokenType.IDENTIFIER, "get", null, 
            bracket.line, bracket.position, bracket.file);

        return ((MerlinCallable)get(getToken)).call(null, Arrays.asList(index), bracket);
    }

    public void set(Object index, Object value, Token bracket) {
        Token setToken = new Token(TokenType.IDENTIFIER, "set", null, 
            bracket.line, bracket.position, bracket.file);
        
        ((MerlinCallable)get(setToken)).call(null, Arrays.asList(index, value), bracket);
    }

    @Override
    public Object next() {
        return list.get(current++);
    }

    @Override
    public boolean isAtEnd() {
        return current == list.size();
    }

    @Override
    public void reset() {
        current = 0;
    }

    public List<Object> getList() {return list;}

    @Override
    public String toString() {
        String result = "[";
        if (!list.isEmpty()) {
            for (Object obj : list.subList(0, list.size() - 1)) result += Interpreter.stringify(obj) + ", ";
            result += Interpreter.stringify(list.get(list.size() - 1));
        }
        return result + "]";
    }

    private void initListMethods() {
        set("get", new get(this));
        set("set", new set(this));
        set("add", new add(this));
        set("isEmpty", new isEmpty(this));
        set("size", new size(this));
        set("pop", new pop(this));
    }

    private int correctIndex(int index) {
        return (index + list.size()) % list.size();
    }

    class get implements MerlinCallable {

        private final MerlinListInstance instance;
        private Token bracket;

        public get(MerlinListInstance instance) {
            this.instance = instance;
        }

        @Override
        public int arity() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token bracket) {
            this.bracket = bracket;
            Object index = arguments.get(0);

            if (index instanceof Double) return getDouble((double) index);
            if (index instanceof MerlinListInstance) return getList((MerlinListInstance) index);

            throw new RuntimeError(bracket, "Index must be an integer or List.");
        }

        private Object getDouble(double n) {
            if (n % 1 != 0) throw new RuntimeError(bracket, "Index must be an integer.");
            int index = (int)n;
            return getIndex(correctIndex(index));
        }
    
        private Object getIndex(int index) {
            index = correctIndex(index);
            if (index < 0 || index >= instance.list.size()) throw new RuntimeError(bracket, "List index out of range.");
            return instance.list.get(index);
        }
    
        private Object getList(MerlinListInstance indexes) {
            List<Object> lst = new ArrayList<>();
            int i = 0;
            for (Object obj : indexes.list) {
                if (!checkInt(obj)) throw new RuntimeError(bracket, "The list argument(position " + i + " in the list) must be an integer.");
                lst.add(getIndex((int)((double)obj)));
                i += 1;
            }
            return new MerlinListInstance(lst);
        }
    
        private boolean checkInt(Object obj) {
            if (!(obj instanceof Double)) return false;
            double n = (double) obj;
            if (n % 1 != 0) return false;
            return true;
        }

    }

    class size implements MerlinCallable {

        private final MerlinListInstance instance;

        public size(MerlinListInstance instance) {
            this.instance = instance;
        }

        @Override
        public int arity() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
            return instance.list.size() * 1.0;
        }

    }

    class isEmpty implements MerlinCallable {

        private final MerlinListInstance instance;

        public isEmpty(MerlinListInstance instance) {
            this.instance = instance;
        }

        @Override
        public int arity() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
            return instance.list.isEmpty();
        }

    }

    class set implements MerlinCallable {

        private final MerlinListInstance instance;
        private Token bracket;

        public set(MerlinListInstance instance) {
            this.instance = instance;
        }

        @Override
        public int arity() {
            return 2;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token bracket) {
            this.bracket = bracket;
            Object index = arguments.get(0), value = arguments.get(1);

            if (index instanceof Double) setDouble((double) index, value);
            else if (index instanceof MerlinListInstance) setList((MerlinListInstance) index, value);
            else throw new RuntimeError(bracket, "Index must be an integer.");

            return null;
        }

        private void setDouble(double n, Object value) {
            if (n % 1 != 0) throw new RuntimeError(bracket, "Index must be an integer.");
            int index = (int)n;
            index = correctIndex(index);
            setIndex(index, value, bracket);
        }
    
        private void setList(MerlinListInstance indexes, Object value) {
            if (value instanceof MerlinListInstance) {
                MerlinListInstance lst = (MerlinListInstance) value;
    
                if (indexes.size() != lst.size()) {
                    throw new RuntimeError(bracket, 
                        "The sizes of index list " + indexes.size() + " and value list " + lst.size() + " must match.");
                }
    
                int i = 0;
                for (Object obj : indexes.list) {
                    if (!checkInt(obj)) throw new RuntimeError(bracket, "The list argument(position " + i + " in the list) must be an integer.");
                    setIndex((int)((double)obj), lst.list.get(i), bracket);
                    i += 1;
                }
            }
            else {
                int i = 0;
                for (Object obj : indexes.list) {
                    if (!checkInt(obj)) throw new RuntimeError(bracket, "The list argument(position " + i + " in the list) must be an integer.");
                    setIndex((int)((double)obj), value, bracket);
                    i += 1;
                }
            }
        }
    
        private void setIndex (int index, Object value, Token bracket) {
            index = correctIndex(index);
            if (index < 0 || index >= instance.list.size()) throw new RuntimeError(bracket, "List index out of range.");
            instance.list.set(index, value);
        }

        private boolean checkInt(Object obj) {
            if (!(obj instanceof Double)) return false;
            double n = (double) obj;
            if (n % 1 != 0) return false;
            return true;
        }
        
    }

    class add implements MerlinCallable {

        private final MerlinListInstance instance;

        public add(MerlinListInstance instance) {
            this.instance = instance;
        }

        @Override
        public int arity() {
            return -256;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token paren) {
            for (Object arg : arguments) instance.list.add(arg);
            return instance;
        }

    }

    class pop implements MerlinCallable {
        private final MerlinListInstance instance;

        public pop(MerlinListInstance instance) {
            this.instance = instance;
        }

        @Override
        public int arity() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments, Token bracket) {
            if (instance.list.isEmpty()) throw new RuntimeError(bracket, "List is empty");
            Object result = instance.list.get(instance.list.size() - 1);
            instance.list.remove(instance.list.size() - 1);
            return result;
        }

        

    }
}
