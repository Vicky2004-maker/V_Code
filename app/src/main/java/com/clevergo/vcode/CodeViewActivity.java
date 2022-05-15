package com.clevergo.vcode;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.clevergo.vcode.codeviewer.CodeView;
import com.clevergo.vcode.codeviewer.Language;
import com.clevergo.vcode.codeviewer.Theme;
import com.clevergo.vcode.editorfiles.CodeViewFile;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CodeViewActivity extends AppCompatActivity implements CodeView.OnHighlightListener, WebView.FindListener, View.OnClickListener {

    private static ProgressDialog progressDialog;
    private static List<CodeViewFile> fileList = new ArrayList<>();
    private static List<String> codeList = new ArrayList<>();
    private static int filesOpened = 0;
    private LinearLayout allFileSwitcher_LinearLayout, info_LinearLayout;
    private ImageView bottomSheet_ImageView;
    private TextView pickFile_TextView;
    private CodeView codeView_Main;
    private boolean loadIntoRAM = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Helper.PICK_FILE_CODE && data != null) {
            pickFile_TextView.setVisibility(View.GONE);
            codeView_Main.setVisibility(View.VISIBLE);
            info_LinearLayout.setVisibility(View.VISIBLE);

            bottomSheet_ImageView.setTranslationX((info_LinearLayout.getWidth() >> 1) - 30);

            if (codeList.size() == 0) {
                addUI_File(data);
            } else {
                for (CodeViewFile codeFile : fileList) {
                    if (codeFile.getUri().equals(data.getData().toString())) {
                        Toast.makeText(CodeViewActivity.this, getString(R.string.fileAlreadyPicked), Toast.LENGTH_LONG).show();
                    } else {
                        addUI_File(data);
                        break;
                    }
                }
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_view);


        allFileSwitcher_LinearLayout = findViewById(R.id.allFileSwitcher);
        pickFile_TextView = findViewById(R.id.pickFileTextView);
        codeView_Main = findViewById(R.id.codeview_Main);
        info_LinearLayout = findViewById(R.id.info_LinearLayout);
        bottomSheet_ImageView = findViewById(R.id.bottomSheet_ImageView);
    }

    //region Private Methods

    private void setCodeView(String code) {
        codeView_Main.setOnHighlightListener(this)
                .setTheme(Theme.MONOKAI)
                .setCode(code)
                .setLanguage(Language.AUTO)
                .setWrapLine(false)
                .setShowLineNumber(true)
                .apply();
        codeView_Main.setFindListener(this);
    }

    private void addUI_File(Intent data) {
        fileList.add(new CodeViewFile(filesOpened,
                Double.parseDouble(Helper.getFileSize(CodeViewActivity.this, data)),
                Helper.getFileName(CodeViewActivity.this, data),
                data.getData().toString(),
                Helper.getFileExtension(CodeViewActivity.this, data)));

        if (loadIntoRAM) {
            codeList.add(Helper.readFile(CodeViewActivity.this, data.getData()));
            setCodeView(codeList.get(filesOpened));
        } else {
            setCodeView(Helper.readFile(CodeViewActivity.this, data.getData()));
        }

        MaterialButton materialButton = new MaterialButton(CodeViewActivity.this);
        materialButton.setText(fileList.get(filesOpened).getName());
        materialButton.setId(filesOpened);
        materialButton.setOnClickListener(CodeViewActivity.this);
        materialButton.setAllCaps(false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(5, 0, 5, 0);

        allFileSwitcher_LinearLayout.addView(materialButton, filesOpened, layoutParams);

        filesOpened++;
    }

    //endregion

    //region Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.codeviewer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        if(drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        */

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.addFile_menu:
                Helper.pickFile(CodeViewActivity.this);
                break;
            case R.id.settings_Menu:
                //TODO : Navigate to Setting Page
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }
    //endregion

    //region CodeView OnHighlightListener & OnFindListener & Button OnCLickListener

    @Override
    public void onStartCodeHighlight() {
        progressDialog = ProgressDialog.show(CodeViewActivity.this, getString(R.string.pleaseWait), getString(R.string.coloring), true);
    }

    @Override
    public void onFinishCodeHighlight() {
        if (progressDialog != null) progressDialog.dismiss();
    }

    @Override
    public void onLanguageDetected(Language language, int relevance) {

    }

    @Override
    public void onFontSizeChanged(int sizeInPx) {

    }

    @Override
    public void onLineClicked(int lineNumber, String content) {

    }

    @Override
    public void onFindResultReceived(int i, int i1, boolean b) {

    }

    // Button OnclickListener
    @Override
    public void onClick(View view) {
        //TODO : Implement Button OnClickListener
    }
    //endregion
}
