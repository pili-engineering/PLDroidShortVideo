package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.qiniu.pili.droid.shortvideo.PLComposeItem;
import com.qiniu.pili.droid.shortvideo.PLImageComposer;
import com.qiniu.pili.droid.shortvideo.PLPreviewListener;
import com.qiniu.pili.droid.shortvideo.PLTransitionType;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.Config;
import com.qiniu.pili.droid.shortvideo.demo.utils.GetPathFromUri;
import com.qiniu.pili.droid.shortvideo.demo.utils.MediaStoreUtils;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageComposeWithTransitionActivity extends AppCompatActivity {

    private static final String TAG = "ImageCompose2";

    private TextureView mTexturePreview;
    private SeekBar mProgressBar;
    private Button mBtnAddImage;
    private Button mBtnStart;
    private Button mBtnResume;
    private Button mBtnPause;
    private Button mBtnStop;
    private Button mBtnSave;
    private Button mBtnCancel;
    private final List<PLComposeItem> mImageItemList = new ArrayList<>();
    private RecyclerView.Adapter<RecyclerView.ViewHolder> mAdapter;
    private final Map<String, PLTransitionType> mTransitionMap = new HashMap<>();
    private PLImageComposer mImageComposer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_compose_with_transition);
        prepareTransitions();

        mTexturePreview = findViewById(R.id.texture_preview);
        mProgressBar = findViewById(R.id.seek_bar);
        mBtnAddImage = findViewById(R.id.btn_add_image);
        mBtnStart = findViewById(R.id.btn_start);
        mBtnResume = findViewById(R.id.btn_resume);
        mBtnPause = findViewById(R.id.btn_pause);
        mBtnStop = findViewById(R.id.btn_stop);
        mBtnSave = findViewById(R.id.btn_save);
        mBtnCancel = findViewById(R.id.btn_cancel);
        RecyclerView recyclerView = findViewById(R.id.rv_video);

        mAdapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ImageComposeWithTransitionActivity.ItemViewHolder(ImageComposeWithTransitionActivity.this);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
                ImageComposeWithTransitionActivity.ItemViewHolder h = (ImageComposeWithTransitionActivity.ItemViewHolder) holder;

                PLComposeItem item = mImageItemList.get(position);
                Glide.with(ImageComposeWithTransitionActivity.this).load(item.getFilePath()).into(h.imageView);

                if (position == mImageItemList.size() - 1) {
                    h.textView.setVisibility(View.GONE);
                } else {
                    h.textView.setVisibility(View.VISIBLE);
                    PLTransitionType transitionType = item.getTransitionType();
                    h.textView.setText(getTransitionName(transitionType));
                }
            }

            @Override
            public int getItemCount() {
                return mImageItemList.size();
            }
        };
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(mAdapter);

        mImageComposer = new PLImageComposer(this);
        mImageComposer.setPreviewListener(new PLPreviewListener() {
            @Override
            public void onProgress(float progress) {
                mProgressBar.setProgress((int) (progress * 10000));
            }

            @Override
            public void onCompleted() {
                Toast.makeText(ImageComposeWithTransitionActivity.this, "播放完成", Toast.LENGTH_SHORT).show();
            }
        });

        mProgressBar.setMax(10000);
        mProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float delta = progress / 10000f;
                    if (mImageComposer.getDurationMs() != 0) {
                        long seekTime = (long) (mImageComposer.getDurationMs() * delta);
                        mImageComposer.seekPreview(seekTime);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        setupAction();
    }

    private void prepareTransitions() {
        mTransitionMap.put("淡入淡出", PLTransitionType.FADE);
        mTransitionMap.put("从左飞入", PLTransitionType.SLIDE_IN_FROM_LEFT);
        mTransitionMap.put("从上飞入", PLTransitionType.SLIDE_IN_FROM_TOP);
        mTransitionMap.put("从右飞入", PLTransitionType.SLIDE_IN_FROM_RIGHT);
        mTransitionMap.put("从下飞入", PLTransitionType.SLIDE_IN_FROM_BOTTOM);
        mTransitionMap.put("从左擦除", PLTransitionType.WIPE_FROM_LEFT);
        mTransitionMap.put("从上擦除", PLTransitionType.WIPE_FROM_TOP);
        mTransitionMap.put("从右擦除", PLTransitionType.WIPE_FROM_RIGHT);
        mTransitionMap.put("从下擦除", PLTransitionType.WIPE_FROM_BOTTOM);
        mTransitionMap.put("闪黑", PLTransitionType.FADE_COLOR_BLACK);
        mTransitionMap.put("闪白", PLTransitionType.FADE_COLOR_WHITE);
        mTransitionMap.put("圆形缩放", PLTransitionType.CIRCLE_CROP);
    }

    private void setupAction() {
        mTexturePreview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mImageComposer.setPreviewTexture(surface);
                mImageComposer.setPreviewSize(width, height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                mImageComposer.setPreviewSize(width, height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

        mBtnAddImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, 100);
        });

        mBtnStart.setOnClickListener(v -> mImageComposer.startPreview());

        mBtnResume.setOnClickListener(v -> mImageComposer.resumePreview());

        mBtnPause.setOnClickListener(v -> mImageComposer.pausePreview());

        mBtnStop.setOnClickListener(v -> mImageComposer.stopPreview());

        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PLVideoEncodeSetting exportConfig = new PLVideoEncodeSetting(ImageComposeWithTransitionActivity.this);
                if (mImageItemList.size() == 0) {
                    ToastUtils.showShortToast("请至少添加一项");
                    return;
                }
                stateChange(true);
                mImageComposer.save(Config.COMPOSE_WITH_TRANSITION_FILE_PATH, exportConfig, new PLVideoSaveListener() {
                    @Override
                    public void onSaveVideoSuccess(final String filepath) {
                        MediaStoreUtils.storeVideo(ImageComposeWithTransitionActivity.this, new File(filepath), "video/mp4");
                        runOnUiThread(() -> {
                            stateChange(false);
                            Toast.makeText(ImageComposeWithTransitionActivity.this, "合成成功", Toast.LENGTH_SHORT).show();
                            PlaybackActivity.start(ImageComposeWithTransitionActivity.this, filepath);
                        });
                    }

                    @Override
                    public void onSaveVideoFailed(int errorCode) {
                        runOnUiThread(() -> {
                            stateChange(false);
                            ToastUtils.showShortToast("错误码：" + errorCode);
                        });
                    }

                    @Override
                    public void onSaveVideoCanceled() {
                        runOnUiThread(() -> {
                            stateChange(false);
                            Toast.makeText(ImageComposeWithTransitionActivity.this, "停止合成", Toast.LENGTH_SHORT).show();

                        });
                    }

                    @Override
                    public void onProgressUpdate(float v) {
                        mProgressBar.setProgress((int) (10000 * v));
                    }
                });
            }
        });

        mBtnCancel.setOnClickListener(v -> mImageComposer.cancelSave());
    }

    private void stateChange(boolean processing) {
        if (processing) {
            mBtnAddImage.setEnabled(false);
            mBtnStart.setEnabled(false);
            mBtnResume.setEnabled(false);
            mBtnPause.setEnabled(false);
            mBtnStop.setEnabled(false);
            mBtnSave.setEnabled(false);
        } else {
            mBtnAddImage.setEnabled(true);
            mBtnStart.setEnabled(true);
            mBtnResume.setEnabled(true);
            mBtnPause.setEnabled(true);
            mBtnStop.setEnabled(true);
            mBtnSave.setEnabled(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data.getData() != null) {
            if (requestCode == 100) {
                final String selectedFilepath = GetPathFromUri.getRealPathFromURI(this, data.getData());
                Log.i(TAG, "Select file: " + selectedFilepath);
                if (selectedFilepath != null && !"".equals(selectedFilepath)) {
                    showAddItemDialog(selectedFilepath);
                }
            }
        }
    }

    private void showAddItemDialog(final String selectedFilepath) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_image_transition, null);
        final EditText etImageDuration = view.findViewById(R.id.et_image_duration);
        final EditText etTransitionDuration = view.findViewById(R.id.et_transition_duration);
        Spinner spinnerTransitionType = view.findViewById(R.id.spinner_transition_type);

        final String[] transitionTypes = mTransitionMap.keySet().toArray(new String[0]);
        final String[] currentKey = {transitionTypes[0]};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, transitionTypes);
        spinnerTransitionType.setAdapter(adapter);
        spinnerTransitionType.setSelection(0);
        spinnerTransitionType.setAdapter(adapter);
        spinnerTransitionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentKey[0] = transitionTypes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("确定", (dialog, which) -> {
                    long imageDuration = Long.parseLong(etImageDuration.getText().toString());
                    long transitionDuration = Long.parseLong(etTransitionDuration.getText().toString());
                    PLTransitionType transitionType = mTransitionMap.get(currentKey[0]);
                    addItem(selectedFilepath, imageDuration, transitionDuration, transitionType);
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mImageComposer != null) {
            mImageComposer.release();
        }
    }

    private void addItem(String path, long imageDuration, long transitionDuration, PLTransitionType transitionType) {
        PLComposeItem item = new PLComposeItem(path);
        item.setDurationMs(imageDuration);
        item.setTransitionTimeMs(transitionDuration);
        item.setItemType(PLComposeItem.ItemType.IMAGE);
        item.setTransitionType(transitionType);
        mImageComposer.addItem(item);
        mImageItemList.add(item);
        mAdapter.notifyDataSetChanged();
    }

    private String getTransitionName(PLTransitionType type) {
        for (Map.Entry<String, PLTransitionType> entry : mTransitionMap.entrySet()) {
            if (entry.getValue() == type) {
                return entry.getKey();
            }
        }
        return "未知转场";
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public TextView textView;

        public ItemViewHolder(Context context) {
            super(LayoutInflater.from(context).inflate(R.layout.holder_image_composer, null));
            imageView = itemView.findViewById(R.id.image_view);
            textView = itemView.findViewById(R.id.text_view);
        }
    }

}
