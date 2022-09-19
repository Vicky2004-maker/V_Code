package com.clevergo.vcode.compiler;

import static com.clevergo.vcode.Helper.copyFile;
import static com.clevergo.vcode.Helper.getAllCompilerSDKs;
import static com.clevergo.vcode.Helper.getFileExtension;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.clevergo.vcode.CodeViewActivity;
import com.clevergo.vcode.Helper;

import org.eclipse.jdt.internal.compiler.batch.Main;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import dalvik.system.DexClassLoader;

public class JavaCompiler {
    private String FileName_WithoutExtension;
    private Context context;

    private String FileName;
    private Uri SourceFile_URI;
    public static OutputStream outputStream_normal;
    public static OutputStream outputStream_error;

    public JavaCompiler(Context context ,@NonNull String fileName, Uri src) {
        FileName = fileName;
        SourceFile_URI = src;
        this.context = context;

        FileName_WithoutExtension = Helper.getFileName_withoutExtension(fileName);

        outputStream_normal = new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        };

        outputStream_error = new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                super.write(b, off, len);
            }
        };


    }

    public void compile() {
        File javaCompile_sdk = getAllCompilerSDKs(getFileExtension(FileName));
        File tempFile = new File(String.valueOf(context.getExternalFilesDir("Compile Files/Java")), FileName);
        copyFile(context, SourceFile_URI, tempFile);

        Main ecjMain = new Main(new PrintWriter(System.out), new PrintWriter(System.err), false, null, null);
        ecjMain.compile(new String[]{"-classpath", javaCompile_sdk.getPath(), tempFile.getPath()});

        com.android.dx.command.Main.main(new String[]{"--dex", "--output=" + context.getExternalFilesDir("Compile Files/Zip").getPath() + "/"+ FileName_WithoutExtension + ".zip", context.getExternalFilesDir("Compile Files/Java").getPath() + "/./"+ FileName_WithoutExtension +".class"});

        DexClassLoader cl = new DexClassLoader(context.getExternalFilesDir("Compile Files/Zip").getPath() + "/"+ FileName_WithoutExtension + ".zip", context.getExternalFilesDir("Compile Files").getPath(), null, context.getClassLoader());

        try {
            Class libProviderClazz = cl.loadClass(FileName_WithoutExtension);
            Object instance = libProviderClazz.newInstance();
            instance.getClass().getDeclaredMethod("main", String[].class).invoke(null, (Object) new String[0]);
        } catch (Exception e) {
            Log.println(Log.ASSERT, "ERROR INIT", "Error while instantiating object: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
