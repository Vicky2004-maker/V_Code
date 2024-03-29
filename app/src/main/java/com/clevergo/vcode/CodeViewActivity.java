/*
 * Created by Viknesh on 2022.
 * Copyright (c) 2022. All rights reserved.
 */

package com.clevergo.vcode;

import static com.clevergo.vcode.Helper.ALL_FILES_MIME;
import static com.clevergo.vcode.Helper.CHOOSE_DIRECTORY_NORMAL;
import static com.clevergo.vcode.Helper.CHOOSE_DIRECTORY_PDF;
import static com.clevergo.vcode.Helper.COMPILER_SDKs_LOCATION;
import static com.clevergo.vcode.Helper.CREATE_FILE_NORMAL_CODE;
import static com.clevergo.vcode.Helper.CREATE_FILE_PDF_CODE;
import static com.clevergo.vcode.Helper.PDF_MIME;
import static com.clevergo.vcode.Helper.PICK_FILE_CODE;
import static com.clevergo.vcode.Helper.chooseDirectory;
import static com.clevergo.vcode.Helper.copyCode;
import static com.clevergo.vcode.Helper.copyFile;
import static com.clevergo.vcode.Helper.createACodeViewFile;
import static com.clevergo.vcode.Helper.createFile;
import static com.clevergo.vcode.Helper.findProgrammingLanguage;
import static com.clevergo.vcode.Helper.generatePDF;
import static com.clevergo.vcode.Helper.getAllCompilerSDKs;
import static com.clevergo.vcode.Helper.getCurrentColumn;
import static com.clevergo.vcode.Helper.getFileExtension;
import static com.clevergo.vcode.Helper.getFileName;
import static com.clevergo.vcode.Helper.getFileSize;
import static com.clevergo.vcode.Helper.getLines;
import static com.clevergo.vcode.Helper.getSelectedLineNumber;
import static com.clevergo.vcode.Helper.isFullScreen;
import static com.clevergo.vcode.Helper.isLowerSDK;
import static com.clevergo.vcode.Helper.isScreenLandscape;
import static com.clevergo.vcode.Helper.isURL;
import static com.clevergo.vcode.Helper.makeFullScreen;
import static com.clevergo.vcode.Helper.pickFile;
import static com.clevergo.vcode.Helper.readFile;
import static com.clevergo.vcode.Helper.revertFullScreen;
import static com.clevergo.vcode.Helper.setBtnIcon;
import static com.clevergo.vcode.Helper.showAlertDialog;
import static com.clevergo.vcode.Helper.uiHandler;
import static com.clevergo.vcode.Helper.writeFile;
import static com.clevergo.vcode.MainActivity.auth;
import static com.clevergo.vcode.MainActivity.storage;
import static com.clevergo.vcode.MainActivity.storageRef_UserFiles;
import static com.clevergo.vcode.regex.JavaManager.getAllMethodsLines;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import com.clevergo.vcode.codeviewer.CodeView;
import com.clevergo.vcode.codeviewer.Language;
import com.clevergo.vcode.codeviewer.Theme;
import com.clevergo.vcode.compiler.JavaCompiler;
import com.clevergo.vcode.editorfiles.Token;
import com.clevergo.vcode.editorfiles.syntax.LanguageManager;
import com.clevergo.vcode.editorfiles.syntax.LanguageName;
import com.clevergo.vcode.editorfiles.syntax.ThemeName;
import com.clevergo.vcode.regex.JavaManager;
import com.google.android.datatransport.BuildConfig;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.perf.metrics.AddTrace;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.batch.Main;

import java.io.File;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import dalvik.system.DexClassLoader;

//TODO : Add Firebase controls
//TODO : Add Firebase Authentication then Firebase Storage

