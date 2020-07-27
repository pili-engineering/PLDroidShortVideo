package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.qiniu.pili.droid.shortvideo.PLShortVideoComposer;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.Config;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;
import com.qiniu.pili.droid.shortvideo.demo.view.CustomProgressDialog;
import com.qiniu.pili.droid.shortvideo.demo.view.DragItemAdapter;

import java.util.ArrayList;

public class VideoFrameActivity extends Activity {
    public static final int TRANSITION_REQUEST_CODE = 1;
    public static final int DIVIDE_REQUEST_CODE = 2;
    public static final String DATA_EXTRA_PATHS = "paths";
    public static final String DATA_EXTRA_PATH = "path";
    public static final String DATA_EXTRA_JUMP = "jump";

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private PLShortVideoComposer mShortVideoComposer;
    private CustomProgressDialog mProcessingDialog;
    private DragItemAdapter mDragItemAdapter;

    private ArrayList<String> mVideoPaths;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_video_frame);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        ArrayList<String> arrayList = getIntent().getStringArrayListExtra(DATA_EXTRA_PATHS);
        mVideoPaths = (arrayList == null) ? new ArrayList<String>() : arrayList;

        mDragItemAdapter = new DragItemAdapter(mVideoPaths);
        mDragItemAdapter.setOnItemMovedListener(new DragItemAdapter.OnItemMovedListener() {
            @Override
            public void onMoveItem(int fromPosition, int toPosition) {
                String movedItem = mVideoPaths.remove(fromPosition);
                mVideoPaths.add(toPosition, movedItem);
            }
        });

        RecyclerViewDragDropManager dragDropManager = new RecyclerViewDragDropManager();
        dragDropManager.setInitiateOnMove(false);
        dragDropManager.setInitiateOnLongPress(true);

        RecyclerView.Adapter adapter = dragDropManager.createWrappedAdapter(mDragItemAdapter);
        mRecyclerView.setAdapter(adapter);
        dragDropManager.attachRecyclerView(mRecyclerView);

        mShortVideoComposer = new PLShortVideoComposer(this);
        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mShortVideoComposer.cancelComposeVideos();
            }
        });
    }

    @Override
    public void onBackPressed() {
        showFinishDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TRANSITION_REQUEST_CODE && data != null) {
            String path = data.getStringExtra(DATA_EXTRA_PATH);
            if (path != null) {
                mVideoPaths.add(path);
            }
        } else if (requestCode == DIVIDE_REQUEST_CODE && data != null) {
            ArrayList<String> appendPaths = data.getStringArrayListExtra(DATA_EXTRA_PATHS);
            if (appendPaths != null) {
                mVideoPaths.addAll(appendPaths);
            }
        }
        mDragItemAdapter.updatePaths(mVideoPaths);
        mDragItemAdapter.notifyDataSetChanged();
    }

    public void onBack(View view) {
        showFinishDialog();
    }

    public void onDone(View view) {
        PLVideoEncodeSetting setting = new PLVideoEncodeSetting(this);
        setting.setEncodingSizeLevel(PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_720P_2);

        if (mShortVideoComposer.composeVideos(mVideoPaths, Config.VIDEO_DIVIDE_FILE_PATH, setting, mVideoSaveListener)) {
            mProcessingDialog.show();
        } else {
            ToastUtils.s(this, "开始拼接失败！");
        }
    }

    public void onClickAddVideo(View view) {
        Intent intent = new Intent(this, VideoDivideActivity.class);
        intent.putExtra(DATA_EXTRA_JUMP, true);
        startActivityForResult(intent, DIVIDE_REQUEST_CODE);
    }

    public void onClickAddTransition(View view) {
        startActivityForResult(new Intent(this, TransitionMakeActivity.class), TRANSITION_REQUEST_CODE);
    }

    private void showFinishDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("确定要放弃剪切的视频吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private PLVideoSaveListener mVideoSaveListener = new PLVideoSaveListener() {
        @Override
        public void onSaveVideoSuccess(final String destFile) {
            mProcessingDialog.dismiss();
            PlaybackActivity.start(VideoFrameActivity.this, destFile);
        }

        @Override
        public void onSaveVideoFailed(int errorCode) {

        }

        @Override
        public void onSaveVideoCanceled() {

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