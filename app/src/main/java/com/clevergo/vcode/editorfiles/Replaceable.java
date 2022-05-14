package com.clevergo.vcode.editorfiles;

/**
 * Interface used to support find and replacement feature
 *
 * @since 1.2.1
 */
public interface Replaceable {

    /**
     * Replace the first string that matched by the regex with new string
     * @param regex regex Regex used to find the first target string
     * @param replacement Text to replace that matched string by it
     */
    void replaceFirstMatch(String regex, String replacement);

    /**
     * Replace all strings that matched by the regex with new string
     * @param regex Regex used to find the target string
     * @param replacement Text to replace that matched string by it
     */
    void replaceAllMatches(String regex, String replacement);
}
