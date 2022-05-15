package com.clevergo.vcode.editorfiles;

import java.io.Serializable;

public class CodeViewFile implements Serializable {
    public int File_ID;
    public double File_Size;
    public String Name;
    public String Uri;
    public String Language;

    public CodeViewFile(int file_ID, double file_Size, String name, String uri, String language) {
        File_ID = file_ID;
        File_Size = file_Size;
        Name = name;
        Uri = uri;
        Language = language;
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
}
