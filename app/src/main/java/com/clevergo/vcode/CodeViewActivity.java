package com.clevergo.vcode;

import static com.clevergo.vcode.Helper.CHOOSE_DIRECTORY_CODE;
import static com.clevergo.vcode.Helper.CODE_HIGHLIGHTER_MAX_LINES;
import static com.clevergo.vcode.Helper.CREATE_FILE_CODE;
import static com.clevergo.vcode.Helper.PICK_FILE_CODE;
import static com.clevergo.vcode.Helper.chooseDirectory;
import static com.clevergo.vcode.Helper.copyCode;
import static com.clevergo.vcode.Helper.createACodeViewFile;
import static com.clevergo.vcode.Helper.createFile;
import static com.clevergo.vcode.Helper.getAllMethods;
import static com.clevergo.vcode.Helper.getFileExtension;
import static com.clevergo.vcode.Helper.getFileName;
import static com.clevergo.vcode.Helper.getFileSize;
import static com.clevergo.vcode.Helper.getLines;
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
import static com.clevergo.vcode.Helper.thisIsMobile;
import static com.clevergo.vcode.Helper.uiHandler;
import static com.clevergo.vcode.Helper.validateRegex;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
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
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import com.clevergo.vcode.codeviewer.CodeView;
import com.clevergo.vcode.codeviewer.Language;
import com.clevergo.vcode.codeviewer.Theme;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeViewActivity extends AppCompatActivity
        implements CodeView.OnHighlightListener,
        WebView.FindListener,
        View.OnClickListener,
        InfoBottomSheet.OnInputListener {

    public static final List<CodeViewFile> fileList = new ArrayList<>();
    private static final List<String> codeList = new ArrayList<>();
    public static int activeFilePosition = 0, currentActiveID = -1;
    public static String[] selectedFileNames = new String[2];
    public static boolean isScreenSplit = false;
    public static String activeSplitScreenFileName;
    public static int filesOpened = 0;
    public static List<Uri> uri_List;
    public static CustomWorkerThread customWorkerThread;
    private static ProgressDialog progressDialog;
    private final boolean loadIntoRAM = true;
    private final List<CodeView> codeViewList = new ArrayList<>();
    private final List<String> fileNames = new ArrayList<>();
    private final HashSet<String> activeFileNames = new HashSet<>();
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    //Expandable ListView
    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle = new ArrayList<>();
    HashMap<String, List<String>> expandableListDetail = new HashMap<>();
    private HashMap<String, Integer> allMethods = new HashMap<>();
    private NavigationView navView;
    private ConstraintLayout searchResult_Layout;
    private LinearLayout allFileSwitcher_LinearLayout, info_LinearLayout, codeView_Container, allFileSwitcherParent;
    private TextView pickFile_TextView, lineInfo_TextView, fileSize_TextView, searchWord_TextView, findResultNum_TextView;
    private CodeView codeView_Main, codeview_SplitScreen1;
    private boolean searchResult = false;
    private boolean configFullScreen = true;
    private String searchWord = "";
    private ActionBar actionBar;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_CODE && data != null && resultCode == RESULT_OK) {
            manageSingleFileIntent(data);
            manageMultipleFileIntent(data);
        }

        if (requestCode == CHOOSE_DIRECTORY_CODE && data != null && resultCode == RESULT_OK) {
            createFile(CodeViewActivity.this, data.getData());
        }

        if (requestCode == CREATE_FILE_CODE && data != null && resultCode == RESULT_OK) {
            manageSingleFileIntent(data);
            editFile();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (EditorActivity.reload) {
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
            EditorActivity.reload = false;
        }

        if (EditorActivity.newFileAdded) {
            for (int i = 0; i < fileList.size(); i++) {
                Uri uri = Uri.parse(fileList.get(i).getUri());
                String fileName = getFileName(CodeViewActivity.this, uri);
                if (!fileNames.contains(fileName)) {
                    addUI_FileURI(uri, i == (fileList.size() - 1), false);
                    addNavMenu(getFileName(CodeViewActivity.this, uri));
                    updateInfo(uri);
                    allFileSwitcherParent.setVisibility(View.VISIBLE);
                    setCodeView(codeViewList.get(activeFilePosition), readFile(CodeViewActivity.this, uri));
                }
            }
            currentActiveID = fileList.size() - 1;
            EditorActivity.newFileAdded = false;
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

        allFileSwitcher_LinearLayout = findViewById(R.id.allFileSwitcher);
        pickFile_TextView = findViewById(R.id.pickFileTextView);
        codeView_Main = findViewById(R.id.codeview_Main);
        info_LinearLayout = findViewById(R.id.info_LinearLayout);
        ImageView bottomSheet_ImageView = findViewById(R.id.bottomSheet_ImageView);
        lineInfo_TextView = findViewById(R.id.lineInfo_TextView);
        fileSize_TextView = findViewById(R.id.fileSize_TextView);
        ImageView findNext_ImageView = findViewById(R.id.findNext_ImageView);
        searchResult_Layout = findViewById(R.id.searchResult_Layout);
        searchWord_TextView = findViewById(R.id.searchWord_TextView);
        findResultNum_TextView = findViewById(R.id.findResultNum_TextView);
        ImageView closeSearch_ImageView = findViewById(R.id.closeSearch_ImageView);
        ImageView findPrev_ImageView = findViewById(R.id.findPrev_ImageView);
        codeview_SplitScreen1 = findViewById(R.id.codeview_SplitScreen1);
        codeView_Container = findViewById(R.id.codeView_Container);
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.navView);
        expandableListView = findViewById(R.id.fileSelector_ExpandableList);
        allFileSwitcherParent = findViewById(R.id.allFileSwitcherParent);
        actionBar = getSupportActionBar();

        expandableListTitle.add("Active Files");
        expandableListTitle.add("Opened Files");
        expandableListTitle.add("All Methods");
        expandableListTitle.add("Settings");

        expandableListAdapter = new ExpandableViewAdapterCustom(CodeViewActivity.this,
                expandableListTitle,
                expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            final CodeViewFile file = fileList.get(childPosition);
            if (groupPosition == 1) {
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
                searchWord = List.copyOf(allMethods.keySet()).get(childPosition);
                codeViewList.get(activeFilePosition).findAllAsync(searchWord);
                searchWord_TextView.setText(searchWord);
            }
            return false;
        });

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(false);

        codeViewList.add(codeView_Main);
        codeViewList.add(codeview_SplitScreen1);

        findNext_ImageView.setOnClickListener(a -> {
            if (searchResult) codeViewList.get(activeFilePosition).findNext(true);
        });

        findPrev_ImageView.setOnClickListener(a -> {
            if (searchResult) codeViewList.get(activeFilePosition).findNext(false);
        });

        closeSearch_ImageView.setOnClickListener(a -> {
            if (searchResult) {
                searchWord = "";
                codeViewList.get(activeFilePosition).findAllAsync("");
                searchResult_Layout.setVisibility(View.GONE);
            }
            searchResult = false;
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
                codeView_Main.setVisibility(View.VISIBLE);
                info_LinearLayout.setVisibility(View.VISIBLE);
                addUI_File(data);
                addNavMenu(getFileName(CodeViewActivity.this, data));
                Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
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
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void manageMultipleFileIntent(Intent data) {
        if (data.getClipData() != null) {
            pickFile_TextView.setVisibility(View.GONE);
            codeView_Main.setVisibility(View.VISIBLE);
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
                Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        }
    }

    @SuppressLint("SetTextI18n")
    private void setCodeView(CodeView codeView, String code) {
        codeView.setTheme(Theme.MONOKAI)
                .setCode(code)
                .setLanguage(Language.AUTO)
                .setWrapLine(false)
                .setShowLineNumber(true)
                .apply();

        codeView.setFindListener(this);
        disableHighlighting(codeView, getLines(code));

        lineInfo_TextView.setText(codeView.getLineCount() + ":Nil (" + codeView.getCode().length() + ")");
    }

    private void setCodeViewSplitScreen(final CodeView[] codeView, @NonNull final String[] code) {
        for (int i = 0; i < codeView.length; i++) {
            codeView[i].setTheme(Theme.MONOKAI)
                    .setCode(code[i])
                    .setLanguage(Language.AUTO)
                    .setWrapLine(false)
                    .setZoomEnabled(true)
                    .setShowLineNumber(true)
                    .apply();

            codeView[i].setFindListener(this);
            disableHighlighting(codeView[i], getLines(code[i]));
            codeView[i].setVisibility(View.VISIBLE);
        }
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
        if (isScreenSplit && isLastFile) selectedFileNames[activeFilePosition] = fileNameTemp;

        final CodeViewFile file = fileList.get(filesOpened);
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
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(5, 0, 5, 0);

        allFileSwitcher_LinearLayout.addView(materialButton, filesOpened, layoutParams);

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
            selectedFileNames[activeFilePosition] = getFileName(CodeViewActivity.this, data);

        MaterialButton materialButton = new MaterialButton(CodeViewActivity.this);
        materialButton.setText(fileList.get(fileList.size() - 1).getName());
        materialButton.setId(currentActiveID);
        materialButton.setOnClickListener(CodeViewActivity.this);
        materialButton.setAllCaps(false);
        setBtnIcon(CodeViewActivity.this, materialButton, getFileExtension(materialButton.getText().toString()));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(5, 0, 5, 0);

        allFileSwitcher_LinearLayout.addView(materialButton, fileList.size() - 1, layoutParams);

        updateInfo(data);
        filesOpened++;
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
            expandableListDetail.put("Active Files", List.copyOf(activeFileNames));
            allMethods.clear();
            allMethods = getAllMethods(codeViewList.get(activeFilePosition).getCode());
            expandableListDetail.put("All Methods", List.copyOf(allMethods.keySet()));
            navView.postInvalidate();

            uiHandler.post(() -> {
                Objects.requireNonNull(actionBar).setSubtitle(actionBarSubtitle);
                fileSize_TextView.setText(fileSize_Text);
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
            expandableListDetail.put("Active Files", List.copyOf(activeFileNames));
            allMethods.clear();
            allMethods = getAllMethods(codeViewList.get(activeFilePosition).getCode());
            expandableListDetail.put("All Methods", List.copyOf(allMethods.keySet()));
            navView.postInvalidate();

            uiHandler.post(() -> {
                Objects.requireNonNull(actionBar).setSubtitle(actionBarSubtitle);
                fileSize_TextView.setText(fileSize_Text);
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
            expandableListDetail.put("Active Files", List.copyOf(activeFileNames));
            allMethods.clear();
            allMethods = getAllMethods(codeViewList.get(activeFilePosition).getCode());
            expandableListDetail.put("All Methods", List.copyOf(allMethods.keySet()));
            navView.postInvalidate();

            uiHandler.post(() -> {
                Objects.requireNonNull(actionBar).setSubtitle(actionBarSubtitle);
                fileSize_TextView.setText(fileSize_Text);
            });
        });
    }

    private void updateInfo(URL url) {
        customWorkerThread.addWork(() -> {
            String actionBarSubtitle = getFileName(url.toString());
            String fileSize_Text = "N/A";

            activeFileNames.clear();
            activeFileNames.add(actionBarSubtitle);
            expandableListDetail.put("Active Files", List.copyOf(activeFileNames));
            allMethods.clear();
            allMethods = getAllMethods(codeViewList.get(activeFilePosition).getCode());
            expandableListDetail.put("All Methods", List.copyOf(allMethods.keySet()));
            navView.postInvalidate();

            uiHandler.post(() -> {
                Objects.requireNonNull(actionBar).setSubtitle(actionBarSubtitle);
                fileSize_TextView.setText(fileSize_Text);
            });
        });
    }

    private void showSearchDialog() {
        AlertDialog.Builder searchDialog = new AlertDialog.Builder(CodeViewActivity.this);
        final View searchDialogView = getLayoutInflater().inflate(R.layout.search_dialog, null);
        final TextInputEditText findTextInput = searchDialogView.findViewById(R.id.searchInputTextField);
        final SwitchCompat isRegexSwitch = searchDialogView.findViewById(R.id.isRegex_switch);
        final SwitchCompat isExactMatchSwitch = searchDialogView.findViewById(R.id.exactMatch_switch);

        isRegexSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            TextWatcher textWatcher = validateRegex(CodeViewActivity.this, findTextInput);

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

    private void splitScreenPositiveButton_2(final String[] fileNameArray, int[] file1, int[] file2, @NonNull final CodeView[] codeViews) {
        isScreenSplit = true;
        LinearLayout.LayoutParams params = ((LinearLayout.LayoutParams) codeView_Main.getLayoutParams());
        params.setMargins(0, 0, 0, 5);
        params.weight = 1;
        codeView_Main.setLayoutParams(params);

        params = ((LinearLayout.LayoutParams) codeview_SplitScreen1.getLayoutParams());
        params.weight = 1;
        params.setMargins(0, 5, 0, 0);
        codeview_SplitScreen1.setLayoutParams(params);
        codeview_SplitScreen1.setVisibility(View.VISIBLE);

        params = null;

        String[] codes = new String[2];
        selectedFileNames[0] = fileNameArray[file1[0]];
        selectedFileNames[1] = fileNameArray[file2[0]];

        for (int i = 0; i < fileList.size(); i++) {
            String fileName = fileList.get(i).getName();

            if (Objects.equals(fileNameArray[file1[0]], fileName)) {
                codes[0] = readFile(CodeViewActivity.this, Uri.parse(fileList.get(i).getUri()));
            }
            if (Objects.equals(fileNameArray[file2[0]], fileName)) {
                codes[1] = readFile(CodeViewActivity.this, Uri.parse(fileList.get(i).getUri()));
            }
        }

        activeFileNames.clear();
        activeFileNames.add(fileNameArray[file1[0]]);
        activeFileNames.add(fileNameArray[file2[0]]);
        expandableListDetail.put("Active Files", List.of(fileNameArray[file1[0]], fileNameArray[file2[0]]));

        setCodeViewSplitScreen(codeViews, codes);
        codes = null;
    }

    private void splitScreen_2(@NonNull final CodeView[] codeViews) {
        if (fileList.size() == 1) {
            splitScreenPositiveButton_2(new String[]{
                    fileList.get(0).getName(),
                    fileList.get(0).getName()
            }, new int[]{0}, new int[]{0}, codeViews);

        } else if (fileList.size() == 2) {
            splitScreenPositiveButton_2(new String[]{
                    fileList.get(0).getName(),
                    fileList.get(1).getName()
            }, new int[]{0}, new int[]{1}, codeViews);
        } else {
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
                splitScreenPositiveButton_2(fileNameArray, file1, file2, codeViews);
            });
            alertBuilder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
            });
            alertBuilder.create();
            alertBuilder.show();
        }
    }

    private void removeSplitScreen_2() {
        if (!isScreenSplit) return;

        CodeView activeCodeView = codeViewList.get(activeFilePosition);

        LinearLayout.LayoutParams params = null;

        for (CodeView codeView : codeViewList) {
            if (codeView != activeCodeView) {
                params = ((LinearLayout.LayoutParams) codeView.getLayoutParams());
                params.weight = 0;
                codeView.setLayoutParams(params);

                codeView.setVisibility(View.GONE);
            } else {
                params = ((LinearLayout.LayoutParams) codeView.getLayoutParams());
                params.weight = 2;
                activeCodeView.setLayoutParams(params);
            }
        }


        isScreenSplit = false;
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
                if (fileList.get(i).getName().equals(String.valueOf(selectedFileNames[activeFilePosition])))
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

        removeSplitScreen_2();
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
                chooseDirectory(CodeViewActivity.this);
            } else if (openFile_checkBox.isChecked()) {
                pickFile(CodeViewActivity.this);
            } else if (loadFile_checkBox.isChecked()) {
                urlOpen(Objects.requireNonNull(urlInput.getText()).toString());
                pickFile_TextView.setVisibility(View.GONE);
                codeView_Main.setVisibility(View.VISIBLE);
                info_LinearLayout.setVisibility(View.VISIBLE);
                if (filesOpened > 0) {
                    allFileSwitcherParent.setVisibility(View.VISIBLE);
                    actionBar.setDisplayHomeAsUpEnabled(true);
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

    private void editFile() {
        Intent i = new Intent();
        i.putExtra("currentFileObject", fileList.get(currentActiveID));
        i.setAction(Intent.ACTION_VIEW);
        i.setClass(CodeViewActivity.this, EditorActivity.class);
        startActivity(i);
    }

    private void disableHighlighting(CodeView codeView, int totalLines) {
        codeView.setOnHighlightListener(totalLines > CODE_HIGHLIGHTER_MAX_LINES ? null : CodeViewActivity.this);
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
                //TODO : Navigate to Setting Page
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }
    //endregion

    //region CodeView OnHighlightListener & OnFindListener & Button OnCLickListener & Data from InfoBottomSheet

    @SuppressLint("SwitchIntDef")
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                if (!isFullScreen && configFullScreen) {
                    makeFullScreen(CodeViewActivity.this);
                    configFullScreen = false;
                }
                if (isScreenSplit) {
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
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                if (isFullScreen && !configFullScreen) {
                    revertFullScreen(CodeViewActivity.this);
                    configFullScreen = true;
                }
                if (isScreenSplit) {
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
            searchResult = true;
            findResultNum_TextView.setText(i1 + " results");
        }
    }

    // Button OnclickListener
    @Override
    public void onClick(View view) {
        int clicked_ID = view.getId();
        if (currentActiveID == clicked_ID) {
            Toast.makeText(CodeViewActivity.this, getString(R.string.fileAreadyDisplayed), Toast.LENGTH_SHORT).show();
            return;
        }

        final CodeViewFile file = fileList.get(clicked_ID);

        if (loadIntoRAM) {
            setCodeView(codeViewList.get(activeFilePosition), codeList.get(clicked_ID));
            if (isScreenSplit)
                selectedFileNames[activeFilePosition] = ((MaterialButton) view).getText().toString();
        } else {
            if (file.isURL)
                setCodeView(codeViewList.get(activeFilePosition), readFile(CodeViewActivity.this, file.getUrl()));
            else
                setCodeView(codeViewList.get(activeFilePosition), readFile(CodeViewActivity.this, Uri.parse(file.getUri())));
        }

        currentActiveID = clicked_ID;
        if (file.isURL) updateInfo(file.url);
        else updateInfo(clicked_ID);
        //isScreenSplit == true ? updateInfo_SplitScreen(clicked_ID) : updateInfo(clicked_ID);
    }

    // Get Data from InfoBottomSheet
    @Override
    public void sendInput(BottomSheetCode code) {
        switch (code) {
            case Compile:
                //TODO : Compile Case
                break;
            case Edit:
                editFile();
                break;
            case Search:
                //FIXME : Regex Search, Case Sensitive
                showSearchDialog();
                break;
            case CopyAll:
                customWorkerThread.addWork(() -> copyCode(CodeViewActivity.this, codeViewList.get(activeFilePosition).getCode()));
                break;
            case FullScreen:
                if (isFullScreen) {
                    revertFullScreen(CodeViewActivity.this);
                } else {
                    makeFullScreen(CodeViewActivity.this);
                }
                break;
            case SplitScreen:
                if (!isScreenLandscape(CodeViewActivity.this))
                    showAlertDialog(getString(R.string.suggestion), getString(R.string.changeToLandscape), CodeViewActivity.this);
                if (thisIsMobile) {
                    splitScreen_2(new CodeView[]{codeView_Main, codeview_SplitScreen1});
                } else {
                    //TODO : Tablet Model Screen
                    //splitScreen_Tablet();
                }
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
                removeSplitScreen_2();
                break;
        }
    }
//endregion
}
