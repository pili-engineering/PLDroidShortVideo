package com.qiniu.shortvideo.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.qiniu.shortvideo.app.R;

public class VideoPuzzleConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_config);
    }

    public void onClickPuzzle2Video(View v) {
        jumpToMediaSelectActivity(MediaSelectActivity.TYPE_VIDEO_PUZZLE_2);
    }

    public void onClickPuzzle3Video(View v) {
        jumpToMediaSelectActivity(MediaSelectActivity.TYPE_VIDEO_PUZZLE_3);
    }

    public void onClickPuzzle4Video(View v) {
        jumpToMediaSelectActivity(MediaSelectActivity.TYPE_VIDEO_PUZZLE_4);
    }

    public void onClickBack(View v) {
        finish();
    }

    private void jumpToMediaSelectActivity(int type) {
        Intent intent = new Intent(this, MediaSelectActivity.class);
        intent.putExtra(MediaSelectActivity.TYPE, type);
        startActivity(intent);
    }
}
