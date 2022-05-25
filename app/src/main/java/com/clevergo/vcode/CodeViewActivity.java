package com.clevergo.vcode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.clevergo.vcode.codeviewer.CodeView;
import com.clevergo.vcode.codeviewer.Language;
import com.clevergo.vcode.codeviewer.Theme;
import com.clevergo.vcode.editorfiles.BottomSheetCode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CodeViewActivity extends AppCompatActivity
        implements CodeView.OnHighlightListener,
        WebView.FindListener,
        View.OnClickListener,
        InfoBottomSheet.OnInputListener {

    private static ProgressDialog progressDialog;
    private static List<CodeViewFile> fileList = new ArrayList<>();
    private static List<Uri> uri_List;
    private static List<String> codeList = new ArrayList<>();
    private static int filesOpened = 0;
    private ConstraintLayout searchResult_Layout;
    private LinearLayout allFileSwitcher_LinearLayout, info_LinearLayout;
    private ImageView bottomSheet_ImageView, findNext_ImageView, closeSearch_ImageView, findPrev_ImageView;
    private TextView pickFile_TextView, lineInfo_TextView, fileSize_TextView, searchWord_TextView, findResultNum_TextView;
    private CodeView codeView_Main;
    private boolean loadIntoRAM = true, searchResult = false;
    private String searchWord = "";


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Helper.PICK_FILE_CODE && data != null && resultCode == Activity.RESULT_OK) {
            if (filesOpened == 0) {
                pickFile_TextView.setVisibility(View.GONE);
                codeView_Main.setVisibility(View.VISIBLE);
                info_LinearLayout.setVisibility(View.VISIBLE);
                addUI_File(data);
            } else if (filesOpened > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (fileList.stream().anyMatch(fileObj -> fileObj.getUri().equals(data.getData().toString()))) {
                        Toast.makeText(CodeViewActivity.this, getString(R.string.fileAlreadyPicked), Toast.LENGTH_LONG).show();
                    } else {
                        addUI_File(data);
                    }
                } else {
                    if (uri_List.contains(data.getData())) {
                        Toast.makeText(CodeViewActivity.this, getString(R.string.fileAlreadyPicked), Toast.LENGTH_LONG).show();
                    } else {
                        addUI_File(data);
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
        lineInfo_TextView = findViewById(R.id.lineInfo_TextView);
        fileSize_TextView = findViewById(R.id.fileSize_TextView);
        findNext_ImageView = findViewById(R.id.findNext_ImageView);
        searchResult_Layout = findViewById(R.id.searchResult_Layout);
        searchWord_TextView = findViewById(R.id.searchWord_TextView);
        findResultNum_TextView = findViewById(R.id.findResultNum_TextView);
        closeSearch_ImageView = findViewById(R.id.closeSearch_ImageView);
        findPrev_ImageView = findViewById(R.id.findPrev_ImageView);

        findNext_ImageView.setOnClickListener(a -> {
            if (searchResult) codeView_Main.findNext(true);
        });

        findPrev_ImageView.setOnClickListener(a -> {
            if (searchResult) codeView_Main.findNext(false);
        });

        closeSearch_ImageView.setOnClickListener(a -> {
            if (searchResult) {
                codeView_Main.findAllAsync("");
                searchResult_Layout.setVisibility(View.GONE);
            }
            searchResult = false;
        });

        bottomSheet_ImageView.setOnClickListener(a -> {
            InfoBottomSheet infoBottomSheet = new InfoBottomSheet();
            infoBottomSheet.show(getSupportFragmentManager(), "ModalBottomSheet");
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri_List = new ArrayList<>();
        }
    }

    //region Private Methods

    @SuppressLint("SetTextI18n")
    private void setCodeView(String code) {
        codeView_Main.setOnHighlightListener(this)
                .setTheme(Theme.MONOKAI)
                .setCode(code)
                .setLanguage(Language.AUTO)
                .setWrapLine(false)
                .setShowLineNumber(true)
                .apply();

        codeView_Main.setFindListener(this);

        lineInfo_TextView.setText(codeView_Main.getLineCount() + ":Nil (" + codeView_Main.getCode().length() + ")");
    }

    private CodeViewFile createACodeViewFile(Intent data) {
        return new CodeViewFile(filesOpened,
                Double.parseDouble(Helper.getFileSize(CodeViewActivity.this, data)),
                Helper.getFileName(CodeViewActivity.this, data),
                data.getData().toString(),
                Helper.getFileExtension(CodeViewActivity.this, data));
    }

    private void addUI_File(Intent data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri_List.add(data.getData());
        }

        fileList.add(createACodeViewFile(data));

        if (loadIntoRAM) {
            codeList.add(Helper.readFile(CodeViewActivity.this, data.getData()));
            setCodeView(codeList.get(filesOpened));
        } else {
            setCodeView(Helper.readFile(CodeViewActivity.this, data.getData()));
        }

        runOnUiThread(() -> {
            MaterialButton materialButton = new MaterialButton(CodeViewActivity.this);
            materialButton.setText(fileList.get(filesOpened).getName());
            materialButton.setId(filesOpened);
            materialButton.setOnClickListener(CodeViewActivity.this);
            materialButton.setAllCaps(false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(5, 0, 5, 0);

            allFileSwitcher_LinearLayout.addView(materialButton, filesOpened, layoutParams);
        });

        updateInfo(data);
        filesOpened++;
    }

    @SuppressLint("SetTextI18n")
    private void updateInfo(int currID) {
        Objects.requireNonNull(getSupportActionBar()).setSubtitle(fileList.get(currID).getName());
        fileSize_TextView.setText(fileList.get(currID).getName() + " - " + fileList.get(currID).getFile_Size() + "KB");
    }

    @SuppressLint("SetTextI18n")
    private void updateInfo(Intent data) {
        Objects.requireNonNull(getSupportActionBar()).setSubtitle(Helper.getFileName(CodeViewActivity.this, data));
        fileSize_TextView.setText(Helper.getFileName(CodeViewActivity.this, data) + " - " + Helper.getFileSize(CodeViewActivity.this, data) + " KB");
    }

    private void showSearchDialog() {
        AlertDialog.Builder searchDialog = new AlertDialog.Builder(CodeViewActivity.this);
        final View searchDialogView = getLayoutInflater().inflate(R.layout.search_dialog, null);
        final TextInputEditText findTextInput = searchDialogView.findViewById(R.id.searchInputTextField);
        final SwitchCompat isRegexSwitch = searchDialogView.findViewById(R.id.isRegex_switch);
        final SwitchCompat isExactMatchSwitch = searchDialogView.findViewById(R.id.exactMatch_switch);

        isRegexSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            TextWatcher textWatcher = Helper.validateRegex(CodeViewActivity.this, findTextInput);

            if(isChecked) {
                findTextInput.addTextChangedListener(textWatcher);
            } else {
                findTextInput.removeTextChangedListener(textWatcher);
            }
        });

        searchDialog.setTitle(getString(R.string.search));
        searchDialog.setView(searchDialogView);
        searchDialog.setPositiveButton(getString(R.string.search), (a, b) -> {
            searchWord = Objects.requireNonNull(findTextInput.getText()).toString();
            if(isRegexSwitch.isChecked()) {
                Pattern pattern = Pattern.compile(searchWord);
                Matcher matcher = pattern.matcher(codeView_Main.getCode());
                int groups = matcher.groupCount();
                StringBuilder matcherString = new StringBuilder();

                for (int i = 0; i < groups; i++) {
                    matcherString.append(matcher.group(i));
                }

                if(matcher.find()) {
                    codeView_Main.findAllAsync(matcherString.toString());
                } else {
                    Toast.makeText(CodeViewActivity.this, getString(R.string.noMatchFound), Toast.LENGTH_SHORT).show();
                }
            }

            if(isExactMatchSwitch.isChecked()) {
                Pattern pattern = Pattern.compile(searchWord);
                Matcher matcher = pattern.matcher(codeView_Main.getCode());

                if(matcher.find()) {
                    codeView_Main.findAllAsync(matcher.group());
                } else {
                    Toast.makeText(CodeViewActivity.this, getString(R.string.noResultFound), Toast.LENGTH_SHORT).show();
                }
            }

            if(!isExactMatchSwitch.isChecked() && !isRegexSwitch.isChecked()) {
                codeView_Main.findAllAsync(searchWord);
            }

            searchWord_TextView.setText(searchWord);
            searchWord = null;
        });

        searchDialog.setNegativeButton(getString(R.string.cancel), (a, b) -> {
        });

        searchDialog.setCancelable(true);
        searchDialog.create();
        searchDialog.show();
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

    //region CodeView OnHighlightListener & OnFindListener & Button OnCLickListener && Data from InfoBottomSheet

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

    @SuppressLint("SetTextI18n")
    @Override
    public void onLineClicked(int lineNumber, String content) {
        lineInfo_TextView.setText(codeView_Main.getLineCount() + ":" + lineNumber);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onFindResultReceived(int i, int i1, boolean b) {
        searchResult_Layout.setVisibility(View.VISIBLE);

        if (b) {
            searchResult = true;
            findResultNum_TextView.setText(i1 + " results");
        }
    }

    // Button OnclickListener
    @Override
    public void onClick(View view) {
        int clicked_ID = view.getId();

        if (loadIntoRAM) {
            setCodeView(codeList.get(clicked_ID));
        } else {
            setCodeView(Helper.readFile(CodeViewActivity.this, Uri.parse(fileList.get(clicked_ID).getUri())));
        }

        updateInfo(clicked_ID);
    }

    // Get Data from InfoBottomSheet
    @Override
    public void sendInput(BottomSheetCode code) {
        switch (code) {
            case Compile:
                //TODO : Compile Case
                break;
            case Edit:
                //TODO : Edit Case
                break;
            case Search:
                //TODO : Search Case, Regex, Exact Match Case
                showSearchDialog();
                break;
            case CopyAll:
                Helper.copyCode(CodeViewActivity.this, codeView_Main.getCode());
                break;
            case FullScreen:
                if (Helper.isFullScreen(CodeViewActivity.this)) {
                    Helper.revertFullScreen(CodeViewActivity.this);
                } else {
                    Helper.makeFullScreen(CodeViewActivity.this);
                }
                break;
            case SplitScreen:
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    Helper.showAlertDialog(getString(R.string.suggestion), getString(R.string.changeToLandscape), CodeViewActivity.this);
                } else {

                }

                break;
            case AddFile:
                Helper.pickFile(CodeViewActivity.this);
                break;
            case DeleteFile:
                //TODO : DeleteFile Case
                break;
        }
    }
    //endregion
}
