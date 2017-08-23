package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.Config;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class ShowGIFActivity extends AppCompatActivity {

    GifImageView mGifImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_gif);
        mGifImageView = (GifImageView) findViewById(R.id.gif_image_view);
        try {
            GifDrawable drawable = new GifDrawable(Config.GIF_SAVE_PATH);
            drawable.start();
            drawable.setLoopCount(10);
            mGifImageView.setBackground(drawable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
