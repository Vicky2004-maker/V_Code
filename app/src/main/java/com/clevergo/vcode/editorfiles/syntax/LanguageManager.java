package com.clevergo.vcode.editorfiles.syntax;

import android.content.Context;

import com.clevergo.vcode.editorfiles.Code;
import com.clevergo.vcode.editorfiles.CodeView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LanguageManager {

    private final Context context;
    private final CodeView codeView;

    public LanguageManager(Context context, CodeView codeView) {
        this.context = context;
        this.codeView = codeView;
    }

    public void applyTheme(LanguageName language, ThemeName theme) {
        switch (theme) {
            case MONOKAI:
                applyMonokaiTheme(language);
                break;
            case NOCTIS_WHITE:
                applyNoctisWhiteTheme(language);
                break;
            case FIVE_COLOR:
                applyFiveColorsDarkTheme(language);
                break;
            case ORANGE_BOX:
                applyOrangeBoxTheme(language);
                break;
        }
    }

    public String[] getLanguageKeywords(LanguageName language) {
        switch (language) {
            case JAVA:
                return JavaLanguage.getKeywords(context);
            case PYTHON:
                return PythonLanguage.getKeywords(context);
            case GO_LANG:
                return GoLanguage.getKeywords(context);
            case JAVASCRIPT:
                return null;
            case CPP:
                return null;
            case C_SHARP:
                return null;
            case HTML:
                return null;
            case CSS:
                return null;
            default:
                return new String[]{};
        }
    }

    public List<Code> getLanguageCodeList(LanguageName language) {
        switch (language) {
            case JAVA:
                return JavaLanguage.getCodeList(context);
            case PYTHON:
                return PythonLanguage.getCodeList(context);
            case GO_LANG:
                return GoLanguage.getCodeList(context);
            case JAVASCRIPT:
                return null;
            case CPP:
                return null;
            case C_SHARP:
                return null;
            case HTML:
                return null;
            case CSS:
                return null;
            default:
                return new ArrayList<>();
        }
    }

    public Set<Character> getLanguageIndentationStarts(LanguageName language) {
        switch (language) {
            case JAVA:
                return JavaLanguage.getIndentationStarts();
            case PYTHON:
                return PythonLanguage.getIndentationStarts();
            case GO_LANG:
                return GoLanguage.getIndentationStarts();
            case JAVASCRIPT:
                return null;
            case CPP:
                return null;
            case C_SHARP:
                return null;
            case HTML:
                return null;
            case CSS:
                return null;
            default:
                return new HashSet<>();
        }
    }

    public Set<Character> getLanguageIndentationEnds(LanguageName language) {
        switch (language) {
            case JAVA:
                return JavaLanguage.getIndentationEnds();
            case PYTHON:
                return PythonLanguage.getIndentationEnds();
            case GO_LANG:
                return GoLanguage.getIndentationEnds();
            case JAVASCRIPT:
                return null;
            case CPP:
                return null;
            case C_SHARP:
                return null;
            case HTML:
                return null;
            case CSS:
                return null;
            default:
                return new HashSet<>();
        }
    }

    public String getCommentStart(LanguageName language) {
        switch (language) {
            case JAVA:
                return JavaLanguage.getCommentStart();
            case PYTHON:
                return PythonLanguage.getCommentStart();
            case GO_LANG:
                return GoLanguage.getCommentStart();
            case JAVASCRIPT:
                return null;
            case CPP:
                return null;
            case C_SHARP:
                return null;
            case HTML:
                return null;
            case CSS:
                return null;
            default:
                return "";
        }
    }

    public String getCommentEnd(LanguageName language) {
        switch (language) {
            case JAVA:
                return JavaLanguage.getCommentEnd();
            case PYTHON:
                return PythonLanguage.getCommentEnd();
            case GO_LANG:
                return GoLanguage.getCommentEnd();
            case JAVASCRIPT:
                return null;
            case CPP:
                return null;
            case C_SHARP:
                return null;
            case HTML:
                return null;
            case CSS:
                return null;
            default:
                return "";
        }
    }

    private void applyMonokaiTheme(LanguageName language) {
        switch (language) {
            case JAVA:
                JavaLanguage.applyMonokaiTheme(context, codeView);
                break;
            case PYTHON:
                PythonLanguage.applyMonokaiTheme(context, codeView);
                break;
            case GO_LANG:
                GoLanguage.applyMonokaiTheme(context, codeView);
                break;
            case JAVASCRIPT:
                break;
            case CPP:
                break;
            case C_SHARP:
                break;
            case HTML:
                break;
            case CSS:
                break;
        }
    }

    private void applyNoctisWhiteTheme(LanguageName language) {
        switch (language) {
            case JAVA:
                JavaLanguage.applyNoctisWhiteTheme(context, codeView);
                break;
            case PYTHON:
                PythonLanguage.applyNoctisWhiteTheme(context, codeView);
                break;
            case GO_LANG:
                GoLanguage.applyNoctisWhiteTheme(context, codeView);
                break;
            case JAVASCRIPT:
                break;
            case CPP:
                break;
            case C_SHARP:
                break;
            case HTML:
                break;
            case CSS:
                break;
        }
    }

    private void applyFiveColorsDarkTheme(LanguageName language) {
        switch (language) {
            case JAVA:
                JavaLanguage.applyFiveColorsDarkTheme(context, codeView);
                break;
            case PYTHON:
                PythonLanguage.applyFiveColorsDarkTheme(context, codeView);
                break;
            case GO_LANG:
                GoLanguage.applyFiveColorsDarkTheme(context, codeView);
                break;
            case JAVASCRIPT:
                break;
            case CPP:
                break;
            case C_SHARP:
                break;
            case HTML:
                break;
            case CSS:
                break;
        }
    }

    private void applyOrangeBoxTheme(LanguageName language) {
        switch (language) {
            case JAVA:
                JavaLanguage.applyOrangeBoxTheme(context, codeView);
                break;
            case PYTHON:
                PythonLanguage.applyOrangeBoxTheme(context, codeView);
                break;
            case GO_LANG:
                GoLanguage.applyOrangeBoxTheme(context, codeView);
                break;
            case JAVASCRIPT:
                break;
            case CPP:
                break;
            case C_SHARP:
                break;
            case HTML:
                break;
            case CSS:
                break;
        }
    }

}
