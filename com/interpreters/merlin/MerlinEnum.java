package com.interpreters.merlin;

import java.util.HashMap;
import java.util.List;

public class MerlinEnum extends MerlinInstance {

    public MerlinEnum(List<Token> consts) {
        super(new MerlinClass("Enum", null, new HashMap<>(), null));
        int i = 0;
        for (Token const_ : consts) {
            set(const_.lexeme, i++);
        }
    }

    @Override
    public String toString() {
        return "<MerlinEnum class>";
    }
    
}
