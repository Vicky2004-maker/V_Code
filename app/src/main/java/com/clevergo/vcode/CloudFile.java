/*
 * Created by Viknesh on 2022.
 * Copyright (c) 2022. All rights reserved.
 */

package com.clevergo.vcode;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CloudFile {
    private final Context context;
    private final int UploadSizeLimit;
    private String FileName;
    private String MailID;
    private long FileSize;
    private long CreatedTime_Millis;
    private long UpdatedTime_Millis;
    private Uri DownloadURL;

    public CloudFile(Context context, String fileName, long fileSize, long createdTime_Millis, long updatedTime_Millis, Uri downloadURL, int uploadSizeLimit, String mail_id) {
        this.context = context;
        FileName = fileName;
        FileSize = fileSize;
        CreatedTime_Millis = createdTime_Millis;
        UpdatedTime_Millis = updatedTime_Millis;
        DownloadURL = downloadURL;
        UploadSizeLimit = uploadSizeLimit;
        MailID = mail_id;
    }

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        FileName = fileName;
    }

    public long getFileSize() {
        return FileSize;
    }

    public void setFileSize(long fileSize) {
        FileSize = fileSize;
    }

    public long getCreatedTime_Millis() {
        return CreatedTime_Millis;
    }

    public void setCreatedTime_Millis(long createdTime_Millis) {
        CreatedTime_Millis = createdTime_Millis;
    }

    public long getUpdatedTime_Millis() {
        return UpdatedTime_Millis;
    }

    public void setUpdatedTime_Millis(long updatedTime_Millis) {
        UpdatedTime_Millis = updatedTime_Millis;
    }

    public Uri getDownloadURL() {
        return DownloadURL;
    }

    public void setDownloadURL(Uri downloadURL) {
        DownloadURL = downloadURL;
    }

    private String convertFileSizeTo_Auto(long bytes) {
        float temp = Float.parseFloat(String.valueOf(bytes)) / 1024f;
        return temp > 1024f ? String.format(Locale.getDefault(), "%4.2f MB", temp / 1024f) : String.format(Locale.getDefault(), "%4.2f KB", temp);
    }

    private String convertMillisToDateTime(long milliseconds) {
        DateFormat formatter = DateFormat.getInstance();
        formatter.setTimeZone(TimeZone.getDefault());

        return formatter.format(new Date(milliseconds));
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString() {
        return context.getString(R.string.name) + " " + this.FileName + "\n" +
                context.getString(R.string.size) + " " + convertFileSizeTo_Auto(this.FileSize) + "\n" +
                context.getString(R.string.created_time) + " " + convertMillisToDateTime(CreatedTime_Millis) + "\n" +
                context.getString(R.string.lastUpdated_time) + " " + convertMillisToDateTime(UpdatedTime_Millis) + "\n" +
                context.getString(R.string.uploaded_from) + " " + MailID;
    }

    public int getUploadSizeLimit() {
        return UploadSizeLimit;
    }

    public String getMailID() {
        return MailID;
    }

    public void setMailID(String mailID) {
        MailID = mailID;
    }
}
