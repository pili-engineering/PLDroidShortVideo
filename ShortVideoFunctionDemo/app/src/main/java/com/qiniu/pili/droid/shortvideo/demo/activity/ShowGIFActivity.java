package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.qiniu.pili.droid.shortvideo.demo.R;

public class ShowGIFActivity extends AppCompatActivity {

    public static final String GIF_PATH = "GifPathToPlay";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_gif);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        setTitle(R.string.title_show_gif);

        String gifFilePath = getIntent().getStringExtra(GIF_PATH);
        if (gifFilePath == null) {
            return;
        }

        ImageView imageView = findViewById(R.id.iv_gif);
        Glide.with(this).load(gifFilePath).into(imageView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
                break;
        }
        return true;
    }
}
