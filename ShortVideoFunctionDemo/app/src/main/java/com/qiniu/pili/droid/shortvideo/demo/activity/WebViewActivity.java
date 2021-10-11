package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.qiniu.pili.droid.shortvideo.demo.R;

public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Intent in = getIntent();
        String web = in.getStringExtra("web");
        if (TextUtils.isEmpty(web)) {
            Toast.makeText(this, "地址为空", Toast.LENGTH_SHORT).show();
        } else {
            WebView webView = findViewById(R.id.web);
            webView.setWebViewClient(new WebViewClient());

            WebSettings webSettings = webView.getSettings();
            // 让WebView能够执行javaScript
            webSettings.setJavaScriptEnabled(true);
            // 让JavaScript可以自动打开windows
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            // 支持缩放(适配到当前屏幕)
            webSettings.setSupportZoom(true);
            // 将图片调整到合适的大小
            webSettings.setUseWideViewPort(true);
            // 支持内容重新布局,一共有四种方式
            // 默认的是NARROW_COLUMNS
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            // 设置可以被显示的屏幕控制
            webSettings.setDisplayZoomControls(true);
            // 设置默认字体大小
            webSettings.setDefaultFontSize(12);
            webView.loadUrl(web);
        }
    }
}
