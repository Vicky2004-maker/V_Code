package com.clevergo.vcode.editorfiles;

public class Token {

    private final int start;
    private final int end;

    public Token(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * @return The start position of the current token in source code
     */
    public int getStart() {
        return start;
    }

    /**
     * @return The end position of the current token in source code
     */
    public int getEnd() {
        return end;
    }
}
