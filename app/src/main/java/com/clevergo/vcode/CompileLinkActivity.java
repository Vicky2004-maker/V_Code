package com.clevergo.vcode;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

public class CompileLinkActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compile_link);

        WebView webViewMain = findViewById(R.id.main_webView);
        WebSettings webSettings = webViewMain.getSettings();
        webSettings.setJavaScriptEnabled(true);
        //webViewMain.setWebChromeClient(new WebChromeClient());
        String code = ((String) getIntent().getExtras().get("code"));
        String encodedHtml = Base64.encodeToString(code.getBytes(), Base64.NO_PADDING);
        webViewMain.loadData(encodedHtml, "text/html", "base64");
    }
}
