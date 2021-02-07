package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import com.qiniu.pili.droid.shortvideo.demo.R;

import static com.qiniu.pili.droid.shortvideo.demo.activity.VideoMixRecordActivity.MIX_MODE;
import static com.qiniu.pili.droid.shortvideo.demo.activity.VideoMixRecordActivity.MIX_MODE_CAMERA_ABOVE_SAMPLE;
import static com.qiniu.pili.droid.shortvideo.demo.activity.VideoMixRecordActivity.MIX_MODE_SAMPLE_ABOVE_CAMERA;
import static com.qiniu.pili.droid.shortvideo.demo.activity.VideoMixRecordActivity.MIX_MODE_VERTICAL;

public class VideoMixRecordConfigActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mix_record_config);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        setTitle(R.string.title_mix_record);
    }

    public void onClickVerticalMix(View view) {
        jumpToActivity(VideoMixRecordActivity.class, MIX_MODE_VERTICAL);
    }

    public void onClickSampleAboveCameraMix(View view) {
        jumpToActivity(VideoMixRecordActivity.class, MIX_MODE_SAMPLE_ABOVE_CAMERA);
    }

    public void onClickCameraAboveSampleMix(View view) {
        jumpToActivity(VideoMixRecordActivity.class, MIX_MODE_CAMERA_ABOVE_SAMPLE);
    }

    private void jumpToActivity(Class<?> cls, int mixMode) {
        Intent intent = new Intent(this, cls);
        intent.putExtra(MIX_MODE, mixMode);
        startActivity(intent);
    }
}
