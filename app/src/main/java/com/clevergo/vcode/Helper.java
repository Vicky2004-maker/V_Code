package com.clevergo.vcode;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Helper {

    public static final int PICK_FILE_CODE = 100;
    public static final int PERMISSION_REQ_CODE = 7;
    public static final int CREATE_FILE_CODE = 1;
    public static final int CHOOSE_DIRECTORY_NORMAL = 2;
    public static final int CHOOSE_DIRECTORY_PDF = 3;
    public static final int CREATE_FILE_PDF_CODE = 4;
    public static final int CREATE_FILE_NORMAL_CODE = 5;
    public static final String ALL_FILES_MIME = "*/*";
    public static final String PDF_MIME = "application/pdf";

    public static final int CODE_HIGHLIGHTER_MAX_LINES = 1000;

    public static final Uri PRIVACY_POLICY_URL = Uri.parse("https://clever-go.web.app/privacy-policy-codeviewer.html");

    public static final String[] PERMISSIONS =
            {"android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE"};
    public static final String[] COMPILER_FILENAMES = {"InputJavaClass.java"};

    public static final Handler uiHandler = new Handler(Looper.getMainLooper());
    private static final String SETTING_DELIMITER = "-";
    private static final int WRAP_LINE_LIMIT = 65;
    public static boolean isFullScreen = false;
    public static boolean thisIsMobile = true;
    public static HashMap<String, String> settingsMap;
    private static File settingsFile;
    private static BufferedWriter bufferedWriter;

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

    public static void initializeFile(AppCompatActivity activity) {
        settingsFile = new File(activity.getExternalFilesDir("Settings"), "settings.txt");
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

            bufferedWriter.append(key + SETTING_DELIMITER + value);

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
                bufferedWriter.append(entry.getKey().toString() + SETTING_DELIMITER + entry.getValue().toString());
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

    public static void getAllMethods(HashMap<String, Integer> toAdd, String code) {
        String[] lines = code.split("\n");
        Pattern pattern = Pattern.compile("(?s)(String|byte|void|int|float|double|long|short|byte\\[\\]|String\\[\\]|int\\[\\]|float\\[\\]|double\\[\\]|long\\[\\]|short\\[\\]) (\\w|\\d|\\w\\d)+(\\((\\w|,|\\d|\\w\\d|\\w,|\\d,|\\w\\d,|\\s|\\[\\])+\\))(?s)");
        Matcher matcher;
        for (int i = 0; i < lines.length; i++) {
            matcher = pattern.matcher(lines[i]);
            if (matcher.find()) toAdd.put(lines[i].replace("{", ""), i + 1);
        }
    }

    public static HashMap<String, Integer> getAllMethods(String code) {
        HashMap<String, Integer> methods = new HashMap<>();
        String[] lines = code.split("\n");
        Pattern pattern = Pattern.compile("(?s)(String|byte|void|int|float|double|long|short|byte\\[\\]|String\\[\\]|int\\[\\]|float\\[\\]|double\\[\\]|long\\[\\]|short\\[\\]) (\\w|\\d|\\w\\d)+(\\((\\w|,|\\d|\\w\\d|\\w,|\\d,|\\w\\d,|\\s|\\[\\])+\\))(?s)");
        Matcher matcher;
        for (int i = 0; i < lines.length; i++) {
            matcher = pattern.matcher(lines[i]);
            if (matcher.find()) methods.put(lines[i].replace("{", ""), i + 1);
        }

        return methods;
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

        for (String str : code.split("\r\n|\r|\n")) {
            toReturn++;
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

    public static void makeFullScreen(AppCompatActivity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(activity.getSupportActionBar()).hide();
        isFullScreen = true;
    }

    public static void revertFullScreen(AppCompatActivity activity) {
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
