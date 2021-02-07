package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;

import com.qiniu.pili.droid.shortvideo.demo.R;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class ShowGIFActivity extends AppCompatActivity {

    public static final String GIF_PATH = "GifPathToPlay";

    GifImageView mGifImageView;

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

        mGifImageView = (GifImageView) findViewById(R.id.gif_image_view);
        try {
            GifDrawable drawable = new GifDrawable(gifFilePath);
            drawable.start();
            drawable.setLoopCount(10);
            mGifImageView.setBackground(drawable);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
