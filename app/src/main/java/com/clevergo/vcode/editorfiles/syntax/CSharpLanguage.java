package com.clevergo.vcode.editorfiles.syntax;

import java.util.regex.Pattern;

public class CSharpLanguage {
    private static final Pattern PATTERN_KEYWORDS = Pattern.compile("\\b(abstract|as|base|bool" +
            "break|byte|case|catch" +
            "char|checked|class|const" +
            "continue|decimal|default|delegate" +
            "do|double|else|enum" +
            "event|explicit|extern|false" +
            "finally|fixed|float|for" +
            "foreach|goto|if|implicit" +
            "in|int|interface" +
            "internal|is|lock|long" +
            "namespace|new|null|object" +
            "operator|out|override" +
            "params|private|protected|public" +
            "readonly|ref|return|sbyte" +
            "sealed|short|sizeof|stackalloc" +
            "static|string|struct|switch" +
            "this|throw|true|try" +
            "typeof|uint|ulong|unchecked" +
            "unsafe|ushort|using|using static" +
            "void|volatile|while)\\b");

    private static final Pattern PATTERN_CLASSES = Pattern.compile("");
    private static final Pattern PATTERN_BUILTINS = Pattern.compile("[,:;[->]{}()]");
    private static final Pattern PATTERN_SINGLE_LINE_COMMENT = Pattern.compile("//[^\\n]*");
    private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("\\.[a-zA-Z0-9_]+");
    private static final Pattern PATTERN_OPERATION = Pattern.compile(":|==|>|<|!=|>=|<=|->|=|>|<|%|-|-=|%=|\\+|\\-|\\-=|\\+=|\\^|\\&|\\|::|\\?|\\*");
    private static final Pattern PATTERN_GENERIC = Pattern.compile("<[a-zA-Z0-9,<>]+>");
    private static final Pattern PATTERN_TODO_COMMENT = Pattern.compile("//TODO[^\n]*");
    private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
    private static final Pattern PATTERN_CHAR = Pattern.compile("'[a-zA-Z]'");
    private static final Pattern PATTERN_STRING = Pattern.compile("\".*\"");
    private static final Pattern PATTERN_HEX = Pattern.compile("0x[0-9a-fA-F]+");
}
