package com.clevergo.vcode;

import static com.clevergo.vcode.CodeViewActivity.signInClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import com.clevergo.vcode.editorfiles.CodeView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Helper {

    public static final int CREATE_FILE_CODE = 1;
    public static final int CHOOSE_DIRECTORY_NORMAL = 2;
    public static final int CHOOSE_DIRECTORY_PDF = 3;
    public static final int CREATE_FILE_PDF_CODE = 4;
    public static final int CREATE_FILE_NORMAL_CODE = 5;
    public static final int GOOGLE_SIGN_IN = 6;
    public static final int PERMISSION_REQ_CODE = 7;
    public static final int PICK_FILE_CODE = 100;
    public static final String ALL_FILES_MIME = "*/*";
    public static final String PDF_MIME = "application/pdf";
    public static final Uri PRIVACY_POLICY_URL = Uri.parse("https://clever-go.web.app/privacy-policy-codeviewer.html");
    public static final String[] PERMISSIONS =
            {"android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE"};
    public static final String[] COMPILER_FILENAMES = {"InputJavaClass.java"};
    public static final Handler uiHandler = new Handler(Looper.getMainLooper());
    private static final String SETTING_DELIMITER = "-";
    private static final int WRAP_LINE_LIMIT = 65;
    public static String COMPILER_SDKs_LOCATION = "";
    public static List<CloudFile> cloudFileList = new ArrayList<>();
    public static boolean isFullScreen = false;
    public static boolean thisIsMobile = true;
    public static HashMap<String, String> settingsMap;
    private static File settingsFile;
    private static BufferedWriter bufferedWriter;

    public static void initializeFile(AppCompatActivity activity) {
        settingsFile = new File(activity.getExternalFilesDir("Settings"), "settings.txt");
        COMPILER_SDKs_LOCATION = activity.getExternalFilesDir("Compiler SDKs").getPath();
    }

    public static int getDifference_progress(int totalCount) {
        return (int) (100f / Float.parseFloat(String.valueOf(totalCount)));
    }

    public static float getTotalCloudFilesSize_KB() {
        float total = 0;
        for (CloudFile file : cloudFileList) {
            total += Float.parseFloat(String.valueOf(file.getFileSize())) / 1024f;
        }

        return total;
    }

    public static float getTotalCloudFilesSize_MB() {
        float total = 0;
        for (CloudFile file : cloudFileList) {
            total += file.getFileSize();
        }

        return (total / 1024f) / 1024f;
    }

    public static void copyFile(Context context, Uri source, File destination) {
        try (InputStream in = context.getContentResolver().openInputStream(source)) {
            try (OutputStream out = new FileOutputStream(destination)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getAllCompilerSDKs(String fileExtension) {
        String child = null;
        switch (fileExtension) {
            case "java":
                child = "android_sdk.jar";
                break;
        }

        assert child != null;
        return new File(COMPILER_SDKs_LOCATION, child);
    }

    public static void createGoogleSignInClient(AppCompatActivity activity) {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getBaseContext().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        signInClient = GoogleSignIn.getClient(activity, googleSignInOptions);
        activity.startActivityForResult(new Intent(signInClient.getSignInIntent()), GOOGLE_SIGN_IN);
    }


    public static int findIndexFromListOfCodeView(List<CodeViewFile> codeViewFiles, String fileName) {
        int toReturn = -1;
        for (CodeViewFile codeFile : codeViewFiles) {
            toReturn++;

            if (codeFile.getName().equals(fileName)) {
                return toReturn;
            }
        }
        return toReturn;
    }

    public static void setThisIsMobile(Context context) {
        float yInches = getScreenWidth_DP(context);
        float xInches = getScreenHeight_DP(context);
        double diagonalInches = Math.sqrt(xInches * xInches + yInches * yInches);
        thisIsMobile = diagonalInches <= 6.5;
    }

    public static void updateSettingsMap() {
        settingsMap = new HashMap<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(settingsFile));
            String receiveString;

            while ((receiveString = br.readLine()) != null) {
                String[] keyValuePair = receiveString.split(SETTING_DELIMITER);
                settingsMap.put(keyValuePair[0].trim(), keyValuePair[1].trim());
            }

            br.close();
        } catch (IOException e) {

        }
    }

    public static void writeSetting(@NonNull String key, @NonNull String value) {
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(settingsFile));

            bufferedWriter.append(key).append(SETTING_DELIMITER).append(value);

            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeSetting(@NonNull String key, boolean value) {
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(settingsFile));

            bufferedWriter.append(key).append(SETTING_DELIMITER).append(String.valueOf(value));

            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeSetting(HashMap<Object, Object> map) {
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(settingsFile));

            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                bufferedWriter.append(entry.getKey().toString()).append(SETTING_DELIMITER).append(entry.getValue().toString());
                bufferedWriter.newLine();
            }

            bufferedWriter.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isLowerSDK() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.N;
    }

    public static CodeViewFile createACodeViewFile(Context context, Intent data, boolean isUrl) {
        return new CodeViewFile(CodeViewActivity.filesOpened,
                Double.parseDouble(getFileSize(context, data)),
                getFileName(context, data),
                data.getData().toString(),
                getFileExtension(context, data),
                isUrl);
    }

    public static CodeViewFile createACodeViewFile(Context context, Uri uri, boolean isUrl) {
        CodeViewFile file = null;

        if (isUrl) {
            try {
                file = new CodeViewFile(CodeViewActivity.filesOpened,
                        0d,
                        "N/A",
                        uri.toString(),
                        "N/A",
                        true,
                        new URL(uri.toString()));
            } catch (MalformedURLException ignored) {
            }
        } else {
            file = new CodeViewFile(CodeViewActivity.filesOpened,
                    Double.parseDouble(getFileSize(context, uri)),
                    getFileName(context, uri),
                    uri.toString(),
                    getFileExtension(context, uri),
                    false);
        }

        return file;
    }

    public static void pickFile(AppCompatActivity activity) {
        Intent filePicker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        filePicker.addCategory(Intent.CATEGORY_OPENABLE);
        filePicker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        filePicker.setType("*/*");

        activity.startActivityForResult(filePicker, PICK_FILE_CODE);
    }

    public static void launchUrlInBrowser(final Uri URL, final AppCompatActivity activity) {
        activity.startActivity(new Intent(Intent.ACTION_VIEW, URL));
    }

    public static String readFile(Context context, URL url) {
        StringBuilder sb = new StringBuilder();
        Thread t = new Thread(() -> {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                String receiveString;

                while ((receiveString = br.readLine()) != null) {
                    sb.append(receiveString).append("\n");
                }

                br.close();
            } catch (IOException e) {
                uiHandler.post(() -> Toast.makeText(context, context.getString(R.string.fileReadingFailed), Toast.LENGTH_SHORT).show());
            }
        });

        t.start();

        try {
            t.join();
        } catch (InterruptedException ignored) {
        }

        return sb.toString();
    }

    public static boolean isURL(String urlContent) {
        try {
            URL url = new URL(urlContent);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Returns the file name for a given URL string
     *
     * @param string The URL as a string
     * @return the file name
     */
    public static String getFileName(String string) {
        String[] syllabus = string.split("/");
        return syllabus[syllabus.length - 1];
    }

    public static String findProgrammingLanguage(final @NonNull String fileName) {
        String toReturn = null;
        switch (getFileExtension(fileName)) {
            case "java":
                toReturn = "JAVA";
                break;
            case "cs":
                toReturn = "C_SHARP";
                break;
            case "cpp":
                toReturn = "CPP";
                break;
            case "py":
                toReturn = "PYTHON";
                break;
            case "js":
                toReturn = "JAVASCRIPT";
                break;
            case "html":
            case "htm":
                toReturn = "HTML";
                break;
            case "css":
                toReturn = "CSS";
                break;
        }
        return toReturn;
    }

    public static String readFile(Context context, Uri uri) {
        StringBuilder sb = new StringBuilder();

        try {
            context.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            InputStream is = context.getContentResolver().openInputStream(uri);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String receiveString;

            while ((receiveString = br.readLine()) != null) {
                sb.append(receiveString).append("\n");
            }

            is.close();
        } catch (IOException e) {
            Toast.makeText(context, context.getString(R.string.fileReadingFailed), Toast.LENGTH_SHORT).show();
        }

        return sb.toString();
    }

    public static void createFile(Activity activity, String mime, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mime);
        intent.putExtra(Intent.EXTRA_TITLE, activity.getString(R.string.fileName_demo));

        activity.startActivityForResult(intent, requestCode);
    }

    public static void chooseDirectory(Activity activity, int reqCode) {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        activity.startActivityForResult(Intent.createChooser(i, "Choose directory"), reqCode);
    }

    public static void getAllMethodsLines_JAVA(HashMap<String, Integer> toAdd, String code) {
        String[] lines = code.split("\n");
        Pattern pattern = Pattern.compile("/(public|private|protected|synchronized|\\W) (\\W|\\w+) (\\w+\\<\\w+\\, \\w+\\>|\\w+\\<\\w+\\>|void|int|long|short|(D|d)ouble|(f|f)loat|\\w+|\\w+\\.\\w+) (\\w\\d|\\w)+\\((\\)|\\w+\\s\\w+\\,|\\w+\\<\\w+\\>|((\\s|\\S)\\w+\\s\\w+)|\\w+\\<\\w+\\, \\w+\\>)/gm");
        Matcher matcher;
        for (int i = 0; i < lines.length; i++) {
            matcher = pattern.matcher(lines[i]);
            if (matcher.find()) toAdd.put(lines[i].replace("{", ""), i + 1);
        }
    }

    public static int getCurrentColumn(String code, int selectionStart) {
        String subString = code.substring(0, selectionStart);
        String[] lines = subString.split("\\n");
        int toReturn;
        if (subString.length() == 0) {
            toReturn = 0;
        } else {
            toReturn = lines[lines.length - 1].length();
        }

        return toReturn + 1;
    }

    public static int getSelectedLineNumber(String code, int selectionStart) {
        String subString = code.substring(0, selectionStart);
        BufferedReader bufferedReader = new BufferedReader(new StringReader(subString));

        int lineNumber = 0;
        try {
            while (bufferedReader.readLine() != null) {
                lineNumber++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lineNumber;
    }

    public static void writeFile(Context context, Uri uri, final String content) {
        ParcelFileDescriptor pfd;
        try {
            pfd = context.getContentResolver().openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
            byte[] bytes = new byte[8 * 1024];
            bytes = content.getBytes();
            fileOutputStream.write(bytes);
            fileOutputStream.flush();
            fileOutputStream.close();
            pfd.close();
            Toast.makeText(context, context.getString(R.string.saved), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(context, context.getString(R.string.unableToSave), Toast.LENGTH_LONG).show();
        }
    }

    public static String getFileName(Context context, Intent data) {
        @SuppressLint("Recycle") Cursor returnCursor = context.getContentResolver().query(data.getData(), null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();

        return returnCursor.getString(nameIndex);
    }

    public static String getFileName(Context context, Uri uri) {
        @SuppressLint("Recycle") Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();

        return returnCursor.getString(nameIndex);
    }

    public static String getFileExtension(Context context, Intent data) {
        @SuppressLint("Recycle") Cursor returnCursor = context.getContentResolver().query(data.getData(), null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String fileName = returnCursor.getString(nameIndex);

        return fileName.substring(fileName.indexOf(".") + 1);
    }

    public static String getFileName_withoutExtension(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] tempSplit = fileName.split("\\.");
        for(int i = 0; i < tempSplit.length - 1; i++) {
            stringBuilder.append(tempSplit[i]).append(i == tempSplit.length - 2 ? "" : ".");
        }

        return stringBuilder.toString();
    }

    public static String getFileExtension(Context context, Uri uri) {
        @SuppressLint("Recycle") Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String fileName = returnCursor.getString(nameIndex);

        return fileName.substring(fileName.indexOf(".") + 1);
    }

    public static String getFileSize(Context context, Intent data) {
        @SuppressLint("Recycle") Cursor returnCursor = context.getContentResolver().query(data.getData(), null, null, null, null);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        return decimalFormat.format(returnCursor.getDouble(sizeIndex) / 1000);
    }

    public static String getFileSize(Context context, Uri uri) {
        @SuppressLint("Recycle") Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        return decimalFormat.format(returnCursor.getDouble(sizeIndex) / 1000);
    }

    public static String getFileExtension(final String fileName) {
        return fileName.substring(fileName.indexOf(".") + 1);
    }

    public static boolean checkPermissions(Context context) {
        int res_0 = context.checkCallingOrSelfPermission(PERMISSIONS[0]);
        int res_1 = context.checkCallingOrSelfPermission(PERMISSIONS[1]);

        return (res_0 == PackageManager.PERMISSION_GRANTED)
                && (res_1 == PackageManager.PERMISSION_GRANTED);
    }

    public static void launchPermission(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(PERMISSIONS, PERMISSION_REQ_CODE);
        }
    }

    public static int getLines(String code) {
        int toReturn = 0;

        BufferedReader bufferedReader = new BufferedReader(new StringReader(code));
        try {
            while (bufferedReader.readLine() != null) {
                toReturn++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public static void copyCode(Context context, String toCopy) {
        try {
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData cd = ClipData.newPlainText("Source Code", toCopy);
            cm.setPrimaryClip(cd);

            uiHandler.post(() -> Toast.makeText(context, context.getString(R.string.copySuccess), Toast.LENGTH_LONG).show());
        } catch (Exception exception) {
            uiHandler.post(() -> Toast.makeText(context, context.getString(R.string.copyFalied), Toast.LENGTH_LONG).show());
        }
    }

    public static double getTotalRAM_GB(Context context) {
        ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        return memInfo.totalMem / 1e9d;
    }

    public static void setHighLightedText(CodeView editor, String textToHighlight) {
        String tvt = editor.getText().toString();
        int ofe = tvt.indexOf(textToHighlight);
        Spannable wordToSpan = new SpannableString(editor.getText());
        for (int ofs = 0; ofs < tvt.length() && ofe != -1; ofs = ofe + 1) {
            ofe = tvt.indexOf(textToHighlight, ofs);
            if (ofe == -1)
                break;
            else {
                wordToSpan.setSpan(new BackgroundColorSpan(0xFFFFFF00), ofe, ofe + textToHighlight.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                editor.setText(wordToSpan, TextView.BufferType.SPANNABLE);
            }
        }
    }

    public static int getScreenWidth_Pixels(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static float getScreenWidth_DP(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels / displayMetrics.xdpi;
    }

    public static float getScreenHeight_DP(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels / displayMetrics.ydpi;
    }

    public static void makeFullScreen(AppCompatActivity activity, AppBarLayout appBarLayout, DrawerLayout drawerLayout) {
        appBarLayout.setVisibility(View.GONE);
        CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.MATCH_PARENT);
        layoutParams.topMargin = 0;
        drawerLayout.setLayoutParams(layoutParams);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(activity.getSupportActionBar()).hide();
        isFullScreen = true;
    }

    public static void revertFullScreen(AppCompatActivity activity, AppBarLayout appBarLayout, DrawerLayout drawerLayout) {
        appBarLayout.setVisibility(View.VISIBLE);
        CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.MATCH_PARENT);
        layoutParams.topMargin = (int) activity.getResources().getDimension(R.dimen.toolBarSize);
        drawerLayout.setLayoutParams(layoutParams);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(activity.getSupportActionBar()).show();
        isFullScreen = false;
    }

    public static boolean isScreenLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static TextWatcher validateRegex(AppCompatActivity activity, TextInputEditText editText) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    Pattern.compile(s.toString());
                } catch (PatternSyntaxException e) {
                    editText.setError(activity.getString(R.string.invalidPattern));
                }
            }
        };
    }

    public static void showAlertDialog(@NonNull String title, @NonNull String message, @NonNull AppCompatActivity activity) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(message);
        alertBuilder.setCancelable(false);
        alertBuilder.setPositiveButton(activity.getString(R.string.ok), (dialog, which) -> {
        });
        alertBuilder.create();
        alertBuilder.show();
    }

    public static boolean isPrivacyPolicyAccepted() {
        try {
            return Objects.equals(Helper.settingsMap.get("privacyPolicy"), "agree");
        } catch (NullPointerException e) {
            return false;
        }
    }

    public static void setBtnIcon(Context context, MaterialButton button, String fileExtension) {
        switch (fileExtension) {
            case "java":
                button.setIcon(AppCompatResources.getDrawable(context, R.drawable.java_logo));
                break;
            case "cs":
                break;
            case "cpp":
            case "h": {

                break;
            }
            case "js":
                button.setIcon(AppCompatResources.getDrawable(context, R.drawable.javascript_logo));
                break;
            case "py":
                button.setIcon(AppCompatResources.getDrawable(context, R.drawable.python_logo));
        }
    }

    public static void generatePDF(Context context, Uri uri, CodeViewFile file) {
        final int pageHeight = 842;
        final int pageWidth = 595;
        PdfDocument pdf = new PdfDocument();

        Paint paint = new Paint();
        Paint title = new Paint();
        Paint rectangle = new Paint();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page myPage = pdf.startPage(pageInfo);

        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Bitmap scaledBmp = Bitmap.createScaledBitmap(bmp, 40, 40, false);

        Canvas canvas = myPage.getCanvas();
        canvas.drawBitmap(scaledBmp, 50, 60, paint);

        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        title.setTextSize(17);
        title.setColor(Color.BLACK);
        canvas.drawText(file.getName(), 100, 80, title);

        rectangle.setColor(Color.rgb(239, 239, 245));
        RectF rectF = new RectF();
        rectF.left = 50;
        rectF.bottom = 792;
        rectF.right = 545;
        rectF.top = 100;
        canvas.drawRoundRect(rectF, 5, 5, rectangle);
        rectangle.setColor(Color.GRAY);
        rectF.right = 70;
        //canvas.drawLine(100, 70, 792, 70, rectangle);
        //canvas.drawRoundRect(rectF, 5, 5, rectangle);

        //canvas.drawRect(50, 140, 50, 50, rectangle);

        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        title.setTextSize(15);
        String[] content = readFile(context, Uri.parse(file.getUri())).split("\n");
        StringBuilder sb = new StringBuilder();

        for (String s : content) {
            String line = s;
            if (line.length() >= WRAP_LINE_LIMIT) {
                line = line.substring(0, WRAP_LINE_LIMIT) + "\n" + line.substring(WRAP_LINE_LIMIT);
            }

            sb.append(line).append("\n");
        }

        content = sb.toString().split("\n");
        for (int i = 0; i < content.length; i++) {
            int y = 150 + ((i - 1) * 20);
            canvas.drawText(String.valueOf(i), 52, y, title);
            canvas.drawText(content[i], 85, y, title);
        }

        pdf.finishPage(myPage);

        try {
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "w");
            pdf.writeTo(new FileOutputStream(pfd.getFileDescriptor()));
            pfd.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pdf.close();
        }
    }

    public static void viewPDF(Context context, Uri uri) {
        try {
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "w");
            PdfRenderer pdfRenderer = new PdfRenderer(pfd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO : Generate PDF
    //TODO : View PDF
    //TODO : Extract text from PDF and load it into CodeView

    public static class AsynchronousBehaviour {

    }
}