public class CodeViewActivity extends AppCompatActivity
        implements CodeView.OnHighlightListener,
        WebView.FindListener,
        View.OnClickListener,
        InfoBottomSheet.OnInputListener {

    public static final List<CodeViewFile> fileList = new ArrayList<>();
    private static final List<String> codeList = new ArrayList<>();
    public static int activeFilePosition = 0, currentActiveID = -1;
    public static List<String> selectedFileNames = new ArrayList<>();
    public static boolean isScreenSplit = false;
    public static String activeSplitScreenFileName;
    public static int filesOpened = 0;
    public static List<Uri> uri_List;
    public static CustomWorkerThread customWorkerThread;
    public static boolean isEditorMode = false;
    //TODO: GoogleSignInClient MEMORY LEAK
    public static GoogleSignInClient signInClient;
    private static ProgressDialog progressDialog;
    private final List<String> fileNames = new ArrayList<>();
    private final List<String> activeFileNames = new ArrayList<>();
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    public List<String> buttonStringList = List.of("\t", "{\n    }", "()", "[]", "<", ">", ";", "=", ",", "&", "<>", "|", "!",
            "~", "+", "-", "*", "/", "%", ":");
    public List<com.clevergo.vcode.editorfiles.CodeView> editorList = new ArrayList<>();
    public List<CodeView> codeViewList = new ArrayList<>();
    private Toolbar toolbar;
    private boolean loadIntoRAM;
    private int totalSearchResult = 0;
    private HashMap<LinearLayout, List<View>> MAIN_VIEW_HOLDER = new HashMap<>();
    private HorizontalScrollView buttonControls_HorizontalScrollView;
    private ExpandableListView expandableListView;
    private ExpandableListAdapter expandableListAdapter;
    private List<String> expandableListTitle = new ArrayList<>();
    private HashMap<String, List<String>> expandableListDetail = new HashMap<>();
    private HashMap<String, Integer> allMethods = new HashMap<>();
    private NavigationView navView;
    private ConstraintLayout searchResult_Layout;
    private LinearLayout codeView_Container_Main, codeView_Container_SplitScreen2, codeView_Container_SplitScreen3, codeView_Container_SplitScreen4,
            editor_Container_Main, editor_Container_SplitScreen2, editor_Container_SplitScreen3, editor_Container_SplitScreen4;
    private LinearLayout codeView_Container_SplitScreen3_Child, editor_Container_SplitScreen3_Child;
    private LinearLayout allFileSwitcher_LinearLayout, info_LinearLayout, allFileSwitcherParent, fileInfo_LinearLayout;
    private LinearLayout miniError_LinearLayout;
    private TextView pickFile_TextView, lineInfo_TextView, fileSize_TextView, searchWord_TextView, findResultNum_TextView,
            totalMethod_TextView, totalVariable_TextView, errorCount_TextView, warningCount_TextView;
    private boolean searchResult = false;
    private boolean configFullScreen = true;
    private String searchWord = "";
    private ActiveLayout activeLayout;
    private SharedPreferences sharedPreferences;
    private int CODE_HIGHLIGHTER_MAX_LINES;
    private AppBarLayout appBarLayout;

    @Override
    @AddTrace(name = "onActivityResultTrace")
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_FILE_CODE: {
                    manageSingleFileIntent(data);
                    manageMultipleFileIntent(data);
                    break;
                }
                case CHOOSE_DIRECTORY_NORMAL:
                    createFile(CodeViewActivity.this, ALL_FILES_MIME, CREATE_FILE_NORMAL_CODE);
                    break;
                case CHOOSE_DIRECTORY_PDF:
                    createFile(CodeViewActivity.this, PDF_MIME, CREATE_FILE_PDF_CODE);
                    break;
                case CREATE_FILE_NORMAL_CODE: {
                    manageSingleFileIntent(data);
                    editFile();
                    break;
                }
                case CREATE_FILE_PDF_CODE:
                    generatePDF(CodeViewActivity.this, data.getData(), fileList.get(currentActiveID));
                    break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (SettingsActivity.refresh && fileList.size() != 0) {
            final CodeViewFile file = fileList.get(currentActiveID);
            final CodeView codeView = codeViewList.get(activeFilePosition);
            if (file.isURL) {
                setCodeView(codeView,
                        readFile(CodeViewActivity.this, file.getUrl()));
            } else {
                setCodeView(codeView,
                        readFile(CodeViewActivity.this, Uri.parse(file.getUri())));
            }
            if (loadIntoRAM) codeList.set(currentActiveID, codeView.getCode());
            SettingsActivity.refresh = false;
        }

        if (SettingsActivity._updateInfo) {
            final CodeViewFile file = fileList.get(currentActiveID);
            if (file.isURL) {
                updateInfo(file.getUrl());
            } else {
                updateInfo(Uri.parse(file.getUri()));
            }

            SettingsActivity._updateInfo = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        customWorkerThread.stop();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        actionBarDrawerToggle.syncState();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_view);
        customWorkerThread = new CustomWorkerThread();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(CodeViewActivity.this);

        activeLayout = ActiveLayout.CodeView_Main;
        appBarLayout = findViewById(R.id.appBarLayout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        errorCount_TextView = findViewById(R.id.errorCount_TextView);
        warningCount_TextView = findViewById(R.id.warningCount_TextView);
        loadIntoRAM = sharedPreferences.getBoolean("pref_fastLoad", false);
        allFileSwitcher_LinearLayout = findViewById(R.id.allFileSwitcher);
        pickFile_TextView = findViewById(R.id.pickFileTextView);
        info_LinearLayout = findViewById(R.id.info_LinearLayout);
        miniError_LinearLayout = findViewById(R.id.miniError_LinearLayout);
        ImageView bottomSheet_ImageView = findViewById(R.id.bottomSheet_ImageView);
        lineInfo_TextView = findViewById(R.id.lineInfo_TextView);
        fileSize_TextView = findViewById(R.id.fileSize_TextView);
        ImageView findNext_ImageView = findViewById(R.id.findNext_ImageView);
        searchResult_Layout = findViewById(R.id.searchResult_Layout);
        searchWord_TextView = findViewById(R.id.searchWord_TextView);
        findResultNum_TextView = findViewById(R.id.findResultNum_TextView);
        ImageView closeSearch_ImageView = findViewById(R.id.closeSearch_ImageView);
        ImageView findPrev_ImageView = findViewById(R.id.findPrev_ImageView);
        codeView_Container_Main = findViewById(R.id.codeView_Container_Main);
        codeView_Container_SplitScreen2 = findViewById(R.id.codeView_Container_SplitScreen2);
        codeView_Container_SplitScreen3 = findViewById(R.id.codeView_Container_SplitScreen3);
        codeView_Container_SplitScreen4 = findViewById(R.id.codeView_Container_SplitScreen4);
        fileInfo_LinearLayout = findViewById(R.id.fileInfo_LinearLayout);
        editor_Container_Main = findViewById(R.id.editor_Container_Main);
        editor_Container_SplitScreen2 = findViewById(R.id.editor_Container_SplitScreen2);
        editor_Container_SplitScreen3 = findViewById(R.id.editor_Container_SplitScreen3);
        editor_Container_SplitScreen4 = findViewById(R.id.editor_Container_SplitScreen4);
        editor_Container_SplitScreen3_Child = findViewById(R.id.editor_Container_SplitScreen3_Child);
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.navView);
        expandableListView = findViewById(R.id.fileSelector_ExpandableList);
        allFileSwitcherParent = findViewById(R.id.allFileSwitcherParent);
        buttonControls_HorizontalScrollView = findViewById(R.id.buttonControls_HorizontalScrollView);
        totalMethod_TextView = findViewById(R.id.totalMethod_TextView);
        totalVariable_TextView = findViewById(R.id.totalVariable_TextView);
        try {
            CODE_HIGHLIGHTER_MAX_LINES = Integer.parseInt(sharedPreferences.getString("maxLineLimit", String.valueOf(1000)));
        } catch (NumberFormatException ex) {
            sharedPreferences.edit().putString("maxLineLimit", String.valueOf(100)).apply();
            CODE_HIGHLIGHTER_MAX_LINES = 100;
        }

        isEditorMode = false;

        updateViewEditorLists();
        codeViewList.add(((CodeView) MAIN_VIEW_HOLDER.get(codeView_Container_Main).get(0)));
        editorList.add((com.clevergo.vcode.editorfiles.CodeView) MAIN_VIEW_HOLDER.get(editor_Container_Main).get(0));

        expandableListTitle.add("Active Files");
        expandableListTitle.add("Opened Files");
        expandableListTitle.add("All Methods");

        expandableListAdapter = new ExpandableViewAdapterCustom(CodeViewActivity.this,
                expandableListTitle,
                expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnGroupExpandListener(groupPosition -> fileInfo_LinearLayout.setVisibility(View.INVISIBLE));
        expandableListView.setOnGroupCollapseListener(groupPosition -> fileInfo_LinearLayout.setVisibility(View.VISIBLE));
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            if (groupPosition == 1) {
                final CodeViewFile file = fileList.get(childPosition);
                if (currentActiveID == childPosition) {
                    Toast.makeText(CodeViewActivity.this, getString(R.string.fileAreadyDisplayed), Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (file.isURL) {
                    setCodeView(codeViewList.get(activeFilePosition), readFile(CodeViewActivity.this, file.getUrl()));
                    updateInfo(file.getUrl());
                } else {
                    setCodeView(codeViewList.get(activeFilePosition), readFile(CodeViewActivity.this, Uri.parse(file.getUri())));
                    updateInfo(childPosition);
                }

                currentActiveID = childPosition;
            }
            if (groupPosition == 2) {
                if (sharedPreferences.getBoolean("pref_organizeFileInfo", false)) {
                    String[] method = List.copyOf(allMethods.keySet()).get(childPosition).split("\n");
                    for (String str : method) {
                        if (str.charAt(0) == 'N') {
                            searchWord = str.substring(6);
                        }
                    }
                } else {
                    searchWord = List.copyOf(allMethods.keySet()).get(childPosition);
                }

                codeViewList.get(activeFilePosition).findAllAsync(searchWord);
                searchWord_TextView.setText(searchWord);
            }
            return false;
        });

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);

        findNext_ImageView.setOnClickListener(a -> {
            if (searchResult) codeViewList.get(activeFilePosition).findNext(true);
        });

        findPrev_ImageView.setOnClickListener(a -> {
            if (searchResult) codeViewList.get(activeFilePosition).findNext(false);
        });

        closeSearch_ImageView.setOnClickListener(a -> {
            //TODO : Change This too
            if (searchResult) {
                searchWord = "";
                codeViewList.get(activeFilePosition).findAllAsync("");
                searchResult_Layout.setVisibility(View.GONE);
            }
            searchResult = false;
            totalSearchResult = 0;
        });

        bottomSheet_ImageView.setOnClickListener(a -> {
            InfoBottomSheet infoBottomSheet = new InfoBottomSheet();
            infoBottomSheet.show(getSupportFragmentManager(), "ModalBottomSheet");
        });

        if (isLowerSDK()) {
            uri_List = new ArrayList<>();
        }

        if ((Intent.ACTION_SEND.equals(getIntent().getAction()) || Intent.ACTION_VIEW.equals(getIntent().getAction()))
                && getIntent().getType() != null) {
            manageSingleFileIntent(getIntent());
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction()) && getIntent().getType() != null) {
            manageMultipleFileIntent(getIntent());
        }
    }

    //region Private Methods
    private void manageSingleFileIntent(Intent data) {
        if (data.getData() != null) {
            if (filesOpened == 0) {
                pickFile_TextView.setVisibility(View.GONE);
                codeView_Container_Main.setVisibility(View.VISIBLE);
                info_LinearLayout.setVisibility(View.VISIBLE);
                addUI_File(data);
                addNavMenu(getFileName(CodeViewActivity.this, data));
                Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            } else if (filesOpened > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (fileList.stream().anyMatch(fileObj -> fileObj.getUri().equals(data.getData().toString()))) {
                        Toast.makeText(CodeViewActivity.this, getString(R.string.fileAlreadyPicked), Toast.LENGTH_LONG).show();
                    } else {
                        addUI_File(data);
                        addNavMenu(getFileName(CodeViewActivity.this, data));
                        allFileSwitcherParent.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (uri_List.contains(data.getData())) {
                        Toast.makeText(CodeViewActivity.this, getString(R.string.fileAlreadyPicked), Toast.LENGTH_LONG).show();
                    } else {
                        addUI_File(data);
                        addNavMenu(getFileName(CodeViewActivity.this, data));
                        allFileSwitcherParent.setVisibility(View.VISIBLE);
                    }
                }
                Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void manageMultipleFileIntent(Intent data) {
        if (data.getClipData() != null) {
            pickFile_TextView.setVisibility(View.GONE);
            codeView_Container_Main.setVisibility(View.VISIBLE);
            info_LinearLayout.setVisibility(View.VISIBLE);
            allFileSwitcherParent.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    if (fileList.stream().anyMatch(fileObj -> fileObj.getUri().equals(uri.toString()))) {
                        Toast.makeText(CodeViewActivity.this, getString(R.string.fileAlreadyPicked), Toast.LENGTH_LONG).show();
                    } else {
                        addUI_FileURI(uri, i == (data.getClipData().getItemCount() - 1), false);
                        addNavMenu(getFileName(CodeViewActivity.this, uri));
                    }
                }
            } else {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    if (uri_List.contains(uri)) {
                        Toast.makeText(CodeViewActivity.this, getString(R.string.fileAlreadyPicked), Toast.LENGTH_LONG).show();
                    } else {
                        addUI_FileURI(uri, i == (data.getClipData().getItemCount() - 1), false);
                        addNavMenu(getFileName(CodeViewActivity.this, uri));
                    }
                }
            }
            if (filesOpened >= 1)
                Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        }
    }

    @SuppressLint("SetTextI18n")
    private void setCodeView(CodeView codeView, String code) {
        int textSize;
        try {
            textSize = Integer.parseInt(sharedPreferences.getString("pref_textSize_codeView", "15"));
        } catch (NumberFormatException e) {
            sharedPreferences.edit().putString("pref_textSize_codeView", "15").apply();
            textSize = 15;
        }
        codeView.setTheme(new Theme(sharedPreferences.getString("pref_codeviewThemes", "monokai-sublime")))
                .setCode(code)
                .setLanguage(Language.AUTO)
                .setZoomEnabled(sharedPreferences.getBoolean("pref_pinchZoom", false))
                .setWrapLine(sharedPreferences.getBoolean("pref_wrapLines_codeView", false))
                .setFontSize(textSize)
                .setShowLineNumber(sharedPreferences.getBoolean("pref_lineNumber_codeView", true))
                .apply();
        codeView.setFindListener(this);
        disableHighlighting(codeView, getLines(code));
        lineInfo_TextView.setText(codeView.getLineCount() + ":Nil (" + codeView.getCode().length() + ")");
    }

    @SuppressLint("SetTextI18n")
    private void setCodeView_SplitScreen(CodeView codeView, final String code, Theme theme) {
        int textSize;
        try {
            textSize = Integer.parseInt(sharedPreferences.getString("pref_textSize_codeView", "15"));
        } catch (NumberFormatException e) {
            sharedPreferences.edit().putString("pref_textSize_codeView", "15").apply();
            textSize = 15;
        }
        codeView.setTheme(theme)
                .setCode(code)
                .setLanguage(Language.AUTO)
                .setZoomEnabled(sharedPreferences.getBoolean("pref_pinchZoom", false))
                .setWrapLine(sharedPreferences.getBoolean("pref_wrapLines_codeView", false))
                .setFontSize(textSize)
                .setShowLineNumber(sharedPreferences.getBoolean("pref_lineNumber_codeView", true))
                .apply();
        codeView.setFindListener(this);
        disableHighlighting(codeView, getLines(code));
        lineInfo_TextView.setText(codeView.getLineCount() + ":Nil (" + codeView.getCode().length() + ")");
    }

    private void addUI_FileURI(final Uri uri, final boolean isLastFile, boolean isUrl) {
        if (isLowerSDK()) {
            uri_List.add(uri);
        }

        fileList.add(createACodeViewFile(CodeViewActivity.this, uri, isUrl));
        currentActiveID++;

        if (loadIntoRAM) {
            if (isUrl) {
                codeList.add(readFile(CodeViewActivity.this, fileList.get(fileList.size() - 1).getUrl()));
            } else {
                codeList.add(readFile(CodeViewActivity.this, uri));
            }
            if (isLastFile)
                setCodeView(codeViewList.get(activeFilePosition), codeList.get(filesOpened));
        } else if (isLastFile) {
            if (isUrl)
                setCodeView(codeViewList.get(activeFilePosition), readFile(CodeViewActivity.this, fileList.get(fileList.size() - 1).getUrl()));
            setCodeView(codeViewList.get(activeFilePosition), readFile(CodeViewActivity.this, uri));
        }

        String fileNameTemp = isUrl ? getFileName(uri.toString()) : getFileName(CodeViewActivity.this, uri);
        if (isScreenSplit && isLastFile) selectedFileNames.set(activeFilePosition, fileNameTemp);

        LinearLayout linearLayout = new LinearLayout(CodeViewActivity.this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, ((int) getResources().getDimension(R.dimen.dimen50dp)));
        CodeViewFile file = fileList.get(fileList.size() - 1);
        linearLayout.addView(createMaterialButton(file, isUrl, Uri.parse(file.getUri()), isLastFile));
        linearLayout.setElevation(8);
        linearLayout.setPadding(2, 0, 2, 0);

        allFileSwitcher_LinearLayout.addView(linearLayout, filesOpened, layoutParams);

        if (isLastFile && isUrl) {
            updateInfo(fileList.get(fileList.size() - 1).getUrl());
        } else if (isLastFile) {
            updateInfo(uri);
        }
        filesOpened++;
    }

    private void addUI_File(Intent data) {
        if (isLowerSDK()) {
            uri_List.add(data.getData());
        }

        fileList.add(createACodeViewFile(CodeViewActivity.this, data, false));
        currentActiveID++;

        if (loadIntoRAM) {
            codeList.add(readFile(CodeViewActivity.this, data.getData()));
            setCodeView(codeViewList.get(activeFilePosition), codeList.get(filesOpened));
        } else {
            setCodeView(codeViewList.get(activeFilePosition), readFile(CodeViewActivity.this, data.getData()));
        }

        if (isScreenSplit)
            selectedFileNames.set(activeFilePosition, getFileName(CodeViewActivity.this, data));

        LinearLayout linearLayout = new LinearLayout(CodeViewActivity.this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, ((int) getResources().getDimension(R.dimen.dimen50dp)));
        CodeViewFile file = fileList.get(fileList.size() - 1);
        linearLayout.addView(createMaterialButton(file, false, Uri.parse(file.getUri()), true));
        linearLayout.setElevation(8);
        linearLayout.setPadding(2, 0, 2, 0);

        allFileSwitcher_LinearLayout.addView(linearLayout, fileList.size() - 1, layoutParams);

        updateInfo(data);
        filesOpened++;
    }

    @SuppressLint("SetTextI18n")
    private void updateInfoEditor(Uri uri) {
        customWorkerThread.addWork(() -> {
            String actionBarSubtitle = getFileName(CodeViewActivity.this, uri);
            String fileSize_Text = actionBarSubtitle
                    + " - "
                    + getFileSize(CodeViewActivity.this, uri) + " KB";

            activeFileNames.clear();
            activeFileNames.add(actionBarSubtitle);
            expandableListDetail.put("Active Files", activeFileNames);
            allMethods.clear();
            allMethods = getAllMethodsLines(codeViewList.get(activeFilePosition).getCode(), sharedPreferences.getBoolean("pref_organizeFileInfo", true));
            expandableListDetail.put("All Methods", List.copyOf(allMethods.keySet()));
            navView.postInvalidate();

            uiHandler.post(() -> {
                Objects.requireNonNull(getSupportActionBar()).setSubtitle(actionBarSubtitle);
                fileSize_TextView.setText(fileSize_Text);
                totalMethod_TextView.setText(getString(R.string.totalMethods) + " " + allMethods.size());
            });
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateInfo(Uri uri) {
        customWorkerThread.addWork(() -> {
            String actionBarSubtitle = getFileName(CodeViewActivity.this, uri);
            String fileSize_Text = actionBarSubtitle
                    + " - "
                    + getFileSize(CodeViewActivity.this, uri) + " KB";

            activeFileNames.clear();
            activeFileNames.add(actionBarSubtitle);
            expandableListDetail.put("Active Files", activeFileNames);
            allMethods.clear();
            allMethods = getAllMethodsLines(codeViewList.get(activeFilePosition).getCode(), sharedPreferences.getBoolean("pref_organizeFileInfo", true));
            expandableListDetail.put("All Methods", List.copyOf(allMethods.keySet()));
            navView.postInvalidate();

            uiHandler.post(() -> {
                Objects.requireNonNull(getSupportActionBar()).setSubtitle(actionBarSubtitle);
                fileSize_TextView.setText(fileSize_Text);
                totalMethod_TextView.setText(getString(R.string.totalMethods) + " " + allMethods.size());
            });
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateInfo(int currID) {
        customWorkerThread.addWork(() -> {
            String actionBarSubtitle = fileList.get(currID).getName();
            String fileSize_Text = actionBarSubtitle + " - " + fileList.get(currID).getFile_Size() + "KB";

            activeFileNames.clear();
            activeFileNames.add(actionBarSubtitle);
            expandableListDetail.put("Active Files", activeFileNames);
            allMethods.clear();
            allMethods = getAllMethodsLines(codeViewList.get(activeFilePosition).getCode(), sharedPreferences.getBoolean("pref_organizeFileInfo", true));
            expandableListDetail.put("All Methods", List.copyOf(allMethods.keySet()));
            navView.postInvalidate();

            uiHandler.post(() -> {
                Objects.requireNonNull(getSupportActionBar()).setSubtitle(actionBarSubtitle);
                fileSize_TextView.setText(fileSize_Text);
                totalMethod_TextView.setText(getString(R.string.totalMethods) + " " + allMethods.size());
            });
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateInfo(Intent data) {
        customWorkerThread.addWork(() -> {
            String actionBarSubtitle = getFileName(CodeViewActivity.this, data);
            String fileSize_Text = actionBarSubtitle
                    + " - "
                    + getFileSize(CodeViewActivity.this, data) + " KB";

            activeFileNames.clear();
            activeFileNames.add(actionBarSubtitle);
            expandableListDetail.put("Active Files", activeFileNames);
            allMethods.clear();
            allMethods = getAllMethodsLines(codeViewList.get(activeFilePosition).getCode(), sharedPreferences.getBoolean("pref_organizeFileInfo", true));
            expandableListDetail.put("All Methods", List.copyOf(allMethods.keySet()));
            navView.postInvalidate();

            uiHandler.post(() -> {
                Objects.requireNonNull(getSupportActionBar()).setSubtitle(actionBarSubtitle);
                fileSize_TextView.setText(fileSize_Text);
                totalMethod_TextView.setText(getString(R.string.totalMethods) + " " + allMethods.size());
            });
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateInfo(URL url) {
        customWorkerThread.addWork(() -> {
            String actionBarSubtitle = getFileName(url.toString());
            String fileSize_Text = "N/A";

            activeFileNames.clear();
            activeFileNames.add(actionBarSubtitle);
            expandableListDetail.put("Active Files", activeFileNames);
            allMethods.clear();
            allMethods = getAllMethodsLines(codeViewList.get(activeFilePosition).getCode(), sharedPreferences.getBoolean("pref_organizeFileInfo", true));
            expandableListDetail.put("All Methods", List.copyOf(allMethods.keySet()));
            navView.postInvalidate();

            uiHandler.post(() -> {
                Objects.requireNonNull(getSupportActionBar()).setSubtitle(actionBarSubtitle);
                fileSize_TextView.setText(fileSize_Text);
                totalMethod_TextView.setText(getString(R.string.totalMethods) + " " + allMethods.size());
            });
        });
    }

    private void showSearchDialog() {
        //TODO : Implement Search and Replace Interface

        AlertDialog.Builder searchDialog = new AlertDialog.Builder(CodeViewActivity.this);
        final View searchView = getLayoutInflater().inflate(R.layout.search_dialog, null);
        searchDialog.setView(searchView);
        final TextInputEditText findWord_Input = searchView.findViewById(R.id.searchInputTextField);
        final TextInputEditText findReplace_Input = searchView.findViewById(R.id.findReplace_Input);
        final TextInputLayout findReplace_Layout = searchView.findViewById(R.id.findReplace_Layout);
        final SwitchCompat findReplace_switch = searchView.findViewById(R.id.findReplace_switch);
        final SwitchCompat isRegex_switch = searchView.findViewById(R.id.isRegex_switch);
        final SwitchCompat exactMatch_switch = searchView.findViewById(R.id.exactMatch_switch);

        if (isEditorMode) {
            findReplace_Layout.setEnabled(true);
            findReplace_Input.setEnabled(true);
            findReplace_switch.setEnabled(true);
            findReplace_switch.setTextColor(Color.WHITE);
        } else {
            findReplace_Layout.setEnabled(false);
            findReplace_Input.setEnabled(false);
            findReplace_switch.setEnabled(false);
            findReplace_switch.setTextColor(Color.GRAY);
        }

        searchDialog.setPositiveButton(getString(R.string.continueStr), (dialog, which) -> {
            String searchWord = null;
            try {
                searchWord = Objects.requireNonNull(findWord_Input.getText()).toString();
            } catch (Exception ex) {
                Toast.makeText(CodeViewActivity.this, getString(R.string.emptyField), Toast.LENGTH_SHORT).show();
                return;
            } finally {
                if (searchWord != null) {
                    if (isEditorMode) {
                        com.clevergo.vcode.editorfiles.CodeView editor = editorList.get(0);
                        //Helper.setHighLightedText(editor, searchWord);


                        List<Token> tokens = editor.findMatches(searchWord);
                        for (Token token : tokens) {

                        }
                    }
                }
            }
        });

        searchDialog.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {

        });

        searchDialog.show();
    }

    //region Split Screen
    private void splitScreen() {
        int fileListSize = fileList.size();
        AlertDialog.Builder layoutDialog = new AlertDialog.Builder(CodeViewActivity.this);
        final View view = getLayoutInflater().inflate(R.layout.layout_selector_dialog, null);
        layoutDialog.setView(view);
        final CheckBox layout2 = view.findViewById(R.id.layout_2_checkBox);
        final CheckBox layout3A = view.findViewById(R.id.layout_3A_checkBox);
        final CheckBox layout3B = view.findViewById(R.id.layout_3B_checkBox);
        final CheckBox layout4 = view.findViewById(R.id.layout_4_checkBox);
        final AutoCompleteTextView file1_Input = view.findViewById(R.id.file1_Input);
        final AutoCompleteTextView file2_Input = view.findViewById(R.id.file2_Input);
        final AutoCompleteTextView file3_Input = view.findViewById(R.id.file3_Input);
        final AutoCompleteTextView file4_Input = view.findViewById(R.id.file4_Input);
        final TextInputLayout textInputLayout1 = view.findViewById(R.id.textInputLayout4);
        final TextInputLayout textInputLayout2 = view.findViewById(R.id.textInputLayout5);
        final TextInputLayout textInputLayout3 = view.findViewById(R.id.textInputLayout6);
        final TextInputLayout textInputLayout4 = view.findViewById(R.id.textInputLayout7);

        ArrayAdapter<String> fileNamesAdapter = new ArrayAdapter<>(CodeViewActivity.this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                fileNames);

        file1_Input.setAdapter(fileNamesAdapter);
        file2_Input.setAdapter(fileNamesAdapter);
        file3_Input.setAdapter(fileNamesAdapter);
        file4_Input.setAdapter(fileNamesAdapter);

        layout2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layout3A.setChecked(false);
                layout3B.setChecked(false);
                layout4.setChecked(false);
                textInputLayout1.setEnabled(true);
                file1_Input.setEnabled(true);
                file2_Input.setEnabled(true);
                textInputLayout2.setEnabled(true);
                file3_Input.setEnabled(false);
                textInputLayout3.setEnabled(false);
                file4_Input.setEnabled(false);
                textInputLayout4.setEnabled(false);
            }
        });

        layout3A.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layout2.setChecked(false);
                layout3B.setChecked(false);
                layout4.setChecked(false);
                textInputLayout1.setEnabled(true);
                file1_Input.setEnabled(true);
                file2_Input.setEnabled(true);
                textInputLayout2.setEnabled(true);
                file3_Input.setEnabled(true);
                textInputLayout3.setEnabled(true);
                file4_Input.setEnabled(false);
                textInputLayout4.setEnabled(false);
            }
        });
        layout3B.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layout2.setChecked(false);
                layout3A.setChecked(false);
                layout4.setChecked(false);
                textInputLayout1.setEnabled(true);
                file1_Input.setEnabled(true);
                file2_Input.setEnabled(true);
                textInputLayout2.setEnabled(true);
                file3_Input.setEnabled(true);
                textInputLayout3.setEnabled(true);
                file4_Input.setEnabled(false);
                textInputLayout4.setEnabled(false);
            }
        });
        layout4.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layout2.setChecked(false);
                layout3A.setChecked(false);
                layout3B.setChecked(false);
                textInputLayout1.setEnabled(true);
                file1_Input.setEnabled(true);
                file2_Input.setEnabled(true);
                textInputLayout2.setEnabled(true);
                file3_Input.setEnabled(true);
                textInputLayout3.setEnabled(true);
                file4_Input.setEnabled(true);
                textInputLayout4.setEnabled(true);
            }
        });

        switch (fileListSize) {
            case 1:
                file1_Input.setText(fileNames.get(0));
                file2_Input.setText(fileNames.get(0));
                layout2.setChecked(true);
                break;
            case 2:
                file1_Input.setText(fileNames.get(0));
                file2_Input.setText(fileNames.get(1));
                layout2.setChecked(true);
                break;
            case 3:
                file1_Input.setText(fileNames.get(0));
                file2_Input.setText(fileNames.get(1));
                file3_Input.setText(fileNames.get(2));
                layout3A.setChecked(true);
                break;
            case 4:
                file1_Input.setText(fileNames.get(0));
                file2_Input.setText(fileNames.get(1));
                file3_Input.setText(fileNames.get(2));
                file4_Input.setText(fileNames.get(3));
                layout4.setChecked(true);
                break;
            default:
                layout4.setChecked(true);
                break;
        }

        layoutDialog.setPositiveButton(getString(R.string.splitScreen), (dialog, which) -> {
            if (isEditorMode) {
                //Editor
                if (layout2.isChecked()) {
                    isScreenSplit = true;

                    activeLayout = ActiveLayout.Editor_SplitScreen2;
                    editor_Container_Main.setVisibility(View.GONE);
                    editor_Container_SplitScreen2.setVisibility(View.VISIBLE);
                    editor_Container_SplitScreen3.setVisibility(View.GONE);
                    editor_Container_SplitScreen4.setVisibility(View.GONE);

                    activeFileNames.clear();
                    String fileName1 = file1_Input.getText().toString();
                    String fileName2 = file2_Input.getText().toString();
                    activeFileNames.add(fileName1);
                    activeFileNames.add(fileName2);
                    expandableListDetail.put("Active Files", activeFileNames);

                    editorSplitScreen_2CommonPart(fileName1, fileName2);

                } else if (layout3A.isChecked()) {
                    isScreenSplit = true;

                    activeLayout = ActiveLayout.Editor_SplitScreen3A;
                    editor_Container_Main.setVisibility(View.GONE);
                    editor_Container_SplitScreen2.setVisibility(View.GONE);
                    editor_Container_SplitScreen3.setVisibility(View.VISIBLE);
                    editor_Container_SplitScreen4.setVisibility(View.GONE);

                    layout_3A_AlignView_Editor();

                    activeFileNames.clear();
                    String fileName1 = file1_Input.getText().toString();
                    String fileName2 = file2_Input.getText().toString();
                    String fileName3 = file3_Input.getText().toString();
                    activeFileNames.add(fileName1);
                    activeFileNames.add(fileName2);
                    activeFileNames.add(fileName3);
                    expandableListDetail.put("Active Files", activeFileNames);

                    editorSplitScreen_3CommonPart(fileName1, fileName2, fileName3);
                } else if (layout3B.isChecked()) {
                    isScreenSplit = true;

                    activeLayout = ActiveLayout.Editor_SplitScreen3B;
                    editor_Container_Main.setVisibility(View.GONE);
                    editor_Container_SplitScreen2.setVisibility(View.GONE);
                    editor_Container_SplitScreen3.setVisibility(View.VISIBLE);
                    editor_Container_SplitScreen4.setVisibility(View.GONE);

                    layout_3B_AlignView_Editor();

                    activeFileNames.clear();
                    String fileName1 = file1_Input.getText().toString();
                    String fileName2 = file2_Input.getText().toString();
                    String fileName3 = file3_Input.getText().toString();
                    activeFileNames.add(fileName1);
                    activeFileNames.add(fileName2);
                    activeFileNames.add(fileName3);
                    expandableListDetail.put("Active Files", activeFileNames);

                    editorSplitScreen_3CommonPart(fileName1, fileName2, fileName3);
                } else if (layout4.isChecked()) {
                    isScreenSplit = true;

                    activeLayout = ActiveLayout.Editor_SplitScreen4;
                    editor_Container_Main.setVisibility(View.GONE);
                    editor_Container_SplitScreen2.setVisibility(View.GONE);
                    editor_Container_SplitScreen3.setVisibility(View.GONE);
                    editor_Container_SplitScreen4.setVisibility(View.VISIBLE);

                    activeFileNames.clear();
                    String fileName1 = file1_Input.getText().toString();
                    String fileName2 = file2_Input.getText().toString();
                    String fileName3 = file3_Input.getText().toString();
                    String fileName4 = file4_Input.getText().toString();
                    activeFileNames.add(fileName1);
                    activeFileNames.add(fileName2);
                    activeFileNames.add(fileName3);
                    activeFileNames.add(fileName4);
                    expandableListDetail.put("Active Files", activeFileNames);

                    editorSplitScreen_4CommonPart(fileName1, fileName2, fileName3, fileName4);
                }
            } else {
                //Code View
                if (layout2.isChecked()) {
                    isScreenSplit = true;

                    activeLayout = ActiveLayout.CodeView_SplitScreen2;
                    codeView_Container_Main.setVisibility(View.GONE);
                    codeView_Container_SplitScreen2.setVisibility(View.VISIBLE);
                    codeView_Container_SplitScreen3.setVisibility(View.GONE);
                    codeView_Container_SplitScreen4.setVisibility(View.GONE);

                    activeFileNames.clear();
                    String fileName1 = file1_Input.getText().toString();
                    String fileName2 = file2_Input.getText().toString();
                    activeFileNames.add(fileName1);
                    activeFileNames.add(fileName2);
                    expandableListDetail.put("Active Files", activeFileNames);

                    codeViewSplitScreen_2CommonPart(fileName1, fileName2);

                } else if (layout3A.isChecked()) {
                    isScreenSplit = true;

                    activeLayout = ActiveLayout.CodeView_SplitScreen3A;
                    codeView_Container_Main.setVisibility(View.GONE);
                    codeView_Container_SplitScreen2.setVisibility(View.GONE);
                    codeView_Container_SplitScreen3.setVisibility(View.VISIBLE);
                    codeView_Container_SplitScreen4.setVisibility(View.GONE);

                    layout_3A_AlignView_CodeView();

                    activeFileNames.clear();
                    String fileName1 = file1_Input.getText().toString();
                    String fileName2 = file2_Input.getText().toString();
                    String fileName3 = file3_Input.getText().toString();
                    activeFileNames.add(fileName1);
                    activeFileNames.add(fileName2);
                    activeFileNames.add(fileName3);
                    expandableListDetail.put("Active Files", activeFileNames);

                    codeViewSplitScreen_3CommonPart(fileName1, fileName2, fileName3);
                } else if (layout3B.isChecked()) {
                    isScreenSplit = true;

                    activeLayout = ActiveLayout.CodeView_SplitScreen3B;
                    codeView_Container_Main.setVisibility(View.GONE);
                    codeView_Container_SplitScreen2.setVisibility(View.GONE);
                    codeView_Container_SplitScreen3.setVisibility(View.VISIBLE);
                    codeView_Container_SplitScreen4.setVisibility(View.GONE);

                    layout_3B_AlignView_CodeView();

                    activeFileNames.clear();
                    String fileName1 = file1_Input.getText().toString();
                    String fileName2 = file2_Input.getText().toString();
                    String fileName3 = file3_Input.getText().toString();
                    activeFileNames.add(fileName1);
                    activeFileNames.add(fileName2);
                    activeFileNames.add(fileName3);
                    expandableListDetail.put("Active Files", activeFileNames);

                    codeViewSplitScreen_3CommonPart(fileName1, fileName2, fileName3);
                } else if (layout4.isChecked()) {
                    isScreenSplit = true;

                    activeLayout = ActiveLayout.CodeView_SplitScreen4;
                    codeView_Container_Main.setVisibility(View.GONE);
                    codeView_Container_SplitScreen2.setVisibility(View.GONE);
                    codeView_Container_SplitScreen3.setVisibility(View.GONE);
                    codeView_Container_SplitScreen4.setVisibility(View.VISIBLE);

                    activeFileNames.clear();
                    String fileName1 = file1_Input.getText().toString();
                    String fileName2 = file2_Input.getText().toString();
                    String fileName3 = file3_Input.getText().toString();
                    String fileName4 = file4_Input.getText().toString();
                    activeFileNames.add(fileName1);
                    activeFileNames.add(fileName2);
                    activeFileNames.add(fileName3);
                    activeFileNames.add(fileName4);
                    expandableListDetail.put("Active Files", activeFileNames);

                    codeViewSplitScreen_4CommonPart(fileName1, fileName2, fileName3, fileName4);
                }
                List<String> tempList = activeFileNames;
                selectedFileNames = new ArrayList<>(tempList);
            }
        });

        layoutDialog.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {

        });
        layoutDialog.show();
    }

    private void layout_3A_AlignView_Editor() {
        View child1 = editor_Container_SplitScreen3.getChildAt(0);
        View child2 = editor_Container_SplitScreen3.getChildAt(1);
        com.clevergo.vcode.editorfiles.CodeView editor = null;
        LinearLayout linearLayout = null;

        if (child1.getClass().equals(com.clevergo.vcode.editorfiles.CodeView.class)) {
            editor = (com.clevergo.vcode.editorfiles.CodeView) child1;
            linearLayout = (LinearLayout) child2;
        } else if (child1.getClass().equals(LinearLayout.class)) {
            linearLayout = (LinearLayout) child1;
            editor = (com.clevergo.vcode.editorfiles.CodeView) child2;
        }

        editor_Container_SplitScreen3.removeAllViews();
        editor_Container_SplitScreen3.addView(editor, 0);
        editor_Container_SplitScreen3.addView(linearLayout, 1);
    }

    private void layout_3B_AlignView_Editor() {
        View child1 = editor_Container_SplitScreen3.getChildAt(0);
        View child2 = editor_Container_SplitScreen3.getChildAt(1);
        com.clevergo.vcode.editorfiles.CodeView editor = null;
        LinearLayout linearLayout = null;

        if (child1.getClass().equals(com.clevergo.vcode.editorfiles.CodeView.class)) {
            editor = (com.clevergo.vcode.editorfiles.CodeView) child1;
            linearLayout = (LinearLayout) child2;
        } else if (child1.getClass().equals(LinearLayout.class)) {
            linearLayout = (LinearLayout) child1;
            editor = (com.clevergo.vcode.editorfiles.CodeView) child2;
        }

        editor_Container_SplitScreen3.removeAllViews();
        editor_Container_SplitScreen3.addView(linearLayout, 0);
        editor_Container_SplitScreen3.addView(editor, 1);
    }

    private void editorSplitScreen_2CommonPart(String fileName1, String fileName2) {
        editorList.clear();

        for (View v : Objects.requireNonNull(MAIN_VIEW_HOLDER.get(editor_Container_SplitScreen2))) {
            editorList.add((com.clevergo.vcode.editorfiles.CodeView) v);
        }

        for (CodeViewFile file : fileList) {
            if (file.getName().equals(fileName1)) {
                if (file.isURL) {
                    setEditor(editorList.get(0), readFile(CodeViewActivity.this, file.getUrl()), file.getName());
                } else {
                    setEditor(editorList.get(0), readFile(CodeViewActivity.this, Uri.parse(file.getUri())), file.getName());
                }
            }
            if (file.getName().equals(fileName2)) {
                if (file.isURL) {
                    setEditor(editorList.get(1), readFile(CodeViewActivity.this, file.getUrl()), file.getName());
                } else {
                    setEditor(editorList.get(1), readFile(CodeViewActivity.this, Uri.parse(file.getUri())), file.getName());
                }
            }
        }
    }

    private void editorSplitScreen_3CommonPart(String fileName1, String fileName2, String fileName3) {
        editorList.clear();
        for (View v : Objects.requireNonNull(MAIN_VIEW_HOLDER.get(editor_Container_SplitScreen3))) {
            editorList.add((com.clevergo.vcode.editorfiles.CodeView) v);
        }
        for (CodeViewFile file : fileList) {
            if (file.getName().equals(fileName1)) {
                if (file.isURL) {
                    setEditor(editorList.get(0), readFile(CodeViewActivity.this, file.getUrl()), file.getName());
                } else {
                    setEditor(editorList.get(0), readFile(CodeViewActivity.this, Uri.parse(file.getUri())), file.getName());
                }
            }
            if (file.getName().equals(fileName2)) {
                if (file.isURL) {
                    setEditor(editorList.get(1), readFile(CodeViewActivity.this, file.getUrl()), file.getName());
                } else {
                    setEditor(editorList.get(1), readFile(CodeViewActivity.this, Uri.parse(file.getUri())), file.getName());
                }
            }

            if (file.getName().equals(fileName3)) {
                if (file.isURL) {
                    setEditor(editorList.get(2), readFile(CodeViewActivity.this, file.getUrl()), file.getName());
                } else {
                    setEditor(editorList.get(2), readFile(CodeViewActivity.this, Uri.parse(file.getUri())), file.getName());
                }
            }
        }
    }

    private void editorSplitScreen_4CommonPart(String fileName1, String fileName2, String fileName3, String fileName4) {
        editorList.clear();

        for (View v : Objects.requireNonNull(MAIN_VIEW_HOLDER.get(editor_Container_SplitScreen4))) {
            editorList.add((com.clevergo.vcode.editorfiles.CodeView) v);
        }

        for (CodeViewFile file : fileList) {
            if (file.getName().equals(fileName1)) {
                if (file.isURL) {
                    setEditor(editorList.get(0), readFile(CodeViewActivity.this, file.getUrl()), file.getName());
                } else {
                    setEditor(editorList.get(0), readFile(CodeViewActivity.this, Uri.parse(file.getUri())), file.getName());
                }
            }
            if (file.getName().equals(fileName2)) {
                if (file.isURL) {
                    setEditor(editorList.get(1), readFile(CodeViewActivity.this, file.getUrl()), file.getName());
                } else {
                    setEditor(editorList.get(1), readFile(CodeViewActivity.this, Uri.parse(file.getUri())), file.getName());
                }
            }

            if (file.getName().equals(fileName3)) {
                if (file.isURL) {
                    setEditor(editorList.get(2), readFile(CodeViewActivity.this, file.getUrl()), file.getName());
                } else {
                    setEditor(editorList.get(2), readFile(CodeViewActivity.this, Uri.parse(file.getUri())), file.getName());
                }
            }

            if (file.getName().equals(fileName4)) {
                if (file.isURL) {
                    setEditor(editorList.get(3), readFile(CodeViewActivity.this, file.getUrl()), file.getName());
                } else {
                    setEditor(editorList.get(3), readFile(CodeViewActivity.this, Uri.parse(file.getUri())), file.getName());
                }
            }
        }
    }

    private void layout_3A_AlignView_CodeView() {
        View child1 = codeView_Container_SplitScreen3.getChildAt(0);
        View child2 = codeView_Container_SplitScreen3.getChildAt(1);
        CodeView codeView = null;
        LinearLayout linearLayout = null;

        if (child1.getClass().equals(CodeView.class)) {
            codeView = (CodeView) child1;
            linearLayout = (LinearLayout) child2;
        } else if (child1.getClass().equals(LinearLayout.class)) {
            linearLayout = (LinearLayout) child1;
            codeView = (CodeView) child2;
        }

        codeView_Container_SplitScreen3.removeAllViews();
        codeView_Container_SplitScreen3.addView(codeView, 0);
        codeView_Container_SplitScreen3.addView(linearLayout, 1);
    }

    private void layout_3B_AlignView_CodeView() {
        View child1 = codeView_Container_SplitScreen3.getChildAt(0);
        View child2 = codeView_Container_SplitScreen3.getChildAt(1);
        CodeView codeView = null;
        LinearLayout linearLayout = null;

        if (child1.getClass().equals(CodeView.class)) {
            codeView = (CodeView) child1;
            linearLayout = (LinearLayout) child2;
        } else if (child1.getClass().equals(LinearLayout.class)) {
            linearLayout = (LinearLayout) child1;
            codeView = (CodeView) child2;
        }

        codeView_Container_SplitScreen3.removeAllViews();
        codeView_Container_SplitScreen3.addView(linearLayout, 0);
        codeView_Container_SplitScreen3.addView(codeView, 1);
    }

    private void codeViewSplitScreen_2CommonPart(String fileName1, String fileName2) {
        codeViewList.clear();

        for (View v : Objects.requireNonNull(MAIN_VIEW_HOLDER.get(codeView_Container_SplitScreen2))) {
            codeViewList.add((CodeView) v);
        }

        for (CodeViewFile file : fileList) {
            if (file.getName().equals(fileName1)) {
                if (sharedPreferences.getBoolean("individualTheme_codeView", false)) {
                    if (file.isURL) {
                        setCodeView_SplitScreen(codeViewList.get(0),
                                readFile(CodeViewActivity.this, file.getUrl()),
                                new Theme(sharedPreferences.getString("pref_codeviewThemes1", "monokai-sublime")));
                    } else {
                        setCodeView_SplitScreen(codeViewList.get(0),
                                readFile(CodeViewActivity.this, Uri.parse(file.getUri())),
                                new Theme(sharedPreferences.getString("pref_codeviewThemes1", "monokai-sublime")));
                    }
                } else {
                    if (file.isURL) {
                        setCodeView(codeViewList.get(0), readFile(CodeViewActivity.this, file.getUrl()));
                    } else {
                        setCodeView(codeViewList.get(0), readFile(CodeViewActivity.this, Uri.parse(file.getUri())));
                    }
                }
            }
            if (file.getName().equals(fileName2)) {
                if (sharedPreferences.getBoolean("individualTheme_codeView", false)) {
                    if (file.isURL) {
                        setCodeView_SplitScreen(codeViewList.get(1),
                                readFile(CodeViewActivity.this, file.getUrl()),
                                new Theme(sharedPreferences.getString("pref_codeviewThemes2", "monokai-sublime")));
                    } else {
                        setCodeView_SplitScreen(codeViewList.get(1),
                                readFile(CodeViewActivity.this, Uri.parse(file.getUri())),
                                new Theme(sharedPreferences.getString("pref_codeviewThemes2", "monokai-sublime")));
                    }
                } else {
                    if (file.isURL) {
                        setCodeView(codeViewList.get(1), readFile(CodeViewActivity.this, file.getUrl()));
                    } else {
                        setCodeView(codeViewList.get(1), readFile(CodeViewActivity.this, Uri.parse(file.getUri())));
                    }
                }
            }
        }
    }

    private void codeViewSplitScreen_3CommonPart(String fileName1, String fileName2, String fileName3) {
        codeViewList.clear();
        for (View v : Objects.requireNonNull(MAIN_VIEW_HOLDER.get(codeView_Container_SplitScreen3))) {
            codeViewList.add((CodeView) v);
        }

        for (CodeViewFile file : fileList) {
            if (file.getName().equals(fileName1)) {
                if (sharedPreferences.getBoolean("individualTheme_codeView", false)) {
                    if (file.isURL) {
                        setCodeView_SplitScreen(codeViewList.get(0),
                                readFile(CodeViewActivity.this, file.getUrl()),
                                new Theme(sharedPreferences.getString("pref_codeviewThemes1", "monokai-sublime")));
                    } else {
                        setCodeView_SplitScreen(codeViewList.get(0),
                                readFile(CodeViewActivity.this, Uri.parse(file.getUri())),
                                new Theme(sharedPreferences.getString("pref_codeviewThemes1", "monokai-sublime")));
                    }
                } else {
                    if (file.isURL) {
                        setCodeView(codeViewList.get(0), readFile(CodeViewActivity.this, file.getUrl()));
                    } else {
                        setCodeView(codeViewList.get(0), readFile(CodeViewActivity.this, Uri.parse(file.getUri())));
                    }
                }
            }
            if (file.getName().equals(fileName2)) {
                if (sharedPreferences.getBoolean("individualTheme_codeView", false)) {
                    if (file.isURL) {
                        setCodeView_SplitScreen(codeViewList.get(1),
                                readFile(CodeViewActivity.this, file.getUrl()),
                                new Theme(sharedPreferences.getString("pref_codeviewThemes2", "monokai-sublime")));
                    } else {
                        setCodeView_SplitScreen(codeViewList.get(1),
                                readFile(CodeViewActivity.this, Uri.parse(file.getUri())),
                                new Theme(sharedPreferences.getString("pref_codeviewThemes2", "monokai-sublime")));
                    }
                } else {
                    if (file.isURL) {
                        setCodeView(codeViewList.get(1), readFile(CodeViewActivity.this, file.getUrl()));
                    } else {
                        setCodeView(codeViewList.get(1), readFile(CodeViewActivity.this, Uri.parse(file.getUri())));
                    }
                }
            }

            if (file.getName().equals(fileName3)) {
                if (sharedPreferences.getBoolean("individualTheme_codeView", false)) {
                    if (file.isURL) {
                        setCodeView_SplitScreen(codeViewList.get(2),
                                readFile(CodeViewActivity.this, file.getUrl()),
                                new Theme(sharedPreferences.getString("pref_codeviewThemes3", "monokai-sublime")));
                    } else {
                        setCodeView_SplitScreen(codeViewList.get(2),
                                readFile(CodeViewActivity.this, Uri.parse(file.getUri())),
                                new Theme(sharedPreferences.getString("pref_codeviewThemes3", "monokai-sublime")));
                    }
                } else {
                    if (file.isURL) {
                        setCodeView(codeViewList.get(2), readFile(CodeViewActivity.this, file.getUrl()));
                    } else {
                        setCodeView(codeViewList.get(2), readFile(CodeViewActivity.this, Uri.parse(file.getUri())));
                    }
                }
            }
        }
    }

    private void codeViewSplitScreen_4CommonPart(String fileName1, String fileName2, String fileName3, String fileName4) {
        codeViewList.clear();

        for (View v : Objects.requireNonNull(MAIN_VIEW_HOLDER.get(codeView_Container_SplitScreen4))) {
            codeViewList.add((CodeView) v);
        }
        for (CodeViewFile file : fileList) {
            if (file.getName().equals(fileName1)) {
                if (sharedPreferences.getBoolean("individualTheme_codeView", false)) {
                    if (file.isURL) {
                        setCodeView_SplitScreen(codeViewList.get(0),
                                readFile(CodeViewActivity.this, file.getUrl()),
                                new Theme(sharedPreferences.getString("pref_codeviewThemes1", "monokai-sublime")));
                    } else {
                        setCodeView_SplitScreen(codeViewList.get(0),
                                readFile(CodeViewActivity.this, Uri.parse(file.getUri())),
                                new Theme(sharedPreferences.getString("pref_codeviewThemes1", "monokai-sublime")));
                    }
                } else {
                    if (file.isURL) {
                        setCodeView(codeViewList.get(0), readFile(CodeViewActivity.this, file.getUrl()));
                    } else {
                        setCodeView(codeViewList.get(0), readFile(CodeViewActivity.this, Uri.parse(file.getUri())));
                    }
                }
            }
            if (file.getName().equals(fileName2)) {
                if (sharedPreferences.getBoolean("individualTheme_codeView", false)) {
                    if (file.isURL) {
                        setCodeView_SplitScreen(codeViewList.get(1),
                                readFile(CodeViewActivity.this, file.getUrl()),
                                new Theme(sharedPreferences.getString("pref_codeviewThemes2", "monokai-sublime")));
                    } else {
                        setCodeView_SplitScreen(codeViewList.get(1),
                                readFile(CodeViewActivity.this, Uri.parse(file.getUri())),
                                new Theme(sharedPreferences.getString("pref_codeviewThemes2", "monokai-sublime")));
                    }
                } else {
                    if (file.isURL) {
                        setCodeView(codeViewList.get(1), readFile(CodeViewActivity.this, file.getUrl()));
                    } else {
                        setCodeView(codeViewList.get(1), readFile(CodeViewActivity.this, Uri.parse(file.getUri())));
                    }
                }
            }

            if (file.getName().equals(fileName3)) {
                if (sharedPreferences.getBoolean("individualTheme_codeView", false)) {
                    if (file.isURL) {
                        setCodeView_SplitScreen(codeViewList.get(2),
                                readFile(CodeViewActivity.this, file.getUrl()),
                                new Theme(sharedPreferences.getString("pref_codeviewThemes3", "monokai-sublime")));
                    } else {
                        setCodeView_SplitScreen(codeViewList.get(2),
                                readFile(CodeViewActivity.this, Uri.parse(file.getUri())),
                                new Theme(sharedPreferences.getString("pref_codeviewThemes3", "monokai-sublime")));
                    }
                } else {
                    if (file.isURL) {
                        setCodeView(codeViewList.get(2), readFile(CodeViewActivity.this, file.getUrl()));
                    } else {
                        setCodeView(codeViewList.get(2), readFile(CodeViewActivity.this, Uri.parse(file.getUri())));
                    }
                }
            }

            if (file.getName().equals(fileName4)) {
                if (sharedPreferences.getBoolean("individualTheme_codeView", false)) {
                    if (file.isURL) {
                        setCodeView_SplitScreen(codeViewList.get(3),
                                readFile(CodeViewActivity.this, file.getUrl()),
                                new Theme(sharedPreferences.getString("pref_codeviewThemes4", "monokai-sublime")));
                    } else {
                        setCodeView_SplitScreen(codeViewList.get(3),
                                readFile(CodeViewActivity.this, Uri.parse(file.getUri())),
                                new Theme(sharedPreferences.getString("pref_codeviewThemes4", "monokai-sublime")));
                    }
                } else {
                    if (file.isURL) {
                        setCodeView(codeViewList.get(3), readFile(CodeViewActivity.this, file.getUrl()));
                    } else {
                        setCodeView(codeViewList.get(3), readFile(CodeViewActivity.this, Uri.parse(file.getUri())));
                    }
                }
            }
        }
    }

    //endregion

    private void removeSplitScreen() {
        isScreenSplit = false;
        CodeViewFile file = fileList.get(fileList.size() - 1);

        if (isEditorMode) {
            activeLayout = ActiveLayout.Editor_Main;

            editor_Container_Main.setVisibility(View.VISIBLE);
            editor_Container_SplitScreen2.setVisibility(View.GONE);
            editor_Container_SplitScreen3.setVisibility(View.GONE);
            editor_Container_SplitScreen4.setVisibility(View.GONE);

            editorList.clear();
            for (View v : Objects.requireNonNull(MAIN_VIEW_HOLDER.get(editor_Container_Main))) {
                editorList.add((com.clevergo.vcode.editorfiles.CodeView) v);
            }

            if (file.isURL) {
                setEditor(editorList.get(0), readFile(CodeViewActivity.this, file.getUrl()), file.getName());
            } else {
                setEditor(editorList.get(0), readFile(CodeViewActivity.this, Uri.parse(file.getUri())), file.getName());
            }

        } else {
            activeLayout = ActiveLayout.CodeView_Main;

            codeView_Container_Main.setVisibility(View.VISIBLE);
            codeView_Container_SplitScreen2.setVisibility(View.GONE);
            codeView_Container_SplitScreen3.setVisibility(View.GONE);
            codeView_Container_SplitScreen4.setVisibility(View.GONE);

            codeViewList.clear();

            for (View v : Objects.requireNonNull(MAIN_VIEW_HOLDER.get(codeView_Container_Main))) {
                codeViewList.add((CodeView) v);
            }

            if (file.isURL) {
                setCodeView(codeViewList.get(0), readFile(CodeViewActivity.this, file.getUrl()));
                updateInfo(file.getUrl());
            } else {
                setCodeView(codeViewList.get(0), readFile(CodeViewActivity.this, Uri.parse(file.getUri())));
                updateInfo(Uri.parse(file.getUri()));
            }
        }
        activeFileNames.clear();
        activeFileNames.add(file.getName());
        expandableListDetail.put("Active Files", activeFileNames);
        currentActiveID = fileList.size() - 1;
        activeFilePosition = 0;
    }

    private void addNavMenu(final String fileName) {
        customWorkerThread.addWork(() -> {
            fileNames.add(fileName);
            expandableListDetail.put("Opened Files", fileNames);
        });
    }

    private void deleteCodeViewFile() {
        if (filesOpened <= 1) {
            Toast.makeText(CodeViewActivity.this, getString(R.string.cantDelOnlyFile), Toast.LENGTH_SHORT).show();
            return;
        }

        if (isScreenSplit) {
            for (int i = 0; i < fileList.size(); i++) {
                if (fileList.get(i).getName().equals(String.valueOf(selectedFileNames.get(activeFilePosition))))
                    currentActiveID = i;
            }
        }

        ((ViewGroup) allFileSwitcher_LinearLayout.getChildAt(currentActiveID).getParent()).removeViewAt(currentActiveID);
        expandableListDetail.remove("Opened Files");
        fileNames.remove(currentActiveID);
        expandableListDetail.put("Opened Files", fileNames);
        fileList.remove(currentActiveID);

        if (currentActiveID < fileList.size()) {
            for (int i = 0; i < allFileSwitcher_LinearLayout.getChildCount(); i++) {
                allFileSwitcher_LinearLayout.getChildAt(i).setId(i);
            }
        }

        if (loadIntoRAM) codeList.remove(currentActiveID);

        filesOpened--;
        currentActiveID = fileList.size() - 1;

        setCodeView(codeViewList.get(activeFilePosition), readFile(CodeViewActivity.this, Uri.parse(fileList.get(fileList.size() - 1).getUri())));

        //removeSplitScreen_2();
        updateInfo(currentActiveID);
    }

    private void updateInfo_SplitScreen(int clicked_id) {
        customWorkerThread.addWork(() -> {
            //TODO : Update Info Split Screen
            //String actionBarSubtitle = fileList.get(clicked_id).getName()
        });
    }

    private void addFileMenu() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(CodeViewActivity.this);
        final View addFileMenu = getLayoutInflater().inflate(R.layout.add_file_menu, null);
        alertDialog.setView(addFileMenu);
        MaterialCheckBox createFile_checkBox = addFileMenu.findViewById(R.id.createFile_checkBox);
        MaterialCheckBox openFile_checkBox = addFileMenu.findViewById(R.id.openFile_checkBox);
        MaterialCheckBox loadFile_checkBox = addFileMenu.findViewById(R.id.loadFromURL_checkBox);
        TextInputEditText urlInput = addFileMenu.findViewById(R.id.urlInput);
        TextInputLayout textInputLayout = addFileMenu.findViewById(R.id.textInputLayout3);
        openFile_checkBox.setChecked(true);

        createFile_checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (openFile_checkBox.isChecked()) openFile_checkBox.setChecked(false);
                if (loadFile_checkBox.isChecked()) loadFile_checkBox.setChecked(false);
                if (textInputLayout.getVisibility() == View.VISIBLE) {
                    textInputLayout.setVisibility(View.GONE);
                }
            }
        });

        openFile_checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (createFile_checkBox.isChecked()) createFile_checkBox.setChecked(false);
                if (loadFile_checkBox.isChecked()) loadFile_checkBox.setChecked(false);
                if (textInputLayout.getVisibility() == View.VISIBLE) {
                    textInputLayout.setVisibility(View.GONE);
                }
            }
        });

        loadFile_checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (createFile_checkBox.isChecked()) createFile_checkBox.setChecked(false);
                if (openFile_checkBox.isChecked()) openFile_checkBox.setChecked(false);
                if (textInputLayout.getVisibility() == View.GONE) {
                    textInputLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        urlInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isURL(s.toString())) urlInput.setError(getString(R.string.malformedURL));
            }
        });
        alertDialog.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
        });

        alertDialog.setPositiveButton(getString(R.string.continueStr), (dialog, which) -> {
            if (createFile_checkBox.isChecked()) {
                chooseDirectory(CodeViewActivity.this, CHOOSE_DIRECTORY_NORMAL);
            } else if (openFile_checkBox.isChecked()) {
                pickFile(CodeViewActivity.this);
            } else if (loadFile_checkBox.isChecked()) {
                urlOpen(Objects.requireNonNull(urlInput.getText()).toString());
                pickFile_TextView.setVisibility(View.GONE);
                //codeView_Main.setVisibility(View.VISIBLE);
                info_LinearLayout.setVisibility(View.VISIBLE);
                if (filesOpened > 0) {
                    allFileSwitcherParent.setVisibility(View.VISIBLE);
                    Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
                }
            }
        });

        alertDialog.show();
    }

    private void urlOpen(String urlContent) {
        customWorkerThread.addWork(() -> {
            try {
                URL url = new URL(urlContent);
                uiHandler.post(() -> {
                    addUI_FileURI(Uri.parse(url.toString()), true, true);
                });
                addNavMenu(getFileName(url.toString()));
                updateInfo(url);
                currentActiveID = fileList.size() - 1;
            } catch (MalformedURLException ignored) {
                uiHandler.post(() -> Toast.makeText(CodeViewActivity.this, getString(R.string.malformedURL), Toast.LENGTH_SHORT).show());
            }
        });
    }

    //TODO: Complete UI design file and it's Backend
    //TODO: Find some other compile sdk and try to integrate it
    private void downloadCompileSDK() {
        File sdkFile = new File(COMPILER_SDKs_LOCATION, "android_sdk.jar");
        if (!sdkFile.exists()) {
            StorageReference sdk_storage = storage.getReference().child("SDKs/Android/android-4.1.1.4.jar");
            sdk_storage.getFile(sdkFile).addOnSuccessListener(this, taskSnapshot -> {
                Toast.makeText(CodeViewActivity.this, "FILE DOWNLOADED" + taskSnapshot.getTotalByteCount(), Toast.LENGTH_SHORT).show();
                Log.println(Log.ASSERT, "FILE EXIST", String.valueOf(sdkFile.exists()));
            });
        } else {
            Toast.makeText(CodeViewActivity.this, "File already exist", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkUpdateErrors(String code) {
        AtomicInteger errors = new AtomicInteger();
        errors.addAndGet(JavaManager.getSemiColonErrors(code));
        errors.addAndGet(JavaManager.getBracketsErrors(code));
        uiHandler.post(() -> errorCount_TextView.setText(String.valueOf(errors)));
    }

    private void editFile() {
        addEditorButtons();
        isEditorMode = true;

        if (isScreenSplit) {
            switch (activeLayout) {
                case CodeView_SplitScreen2:
                    activeLayout = ActiveLayout.Editor_SplitScreen2;
                    codeView_Container_SplitScreen2.setVisibility(View.GONE);
                    editor_Container_SplitScreen2.setVisibility(View.VISIBLE);

                    codeViewList.clear();
                    editorSplitScreen_2CommonPart(activeFileNames.get(0), activeFileNames.get(1));
                    break;
                case CodeView_SplitScreen3A:
                    activeLayout = ActiveLayout.Editor_SplitScreen3A;
                    codeView_Container_SplitScreen3.setVisibility(View.GONE);
                    editor_Container_SplitScreen3.setVisibility(View.VISIBLE);

                    layout_3A_AlignView_Editor();

                    codeViewList.clear();
                    editorSplitScreen_3CommonPart(activeFileNames.get(0), activeFileNames.get(1), activeFileNames.get(2));
                    break;
                case CodeView_SplitScreen3B:
                    activeLayout = ActiveLayout.Editor_SplitScreen3B;
                    codeView_Container_SplitScreen3.setVisibility(View.GONE);
                    editor_Container_SplitScreen3.setVisibility(View.VISIBLE);

                    layout_3B_AlignView_Editor();

                    codeViewList.clear();
                    editorSplitScreen_3CommonPart(activeFileNames.get(0), activeFileNames.get(1), activeFileNames.get(2));
                    break;
                case CodeView_SplitScreen4:
                    activeLayout = ActiveLayout.Editor_SplitScreen4;
                    codeView_Container_SplitScreen4.setVisibility(View.GONE);
                    editor_Container_SplitScreen4.setVisibility(View.VISIBLE);

                    codeViewList.clear();
                    editorSplitScreen_4CommonPart(activeFileNames.get(0), activeFileNames.get(1), activeFileNames.get(2), activeFileNames.get(3));
                    break;
            }
        } else {
            editor_Container_Main.setVisibility(View.VISIBLE);
            codeView_Container_Main.setVisibility(View.GONE);

            CodeViewFile file = fileList.get(currentActiveID);
            List<View> tempList = new ArrayList<>(Objects.requireNonNull(MAIN_VIEW_HOLDER.get(editor_Container_Main)));
            if (editorList.size() == 0)
                editorList.add((com.clevergo.vcode.editorfiles.CodeView) tempList.get(0));
            com.clevergo.vcode.editorfiles.CodeView editor = editorList.get(0);
            setEditor(editor, readFile(CodeViewActivity.this, Uri.parse(file.getUri())), file.getName());
        }
    }

    private void setEditor(@NonNull final com.clevergo.vcode.editorfiles.CodeView editor, @NonNull final String code, @NonNull final String fileName) {
        customWorkerThread.addWork(() -> checkUpdateErrors(code));
        LanguageManager languageManager = new LanguageManager(CodeViewActivity.this, editor);
        //TODO: Add a Helper method to find the language of the code using the file Extension
        languageManager.applyTheme(LanguageName.valueOf(findProgrammingLanguage(fileName)), ThemeName.valueOf(sharedPreferences.getString("pref_editorThemes", "MONOKAI")));
        editor.setText(code);
        editor.setHighlightWhileTextChanging(true);
        editor.setEnableAutoIndentation(true);
        editor.setEnableLineNumber(true);
        editor.setLineNumberTextColor(Color.GRAY);
        editor.setLineNumberTextSize(getResources().getDimension(R.dimen.dimen15sp));
        editor.setTabLength(4);
        //editor.setIndentationStarts(indentationStarts);
        //editor.setIndentationEnds(indentationEnds);

        String[] languageKeywords = getResources().getStringArray(R.array.java_keywords);
        ArrayAdapter<String> codeAdapter = new ArrayAdapter<>(CodeViewActivity.this,
                R.layout.list_item_suggestion,
                R.id.suggestItemTextView,
                languageKeywords);

        editor.setAdapter(codeAdapter);
        editor.enablePairComplete(true);
        editor.enablePairCompleteCenterCursor(true);
        Map<Character, Character> pairCompleteMap = new HashMap<>();
        pairCompleteMap.put('{', '}');
        pairCompleteMap.put('[', ']');
        pairCompleteMap.put('(', ')');
        pairCompleteMap.put('<', '>');
        pairCompleteMap.put('"', '"');
        pairCompleteMap.put('\'', '\'');
        editor.setPairCompleteMap(pairCompleteMap);
        editor.addTextChangedListener(new EditorTextWatcher());
    }

    private void disableHighlighting(CodeView codeView, int totalLines) {
        if (sharedPreferences.getBoolean("pref_disableHighlightLargerFile", true)) {
            codeView.setOnHighlightListener(totalLines > CODE_HIGHLIGHTER_MAX_LINES ? null : CodeViewActivity.this);
        } else {
            codeView.setOnHighlightListener(CodeViewActivity.this);
        }
    }

    private void addEditorButtons() {
        LinearLayout buttonControls_LinearLayout = findViewById(R.id.buttonControls_LinearLayout);
        buttonControls_HorizontalScrollView.setVisibility(View.VISIBLE);
        miniError_LinearLayout.setVisibility(View.VISIBLE);
        buttonControls_LinearLayout.removeAllViews();
        for (int i = 0; i < buttonStringList.size(); i++) {
            AppCompatButton simpleButton = new AppCompatButton(CodeViewActivity.this);
            simpleButton.setId(i);
            String txt = buttonStringList.get(i);
            if (i == 0) txt = "->";
            if (i == 1) txt = "{}";
            simpleButton.setText(txt);
            simpleButton.setOnClickListener(new CodeViewActivity.BottomControlsClickListener());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ((int) getResources().getDimension(R.dimen.dimen40dp)),
                    ((int) getResources().getDimension(R.dimen.dimen45dp)));
            buttonControls_LinearLayout.addView(simpleButton, i, layoutParams);
        }
    }

    private void saveEdit() {
        if (isScreenSplit) {
            switch (activeLayout) {
                case Editor_SplitScreen2: {
                    String fileName1 = activeFileNames.get(0), fileName2 = activeFileNames.get(1);

                    for (CodeViewFile file : fileList) {
                        if (file.getName().equals(fileName1)) {
                            writeFile(CodeViewActivity.this, Uri.parse(file.getUri()), editorList.get(0).getText().toString());
                        }
                        if (file.getName().equals(fileName2)) {
                            writeFile(CodeViewActivity.this, Uri.parse(file.getUri()), editorList.get(1).getText().toString());
                        }
                    }
                    break;
                }
                case Editor_SplitScreen3A:
                case Editor_SplitScreen3B: {
                    String fileName1 = activeFileNames.get(0), fileName2 = activeFileNames.get(1), fileName3 = activeFileNames.get(2);

                    for (CodeViewFile file : fileList) {
                        if (file.getName().equals(fileName1)) {
                            writeFile(CodeViewActivity.this, Uri.parse(file.getUri()), editorList.get(0).getText().toString());
                        }
                        if (file.getName().equals(fileName2)) {
                            writeFile(CodeViewActivity.this, Uri.parse(file.getUri()), editorList.get(1).getText().toString());
                        }
                        if (file.getName().equals(fileName3)) {
                            writeFile(CodeViewActivity.this, Uri.parse(file.getUri()), editorList.get(2).getText().toString());
                        }
                    }
                    break;
                }
                case Editor_SplitScreen4: {
                    String fileName1 = activeFileNames.get(0),
                            fileName2 = activeFileNames.get(1),
                            fileName3 = activeFileNames.get(2),
                            fileName4 = activeFileNames.get(3);

                    for (CodeViewFile file : fileList) {
                        if (file.getName().equals(fileName1)) {
                            writeFile(CodeViewActivity.this, Uri.parse(file.getUri()), editorList.get(0).getText().toString());
                        }
                        if (file.getName().equals(fileName2)) {
                            writeFile(CodeViewActivity.this, Uri.parse(file.getUri()), editorList.get(1).getText().toString());
                        }
                        if (file.getName().equals(fileName3)) {
                            writeFile(CodeViewActivity.this, Uri.parse(file.getUri()), editorList.get(2).getText().toString());
                        }
                        if (file.getName().equals(fileName4)) {
                            writeFile(CodeViewActivity.this, Uri.parse(file.getUri()), editorList.get(3).getText().toString());
                        }
                    }
                    break;
                }
            }
        } else {
            CodeViewFile file = fileList.get(currentActiveID);
            writeFile(CodeViewActivity.this, Uri.parse(file.getUri()), editorList.get(0).getText().toString());
        }
        exitEditMode();
    }

    private void exitEditMode() {
        isEditorMode = false;

        buttonControls_HorizontalScrollView.setVisibility(View.GONE);
        miniError_LinearLayout.setVisibility(View.GONE);
        if (isScreenSplit) {
            switch (activeLayout) {
                case Editor_SplitScreen2:
                    activeLayout = ActiveLayout.CodeView_SplitScreen2;
                    editor_Container_SplitScreen2.setVisibility(View.GONE);
                    codeView_Container_SplitScreen2.setVisibility(View.VISIBLE);
                    editorList.clear();
                    codeViewSplitScreen_2CommonPart(activeFileNames.get(0), activeFileNames.get(1));
                    break;
                case Editor_SplitScreen3A:
                    activeLayout = ActiveLayout.CodeView_SplitScreen3A;
                    editor_Container_SplitScreen3.setVisibility(View.GONE);
                    codeView_Container_SplitScreen3.setVisibility(View.VISIBLE);
                    editorList.clear();

                    layout_3A_AlignView_CodeView();

                    codeViewSplitScreen_3CommonPart(activeFileNames.get(0), activeFileNames.get(1), activeFileNames.get(2));
                    break;
                case Editor_SplitScreen3B:
                    activeLayout = ActiveLayout.CodeView_SplitScreen3B;
                    editor_Container_SplitScreen3.setVisibility(View.GONE);
                    codeView_Container_SplitScreen3.setVisibility(View.VISIBLE);
                    editorList.clear();

                    layout_3B_AlignView_CodeView();

                    codeViewSplitScreen_3CommonPart(activeFileNames.get(0), activeFileNames.get(1), activeFileNames.get(2));
                    break;
                case Editor_SplitScreen4:
                    activeLayout = ActiveLayout.CodeView_SplitScreen4;
                    editor_Container_SplitScreen4.setVisibility(View.GONE);
                    codeView_Container_SplitScreen4.setVisibility(View.VISIBLE);
                    editorList.clear();
                    codeViewSplitScreen_4CommonPart(activeFileNames.get(0), activeFileNames.get(1),
                            activeFileNames.get(2), activeFileNames.get(3));
                    break;
            }
        } else {
            editor_Container_Main.setVisibility(View.GONE);
            codeView_Container_Main.setVisibility(View.VISIBLE);

            CodeViewFile file = fileList.get(currentActiveID);
            if (codeViewList.size() == 0) {
                codeViewList.add((CodeView) Objects.requireNonNull(MAIN_VIEW_HOLDER.get(codeView_Container_Main)).get(0));
            }
            CodeView codeView = codeViewList.get(0);
            setCodeView(codeView, readFile(CodeViewActivity.this, Uri.parse(file.getUri())));
        }
    }

    private void copyAll() {
        customWorkerThread.addWork(() -> {
            if (isScreenSplit) {
                showAlertDialog(getString(R.string.copy),
                        getString(R.string.copyAll_message),
                        CodeViewActivity.this);
            } else {
                if (isEditorMode) {
                    copyCode(CodeViewActivity.this, editorList.get(0).getText().toString());
                } else {
                    copyCode(CodeViewActivity.this, codeViewList.get(0).getCode());
                }
            }
        });
    }

    private MaterialButton createMaterialButton(CodeViewFile file, boolean isUrl, Uri uri, boolean isLastFile) {
        MaterialButton materialButton = new MaterialButton(CodeViewActivity.this);
        materialButton.setText(isUrl ? getFileName(uri.toString()) : file.getName());
        materialButton.setId(filesOpened);
        materialButton.setOnClickListener(CodeViewActivity.this);
        materialButton.setAllCaps(false);
        if (isUrl) {
            materialButton.setIcon(AppCompatResources.getDrawable(CodeViewActivity.this, R.drawable.ic_link));
        } else {
            setBtnIcon(CodeViewActivity.this, materialButton, getFileExtension(file.getName()));
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);

        layoutParams.setMargins(5, 2, 5, 2);
        materialButton.setLayoutParams(layoutParams);
        materialButton.setPadding(2, 0, 2, 0);
        materialButton.setIncludeFontPadding(true);
        materialButton.setSelected(isLastFile);
        materialButton.setTextColor(getResources().getColor(isLastFile ? R.color.cyan : R.color.white));

        return materialButton;
    }

    private void cloudUpload() {
        if (auth.getCurrentUser() != null) {
            CodeViewFile file = fileList.get(currentActiveID);
            Uri uploadFileUri = Uri.parse(file.getUri());
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setCustomMetadata("User Name", auth.getCurrentUser().getDisplayName())
                    .setCustomMetadata("Email", auth.getCurrentUser().getEmail())
                    .setCustomMetadata("App Version Code", String.valueOf(BuildConfig.VERSION_CODE))
                    .setCustomMetadata("App Version Name", BuildConfig.VERSION_NAME)
                    .setCustomMetadata("Android Version", String.valueOf(Build.VERSION.SDK_INT))
                    .build();

            UploadTask uploadTask = storageRef_UserFiles.child(file.getName()).putFile(uploadFileUri, metadata);
            progressDialog = new ProgressDialog(CodeViewActivity.this);
            progressDialog.setMessage("Uploading");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            uploadTask.addOnProgressListener(this, snapshot -> {
                progressDialog.show();
            }).addOnSuccessListener(CodeViewActivity.this, taskSnapshot -> {
                Toast.makeText(CodeViewActivity.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                progressDialog = null;
            }).addOnFailureListener(CodeViewActivity.this, e -> {
                progressDialog.dismiss();
                progressDialog = null;
            }).addOnCanceledListener(CodeViewActivity.this, () -> {
                progressDialog.dismiss();
                progressDialog = null;
            });
        }
    }

    private void addTextButton(com.clevergo.vcode.editorfiles.CodeView editor, final String text) {
        editor.getText().insert(editor.getSelectionStart(), text);
        if (text.equals(buttonStringList.get(1))
                || text.equals(buttonStringList.get(2))
                || text.equals(buttonStringList.get(3)))
            editor.setSelection(editor.getSelectionStart() - 1);
        //editor.getText().insert(editor.getSelectionStart(), "\t");
    }
    //endregion

    private void updateViewEditorLists() {
        MAIN_VIEW_HOLDER.put(codeView_Container_Main, List.of(findViewById(R.id.codeview_1)));
        MAIN_VIEW_HOLDER.put(codeView_Container_SplitScreen2,
                List.of(findViewById(R.id.codeview_2),
                        findViewById(R.id.codeview_3)));
        MAIN_VIEW_HOLDER.put(codeView_Container_SplitScreen3,
                List.of(findViewById(R.id.codeview_4),
                        findViewById(R.id.codeview_5),
                        findViewById(R.id.codeview_6)));
        MAIN_VIEW_HOLDER.put(codeView_Container_SplitScreen4,
                List.of(findViewById(R.id.codeview_7),
                        findViewById(R.id.codeview_8),
                        findViewById(R.id.codeview_9),
                        findViewById(R.id.codeview_10)));

        MAIN_VIEW_HOLDER.put(editor_Container_Main, List.of(findViewById(R.id.editor_1)));
        MAIN_VIEW_HOLDER.put(editor_Container_SplitScreen2,
                List.of(findViewById(R.id.editor_2),
                        findViewById(R.id.editor_3)));
        MAIN_VIEW_HOLDER.put(editor_Container_SplitScreen3,
                List.of(findViewById(R.id.editor_4),
                        findViewById(R.id.editor_5),
                        findViewById(R.id.editor_6)));
        MAIN_VIEW_HOLDER.put(editor_Container_SplitScreen4,
                List.of(findViewById(R.id.editor_7),
                        findViewById(R.id.editor_8),
                        findViewById(R.id.editor_9),
                        findViewById(R.id.editor_10)));
    }

    //region Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.codeviewer_menu, menu);

        return true;
    }
    //endregion

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.addFile_menu:
                if (drawerLayout.isOpen()) drawerLayout.close();
                addFileMenu();
                break;
            case R.id.settings_Menu:
                startActivity(new Intent(CodeViewActivity.this, SettingsActivity.class));
                break;
            case R.id.cloud_browser:
                startActivity(new Intent(CodeViewActivity.this, auth.getCurrentUser() == null ? AccountActivity.class : CloudFileBrowser.class));
                break;
            case R.id.account:
                startActivity(new Intent(CodeViewActivity.this, AccountActivity.class));
                break;
            case R.id.compile_sdk_download:
                downloadCompileSDK();
            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    //region CodeView OnHighlightListener & OnFindListener & Button OnCLickListener & Data from InfoBottomSheet
    @SuppressLint("SwitchIntDef")
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                if (!isFullScreen && configFullScreen) {
                    makeFullScreen(CodeViewActivity.this, appBarLayout, drawerLayout);
                    configFullScreen = false;
                }
                if (isScreenSplit) {
                    switch (activeLayout) {
                        case CodeView_SplitScreen2:
                            codeView_Container_SplitScreen2.setOrientation(LinearLayout.HORIZONTAL);
                            break;
                        case CodeView_SplitScreen3A:
                        case CodeView_SplitScreen3B:
                            codeView_Container_SplitScreen3.setOrientation(LinearLayout.HORIZONTAL);
                            codeView_Container_SplitScreen3_Child.setOrientation(LinearLayout.VERTICAL);
                            break;
                        case Editor_SplitScreen2:
                            editor_Container_SplitScreen2.setOrientation(LinearLayout.HORIZONTAL);
                            break;
                        case Editor_SplitScreen3A:
                        case Editor_SplitScreen3B:
                            editor_Container_SplitScreen3.setOrientation(LinearLayout.HORIZONTAL);
                            editor_Container_SplitScreen3_Child.setOrientation(LinearLayout.VERTICAL);
                            break;
                    }
                }
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                if (isFullScreen && !configFullScreen) {
                    revertFullScreen(CodeViewActivity.this, appBarLayout, drawerLayout);
                    configFullScreen = true;
                }
                if (isScreenSplit) {
                    switch (activeLayout) {
                        case CodeView_SplitScreen2:
                            codeView_Container_SplitScreen2.setOrientation(LinearLayout.VERTICAL);
                            break;
                        case CodeView_SplitScreen3A:
                        case CodeView_SplitScreen3B:
                            codeView_Container_SplitScreen3.setOrientation(LinearLayout.VERTICAL);
                            codeView_Container_SplitScreen3_Child.setOrientation(LinearLayout.HORIZONTAL);
                            break;
                        case Editor_SplitScreen2:
                            editor_Container_SplitScreen2.setOrientation(LinearLayout.VERTICAL);
                            break;
                        case Editor_SplitScreen3A:
                        case Editor_SplitScreen3B:
                            editor_Container_SplitScreen3.setOrientation(LinearLayout.VERTICAL);
                            editor_Container_SplitScreen3_Child.setOrientation(LinearLayout.HORIZONTAL);
                            break;
                    }
                }
                break;
            case Configuration.ORIENTATION_UNDEFINED:
                break;
        }
    }

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
        customWorkerThread.addWork(() -> {
            String lineInfo = codeViewList.get(activeFilePosition).getLineCount()
                    + ":"
                    + lineNumber
                    + "("
                    + codeViewList.get(activeFilePosition).getCode().length()
                    + ")";
            uiHandler.post(() -> lineInfo_TextView.setText(lineInfo));
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onFindResultReceived(int i, int i1, boolean b) {
        searchResult_Layout.setVisibility(View.VISIBLE);

        if (b) {
            if (isScreenSplit) {
                totalSearchResult += i1;
                findResultNum_TextView.setText(totalSearchResult + " results");
            } else {
                findResultNum_TextView.setText(i1 + " results");
            }
            searchResult = true;
        }
    }

    // Button OnclickListener
    @Override
    public void onClick(View view) {
        int clicked_ID = view.getId();
        view.setSelected(true);

        if (currentActiveID == clicked_ID) {
            Toast.makeText(CodeViewActivity.this, getString(R.string.fileAreadyDisplayed), Toast.LENGTH_SHORT).show();
            return;
        }
        final CodeViewFile file = fileList.get(clicked_ID);

        if (isEditorMode) {
            if (file.isURL) {
                setEditor(editorList.get(activeFilePosition), readFile(CodeViewActivity.this, file.getUrl()), file.getName());
            } else {
                setEditor(editorList.get(activeFilePosition), readFile(CodeViewActivity.this, Uri.parse(file.getUri())), file.getName());
            }

        } else {
            if (loadIntoRAM) {
                setCodeView(codeViewList.get(activeFilePosition), codeList.get(clicked_ID));
                if (isScreenSplit) {
                    selectedFileNames.set(activeFilePosition, ((MaterialButton) view).getText().toString());
                }
            } else {
                if (file.isURL) {
                    setCodeView(codeViewList.get(activeFilePosition), readFile(CodeViewActivity.this, file.getUrl()));
                } else {
                    setCodeView(codeViewList.get(activeFilePosition), readFile(CodeViewActivity.this, Uri.parse(file.getUri())));
                }
            }

            if (file.isURL) updateInfo(file.url);
            else updateInfo(clicked_ID);
        }

        currentActiveID = clicked_ID;
        for (int i = 0; i < allFileSwitcher_LinearLayout.getChildCount(); i++) {
            MaterialButton tempBtn = ((MaterialButton) ((LinearLayout) allFileSwitcher_LinearLayout.getChildAt(i)).getChildAt(0));
            if (i == currentActiveID) {
                tempBtn.setSelected(true);
                tempBtn.setTextColor(getResources().getColor(R.color.cyan));
            } else {
                tempBtn.setSelected(false);
                tempBtn.setTextColor(getResources().getColor(R.color.white));
            }
        }
        //isScreenSplit == true ? updateInfo_SplitScreen(clicked_ID) : updateInfo(clicked_ID);
    }

    // Get Data from InfoBottomSheet
    @Override
    public void sendInput(BottomSheetCode code) {
        switch (code) {
            case Compile:
                if (fileList.get(currentActiveID).isURL) {
                    Intent i = new Intent();
                    i.putExtra("code", codeViewList.get(activeFilePosition).getCode());
                    i.setAction(Intent.ACTION_VIEW);
                    i.setClass(CodeViewActivity.this, CompileLinkActivity.class);
                    startActivity(i);
                } else {
                    String fileName = fileNames.get(currentActiveID);
                    JavaCompiler javaCompiler = new JavaCompiler(CodeViewActivity.this, fileName, Uri.parse(fileList.get(currentActiveID).getUri()));
                    javaCompiler.compile();
                    /*
                    String fileName_without_extension = fileName.substring(0, fileName.indexOf("."));
                    Log.println(Log.ASSERT, "FILE NAME", fileName + "\t::\t" + fileName_without_extension);
                    File javaCompile_sdk = getAllCompilerSDKs(getFileExtension(fileName));
                    File tempFile = new File(String.valueOf(getExternalFilesDir("Compile")), fileName);
                    copyFile(this, Uri.parse(fileList.get(currentActiveID).getUri()), tempFile);

                    Main ecjMain = new Main(new PrintWriter(System.out), new PrintWriter(System.err), false, null, null);
                    ecjMain.compile(new String[]{"-classpath", javaCompile_sdk.getPath(), tempFile.getPath()});

                    com.android.dx.command.Main.main(new String[]{"--dex", "--output=" + getExternalFilesDir("Compile").getPath() + "/"+ fileName_without_extension + ".zip", getExternalFilesDir("Compile").getPath() + "/./"+ fileName_without_extension +".class"});

                    DexClassLoader cl = new DexClassLoader(getExternalFilesDir("Compile").getPath() + "/"+ fileName_without_extension + ".zip", getExternalFilesDir("Compile").getPath(), null, CodeViewActivity.class.getClassLoader());

                    try {
                        Class libProviderClazz = cl.loadClass(fileName_without_extension);
                        Object instance = libProviderClazz.newInstance();
                        instance.getClass().getDeclaredMethod("main", String[].class).invoke(null, (Object) new String[0]);
                    } catch (Exception e) {
                        Log.println(Log.ASSERT, "ERROR INIT", "Error while instantiating object: " + e.getMessage());
                        e.printStackTrace();
                    }

                     */
                }
                break;
            case Edit:
                editFile();
                break;
            case Search:
                //FIXME : Regex Search, Case Sensitive
                showSearchDialog();
                break;
            case CopyAll:
                copyAll();
                break;
            case FullScreen:
                if (isFullScreen) {
                    revertFullScreen(CodeViewActivity.this, appBarLayout, drawerLayout);
                } else {
                    makeFullScreen(CodeViewActivity.this, appBarLayout, drawerLayout);
                }
                break;
            case SplitScreen:
                if (!isScreenLandscape(CodeViewActivity.this))
                    showAlertDialog(getString(R.string.suggestion), getString(R.string.changeToLandscape), CodeViewActivity.this);
                splitScreen();
                break;
            case AddFile:
                if (drawerLayout.isOpen()) drawerLayout.close();
                addFileMenu();
                break;
            case DeleteFile:
                deleteCodeViewFile();
                break;
            case SetActiveCodeViewFile:
                break;
            case RemoveSplitScreen:
                removeSplitScreen();
                break;
            case ConvertToPDF:
                chooseDirectory(CodeViewActivity.this, CHOOSE_DIRECTORY_PDF);
                break;
            case SaveEdits:
                saveEdit();
                break;
            case ExitEditMode:
                exitEditMode();
                break;
            case CloudUpload:
                cloudUpload();
                break;
        }
    }

    private class BottomControlsClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            addTextButton(editorList.get(activeFilePosition), buttonStringList.get(v.getId()));
        }
    }

    private class EditorTextWatcher implements TextWatcher {
        com.clevergo.vcode.editorfiles.CodeView editor = editorList.get(activeFilePosition);

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            customWorkerThread.addWork(() -> {
                String code = editor.getText().toString();
                checkUpdateErrors(code);
                if (editor.getSelectionStart() == -1) editor.setSelection(0);
                String infoText = getSelectedLineNumber(code, editor.getSelectionStart()) +
                        ":" +
                        getCurrentColumn(code, editor.getSelectionStart()) +
                        "(" +
                        (getLines(code) + 1) +
                        ":" +
                        editor.getText().length() +
                        ")";
                uiHandler.post(() -> lineInfo_TextView.setText(infoText));
            });
        }
    }
//endregion
}
