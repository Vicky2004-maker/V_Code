package com.clevergo.vcode.editorfiles;

/**
 * Interface to represent a different types of code such as keywords or snippets
 *
 * @since 1.1.0
 */
public interface Code {

    /**
     * @return The title of code
     */
    String getCodeTitle();

    /**
     * @return The prefix value of the code
     */
    String getCodePrefix();

    /**
     * @return The body of the code to insert it
     */
    String getCodeBody();
}
