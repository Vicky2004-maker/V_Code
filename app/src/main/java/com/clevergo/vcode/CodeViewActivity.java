package com.clevergo.vcode;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.clevergo.vcode.codeviewer.CodeView;
import com.clevergo.vcode.codeviewer.Language;
import com.clevergo.vcode.codeviewer.Theme;

public class CodeViewActivity extends AppCompatActivity implements CodeView.OnHighlightListener, WebView.FindListener {

    private LinearLayout allFileSwitcher_LinearLayout;
    private TextView pickFile_TextView;
    private CodeView codeView_Main;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Helper.PICK_FILE_CODE && data != null) {
            pickFile_TextView.setVisibility(View.GONE);
            codeView_Main.setVisibility(View.VISIBLE);


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_view);

        allFileSwitcher_LinearLayout = findViewById(R.id.allFileSwitcher);
        pickFile_TextView = findViewById(R.id.pickFileTextView);
        codeView_Main = findViewById(R.id.codeview_Main);
    }

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

    //region CodeView OnHighlightListener & OnFindListener

    @Override
    public void onStartCodeHighlight() {

    }

    @Override
    public void onFinishCodeHighlight() {

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
    //endregion
}
