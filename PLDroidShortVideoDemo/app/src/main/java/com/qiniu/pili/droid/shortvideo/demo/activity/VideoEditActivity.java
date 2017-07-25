package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.PLBuiltinFilter;
import com.qiniu.pili.droid.shortvideo.PLShortVideoEditor;
import com.qiniu.pili.droid.shortvideo.PLVideoEditSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.PLWatermarkSetting;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.Config;
import com.qiniu.pili.droid.shortvideo.demo.utils.GetPathFromUri;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;
import com.qiniu.pili.droid.shortvideo.demo.view.AudioMixSettingDialog;

import java.io.IOException;
import java.io.InputStream;

public class VideoEditActivity extends Activity implements PLVideoSaveListener {
    private static final String TAG = "VideoEditActivity";
    private static final String MP4_PATH = "MP4_PATH";

    private GLSurfaceView mPreviewView;
    private RecyclerView mFiltersList;
    private ProgressDialog mProcessingDialog;
    private ImageButton mMuteButton;
    private AudioMixSettingDialog mAudioMixSettingDialog;

    private PLShortVideoEditor mShortVideoEditor;
    private String mSelectedFilter;
    private PLWatermarkSetting mWatermarkSetting;

    private int mFgVolume = 100;
    private int mBgVolume = 100;
    private int mFgVolumeBeforeMute = 100;
    private long mAudioMixPosition = 0;
    private long mMixDuration = 5000; // ms
    private boolean mIsMuted = false;
    private boolean mIsMixAudio = false;
    private boolean mIsAudioMixDialogShown = false;

    public static void start(Activity activity, String mp4Path) {
        Intent intent = new Intent(activity, VideoEditActivity.class);
        intent.putExtra(MP4_PATH, mp4Path);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_editor);
        mPreviewView = (GLSurfaceView) findViewById(R.id.preview);
        mFiltersList = (RecyclerView) findViewById(R.id.recycler_view);
        mProcessingDialog = new ProgressDialog(this);
        mProcessingDialog.setMessage("处理中...");
        mProcessingDialog.setCancelable(false);

        mMuteButton = (ImageButton) findViewById(R.id.mute_button);
        mMuteButton.setImageResource(R.mipmap.btn_unmute);

        mWatermarkSetting = new PLWatermarkSetting();
        mWatermarkSetting.setResourceId(R.drawable.qiniu_logo);
        mWatermarkSetting.setPosition(0.01f, 0.01f);
        mWatermarkSetting.setAlpha(128);

        PLVideoEditSetting setting = new PLVideoEditSetting();
        setting.setSourceFilepath(getIntent().getStringExtra(MP4_PATH));
        setting.setDestFilepath(Config.EDITED_FILE_PATH);

        mShortVideoEditor = new PLShortVideoEditor(mPreviewView, setting);
        mShortVideoEditor.setVideoSaveListener(this);

        mMixDuration = mShortVideoEditor.getDurationMs();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mFiltersList.setLayoutManager(layoutManager);
        mFiltersList.setAdapter(new FilterListAdapter(mShortVideoEditor.getBuiltinFilterList()));

