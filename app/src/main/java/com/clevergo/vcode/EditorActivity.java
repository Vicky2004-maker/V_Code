package com.clevergo.vcode;

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
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

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

public class EditorActivity extends AppCompatActivity implements View.OnClickListener {

    public static boolean reload;
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
    private CodeView editorMain;
    private CustomWorkerThread workerThread;
    private List<String> buttonStringList = List.of("{\n    }", "()", "[]", "<>", ";", ",", "&", "|", "!",
            "~", "+", "-", "*", "/", "%", ":");
    private ListView methodListView;

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

    private void addTextButton(CodeView editor, final String text) {
        editor.getText().insert(editor.getSelectionStart(), text);
        editor.setSelection(editor.getSelectionStart() - 1);
        //editor.getText().insert(editor.getSelectionStart(), "\t");
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
        methodListView = findViewById(R.id.methodListView);
        LinearLayout buttonControls_LinearLayout = findViewById(R.id.buttonControls_LinearLayout);
        workerThread = new CustomWorkerThread();

        for (int i = 0; i < buttonStringList.size(); i++) {
            AppCompatButton simpleButton = new AppCompatButton(EditorActivity.this);
            simpleButton.setId(i);
            simpleButton.setText(i == 0 ? "{}" : buttonStringList.get(i));
            simpleButton.setOnClickListener(EditorActivity.this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ((int) getResources().getDimension(R.dimen.dimen40dp)),
                    ((int) getResources().getDimension(R.dimen.dimen45dp)));
            buttonControls_LinearLayout.addView(simpleButton, i, layoutParams);
            simpleButton = null;
        }

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);

        indentationStarts.add('{');
        indentationEnds.add('}');

        file = ((CodeViewFile) getIntent().getExtras().get("currentFileObject"));
        actionBar.setSubtitle(file.getName());
        editorList.add(findViewById(R.id.editor_Main));
        editorMain = findViewById(R.id.editor_Main);
        CodeView editor_Main = editorList.get(0);
        LanguageManager languageManager = new LanguageManager(EditorActivity.this, editor_Main);
        languageManager.applyTheme(LanguageName.JAVA, ThemeName.MONOKAI);
        editor_Main.setText(Helper.readFile(EditorActivity.this, Uri.parse(file.getUri())));
        editor_Main.setHighlightWhileTextChanging(true);
        editor_Main.setEnableAutoIndentation(true);
        editor_Main.setEnableLineNumber(true);
        editor_Main.setLineNumberTextColor(Color.GRAY);
        editor_Main.setLineNumberTextSize(35);
        editor_Main.setTabLength(4);
        editor_Main.setIndentationStarts(indentationStarts);
        editor_Main.setIndentationEnds(indentationEnds);

        String[] languageKeywords = getResources().getStringArray(R.array.java_keywords);
        ArrayAdapter<String> codeAdapter = new ArrayAdapter<>(EditorActivity.this,
                R.layout.list_item_suggestion,
                R.id.suggestItemTextView,
                languageKeywords);

        editor_Main.setAdapter(codeAdapter);
        editor_Main.enablePairComplete(true);
        editor_Main.enablePairCompleteCenterCursor(true);
        Map<Character, Character> pairCompleteMap = new HashMap<>();
        pairCompleteMap.put('{', '}');
        pairCompleteMap.put('[', ']');
        pairCompleteMap.put('(', ')');
        pairCompleteMap.put('<', '>');
        pairCompleteMap.put('"', '"');
        pairCompleteMap.put('\'', '\'');
        editor_Main.setPairCompleteMap(pairCompleteMap);

        undoRedoManager = new UndoRedoManager(editor_Main);

        if ((Intent.ACTION_SEND.equals(getIntent().getAction()) || Intent.ACTION_VIEW.equals(getIntent().getAction()))
                && getIntent().getType() != null) {
            //file = new CodeViewFile();
            //manageSingleFileIntent(getIntent());
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction()) && getIntent().getType() != null) {
            //manageMultipleFileIntent(getIntent());
        }

        workerThread.addWork(() -> {
            Helper.getAllMethods(methods, editor_Main.getText().toString());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(EditorActivity.this,
                    androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
            adapter.addAll(methods.keySet());

            adapter.notifyDataSetChanged();
            Helper.uiHandler.post(() -> methodListView.setAdapter(adapter));
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.addFileEditor_menu:
                Helper.pickFile(EditorActivity.this);
                break;
            case R.id.settingsEditor_Menu:
                //TODO : Navigate to Setting Page
                break;
            case R.id.readOnlyEditor_menu:
                //TODO : Read Only setting
                break;
            case R.id.saveFileEditor_menu: {
                String content = editorMain.getText().toString();
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

    @Override
    public void onClick(View v) {
        addTextButton(editorList.get(activeEditor), buttonStringList.get(v.getId()));
    }
}
