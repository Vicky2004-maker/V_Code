package com.clevergo.vcode;

import java.io.Serializable;
import java.net.URL;
import java.util.Objects;

public class CodeViewFile implements Serializable, Comparable<CodeViewFile> {
    public int File_ID;
    public double File_Size;
    public String Name;
    public String Uri;
    public String Language;
    public boolean isURL;
    public URL url;

    public CodeViewFile(int file_ID, double file_Size, String name, String uri, String language, boolean isURL, URL url) {
        File_ID = file_ID;
        File_Size = file_Size;
        Name = name;
        Uri = uri;
        Language = language;
        this.isURL = isURL;
        this.url = url;
    }

    public CodeViewFile(int file_ID, double file_Size, String name, String uri, String language, boolean isUrl) {
        File_ID = file_ID;
        File_Size = file_Size;
        Name = name;
        Uri = uri;
        Language = language;
        isURL = isUrl;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public int getFile_ID() {
        return File_ID;
    }

    public double getFile_Size() {
        return File_Size;
    }

    public String getName() {
        return Name;
    }

    public String getUri() {
        return Uri;
    }

    public String getLanguage() {
        return Language;
    }

    @Override
    public int compareTo(CodeViewFile codeViewFile) {
        if (Objects.equals(this.getUri(), codeViewFile.getUri())) {
            return 0;
        } else {
            return 1;
        }
    }
}
