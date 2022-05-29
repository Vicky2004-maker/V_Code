package com.clevergo.vcode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.clevergo.vcode.codeviewer.CodeView;
import com.clevergo.vcode.codeviewer.Language;
import com.clevergo.vcode.codeviewer.Theme;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private LinearLayout allFileSwitcher_LinearLayout, info_LinearLayout, codeView_Container;
    private ImageView bottomSheet_ImageView, findNext_ImageView, closeSearch_ImageView, findPrev_ImageView;
    private TextView pickFile_TextView, lineInfo_TextView, fileSize_TextView, searchWord_TextView, findResultNum_TextView;
    private CodeView codeView_Main, codeview_SplitScreen1;
    private boolean loadIntoRAM = true, searchResult = false, configFullScreen = true, isScreenSplitted = false;
    private String searchWord = "";

    //TODO : Thread Started, Change some workflow to async
    private CustomWorkerThread customWorkerThread;


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
    protected void onDestroy() {
        super.onDestroy();

        customWorkerThread.stop();
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
        codeview_SplitScreen1 = findViewById(R.id.codeview_SplitScreen1);
        codeView_Container = findViewById(R.id.codeView_Container);

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

        customWorkerThread = new CustomWorkerThread();
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

    private void setCodeViewSplitScreen(final CodeView[] codeView, @NonNull final String[] code) {
        for (int i = 0; i < codeView.length; i++) {
            codeView[i].setOnHighlightListener(this)
                    .setTheme(Theme.MONOKAI)
                    .setCode(code[i])
                    .setLanguage(Language.AUTO)
                    .setWrapLine(false)
                    .setShowLineNumber(true)
                    .apply();

            codeView[i].setFindListener(this);
        }
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

            if (isChecked) {
                findTextInput.addTextChangedListener(textWatcher);
            } else {
                findTextInput.removeTextChangedListener(textWatcher);
            }
        });

        searchDialog.setTitle(getString(R.string.search));
        searchDialog.setView(searchDialogView);
        searchDialog.setPositiveButton(getString(R.string.search), (a, b) -> {
            searchWord = Objects.requireNonNull(findTextInput.getText()).toString();
            if (isRegexSwitch.isChecked()) {
                Pattern pattern = Pattern.compile(searchWord);
                Matcher matcher = pattern.matcher(codeView_Main.getCode());
                int groups = matcher.groupCount();
                StringBuilder matcherString = new StringBuilder();

                for (int i = 0; i < groups; i++) {
                    matcherString.append(matcher.group(i));
                }

                if (matcher.find()) {
                    codeView_Main.findAllAsync(matcherString.toString());
                } else {
                    Toast.makeText(CodeViewActivity.this, getString(R.string.noMatchFound), Toast.LENGTH_SHORT).show();
                }
            }

            if (isExactMatchSwitch.isChecked()) {
                Pattern pattern = Pattern.compile(searchWord);
                Matcher matcher = pattern.matcher(codeView_Main.getCode());

                if (matcher.find()) {
                    codeView_Main.findAllAsync(matcher.group());
                } else {
                    Toast.makeText(CodeViewActivity.this, getString(R.string.noResultFound), Toast.LENGTH_SHORT).show();
                }
            }

            if (!isExactMatchSwitch.isChecked() && !isRegexSwitch.isChecked()) {
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

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                if (!Helper.isFullScreen && configFullScreen) {
                    Helper.makeFullScreen(CodeViewActivity.this);
                    configFullScreen = false;
                    if (isScreenSplitted) {
                        codeView_Container.setOrientation(LinearLayout.HORIZONTAL);
                        LinearLayout.LayoutParams params = ((LinearLayout.LayoutParams) codeView_Main.getLayoutParams());
                        params.width = 0;
                        params.height = LinearLayout.LayoutParams.MATCH_PARENT;
                        codeView_Main.setLayoutParams(params);

                        params = ((LinearLayout.LayoutParams) codeview_SplitScreen1.getLayoutParams());
                        params.width = 0;
                        params.height = LinearLayout.LayoutParams.MATCH_PARENT;
                        codeview_SplitScreen1.setLayoutParams(params);

                        params = null;
                    }
                }
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                if (Helper.isFullScreen && !configFullScreen) {
                    Helper.revertFullScreen(CodeViewActivity.this);
                    configFullScreen = true;
                    if (isScreenSplitted) {
                        codeView_Container.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams params = ((LinearLayout.LayoutParams) codeView_Main.getLayoutParams());
                        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
                        params.height = 0;
                        codeView_Main.setLayoutParams(params);

                        params = ((LinearLayout.LayoutParams) codeview_SplitScreen1.getLayoutParams());
                        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
                        params.height = 0;
                        codeview_SplitScreen1.setLayoutParams(params);

                        params = null;
                    }
                }
                break;
            case Configuration.ORIENTATION_SQUARE:
                break;
            case Configuration.ORIENTATION_UNDEFINED:
                break;
        }
    }

    private void splitScreen_2(@NonNull final CodeView[] codeViews) {
        androidx.appcompat.app.AlertDialog.Builder alertBuilder = new androidx.appcompat.app.AlertDialog.Builder(CodeViewActivity.this);
        alertBuilder.setTitle(getString(R.string.splitScreen));
        final View searchDialog_View = getLayoutInflater().inflate(R.layout.splitscreeen_dialog, null);
        final AutoCompleteTextView fileSelector_1 = searchDialog_View.findViewById(R.id.fileSelector_1);
        final AutoCompleteTextView fileSelector_2 = searchDialog_View.findViewById(R.id.fileSelector_2);
        String[] fileNameArray = new String[fileList.size()];
        for (int i = 0; i < fileList.size(); i++) {
            fileNameArray[i] = fileList.get(i).getName();
        }
        ArrayAdapter<String> fileNameAdapter = new ArrayAdapter<>(CodeViewActivity.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, fileNameArray);
        fileSelector_1.setAdapter(fileNameAdapter);
        fileSelector_2.setAdapter(fileNameAdapter);

        int[] file1 = new int[1];
        int[] file2 = new int[1];
        fileSelector_1.setOnItemClickListener((parent, view, position, id) -> {
            file1[0] = position;
        });

        fileSelector_2.setOnItemClickListener((parent, view, position, id) -> {
            file2[0] = position;
        });
        alertBuilder.setView(searchDialog_View);
        alertBuilder.setCancelable(true);
        alertBuilder.setPositiveButton(getString(R.string.split), (dialog, which) -> {
            isScreenSplitted = true;
            LinearLayout.LayoutParams params = ((LinearLayout.LayoutParams) codeView_Main.getLayoutParams());
            params.weight = 1;
            codeView_Main.setLayoutParams(params);

            params = ((LinearLayout.LayoutParams) codeview_SplitScreen1.getLayoutParams());
            params.weight = 1;
            codeview_SplitScreen1.setLayoutParams(params);
            codeview_SplitScreen1.setVisibility(View.VISIBLE);

            params = null;

            String[] codes = new String[2];
            for (int i = 0; i < fileList.size(); i++) {
                String fileName = fileList.get(i).getName();

                if (Objects.equals(fileNameArray[file1[0]], fileName)) {
                    codes[0] = Helper.readFile(CodeViewActivity.this, Uri.parse(fileList.get(i).getUri()));
                }
                if (Objects.equals(fileNameArray[file2[0]], fileName)) {
                    codes[1] = Helper.readFile(CodeViewActivity.this, Uri.parse(fileList.get(i).getUri()));
                }
            }

            setCodeViewSplitScreen(codeViews, codes);
        });
        alertBuilder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
        });
        alertBuilder.create();
        alertBuilder.show();
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
                //FIXME : Regex Search
                showSearchDialog();
                break;
            case CopyAll:
                customWorkerThread.addWork(() -> Helper.copyCode(CodeViewActivity.this, codeView_Main.getCode()));
                break;
            case FullScreen:
                if (Helper.isFullScreen) {
                    Helper.revertFullScreen(CodeViewActivity.this);
                } else {
                    Helper.makeFullScreen(CodeViewActivity.this);
                }
                break;
            case SplitScreen:
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    Helper.showAlertDialog(getString(R.string.suggestion), getString(R.string.changeToLandscape), CodeViewActivity.this);

                if (Helper.thisIsMobile) {
                    splitScreen_2(new CodeView[]{codeView_Main, codeview_SplitScreen1});
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
