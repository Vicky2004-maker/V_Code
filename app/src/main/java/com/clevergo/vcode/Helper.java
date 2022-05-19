package com.clevergo.vcode;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class Helper {

    public static final int PICK_FILE_CODE = 100;
    public static final int PERMISSION_REQ_CODE = 7;

    public static final Uri PRIVACY_POLICY_URL = Uri.parse("https://clever-go.web.app/privacy-policy-codeviewer.html");

    public static final String[] PERMISSIONS =
            {"android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE"};
    private static final String SETTING_DELIMITER = "-";

    public static HashMap<String, String> settingsMap;
    private static File settingsFile;
    private static BufferedWriter bufferedWriter;

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

    public static void pickFile(AppCompatActivity activity) {
        Intent filePicker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        filePicker.addCategory(Intent.CATEGORY_OPENABLE);
        filePicker.setType("*/*");

        activity.startActivityForResult(filePicker, PICK_FILE_CODE);
    }

    public static void launchUrlInBrowser(final Uri URL, final AppCompatActivity activity) {
        activity.startActivity(new Intent(Intent.ACTION_VIEW, URL));
    }

    public static String readFile(Context context, Uri uri) {
        StringBuilder sb = new StringBuilder();

        try {
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

    public static void writeFile(Context context, Uri uri, String content) {
        ParcelFileDescriptor pfd;
        try {
            pfd = context.getContentResolver().openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(content.getBytes());
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

    public static String getFileExtension(Context context, Intent data) {
        @SuppressLint("Recycle") Cursor returnCursor = context.getContentResolver().query(data.getData(), null, null, null, null);
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

    public static String getFileExtension(final String fileName) {
        return fileName.substring(fileName.indexOf(".") + 1);
    }

    public static boolean checkPermissions(Context context) {
        return ActivityCompat.checkSelfPermission(context, PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
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
            Toast.makeText(context, context.getString(R.string.copySuccess), Toast.LENGTH_LONG).show();
        } catch (Exception exception) {
            Toast.makeText(context, context.getString(R.string.copyFalied), Toast.LENGTH_LONG).show();
        }
    }

    public static float getScreenWidth_DP(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels / displayMetrics.density;
    }

    public static float getScreenHeight_DP(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels / displayMetrics.density;
    }

    //TODO : Generate PDF
    //TODO : View PDF
    //TODO : Extract text from PDF and load it into CodeView

    public static class AsynchronousBehaviour {
        //TODO : Make Async methods, Read and Write
    }
}