        mAudioMixSettingDialog = new AudioMixSettingDialog(this);
        mAudioMixSettingDialog.setOnAudioVolumeChangedListener(mOnAudioVolumeChangedListener);
        mAudioMixSettingDialog.setOnPositionSelectedListener(mOnPositionSelectedListener);
    }

    private void setMixVolume() {
        mShortVideoEditor.setAudioMixVolume(mFgVolume / 100f, mBgVolume / 100f);
    }

    private void setMixAudioDuration() {
        if (mShortVideoEditor == null) {
            return;
        }

        int duration = mShortVideoEditor.getAudioMixFileDuration();
        mAudioMixSettingDialog.setMixMaxPosition(duration);
        Log.i(TAG, "duration = " + duration);
    }

    public void onClear(View v) {
        mSelectedFilter = null;
        mShortVideoEditor.setBuiltinFilter(null);
        mShortVideoEditor.setAudioMixFile(null);
        mIsMixAudio = false;
        if (mIsAudioMixDialogShown) {
            mAudioMixPosition = 0;
            mAudioMixSettingDialog.clearMixAudio();
        }
    }

    public void onClickMix(View v) {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");
        }
        startActivityForResult(Intent.createChooser(intent, "请选择混音文件："), 0);
    }

    public void onClickMute(View v) {
        if (!mIsMuted) {
            mFgVolumeBeforeMute = mFgVolume;
            mFgVolume = 0;
            mShortVideoEditor.muteOriginAudio(true);
            mIsMuted = true;
            mMuteButton.setImageResource(R.mipmap.btn_mute);
        } else {
            mFgVolume = mFgVolumeBeforeMute;
            mShortVideoEditor.muteOriginAudio(false);
            mIsMuted = false;
            mMuteButton.setImageResource(R.mipmap.btn_unmute);
        }
    }

    public void onClickAudioMixSetting(View v) {
        if (mIsMixAudio) {
            mAudioMixSettingDialog.show();
            mIsAudioMixDialogShown = true;
            mAudioMixSettingDialog.setSrcVolumeProgress(mFgVolume);
            mAudioMixSettingDialog.setMixVolumeProgress(mBgVolume);
            setMixAudioDuration();
            mAudioMixSettingDialog.setMixPosition((int) mAudioMixPosition);
        } else {
            ToastUtils.s(this, "请先选择混音文件！");
        }
    }

    public void onClickBack(View v) {
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String selectedFilepath = GetPathFromUri.getPath(this, data.getData());
            Log.i(TAG, "Select file: " + selectedFilepath);
            if (selectedFilepath != null && !"".equals(selectedFilepath)) {
                mShortVideoEditor.setAudioMixFile(selectedFilepath);
                mIsMixAudio = true;
                mBgVolume = 100;
                setMixVolume();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mShortVideoEditor.stopPlayback();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mShortVideoEditor.setBuiltinFilter(mSelectedFilter);
        mShortVideoEditor.setWatermark(mWatermarkSetting);
        mShortVideoEditor.startPlayback();
        if (mIsMuted) {
            mFgVolume = 0;
            mShortVideoEditor.muteOriginAudio(true);
            mMuteButton.setImageResource(R.mipmap.btn_mute);
        }
        setMixVolume();
    }

    public void onSaveEdit(View v) {
        mProcessingDialog.show();
        mShortVideoEditor.save();
    }

    @Override
    public void onSaveVideoSuccess(String filePath) {
        Log.i(TAG, "save edit success filePath: " + filePath);
        mProcessingDialog.dismiss();
        PlaybackActivity.start(VideoEditActivity.this, filePath);
    }

    @Override
    public void onSaveVideoFailed(int errorCode) {
        Log.e(TAG, "save edit failed errorCode:" + errorCode);
        mProcessingDialog.dismiss();
        ToastUtils.s(this, "save edit failed: " + errorCode);
    }

    private class FilterItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView mIcon;
        public TextView mName;

        public FilterItemViewHolder(View itemView) {
            super(itemView);
            mIcon = (ImageView) itemView.findViewById(R.id.icon);
            mName = (TextView) itemView.findViewById(R.id.name);
        }
    }

    private class FilterListAdapter extends RecyclerView.Adapter<FilterItemViewHolder> {
        private PLBuiltinFilter[] mFilters;

        public FilterListAdapter(PLBuiltinFilter[] filters) {
            this.mFilters = filters;
        }

        @Override
        public FilterItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View contactView = inflater.inflate(R.layout.filter_item, parent, false);

            // Return a new holder instance
            FilterItemViewHolder viewHolder = new FilterItemViewHolder(contactView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(FilterItemViewHolder holder, int position) {
            // Get the data model based on position
            final PLBuiltinFilter filter = mFilters[position];

            // Set item views based on your views and data model
            holder.mName.setText(filter.getName());
            try {
                InputStream is = getAssets().open(filter.getAssetFilePath());
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                holder.mIcon.setImageBitmap(bitmap);
                holder.mIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSelectedFilter = filter.getName();
                        mShortVideoEditor.setBuiltinFilter(mSelectedFilter);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return mFilters.length;
        }
    }

    private AudioMixSettingDialog.OnAudioVolumeChangedListener mOnAudioVolumeChangedListener = new AudioMixSettingDialog.OnAudioVolumeChangedListener() {
        @Override
        public void onAudioVolumeChanged(int fgVolume, int bgVolume) {
            mFgVolume = fgVolume;
            mBgVolume = bgVolume;
            setMixVolume();
            if (mFgVolume == 0) {
                mIsMuted = true;
                mMuteButton.setImageResource(R.mipmap.btn_mute);
            } else {
                mIsMuted = false;
                mMuteButton.setImageResource(R.mipmap.btn_unmute);
            }
        }
    };

    private AudioMixSettingDialog.OnPositionSelectedListener mOnPositionSelectedListener = new AudioMixSettingDialog.OnPositionSelectedListener() {
        @Override
        public void onPositionSelected(long position) {
            mAudioMixPosition = position;
            mShortVideoEditor.setAudioMixFileRange(position, position + mMixDuration);
        }
    };

}
