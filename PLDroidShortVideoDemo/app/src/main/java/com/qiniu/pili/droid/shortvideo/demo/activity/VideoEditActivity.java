package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.qiniu.pili.droid.shortvideo.demo.view.CustomProgressDialog;
import com.qiniu.pili.droid.shortvideo.demo.view.StrokedTextView;
import com.qiniu.pili.droid.shortvideo.demo.view.TextSelectorPanel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

public class VideoEditActivity extends Activity implements PLVideoSaveListener {
    private static final String TAG = "VideoEditActivity";
    private static final String MP4_PATH = "MP4_PATH";

    private GLSurfaceView mPreviewView;
    private RecyclerView mFiltersList;
    private TextSelectorPanel mTextSelectorPanel;
    private CustomProgressDialog mProcessingDialog;
    private ImageButton mMuteButton;
    private AudioMixSettingDialog mAudioMixSettingDialog;

    private PLShortVideoEditor mShortVideoEditor;
    private String mSelectedFilter;
    private String mSelectedMV;
    private String mSelectedMask;
    private PLWatermarkSetting mWatermarkSetting;
    private LinearLayout mEditingPanel;

    private EditText mCurTextView;
    private boolean mIsTextViewMoved;

    private int mFgVolume = 100;
    private int mBgVolume = 100;
    private int mFgVolumeBeforeMute = 100;
    private long mAudioMixPosition = 0;
    private long mMixDuration = 5000; // ms
    private boolean mIsMuted = false;
    private boolean mIsMixAudio = false;
    private boolean mIsAudioMixDialogShown = false;
    private boolean mIsUseWatermark = true;

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
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        setContentView(R.layout.activity_editor);
        mPreviewView = (GLSurfaceView) findViewById(R.id.preview);
        mFiltersList = (RecyclerView) findViewById(R.id.recycler_view);
        mTextSelectorPanel = (TextSelectorPanel) findViewById(R.id.text_selector_panel);
        mEditingPanel = (LinearLayout) findViewById(R.id.editing_panel);
        mTextSelectorPanel.setOnTextClickedListener(new TextSelectorPanel.OnTextClickedListener() {
            @Override
            public void onTextClicked(StrokedTextView textView) {
                addText(textView);
            }
        });
        mTextSelectorPanel.setOnViewClosedListener(new TextSelectorPanel.OnViewClosedListener() {
            @Override
            public void onViewClosed() {
                mEditingPanel.setVisibility(View.VISIBLE);
                mTextSelectorPanel.setVisibility(View.GONE);
            }
        });

