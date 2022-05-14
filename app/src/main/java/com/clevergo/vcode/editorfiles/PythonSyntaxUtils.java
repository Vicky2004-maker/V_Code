package com.clevergo.vcode.editorfiles;

import android.content.Context;

import com.clevergo.vcode.R;

import java.util.regex.Pattern;

public class PythonSyntaxUtils {

    //Language Keywords
    private static final Pattern PATTERN_KEYWORDS = Pattern.compile("\\b(False|await|else|import|pass|None|break|except|in|raise" +
            "|True|class|finally|is|return|and|continue|for|lambda" +
            "|try|as|def|from|nonlocal|while|assert|del|global|not" +
            "|with|async|elif|if|or|yield)\\b");

    //Brackets and Colons
    private static final Pattern PATTERN_BUILTINS = Pattern.compile("[,:;[->]{}()]");

    //Data
    private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
    private static final Pattern PATTERN_CHAR = Pattern.compile("'[a-zA-Z]'");
    private static final Pattern PATTERN_STRING = Pattern.compile("\".*\"");
    private static final Pattern PATTERN_HEX = Pattern.compile("0x[0-9a-fA-F]+");
    private static final Pattern PATTERN_TODO_COMMENT = Pattern.compile("#TODO[^\n]*");
    private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("\\.[a-zA-Z0-9_]+");
    private static final Pattern PATTERN_OPERATION =Pattern.compile( ":|==|>|<|!=|>=|<=|->|=|>|<|%|-|-=|%=|\\+|\\-|\\-=|\\+=|\\^|\\&|\\|::|\\?|\\*");
    private static final Pattern PATTERN_HASH_COMMENT = Pattern.compile("#(?!TODO )[^\\n]*");

    public static void applyMonokaiTheme(Context context, CodeView codeView) {
        codeView.resetSyntaxPatternList();
        codeView.resetHighlighter();

        //View Background
        codeView.setBackgroundColor(codeView.getResources().getColor(R.color.background));

        //Syntax Colors
        codeView.addSyntaxPattern(PATTERN_HEX, context.getResources().getColor(R.color.purple));
        codeView.addSyntaxPattern(PATTERN_CHAR, context.getResources().getColor(R.color.green));
        codeView.addSyntaxPattern(PATTERN_STRING, context.getResources().getColor(R.color.orange));
        codeView.addSyntaxPattern(PATTERN_NUMBERS, context.getResources().getColor(R.color.purple));
        codeView.addSyntaxPattern(PATTERN_KEYWORDS, context.getResources().getColor(R.color.red));
        codeView.addSyntaxPattern(PATTERN_BUILTINS, context.getResources().getColor(R.color.white));
        codeView.addSyntaxPattern(PATTERN_HASH_COMMENT, context.getResources().getColor(R.color.grey));
        codeView.addSyntaxPattern(PATTERN_ATTRIBUTE, context.getResources().getColor(R.color.blue));
        codeView.addSyntaxPattern(PATTERN_OPERATION, context.getResources().getColor(R.color.red));
        //Default Color
        codeView.setTextColor( context.getResources().getColor(R.color.white));

        codeView.addSyntaxPattern(PATTERN_TODO_COMMENT, context.getResources().getColor(R.color.light_orange));

        codeView.reHighlightSyntax();
    }

    public static void applyNoctisWhiteTheme(Context context, CodeView codeView) {
        codeView.resetSyntaxPatternList();
        codeView.resetHighlighter();

        //View Background
        codeView.setBackgroundColor(codeView.getResources().getColor(R.color.noctis_white));

        //Syntax Colors
        codeView.addSyntaxPattern(PATTERN_HEX, context.getResources().getColor(R.color.noctis_purple));
        codeView.addSyntaxPattern(PATTERN_CHAR, context.getResources().getColor(R.color.noctis_green));
        codeView.addSyntaxPattern(PATTERN_STRING, context.getResources().getColor(R.color.noctis_green));
        codeView.addSyntaxPattern(PATTERN_NUMBERS, context.getResources().getColor(R.color.noctis_purple));
        codeView.addSyntaxPattern(PATTERN_KEYWORDS, context.getResources().getColor(R.color.noctis_pink));
        codeView.addSyntaxPattern(PATTERN_BUILTINS, context.getResources().getColor(R.color.noctis_dark_blue));
        codeView.addSyntaxPattern(PATTERN_HASH_COMMENT, context.getResources().getColor(R.color.noctis_grey));
        codeView.addSyntaxPattern(PATTERN_ATTRIBUTE, context.getResources().getColor(R.color.noctis_blue));
        codeView.addSyntaxPattern(PATTERN_OPERATION, context.getResources().getColor(R.color.red));
        //Default Color
        codeView.setTextColor( context.getResources().getColor(R.color.noctis_orange));

        codeView.addSyntaxPattern(PATTERN_TODO_COMMENT, context.getResources().getColor(R.color.light_orange));

        codeView.reHighlightSyntax();
    }

