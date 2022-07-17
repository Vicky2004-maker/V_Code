package com.clevergo.vcode.regex;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
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
    public static HashMap<String, Integer> getAllVariables(String code, boolean organize) {
        HashMap<String, Integer> variables = new HashMap<>();
        String[] lines = code.split("\n");
        Pattern pattern = Pattern.compile("((public|private|protected|synchronized)?( )?(static)?( )?(int|long|short|double|Double|float|void|Float|boolean|String|byte|int\\[\\]|long\\[\\]|short\\[\\]|void\\[\\]|String\\[\\]|byte\\[\\]|[A-Z]+\\w+|\\w+<\\w+>|\\w+<\\w+, \\w+>)) (\\w+\\()([a-zA-Z0-9 ,<>@_\"\"()]+\\)|\\))");
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

    private static String getBrackets(String code) {
        Pattern pattern = Pattern.compile("[{(<\\[]");
        StringBuilder sb = new StringBuilder();
        Matcher matcher = pattern.matcher(code);
        matcher.find();
        sb.append(matcher.group(0));

        pattern = Pattern.compile("[\\])>}]");
        matcher = pattern.matcher(code);
        matcher.find();
        sb.append(matcher.group(0));

        return sb.toString();
    }

    //TODO : Run and test isBracketsBalanced method
    public static int getBracketsErrors(String code) {
        int errors = 0;
        //String code = getBrackets(inputCode);
        Deque<Character> stack = new ArrayDeque<>();

        for (long i = 0; i < code.length(); i++) {
            char x = code.charAt((int) i);

            if (x == '(' || x == '[' || x == '{' || x == '<') {
                stack.push(x);
                continue;
            }

            if (stack.isEmpty())
                return errors;

            char check;
            switch (x) {
                case ')':
                    check = stack.pop();
                    if (check == '{' || check == '[' || check == '<')
                        errors++;
                    break;

                case '}':
                    check = stack.pop();
                    if (check == '(' || check == '[' || check == '<')
                        errors++;
                    break;

                case ']':
                    check = stack.pop();
                    if (check == '(' || check == '{' || check == '<')
                        errors++;
                    break;
                case '>':
                    check = stack.pop();
                    if (check == '(' || check == '{' || check == '[')
                        errors++;
                    break;
            }
        }
        return errors;
    }

    //TODO: Remove comments and lines ending with '{' and then check those lines for semicolon
    public static int getSemiColonErrors(String code) {
        int errors = 0;
        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String trimmedLine = lines[i].trim();
            if (trimmedLine.length() > 0) {
                char x = trimmedLine.charAt(trimmedLine.length() - 1);
                if (x != '{') {
                    if (x != ';') errors++;
                }
            }
        }

        return errors;
    }
}
