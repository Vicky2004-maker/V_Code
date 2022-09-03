package com.clevergo.vcode.regex;

import android.util.Log;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaManager {
    //HashMap<METHOD_LINE, LINE_NUMBER>
    public static HashMap<String, Integer> getAllMethodsLines(String code, boolean organize) {
        HashMap<String, Integer> methods = new HashMap<>();
        String[] lines = code.split("\n");
        Pattern pattern = Pattern.compile("((public|private|protected|synchronized)?( )?(static)?( )?(int|long|short|double|Double|float|void|Float|boolean|String|byte|int\\[\\]|long\\[\\]|short\\[\\]|void\\[\\]|String\\[\\]|byte\\[\\]|[A-Z]+\\w+|\\w+<\\w+>|\\w+<\\w+, \\w+>)) (\\w+\\()([a-zA-Z0-9 ,<>@_\"\"()]+\\)|\\))");
        Matcher matcher;
        StringBuilder stringBuilder;
        for (int i = 0; i < lines.length; i++) {
            matcher = pattern.matcher(lines[i]);
            if (matcher.find()) {
                if (organize) {
                    stringBuilder = new StringBuilder();
                    if (matcher.group(2) != null)
                        stringBuilder.append("Modifier: ").append(matcher.group(2)).append("\n");
                    stringBuilder.append("isStatic?: ").append(matcher.group(4) != null).append("\n");
                    stringBuilder.append("Return type: ").append(matcher.group(6)).append("\n");
                    stringBuilder.append("Name: ").append(Objects.requireNonNull(matcher.group(7)).replace("(", "")).append("\n");

                    String preParameter = Objects.requireNonNull(matcher.group(8)).replace(")", "");
                    String[] parameters = preParameter.isEmpty() ? null : preParameter.split("\\,");
                    if (parameters != null) {
                        stringBuilder.append("Arguments: ");
                        for (int j = 0; j < parameters.length; j++) {
                            if (j == 0) {
                                stringBuilder.append(parameters[j].trim()).append("\n");
                            } else {
                                stringBuilder.append("\t").append(parameters[j].trim()).append("\n");
                            }
                        }
                    } else {
                        stringBuilder.append("Arguments: N/A");
                    }
                    methods.put(stringBuilder.toString(), i + 1);
                } else {
                    methods.put(matcher.group(0), i + 1);
                }
            }
        }

        return methods;
    }

    //TODO: Complete RegEx for variables, classes, constructors
    public static HashMap<String, Integer> getAllNormalVariables(String code, boolean organize) {
        HashMap<String, Integer> variables = new HashMap<>();
        String[] lines = code.split("\n");
        Pattern pattern = Pattern.compile("((public |private )(static )?(final )?([\\w<>.,\\[\\]]+ )([\\w\\d]+)(\\s)?(;)?(=)?(\\s)?([\\w()<>,=\\s\\d-]+)(;)?) (\\w+\\()([a-zA-Z0-9 ,<>@_\"\"()]+\\)|\\))");
        Matcher matcher;
        StringBuilder stringBuilder;

        for (int i = 0; i < lines.length; i++) {
            matcher = pattern.matcher(lines[i]);
            if (matcher.find()) {
                if (organize) {

                } else {

                }
            } else {

            }
        }

        return null;
    }

    //TODO : Complete these methods
    public static HashMap<String, Integer> getAllImports(String code, boolean organize) {
        HashMap<String,Integer> allImports = new HashMap<>();
        int beginIndex = Pattern.compile("(public class)").matcher(code).regionStart();
        code = code.substring(0, beginIndex);
        Pattern pattern = Pattern.compile("((import )(static )?([\\w.*]+;))");
        Matcher matcher = pattern.matcher(code);
        if(matcher.find())
        Log.e("Code ", matcher.group());

        return null;
    }

    public static HashMap<String, Integer> getAllConstructor(String code, boolean organize) {
        HashMap<String, Integer> variables = new HashMap<>();
        String[] lines = code.split("\n");
        Pattern pattern = Pattern.compile("((public|private|protected|synchronized)?( )?(static)?( )?(int|long|short|double|Double|float|void|Float|boolean|String|byte|int\\[\\]|long\\[\\]|short\\[\\]|void\\[\\]|String\\[\\]|byte\\[\\]|[A-Z]+\\w+|\\w+<\\w+>|\\w+<\\w+, \\w+>)) (\\w+\\()([a-zA-Z0-9 ,<>@_\"\"()]+\\)|\\))");
        Matcher matcher;
        StringBuilder stringBuilder;
        String className;
        for (int i = 0; i < lines.length; i++) {
            matcher = pattern.matcher(lines[i]);
            if (matcher.find()) {
                if (organize) {

                } else {

                }
            } else {

            }
        }

        return null;
    }

    public static int getBracketsErrors(String code) {
        code = code.replaceAll("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/", "");
        code = code.replaceAll("//[^\\n]*", "");
        code = code.replaceAll("//TODO[^\n]*", "");
        code = code.replaceAll("//FIXME[^\n]*", "");
        code = code.replaceAll("[^\\[\\]{}<>()]", "");
        Log.e("Brackets ", code);
        int errors = 0;
        Deque<Character> deque = new LinkedList<>();
        for (char ch : code.toCharArray()) {
            if (ch == '{' || ch == '[' || ch == '(') {
                deque.addFirst(ch);
            } else {
                if (!deque.isEmpty()
                        && ((deque.peekFirst() == '{' && ch == '}')
                        || (deque.peekFirst() == '[' && ch == ']')
                        || (deque.peekFirst() == '(' && ch == ')'))) {
                    deque.removeFirst();
                } else {
                    errors++;
                }
            }
        }
        return errors;
    }

    public static int getSemiColonErrors(String code) {
        code = code.replaceAll("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/", "");
        code = code.replaceAll("//[^\\n]*", "");
        code = code.replaceAll("@.[a-zA-Z0-9]+", "");
        code = code.replaceAll("//TODO[^\n]*", "");
        code = code.replaceAll("//FIXME[^\n]*", "");

        int errors = 0;
        String[] lines = code.split("\n");
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.length() > 0) {
                char x = trimmedLine.charAt(trimmedLine.length() - 1);
                if (x != ';') {
                    if (x != '{' && x != '}') {
                        errors++;
                    }
                }
            }
        }
        return errors;
    }
}

