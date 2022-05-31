package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.qiniu.pili.droid.shortvideo.PLDisplayMode;
import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.pili.droid.shortvideo.PLShortVideoComposer;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoRange;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.Config;
import com.qiniu.pili.droid.shortvideo.demo.utils.GetPathFromUri;
import com.qiniu.pili.droid.shortvideo.demo.utils.MediaStoreUtils;
import com.qiniu.pili.droid.shortvideo.demo.utils.RecordSettings;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;
import com.qiniu.pili.droid.shortvideo.demo.view.CustomProgressDialog;
import com.qiniu.pili.droid.shortvideo.demo.view.VideoListAdapter;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class VideoComposeActivity extends AppCompatActivity {

    private static final String TAG = "VideoComposeActivity";

    private CustomProgressDialog mProcessingDialog;
    private PLShortVideoComposer mShortVideoComposer;

    private VideoListAdapter mVideoListAdapter;

    private RadioButton mRbModeFit;
    private Spinner mEncodingSizeLevelSpinner;
    private Spinner mEncodingBitrateLevelSpinner;
    private boolean mIsVideoRange;

    private int mDeletePosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_compose);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        setTitle(R.string.title_compose);

        ListView videoListView = findViewById(R.id.VideoListView);
        mVideoListAdapter = new VideoListAdapter(this);
        videoListView.setAdapter(mVideoListAdapter);

        mEncodingSizeLevelSpinner = findViewById(R.id.EncodingSizeLevelSpinner);
        mEncodingBitrateLevelSpinner = findViewById(R.id.EncodingBitrateLevelSpinner);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, RecordSettings.ENCODING_SIZE_LEVEL_TIPS_ARRAY);
        mEncodingSizeLevelSpinner.setAdapter(adapter1);
        mEncodingSizeLevelSpinner.setSelection(7);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, RecordSettings.ENCODING_BITRATE_LEVEL_TIPS_ARRAY);
        mEncodingBitrateLevelSpinner.setAdapter(adapter2);
        mEncodingBitrateLevelSpinner.setSelection(2);

        CheckBox videoRangeCheck = findViewById(R.id.video_range_check);
        videoRangeCheck.setOnCheckedChangeListener((buttonView, isChecked) -> mIsVideoRange = isChecked);


        mShortVideoComposer = new PLShortVideoComposer(this);

        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(dialog -> mShortVideoComposer.cancelComposeVideos());

        videoListView.setOnCreateContextMenuListener((menu, v, menuInfo) -> menu.add(0, 0, 0, "删除"));

        videoListView.setOnItemLongClickListener((parent, view, position, id) -> {
            mDeletePosition = position;
            return false;
        });

        mRbModeFit = findViewById(R.id.rb_mode_fit);
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

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                mVideoListAdapter.removeVideoFile(mDeletePosition);
                mVideoListAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
        return true;
    }

    public void onClickAddVideo(View v) {
        chooseVideoFile();
    }

    public void onClickCompose(View v) {
        List<String> videos = mVideoListAdapter.getVideoList();
        if (videos.size() < 2) {
            ToastUtils.showShortToast("请先添加至少 2 个视频");
            return;
        }

        PLDisplayMode displayMode = mRbModeFit.isChecked() ? PLDisplayMode.FIT : PLDisplayMode.FULL;

        PLVideoEncodeSetting setting = new PLVideoEncodeSetting(this);
        setting.setEncodingSizeLevel(getEncodingSizeLevel(mEncodingSizeLevelSpinner.getSelectedItemPosition()));
        setting.setEncodingBitrate(getEncodingBitrateLevel(mEncodingBitrateLevelSpinner.getSelectedItemPosition()));

        boolean composeSuccess;
        if (mIsVideoRange) {
            List<PLVideoRange> videoRanges = new LinkedList<>();
            for (String video : videos) {
                PLMediaFile mediaFile = new PLMediaFile(video);
                long durationMs = mediaFile.getDurationMs();
                mediaFile.release();

                PLVideoRange videoRange = new PLVideoRange(video);
                videoRange.setStartTime(0);
                videoRange.setEndTime(durationMs / 2);
                videoRanges.add(videoRange);
            }
            composeSuccess = mShortVideoComposer.composeVideoRanges(videoRanges, Config.COMPOSE_FILE_PATH, displayMode, setting, mVideoSaveListener);
        } else {
            composeSuccess = mShortVideoComposer.composeVideos(videos, Config.COMPOSE_FILE_PATH, displayMode, setting, mVideoSaveListener);
        }

        if (composeSuccess) {
            mProcessingDialog.show();
            mProcessingDialog.setProgress(0);
        } else {
            ToastUtils.showShortToast("开始拼接失败！");
        }
    }

    private final PLVideoSaveListener mVideoSaveListener = new PLVideoSaveListener() {
        @Override
        public void onSaveVideoSuccess(String filepath) {
            MediaStoreUtils.storeVideo(VideoComposeActivity.this, new File(filepath), "video/mp4");
            mProcessingDialog.dismiss();
            PlaybackActivity.start(VideoComposeActivity.this, filepath);
        }

        @Override
        public void onSaveVideoFailed(final int errorCode) {
            runOnUiThread(() -> {
                mProcessingDialog.dismiss();
                ToastUtils.toastErrorCode(errorCode);
            });
        }

        @Override
        public void onSaveVideoCanceled() {
            mProcessingDialog.dismiss();
        }

        @Override
        public void onProgressUpdate(final float percentage) {
            runOnUiThread(() -> mProcessingDialog.setProgress((int) (100 * percentage)));
        }
    };

    private void chooseVideoFile() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data.getData() != null) {
            String selectedFilepath = GetPathFromUri.getRealPathFromURI(this, data.getData());
            Log.i(TAG, "Select file: " + selectedFilepath);
            if (selectedFilepath != null && !"".equals(selectedFilepath)) {
                mVideoListAdapter.addVideoFile(selectedFilepath);
                mVideoListAdapter.notifyDataSetChanged();
            }
        }
    }

    private PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL getEncodingSizeLevel(int position) {
        return RecordSettings.ENCODING_SIZE_LEVEL_ARRAY[position];
    }

    private int getEncodingBitrateLevel(int position) {
        return RecordSettings.ENCODING_BITRATE_LEVEL_ARRAY[position];
    }
}
