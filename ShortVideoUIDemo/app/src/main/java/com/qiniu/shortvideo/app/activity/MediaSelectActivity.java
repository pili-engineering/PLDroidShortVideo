package com.qiniu.shortvideo.app.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.qiniu.pili.droid.shortvideo.PLComposeItem;
import com.qiniu.pili.droid.shortvideo.PLShortVideoComposer;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.adapter.MediaFileAdapter;
import com.qiniu.shortvideo.app.model.MediaFile;
import com.qiniu.shortvideo.app.utils.Config;
import com.qiniu.shortvideo.app.utils.MediaUtils;
import com.qiniu.shortvideo.app.utils.RecordSettings;
import com.qiniu.shortvideo.app.utils.ToastUtils;
import com.qiniu.shortvideo.app.view.CustomProgressDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.qiniu.shortvideo.app.activity.VideoMixRecordActivity.VIDEO_PATH;

/**
 * 获取并选择本地视频
 */
public class MediaSelectActivity extends AppCompatActivity implements MediaFileAdapter.OnItemClickListener {
    public static final String TYPE = "operation_type";
    public static final int GRID_ITEM_COUNT = 4;

    public static final int TYPE_VIDEO_MIX = 0;
    public static final int TYPE_VIDEO_EDIT = 1;
    public static final int TYPE_MEDIA_COMPOSE = 2;
    public static final int TYPE_VIDEO_PUZZLE_2 = 3;
    public static final int TYPE_VIDEO_PUZZLE_3 = 4;
    public static final int TYPE_VIDEO_PUZZLE_4 = 5;

    private RecyclerView mMediaRecyclerView;
    private RecyclerView mMediaChosenView;
    private MediaFileAdapter mMediaFileAdapter;
    private MediaFileAdapter mMediaChosenAdapter;
    private GridLayoutManager mGridLayoutManager;

    private int mType;

