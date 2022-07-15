package com.clevergo.vcode.regex;

import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaRegexManager {
    //HashMap<METHOD_LINE, LINE_NUMBER>
    public static HashMap<String, Integer> getAllMethodsLines_JAVA(String code, boolean organize) {
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
}
