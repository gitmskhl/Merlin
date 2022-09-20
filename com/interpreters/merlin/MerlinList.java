package com.interpreters.merlin;

import java.util.ArrayList;
import java.util.List;

public class MerlinList implements MerlinLenable {

    private final Token bracket;
    private final List<Object> list;

    public MerlinList(List<Object> list, Token bracket) {
        this.list = list;
        this.bracket = bracket;
    }

    public int size() {
        return list.size();
    }

    public void set(Object index, Object value, Token bracket) {
        if (index instanceof Double) setDouble((double) index, value, bracket);
        else if (index instanceof MerlinList) setList((MerlinList) index, value, bracket);
        else throw new RuntimeError(bracket, "Index must be an integer.");
    }

    private void setDouble(double n, Object value, Token bracket) {
        if (n % 1 != 0) throw new RuntimeError(bracket, "Index must be an integer.");
        int index = (int)n;
        setIndex(index, value, bracket);
    }

    private void setList(MerlinList indexes, Object value, Token bracket) {
        if (value instanceof MerlinList) {
            MerlinList lst = (MerlinList) value;

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
        if (index < 0 || index >= list.size()) throw new RuntimeError(bracket, "List index out of range.");
        list.set(index, value);
    }

    public Object get(Object index, Token bracket) {
        if (index instanceof Double) return getDouble((double) index, bracket);
        if (index instanceof MerlinList) return getList((MerlinList) index, bracket);
        throw new RuntimeError(bracket, "Index must be an integer.");
    }

    private Object getDouble(double n, Token bracket) {
        if (n % 1 != 0) throw new RuntimeError(bracket, "Index must be an integer.");
        int index = (int)n;
        return getIndex(index);
    }

    private Object getIndex(int index) {
        if (index < 0 || index >= list.size()) throw new RuntimeError(bracket, "List index out of range.");
        return list.get(index);
    }

    private Object getList(MerlinList indexes, Token bracket) {
        List<Object> lst = new ArrayList<>();
        int i = 0;
        for (Object obj : indexes.list) {
            if (!checkInt(obj)) throw new RuntimeError(bracket, "The list argument(position " + i + " in the list) must be an integer.");
            lst.add(getIndex((int)((double)obj)));
            i += 1;
        }
        return new MerlinList(lst, null);
    }

    private boolean checkInt(Object obj) {
        if (!(obj instanceof Double)) return false;
        double n = (double) obj;
        if (n % 1 != 0) return false;
        return true;
    }

    @Override
    public String toString() {
        String result = "[";
        if (!list.isEmpty()) {
            for (Object obj : list.subList(0, list.size() - 1)) result += Interpreter.stringify(obj) + ", ";
            result += Interpreter.stringify(list.get(list.size() - 1));
        }
        return result + "]";
    }

}
