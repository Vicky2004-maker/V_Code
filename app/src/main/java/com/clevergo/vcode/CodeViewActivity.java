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
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import com.clevergo.vcode.codeviewer.CodeView;
import com.clevergo.vcode.codeviewer.Language;
import com.clevergo.vcode.codeviewer.Theme;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

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

    public static int activeFilePosition = 0, currentActiveID = -1;

    public static String[] selectedFileNames = new String[2];
    public static boolean isScreenSplit = false;
    private static ProgressDialog progressDialog;
    private static List<CodeViewFile> fileList = new ArrayList<>();
    private static List<Uri> uri_List;
    private static List<String> codeList = new ArrayList<>();
    private static int filesOpened = 0;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    //Expandable ListView
    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle = new ArrayList<>();
    HashMap<String, List<String>> expandableListDetail = new HashMap<>();
    private NavigationView navView;
    private ConstraintLayout searchResult_Layout;
    private LinearLayout allFileSwitcher_LinearLayout, info_LinearLayout, codeView_Container, allFileSwitcherParent;
    private TextView pickFile_TextView, lineInfo_TextView, fileSize_TextView, searchWord_TextView, findResultNum_TextView;
    private CodeView codeView_Main, codeview_SplitScreen1;
    private boolean loadIntoRAM = true, searchResult = false, configFullScreen = true;
    private String searchWord = "";
    private List<CodeView> codeViewList = new ArrayList<>();
    //TODO : Thread Started, Change some workflow to async
    private CustomWorkerThread customWorkerThread;
    private List<String> fileNames = new ArrayList<>();
    private HashSet<String> activeFileNames = new HashSet<>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Helper.PICK_FILE_CODE && data != null && resultCode == Activity.RESULT_OK) {
            manageSingleFileIntent(data);
            manageMultipleFileIntent(data);
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

        expandableListTitle.add("Active Files");
        expandableListTitle.add("Opened Files");
        expandableListTitle.add("Settings");

        expandableListAdapter = new ExpandableViewAdapterCustom(CodeViewActivity.this,
                expandableListTitle,
                expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            if (groupPosition == 1) {
                updateInfo(childPosition);
                setCodeView(codeViewList.get(activeFilePosition), Helper.readFile(CodeViewActivity.this, Uri.parse(fileList.get(childPosition).getUri())));
            }
            return false;
        });

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);

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
                codeViewList.get(activeFilePosition).findAllAsync("");
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

        if ((Intent.ACTION_SEND.equals(getIntent().getAction()) || Intent.ACTION_VIEW.equals(getIntent().getAction())) && getIntent().getType() != null) {
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
                addNavMenu(Helper.getFileName(CodeViewActivity.this, data));
            } else if (filesOpened > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (fileList.stream().anyMatch(fileObj -> fileObj.getUri().equals(data.getData().toString()))) {
                        Toast.makeText(CodeViewActivity.this, getString(R.string.fileAlreadyPicked), Toast.LENGTH_LONG).show();
                    } else {
                        addUI_File(data);
                        addNavMenu(Helper.getFileName(CodeViewActivity.this, data));
                        allFileSwitcherParent.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (uri_List.contains(data.getData())) {
                        Toast.makeText(CodeViewActivity.this, getString(R.string.fileAlreadyPicked), Toast.LENGTH_LONG).show();
                    } else {
                        addUI_File(data);
                        addNavMenu(Helper.getFileName(CodeViewActivity.this, data));
                        allFileSwitcherParent.setVisibility(View.VISIBLE);
                    }
                }
            }

            if (filesOpened == 2)
                Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
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
                        addUI_FileURI(uri, i == (data.getClipData().getItemCount() - 1));
                        addNavMenu(Helper.getFileName(CodeViewActivity.this, uri));
                    }
                }
            } else {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    if (uri_List.contains(uri)) {
                        Toast.makeText(CodeViewActivity.this, getString(R.string.fileAlreadyPicked), Toast.LENGTH_LONG).show();
                    } else {
                        addUI_FileURI(uri, i == (data.getClipData().getItemCount() - 1));
                        addNavMenu(Helper.getFileName(CodeViewActivity.this, uri));
                    }
                }
            }
            if (filesOpened >= 1)
                Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        }
    }

    @SuppressLint("SetTextI18n")
    private void setCodeView(CodeView codeView, String code) {
        codeView.setOnHighlightListener(this)
                .setTheme(Theme.MONOKAI)
                .setCode(code)
                .setLanguage(Language.AUTO)
                .setWrapLine(false)
                .setShowLineNumber(true)
                .apply();

        codeView.setFindListener(this);

        lineInfo_TextView.setText(codeView.getLineCount() + ":Nil (" + codeView.getCode().length() + ")");
    }

    private void setCodeViewSplitScreen(final CodeView[] codeView, @NonNull final String[] code) {
        for (int i = 0; i < codeView.length; i++) {
            codeView[i].setOnHighlightListener(this)
                    .setTheme(Theme.MONOKAI)
                    .setCode(code[i])
                    .setLanguage(Language.AUTO)
                    .setWrapLine(false)
                    .setZoomEnabled(true)
                    .setShowLineNumber(true)
                    .apply();

            codeView[i].setFindListener(this);
            codeView[i].setVisibility(View.VISIBLE);
        }
    }

    private CodeViewFile createACodeViewFile(Intent data) {
        return new CodeViewFile(filesOpened,
                Double.parseDouble(Helper.getFileSize(CodeViewActivity.this, data)),
                Helper.getFileName(CodeViewActivity.this, data),
                data.getData().toString(),
                Helper.getFileExtension(CodeViewActivity.this, data));
    }

    private void addUI_FileURI(final Uri uri, final boolean isLastFile) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri_List.add(uri);
        }

        fileList.add(createACodeViewFile(uri));
        currentActiveID++;

        if (loadIntoRAM) {
            codeList.add(Helper.readFile(CodeViewActivity.this, uri));
            if (isLastFile)
                setCodeView(codeViewList.get(activeFilePosition), codeList.get(filesOpened));
        } else if (isLastFile) {
            setCodeView(codeViewList.get(activeFilePosition), Helper.readFile(CodeViewActivity.this, uri));
        }

        String fileNameTemp = Helper.getFileName(CodeViewActivity.this, uri);
        if (isScreenSplit && isLastFile) selectedFileNames[activeFilePosition] = fileNameTemp;

        MaterialButton materialButton = new MaterialButton(CodeViewActivity.this);
        materialButton.setText(fileList.get(filesOpened).getName());
        materialButton.setId(filesOpened);
        materialButton.setOnClickListener(CodeViewActivity.this);
        materialButton.setAllCaps(false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(5, 0, 5, 0);

        allFileSwitcher_LinearLayout.addView(materialButton, filesOpened, layoutParams);

        if (isLastFile) updateInfo(uri);
        filesOpened++;
    }

    private CodeViewFile createACodeViewFile(Uri uri) {
        return new CodeViewFile(filesOpened,
                Double.parseDouble(Helper.getFileSize(CodeViewActivity.this, uri)),
                Helper.getFileName(CodeViewActivity.this, uri),
                uri.toString(),
                Helper.getFileExtension(CodeViewActivity.this, uri));
    }

    private void addUI_File(Intent data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri_List.add(data.getData());
        }

        fileList.add(createACodeViewFile(data));
        currentActiveID++;

        if (loadIntoRAM) {
            codeList.add(Helper.readFile(CodeViewActivity.this, data.getData()));
            setCodeView(codeViewList.get(activeFilePosition), codeList.get(filesOpened));
        } else {
            setCodeView(codeViewList.get(activeFilePosition), Helper.readFile(CodeViewActivity.this, data.getData()));
        }

        if (isScreenSplit)
            selectedFileNames[activeFilePosition] = Helper.getFileName(CodeViewActivity.this, data);

        MaterialButton materialButton = new MaterialButton(CodeViewActivity.this);
        materialButton.setText(fileList.get(fileList.size() - 1).getName());
        materialButton.setId(currentActiveID);
        materialButton.setOnClickListener(CodeViewActivity.this);
        materialButton.setAllCaps(false);
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
            String actionBarSubtitle = Helper.getFileName(CodeViewActivity.this, uri);
            String fileSize_Text = actionBarSubtitle
                    + " - "
                    + Helper.getFileSize(CodeViewActivity.this, uri) + " KB";

            activeFileNames.clear();
            activeFileNames.add(actionBarSubtitle);
            expandableListDetail.put("Active Files", List.copyOf(activeFileNames));
            navView.postInvalidate();

            Helper.uiHandler.post(() -> {
                Objects.requireNonNull(getSupportActionBar()).setSubtitle(actionBarSubtitle);
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
            navView.postInvalidate();

            Helper.uiHandler.post(() -> {
                Objects.requireNonNull(getSupportActionBar()).setSubtitle(actionBarSubtitle);
                fileSize_TextView.setText(fileSize_Text);
            });
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateInfo(Intent data) {
        customWorkerThread.addWork(() -> {
            String actionBarSubtitle = Helper.getFileName(CodeViewActivity.this, data);
            String fileSize_Text = actionBarSubtitle
                    + " - "
                    + Helper.getFileSize(CodeViewActivity.this, data) + " KB";

            activeFileNames.clear();
            activeFileNames.add(actionBarSubtitle);
            expandableListDetail.put("Active Files", List.copyOf(activeFileNames));
            navView.postInvalidate();

            Helper.uiHandler.post(() -> {
                Objects.requireNonNull(getSupportActionBar()).setSubtitle(actionBarSubtitle);
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
            isScreenSplit = true;
            LinearLayout.LayoutParams params = ((LinearLayout.LayoutParams) codeView_Main.getLayoutParams());
            params.weight = 1;
            codeView_Main.setLayoutParams(params);

            params = ((LinearLayout.LayoutParams) codeview_SplitScreen1.getLayoutParams());
            params.weight = 1;
            codeview_SplitScreen1.setLayoutParams(params);
            codeview_SplitScreen1.setVisibility(View.VISIBLE);

            params = null;

            String[] codes = new String[2];
            selectedFileNames[0] = fileNameArray[file1[0]];
            selectedFileNames[1] = fileNameArray[file2[0]];

            for (int i = 0; i < fileList.size(); i++) {
                String fileName = fileList.get(i).getName();

                if (Objects.equals(fileNameArray[file1[0]], fileName)) {
                    codes[0] = Helper.readFile(CodeViewActivity.this, Uri.parse(fileList.get(i).getUri()));
                }
                if (Objects.equals(fileNameArray[file2[0]], fileName)) {
                    codes[1] = Helper.readFile(CodeViewActivity.this, Uri.parse(fileList.get(i).getUri()));
                }
            }

            activeFileNames.clear();
            activeFileNames.add(fileNameArray[file1[0]]);
            activeFileNames.add(fileNameArray[file2[0]]);
            expandableListDetail.put("Active Files", List.of(fileNameArray[file1[0]], fileNameArray[file2[0]]));

            setCodeViewSplitScreen(codeViews, codes);
            codes = null;
        });
        alertBuilder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
        });
        alertBuilder.create();
        alertBuilder.show();
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
        //TODO : Delete File Method Implementation when in Split Screen
        if (filesOpened <= 1) {
            Toast.makeText(CodeViewActivity.this, getString(R.string.cantDelOnlyFile), Toast.LENGTH_SHORT).show();
            return;
        }

        if (isScreenSplit) {

        } else {
            //TODO : Ask permission to delete file using alert dialog
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

            setCodeView(codeViewList.get(activeFilePosition), Helper.readFile(CodeViewActivity.this, Uri.parse(fileList.get(fileList.size() - 1).getUri())));

        }
    }

    private void updateInfo_SplitScreen(int clicked_id) {
        customWorkerThread.addWork(() -> {
            //TODO : Update Info Split Screen
            //String actionBarSubtitle = fileList.get(clicked_id).getName()
        });
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

    //region CodeView OnHighlightListener & OnFindListener & Button OnCLickListener & Data from InfoBottomSheet

    @SuppressLint("SwitchIntDef")
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                if (!Helper.isFullScreen && configFullScreen) {
                    Helper.makeFullScreen(CodeViewActivity.this);
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
                if (Helper.isFullScreen && !configFullScreen) {
                    Helper.revertFullScreen(CodeViewActivity.this);
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
            Helper.uiHandler.post(() -> lineInfo_TextView.setText(lineInfo));
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
        if (fileList.get(clicked_ID).getName() == getSupportActionBar().getSubtitle().toString())
            return;

        if (loadIntoRAM) {
            setCodeView(codeViewList.get(activeFilePosition), codeList.get(clicked_ID));
            if (isScreenSplit)
                selectedFileNames[activeFilePosition] = ((MaterialButton) view).getText().toString();
        } else {
            setCodeView(codeViewList.get(activeFilePosition), Helper.readFile(CodeViewActivity.this, Uri.parse(fileList.get(clicked_ID).getUri())));
        }

        currentActiveID = clicked_ID;
        updateInfo(clicked_ID);
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
                //TODO : Edit Case
                break;
            case Search:
                //FIXME : Regex Search
                showSearchDialog();
                break;
            case CopyAll:
                customWorkerThread.addWork(() -> Helper.copyCode(CodeViewActivity.this, codeViewList.get(activeFilePosition).getCode()));
                break;
            case FullScreen:
                if (Helper.isFullScreen) {
                    Helper.revertFullScreen(CodeViewActivity.this);
                } else {
                    Helper.makeFullScreen(CodeViewActivity.this);
                }
                break;
            case SplitScreen:
                if (!Helper.isScreenLandscape(CodeViewActivity.this))
                    Helper.showAlertDialog(getString(R.string.suggestion), getString(R.string.changeToLandscape), CodeViewActivity.this);

                if (Helper.thisIsMobile) {
                    splitScreen_2(new CodeView[]{codeView_Main, codeview_SplitScreen1});
                } else {
                    //TODO : Tablet Model Screen
                    //splitScreen_Tablet();
                }
                break;
            case AddFile:
                if (drawerLayout.isOpen()) drawerLayout.close();
                Helper.pickFile(CodeViewActivity.this);

                //FIXME : Implement file reading from URL

                /*
                String[] codeURL = new String[1];
                customWorkerThread.addWork(() -> {
                    try {
                        codeURL[0] = Helper.readFile(CodeViewActivity.this, new URL("https://clever-go.web.app/app-ads.txt"));
                        Helper.uiHandler.post(() -> setCodeView(codeView_Main, codeURL[0]));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                });
                 */
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
