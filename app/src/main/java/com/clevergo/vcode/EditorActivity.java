package com.clevergo.vcode;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.clevergo.vcode.editorfiles.CodeView;
import com.clevergo.vcode.editorfiles.syntax.LanguageManager;
import com.clevergo.vcode.editorfiles.syntax.LanguageName;
import com.clevergo.vcode.editorfiles.syntax.ThemeName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EditorActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private List<CodeView> editorList = new ArrayList<>();
    private Set<Character> intendationStarts = new HashSet<>(), intendationEnds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(getString(R.string.editor));

        intendationStarts.add('{');
        intendationEnds.add('}');

        CodeViewFile file = ((CodeViewFile) getIntent().getExtras().get("currentFileObject"));
        actionBar.setSubtitle(file.getName());
        editorList.add(findViewById(R.id.editor_Main));
        CodeView editor_Main = editorList.get(0);
        editor_Main.setText(Helper.readFile(EditorActivity.this, Uri.parse(file.getUri())));
        editor_Main.setHighlightWhileTextChanging(true);
        editor_Main.setEnableAutoIndentation(true);
        editor_Main.setEnableLineNumber(true);
        editor_Main.setLineNumberTextColor(Color.GRAY);
        editor_Main.setLineNumberTextSize(35);
        editor_Main.setTabLength(4);
        editor_Main.setIndentationStarts(intendationStarts);
        editor_Main.setIndentationEnds(intendationEnds);
        LanguageManager languageManager = new LanguageManager(EditorActivity.this, editor_Main);
        languageManager.applyTheme(LanguageName.JAVA, ThemeName.MONOKAI);

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
        editor_Main.setPairCompleteMap(pairCompleteMap);
    }
}
