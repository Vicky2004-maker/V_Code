package com.clevergo.vcode.compiler;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.clevergo.vcode.Helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JavaCompiler {

    private File customJavaFile;

    public JavaCompiler(Context context, String code, File file) {
        customJavaFile = file;

        Helper.writeFile(context, Uri.parse(file.getPath()), code);
        //Helper.writeToAssetFile(context, Helper.COMPILER_FILENAMES[0], code);
    }

    public void run() {
        try {
            runProcess("pwd");
            //System.out.println("**********");
            runProcess("javac -cp src src/com/journaldev/files/Test.java");
            //System.out.println("**********");
            runProcess("java -cp src com/journaldev/files/Test Hi Pankaj");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printLines(String cmd, InputStream ins) throws Exception {
        String line = null;
        BufferedReader in = new BufferedReader(
                new InputStreamReader(ins));
        while ((line = in.readLine()) != null) {
            Log.e("OUTPUT" ,cmd + " " + line);
        }
    }

    private static void runProcess(String command) throws Exception {
        Process pro = Runtime.getRuntime().exec(command);
        printLines(command + " stdout:", pro.getInputStream());
        printLines(command + " stderr:", pro.getErrorStream());
        pro.waitFor();
        Log.e("OUTPUT",command + " exitValue() " + pro.exitValue());
    }
}
