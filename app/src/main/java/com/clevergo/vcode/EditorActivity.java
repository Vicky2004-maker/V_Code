package com.clevergo.vcode;

import static com.clevergo.vcode.Helper.PICK_FILE_CODE;
import static com.clevergo.vcode.Helper.createACodeViewFile;
import static com.clevergo.vcode.Helper.getAllMethods;
import static com.clevergo.vcode.Helper.isLowerSDK;
import static com.clevergo.vcode.Helper.pickFile;
import static com.clevergo.vcode.Helper.readFile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.drawerlayout.widget.DrawerLayout;

import com.clevergo.vcode.editorfiles.CodeView;
import com.clevergo.vcode.editorfiles.plugin.UndoRedoManager;
import com.clevergo.vcode.editorfiles.syntax.LanguageManager;
import com.clevergo.vcode.editorfiles.syntax.LanguageName;
import com.clevergo.vcode.editorfiles.syntax.ThemeName;
import com.google.android.material.navigation.NavigationView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class EditorActivity extends AppCompatActivity {

    public static boolean reload, newFileAdded = false;
    private static int filesOpened = 0;

    public static HashMap<String, Integer> methods = new HashMap<>();
    private int activeEditor = 0;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navView;
    private ExpandableListView fileEditor_ExpandableList;
    private ActionBar actionBar;
    private List<CodeView> editorList = new ArrayList<>();
    private Set<Character> indentationStarts = new HashSet<>(), indentationEnds = new HashSet<>();
    private CodeViewFile file;
    private UndoRedoManager undoRedoManager;
    private CustomWorkerThread customWorkerThread;
    private List<String> buttonStringList = List.of("\t", "{\n    }", "()", "[]", "<", ">", ";", "=", ",", "&", "<>", "|", "!",
            "~", "+", "-", "*", "/", "%", ":");
    private ExpandableListView expandableListView_Editor;
    private ExpandableListView expandableListView;
    private ExpandableListAdapter expandableListAdapter;
    private List<String> expandableListTitle = new ArrayList<>();
    private HashMap<String, List<String>> expandableListDetail = new HashMap<>();
    private LinearLayout buttonSwitcher_LinearLayout;

    private void writeFile(Context context, Uri uri, final String content) {
        try {
            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(content.getBytes());
            fileOutputStream.close();
            pfd.close();
            Toast.makeText(context, context.getString(R.string.saved), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(context, context.getString(R.string.unableToSave), Toast.LENGTH_LONG).show();
        } finally {
            reload = true;
            EditorActivity.this.finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        customWorkerThread.stop();
    }

    private void addTextButton(CodeView editor, final String text) {
        editor.getText().insert(editor.getSelectionStart(), text);
        if (text.equals(buttonStringList.get(1))
                || text.equals(buttonStringList.get(2))
                || text.equals(buttonStringList.get(3)))
            editor.setSelection(editor.getSelectionStart() - 1);
        //editor.getText().insert(editor.getSelectionStart(), "\t");
    }

    private void createMaterialButton() {
        
    }

    private void setEditor(@NonNull final CodeView editor, @NonNull final String code) {
        LanguageManager languageManager = new LanguageManager(EditorActivity.this, editor);
        languageManager.applyTheme(LanguageName.JAVA, ThemeName.MONOKAI);
        editor.setText(code);
        editor.setHighlightWhileTextChanging(true);
        editor.setEnableAutoIndentation(true);
        editor.setEnableLineNumber(true);
        editor.setLineNumberTextColor(Color.GRAY);
        editor.setLineNumberTextSize(35);
        editor.setTabLength(4);
        editor.setIndentationStarts(indentationStarts);
        editor.setIndentationEnds(indentationEnds);

        String[] languageKeywords = getResources().getStringArray(R.array.java_keywords);
        ArrayAdapter<String> codeAdapter = new ArrayAdapter<>(EditorActivity.this,
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
    }

    private void singleFile(@Nullable Intent data, boolean isUrl) {
        if (data != null) {
            if (isLowerSDK()) {

            } else {
                if (CodeViewActivity.fileList.stream().anyMatch(fileObj -> fileObj.getUri().equals(data.getData().toString()))) {
                    editorList.get(activeEditor).setText(readFile(EditorActivity.this, data.getData()));
                } else {
                    CodeViewActivity.filesOpened++;
                    file = createACodeViewFile(EditorActivity.this, data, isUrl);
                    CodeViewActivity.fileList.add(file);
                    setEditor(editorList.get(activeEditor), readFile(EditorActivity.this, Uri.parse(file.getUri())));
                    newFileAdded = true;
                }
            }
        }
    }

    private void multipleFile(@NonNull Intent data, boolean isUrl) {
        buttonSwitcher_LinearLayout.setVisibility(View.VISIBLE);

        int itemCount = data.getClipData().getItemCount();
        if (isLowerSDK()) {
            //TODO : Add in Nav Menu and Add Material Button
            for (int i = 0; i < itemCount; i++) {
                Uri uri = data.getClipData().getItemAt(i).getUri();
                if (CodeViewActivity.uri_List.contains(uri)) {
                    if (i == itemCount - 1)
                        setEditor(editorList.get(activeEditor), readFile(EditorActivity.this, uri));
                } else {
                    CodeViewFile tempFile = createACodeViewFile(EditorActivity.this, uri, isUrl);
                    CodeViewActivity.fileList.add(tempFile);
                    CodeViewActivity.uri_List.add(uri);
                    if (i == itemCount - 1) {
                        setEditor(editorList.get(activeEditor), readFile(EditorActivity.this, uri));
                        newFileAdded = true;
                        file = tempFile;
                    }
                }
            }
        } else {
            for (int i = 0; i < itemCount; i++) {
                Uri uri = data.getClipData().getItemAt(i).getUri();
                if (CodeViewActivity.fileList.stream().anyMatch(fileObj -> fileObj.getUri().equals(uri.toString()))) {
                    if (i == itemCount - 1)
                        setEditor(editorList.get(activeEditor), readFile(EditorActivity.this, uri));
                } else {
                    CodeViewFile tempFile = createACodeViewFile(EditorActivity.this, uri, isUrl);
                    CodeViewActivity.fileList.add(tempFile);
                    if (i == itemCount - 1) {
                        setEditor(editorList.get(activeEditor), readFile(EditorActivity.this, uri));
                        newFileAdded = true;
                        file = tempFile;
                    }
                }
            }
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        actionBarDrawerToggle.syncState();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        actionBar = getSupportActionBar();
        assert actionBar != null;
        drawerLayout = findViewById(R.id.drawerLayout_Editor);
        navView = findViewById(R.id.navView_Editor);
        actionBar.setTitle(getString(R.string.editor));
        //fileEditor_ExpandableList = findViewById(R.id.fileEditor_ExpandableList);
        LinearLayout buttonControls_LinearLayout = findViewById(R.id.buttonControls_LinearLayout);
        buttonSwitcher_LinearLayout = findViewById(R.id.buttonSwitcher_LinearLayout);
        expandableListView = findViewById(R.id.expandableListView_Editor);
        customWorkerThread = new CustomWorkerThread();

        expandableListTitle.add("Active Files");
        expandableListTitle.add("Opened Files");
        expandableListTitle.add("All Methods");
        expandableListTitle.add("Settings");

        for (int i = 0; i < buttonStringList.size(); i++) {
            AppCompatButton simpleButton = new AppCompatButton(EditorActivity.this);
            simpleButton.setId(i);
            String txt = buttonStringList.get(i);
            if (i == 0) txt = "->";
            if (i == 1) txt = "{}";
            simpleButton.setText(txt);
            simpleButton.setOnClickListener(new BottomControlsClickListener());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ((int) getResources().getDimension(R.dimen.dimen40dp)),
                    ((int) getResources().getDimension(R.dimen.dimen45dp)));
            buttonControls_LinearLayout.addView(simpleButton, i, layoutParams);
        }

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);

        indentationStarts.add('{');
        indentationEnds.add('}');

        editorList.add(findViewById(R.id.editor_Main));
        if (getIntent().getExtras().get("currentFileObject") != null) {
            file = ((CodeViewFile) getIntent().getExtras().get("currentFileObject"));
            actionBar.setSubtitle(file.getName());
            setEditor(editorList.get(activeEditor),
                    readFile(EditorActivity.this, Uri.parse(file.getUri())));
        }

        if ((Intent.ACTION_SEND.equals(getIntent().getAction()) || Intent.ACTION_VIEW.equals(getIntent().getAction()))
                && getIntent().getType() != null) {
            singleFile(getIntent(), false);
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction()) && getIntent().getType() != null) {
            //manageMultipleFileIntent(getIntent());
        }

        customWorkerThread.addWork(() -> {
            expandableListAdapter = new ExpandableViewAdapterCustom(EditorActivity.this,
                    expandableListTitle,
                    expandableListDetail);
            expandableListView.setAdapter(expandableListAdapter);

            getAllMethods(methods, editorList.get(activeEditor).getText().toString());

            expandableListDetail.put("All Methods", List.copyOf(methods.keySet()));
            navView.postInvalidate();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editor_menu, menu);

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        reload = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_CODE && data.getData() != null) singleFile(data, false);
        if (requestCode == PICK_FILE_CODE && data.getClipData() != null) multipleFile(data, false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.addFileEditor_menu:
                pickFile(EditorActivity.this);
                break;
            case R.id.settingsEditor_Menu:
                //TODO : Navigate to Setting Page
                break;
            case R.id.readOnlyEditor_menu:
                //TODO : Read Only setting
                break;
            case R.id.saveFileEditor_menu: {
                String content = editorList.get(activeEditor).getText().toString();
                writeFile(EditorActivity.this,
                        Uri.parse(file.getUri()),
                        content);
                break;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    private class BottomControlsClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            addTextButton(editorList.get(activeEditor), buttonStringList.get(v.getId()));
        }
    }

    private class EditorFileSwitcherClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

        }
    }
}