        mPreviewView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurTextView != null && mCurTextView.getBackground() != null) {
                    mCurTextView.setBackgroundResource(0);
                    mCurTextView = null;
                }
            }
        });

        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mShortVideoEditor.cancelSave();
            }
        });

        mMuteButton = (ImageButton) findViewById(R.id.mute_button);
        mMuteButton.setImageResource(R.mipmap.btn_unmute);

        mWatermarkSetting = new PLWatermarkSetting();
        mWatermarkSetting.setResourceId(R.drawable.qiniu_logo);
        mWatermarkSetting.setPosition(0.01f, 0.01f);
        mWatermarkSetting.setAlpha(128);

        String path = getIntent().getStringExtra(MP4_PATH);
        Log.i(TAG, "editing file: " + path);

        PLVideoEditSetting setting = new PLVideoEditSetting();
        setting.setSourceFilepath(path);
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

    public void onClickReset(View v) {
        mSelectedFilter = null;
        mSelectedMV = null;
        mSelectedMask = null;
        mShortVideoEditor.setBuiltinFilter(null);
        mShortVideoEditor.setMVEffect(null, null);
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

    public void onClickTextSelect(View v) {
        mEditingPanel.setVisibility(View.GONE);
        mTextSelectorPanel.setVisibility(View.VISIBLE);
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

    public void onClickToggleWatermark(View v) {
        mIsUseWatermark = !mIsUseWatermark;
        mShortVideoEditor.setWatermark(mIsUseWatermark ? mWatermarkSetting : null);
    }

    public void addText(StrokedTextView selectText) {
        final StrokedTextView textView = new StrokedTextView(this);
        textView.setText("点击输入文字");
        textView.setTextSize(40);
        textView.setTypeface(selectText.getTypeface());
        textView.setTextColor(selectText.getTextColors());
        textView.setShadowLayer(selectText.getShadowRadius(), selectText.getShadowDx(), selectText.getShadowDy(), selectText.getShadowColor());
        textView.setAlpha(selectText.getAlpha());
        textView.setStrokeWidth(selectText.getStrokeWidth());
        textView.setStrokeColor(selectText.getStrokeColor());

        mShortVideoEditor.addTextView(textView);

        if (mCurTextView != null) {
            mCurTextView.setBackgroundResource(0);
        }
        mCurTextView = textView;
        mCurTextView.setBackgroundResource(R.drawable.border_text_view);

        GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                mShortVideoEditor.removeTextView(textView);
                if (mCurTextView != null) {
                    mCurTextView = null;
                }
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (mIsTextViewMoved) {
                    return true;
                }
                final EditText edit = new EditText(VideoEditActivity.this);
                edit.setText(textView.getText());

                AlertDialog.Builder builder = new AlertDialog.Builder(VideoEditActivity.this);
                builder.setView(edit);
                builder.setTitle("请输入文字");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        textView.setText(edit.getText());
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

                return true;
            }
        };
        final GestureDetector gestureDetector = new GestureDetector(VideoEditActivity.this, simpleOnGestureListener);

        textView.setOnTouchListener(new View.OnTouchListener() {
            private float lastTouchRawX;
            private float lastTouchRawY;
            private boolean scale;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }

                int action = event.getAction();
                float touchRawX = event.getRawX();
                float touchRawY = event.getRawY();
                float touchX = event.getX();
                float touchY = event.getY();

                if (action == MotionEvent.ACTION_DOWN) {
                    boolean xOK = touchX >= v.getWidth() * 3 / 4 && touchX <= v.getWidth();
                    boolean yOK = touchY >= v.getHeight() * 2 / 4 && touchY <= v.getHeight();
                    scale = xOK && yOK;

                    if (mCurTextView != null) {
                        mCurTextView.setBackgroundResource(0);
                    }
                    mCurTextView = textView;
                    mCurTextView.setBackgroundResource(R.drawable.border_text_view);
                }

                if (action == MotionEvent.ACTION_MOVE) {
                    float deltaRawX = touchRawX - lastTouchRawX;
                    float deltaRawY = touchRawY - lastTouchRawY;

                    if (scale) {
                        // rotate
                        float centerX = v.getX() + (float) v.getWidth() / 2;
                        float centerY = v.getY() + (float) v.getHeight() / 2;
                        double angle = Math.atan2(touchRawY - centerY, touchRawX - centerX) * 180 / Math.PI;
                        v.setRotation((float) angle - 45);

                        // scale
                        float xx = (touchRawX >= centerX ? deltaRawX : -deltaRawX);
                        float yy = (touchRawY >= centerY ? deltaRawY : -deltaRawY);
                        float sf = (v.getScaleX() + xx / v.getWidth() + v.getScaleY() + yy / v.getHeight()) / 2;
                        v.setScaleX(sf);
                        v.setScaleY(sf);
                    } else {
                        // translate
                        v.setTranslationX(v.getTranslationX() + deltaRawX);
                        v.setTranslationY(v.getTranslationY() + deltaRawY);
                    }
                    mIsTextViewMoved = true;
                }

                if (action == MotionEvent.ACTION_UP) {
                    mIsTextViewMoved = false;
                }

                lastTouchRawX = touchRawX;
                lastTouchRawY = touchRawY;
                return true;
            }
        });
        ToastUtils.s(this, "触摸文字右下角控制缩放与旋转，双击移除。");
    }

    public void onClickShowFilters(View v) {
        mFiltersList.setAdapter(new FilterListAdapter(mShortVideoEditor.getBuiltinFilterList()));
    }

    public void onClickShowMVs(View v) {
        try {
            File dir = new File(Environment.getExternalStorageDirectory() + "/ShortVideo/mvs");
            // copy mv assets to sdcard
            if (!dir.exists()) {
                dir.mkdirs();
                String[] fs = getAssets().list("mvs");
                for (String file : fs) {
                    InputStream is = getAssets().open("mvs/" + file);
                    FileOutputStream fos = new FileOutputStream(new File(dir, file));
                    byte[] buffer = new byte[1024];
                    int byteCount;
                    while ((byteCount = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, byteCount);
                    }
                    fos.flush();
                    is.close();
                    fos.close();
                }
            }

            FileReader jsonFile = new FileReader(new File(dir, "plsMVs.json"));
            StringBuilder sb = new StringBuilder();
            int read;
            char[] buf = new char[2048];
            while ((read = jsonFile.read(buf, 0, 2048)) != -1) {
                sb.append(buf, 0, read);
            }
            Log.i(TAG, sb.toString());
            JSONObject json = new JSONObject(sb.toString());
            mFiltersList.setAdapter(new MVListAdapter(json.getJSONArray("MVs")));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        mShortVideoEditor.setMVEffect(mSelectedMV, mSelectedMask);
        mShortVideoEditor.setWatermark(mIsUseWatermark ? mWatermarkSetting : null);
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

        if (mCurTextView != null && mCurTextView.getBackground() != null) {
            mCurTextView.setBackgroundResource(0);
            mCurTextView = null;
        }
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

    @Override
    public void onSaveVideoCanceled() {
        mProcessingDialog.dismiss();
    }

    @Override
    public void onProgressUpdate(float percentage) {
        mProcessingDialog.setProgress((int) (100 * percentage));
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
            View contactView = inflater.inflate(R.layout.filter_item, parent, false);
            FilterItemViewHolder viewHolder = new FilterItemViewHolder(contactView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(FilterItemViewHolder holder, int position) {
            try {
                if (position == 0) {
                    holder.mName.setText("None");
                    Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("filters/none.png"));
                    holder.mIcon.setImageBitmap(bitmap);
                    holder.mIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mSelectedFilter = null;
                            mShortVideoEditor.setBuiltinFilter(null);
                        }
                    });
                    return;
                }

                final PLBuiltinFilter filter = mFilters[position - 1];
                holder.mName.setText(filter.getName());
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
            return mFilters != null ? mFilters.length + 1 : 0;
        }
    }

    private class MVListAdapter extends RecyclerView.Adapter<FilterItemViewHolder> {
        private JSONArray mMVArray;

        public MVListAdapter(JSONArray mvArray) {
            this.mMVArray = mvArray;
        }

        @Override
        public FilterItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View contactView = inflater.inflate(R.layout.filter_item, parent, false);
            FilterItemViewHolder viewHolder = new FilterItemViewHolder(contactView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(FilterItemViewHolder holder, int position) {
            try {
                if (position == 0) {
                    holder.mName.setText("None");
                    Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("mvs/mv.png"));
                    holder.mIcon.setImageBitmap(bitmap);
                    holder.mIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mSelectedMV = null;
                            mSelectedMask = null;
                            mShortVideoEditor.setMVEffect(null, null);
                        }
                    });
                    return;
                }

                final JSONObject mv = mMVArray.getJSONObject(position - 1);
                holder.mName.setText(mv.getString("name"));
                InputStream is = getAssets().open("mvs/" + mv.getString("coverDir") + ".png");
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                holder.mIcon.setImageBitmap(bitmap);
                holder.mIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            mSelectedMV = Config.VIDEO_STORAGE_DIR + "mvs/" + mv.getString("colorDir") + ".mp4";
                            mSelectedMask = Config.VIDEO_STORAGE_DIR + "mvs/" + mv.getString("alphaDir") + ".mp4";
                            mShortVideoEditor.setMVEffect(mSelectedMV, mSelectedMask);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return mMVArray != null ? mMVArray.length() + 1 : 0;
        }
    }

    private AudioMixSettingDialog.OnAudioVolumeChangedListener mOnAudioVolumeChangedListener = new AudioMixSettingDialog.OnAudioVolumeChangedListener() {
        @Override
        public void onAudioVolumeChanged(int fgVolume, int bgVolume) {
            mFgVolume = fgVolume;
            mBgVolume = bgVolume;
            setMixVolume();
            mIsMuted = mFgVolume == 0;
            mMuteButton.setImageResource(mIsMuted ? R.mipmap.btn_mute : R.mipmap.btn_unmute);
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
