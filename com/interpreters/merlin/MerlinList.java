package com.interpreters.merlin;

import java.util.ArrayList;
import java.util.List;

public class MerlinList {

    private final Token bracket;
    private final List<Object> list;

    public MerlinList(List<Object> list, Token bracket) {
        this.list = list;
        this.bracket = bracket;
    }

    public int size() {
        return list.size();
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
