package com.interpreters.merlin;

public interface Container {
    public void add(Object object);
    public Object get(Object object);
    public Object length();
    public Object isEmpty();
    public Object pop();
}