    public static void applyFiveColorsDarkTheme(Context context, CodeView codeView) {
        codeView.resetSyntaxPatternList();
        codeView.resetHighlighter();

        //View Background
        codeView.setBackgroundColor(codeView.getResources().getColor(R.color.five_dark_black));

        //Syntax Colors
        codeView.addSyntaxPattern(PATTERN_HEX, context.getResources().getColor(R.color.five_dark_purple));
        codeView.addSyntaxPattern(PATTERN_CHAR, context.getResources().getColor(R.color.five_dark_yellow));
        codeView.addSyntaxPattern(PATTERN_STRING, context.getResources().getColor(R.color.five_dark_yellow));
        codeView.addSyntaxPattern(PATTERN_NUMBERS, context.getResources().getColor(R.color.five_dark_purple));
        codeView.addSyntaxPattern(PATTERN_KEYWORDS, context.getResources().getColor(R.color.five_dark_purple));
        codeView.addSyntaxPattern(PATTERN_BUILTINS, context.getResources().getColor(R.color.five_dark_white));
        codeView.addSyntaxPattern(PATTERN_HASH_COMMENT, context.getResources().getColor(R.color.five_dark_grey));
        codeView.addSyntaxPattern(PATTERN_ATTRIBUTE, context.getResources().getColor(R.color.five_dark_blue));
        codeView.addSyntaxPattern(PATTERN_OPERATION, context.getResources().getColor(R.color.five_dark_purple));
        //Default Color
        codeView.setTextColor( context.getResources().getColor(R.color.five_dark_white));

        codeView.addSyntaxPattern(PATTERN_TODO_COMMENT, context.getResources().getColor(R.color.light_orange));

        codeView.reHighlightSyntax();
    }

    public static void applyOrangeBoxTheme(Context context, CodeView codeView) {
        codeView.resetSyntaxPatternList();
        codeView.resetHighlighter();

        //View Background
        codeView.setBackgroundColor(codeView.getResources().getColor(R.color.orange_box_black));

        //Syntax Colors
        codeView.addSyntaxPattern(PATTERN_HEX, context.getResources().getColor(R.color.light_orange));
        codeView.addSyntaxPattern(PATTERN_CHAR, context.getResources().getColor(R.color.orange_box_orange2));
        codeView.addSyntaxPattern(PATTERN_STRING, context.getResources().getColor(R.color.orange_box_orange2));
        codeView.addSyntaxPattern(PATTERN_NUMBERS, context.getResources().getColor(R.color.five_dark_purple));
        codeView.addSyntaxPattern(PATTERN_KEYWORDS, context.getResources().getColor(R.color.orange_box_orange1));
        codeView.addSyntaxPattern(PATTERN_BUILTINS, context.getResources().getColor(R.color.orange_box_grey));
        codeView.addSyntaxPattern(PATTERN_HASH_COMMENT, context.getResources().getColor(R.color.orange_box_dark_grey));
        codeView.addSyntaxPattern(PATTERN_ATTRIBUTE, context.getResources().getColor(R.color.orange_box_orange3));
        codeView.addSyntaxPattern(PATTERN_OPERATION, context.getResources().getColor(R.color.light_orange));

        //Default Color
        codeView.setTextColor(context.getResources().getColor(R.color.five_dark_white));

        codeView.addSyntaxPattern(PATTERN_TODO_COMMENT, context.getResources().getColor(R.color.light_orange));

        codeView.reHighlightSyntax();
    }
}