    // 素材拼接部分的处理
    private PLShortVideoComposer mShortVideoComposer;
    private CustomProgressDialog mProcessingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_select);

        mType = getIntent().getIntExtra(TYPE, -1);

        mMediaRecyclerView = findViewById(R.id.media_recycleView);
        mGridLayoutManager = new GridLayoutManager(MediaSelectActivity.this, GRID_ITEM_COUNT);
        mMediaRecyclerView.setLayoutManager(mGridLayoutManager);

        mMediaChosenView = findViewById(R.id.media_choosed_view);
        mMediaChosenView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        getLocalMedia();
    }

    public void onClickBack(View v) {
        finish();
    }

    public void onClickNext(View v) {
        if (mMediaChosenAdapter == null || mMediaChosenAdapter.getMediaFiles() == null || mMediaChosenAdapter.getMediaFiles().isEmpty()) {
            ToastUtils.s(this, getString(R.string.choose_video_string));
            return;
        }
        if (mType == TYPE_MEDIA_COMPOSE && mMediaChosenAdapter.getMediaFiles().size() == 1) {
            ToastUtils.s(this, getString(R.string.choose_media_compose_string));
            return;
        }
        if ((mType == TYPE_VIDEO_EDIT || mType == TYPE_VIDEO_MIX) && mMediaChosenAdapter.getMediaFiles().size() > 1) {
            ToastUtils.s(this, getString(R.string.choose_media_overflow_string));
            return;
        }
        if (mType == TYPE_VIDEO_PUZZLE_2 && mMediaChosenAdapter.getMediaFiles().size() != 2) {
            ToastUtils.s(this, getString(R.string.choose_media_puzzle_2video_string));
            return;
        }
        if (mType == TYPE_VIDEO_PUZZLE_3 && mMediaChosenAdapter.getMediaFiles().size() != 3) {
            ToastUtils.s(this, getString(R.string.choose_media_puzzle_3video_string));
            return;
        }
        if (mType == TYPE_VIDEO_PUZZLE_4 && mMediaChosenAdapter.getMediaFiles().size() != 4) {
            ToastUtils.s(this, getString(R.string.choose_media_puzzle_4video_string));
            return;
        }
        Intent intent;
        if (mType == TYPE_VIDEO_MIX) {
            intent = new Intent(MediaSelectActivity.this, VideoMixRecordActivity.class);
            intent.putExtra(VIDEO_PATH, mMediaChosenAdapter.getMediaFiles().get(0).getPath());
        } else if (mType == TYPE_VIDEO_EDIT) {
            intent = new Intent(MediaSelectActivity.this, VideoTrimActivity.class);
            intent.putExtra(VIDEO_PATH, mMediaChosenAdapter.getMediaFiles().get(0).getPath());
        } else if (mType == TYPE_MEDIA_COMPOSE) {
            composeMediaFiles();
            return;
        } else if (mType == TYPE_VIDEO_PUZZLE_2 || mType == TYPE_VIDEO_PUZZLE_3 || mType == TYPE_VIDEO_PUZZLE_4) {
            intent = new Intent(MediaSelectActivity.this, VideoPuzzleActivity.class);
            intent.putParcelableArrayListExtra(VideoPuzzleActivity.VIDEO_LIST, mMediaChosenAdapter.getMediaFiles());
        } else {
            return;
        }
        startActivity(intent);
        finish();
    }

    private void getLocalMedia() {
        MediaUtils.getLocalMedia(getApplicationContext(),
                mType == TYPE_MEDIA_COMPOSE ? MediaUtils.MEDIA_TYPE_ALL : MediaUtils.MEDIA_TYPE_VIDEO,
                new MediaUtils.LocalMediaCallback() {
            @Override
            public void onLocalMediaFileUpdate(final List<MediaFile> mediaFiles) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mMediaFileAdapter == null) {
                            mMediaFileAdapter = new MediaFileAdapter(MediaSelectActivity.this, MediaFileAdapter.TYPE_MEDIA_SHOW, mediaFiles);
                            mMediaFileAdapter.setOnItemClickListener(MediaSelectActivity.this);
                            mMediaRecyclerView.setAdapter(mMediaFileAdapter);
                        } else {
                            mMediaFileAdapter.addMediaFiles(mediaFiles);
                        }
                    }
                });
            }
        });
    }

    private void composeMediaFiles() {
        if (mShortVideoComposer == null) {
            mShortVideoComposer = new PLShortVideoComposer(this);
        }
        if (mProcessingDialog == null) {
            mProcessingDialog = new CustomProgressDialog(this);
            mProcessingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mShortVideoComposer.cancelComposeItems();
                }
            });
        }

        PLVideoEncodeSetting setting = new PLVideoEncodeSetting(this);
        setting.setEncodingSizeLevel(RecordSettings.ENCODING_SIZE_LEVEL_ARRAY[ConfigActivity.ENCODING_SIZE_LEVEL_POS]);
        setting.setEncodingBitrate(RecordSettings.ENCODING_BITRATE_LEVEL_ARRAY[ConfigActivity.ENCODING_BITRATE_LEVEL_POS]);

        List<PLComposeItem> items = new ArrayList<>();
        Iterator iterator = mMediaChosenAdapter.getMediaFiles().iterator();
        while (iterator.hasNext()) {
            MediaFile mediaFile = (MediaFile) iterator.next();
            if (mediaFile.getType() == MediaFile.VIDEO) {
                items.add(createVideoItem(mediaFile.getPath()));
            } else if (mediaFile.getType() == MediaFile.IMAGE) {
                items.add(createImageItem(mediaFile.getPath()));
            }
        }
        if (mShortVideoComposer.composeItems(items, Config.COMPOSE_FILE_PATH, setting, mVideoSaveListener)) {
            mProcessingDialog.show();
        } else {
            ToastUtils.s(this, "开始拼接失败！");
        }
    }

    private PLComposeItem createImageItem(String path) {
        PLComposeItem item = new PLComposeItem(path);
        item.setItemType(PLComposeItem.ItemType.IMAGE);
        item.setDurationMs(5000);
        item.setTransitionTimeMs(1000);
        return item;
    }

    private PLComposeItem createVideoItem(String path) {
        PLComposeItem item = new PLComposeItem(path);
        item.setItemType(PLComposeItem.ItemType.VIDEO);
        item.setTransitionTimeMs(1000);
        return item;
    }

    @Override
    public void onMediaItemClicked(MediaFile mediaFile, int position) {
        if (mMediaChosenAdapter == null) {
            mMediaChosenAdapter = new MediaFileAdapter(this, MediaFileAdapter.TYPE_MEDIA_CHOSEN);
            mMediaChosenView.setAdapter(mMediaChosenAdapter);
        }
        mMediaChosenAdapter.addMediaFile(mediaFile);
        mMediaChosenView.smoothScrollToPosition(mMediaChosenAdapter.getMediaFiles().size() - 1);
    }

    private PLVideoSaveListener mVideoSaveListener = new PLVideoSaveListener() {
        @Override
        public void onSaveVideoSuccess(String filepath) {
            mProcessingDialog.dismiss();
            MediaUtils.storeVideo(MediaSelectActivity.this, new File(filepath), Config.MIME_TYPE_VIDEO);
            Intent intent = new Intent(MediaSelectActivity.this, VideoTrimActivity.class);
            intent.putExtra(VIDEO_PATH, filepath);
            startActivity(intent);
        }

        @Override
        public void onSaveVideoFailed(final int errorCode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProcessingDialog.dismiss();
                    ToastUtils.toastErrorCode(MediaSelectActivity.this, errorCode);
                }
            });
        }

        @Override
        public void onSaveVideoCanceled() {
            mProcessingDialog.dismiss();
        }

        @Override
        public void onProgressUpdate(final float percentage) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProcessingDialog.setProgress((int) (100 * percentage));
                }
            });
        }
    };
}
