package com.clevergo.vcode.editorfiles;

import java.util.List;

/**
 * Interface used to support find and match features
 *
 * @since 1.2.1
 */
public interface Findable {

    /**
     * Find all the the tokens that matches the regex string and save them on a list
     *
     * @param regex The regex used to find tokens
     * @return List of the matches Tokens
     */
    List<Token> findMatches(String regex);

    /**
     * Highlight and return the next token
     *
     * @return The next matched token, {@code null} if not found
     */
    Token findNextMatch();

    /**
     * Highlight and return the previous token
     *
     * @return The previous matched token, {@code null} if not found
     */
    Token findPrevMatch();

    /**
     * Clear all the matches tokens
     */
    void clearMatches();
}
