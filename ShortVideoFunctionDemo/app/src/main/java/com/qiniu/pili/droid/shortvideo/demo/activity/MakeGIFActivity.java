package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.collection.LruCache;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.pili.droid.shortvideo.PLShortVideoComposer;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.GetPathFromUri;
import com.qiniu.pili.droid.shortvideo.demo.utils.MediaStoreUtils;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.qiniu.pili.droid.shortvideo.demo.utils.Config.GIF_SAVE_PATH;

public class MakeGIFActivity extends AppCompatActivity {
    private static final String TAG = "MakeGIFActivity";

    private static final float CACHE_FREE_MEMORY_PERCENTAGE = 0.7f;
    private static final int THUMBNAIL_EDGE = 400;

    private ProgressDialog mProcessingDialog;
    private GridView mGridView;

    private PLMediaFile mMediaFile;
    private PLShortVideoComposer mShortVideoComposer;
    private List<Integer> mSelectedFrameIndex;

    private LruCache<Integer, Bitmap> mBitmapCache;
    private Map<Integer, LoadFrameTask> mOngoingTasks;

    private String mInputFilePath;
    private EditText mGIFZoomFactorText;
    private EditText mGIFTotalFrameCountText;
    private EditText mGIFFrameRateText;
    private EditText mGIFStartTimeText;
    private EditText mGIFEndTimeText;
    public static final String OUTPUT_GIF_COVER_PATH = "/sdcard/ShortVideo/gif_cover.gif";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        setTitle(R.string.title_gif);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 0);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            mInputFilePath = GetPathFromUri.getPath(this, data.getData());
            Log.i(TAG, "Select file: " + mInputFilePath);
            if (mInputFilePath != null && !"".equals(mInputFilePath)) {
                init(mInputFilePath);
            }
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaFile != null) {
            mMediaFile.release();
        }
    }

    private int calculateCacheCount() {
        Runtime runtime = Runtime.getRuntime();
        long freeBytes = runtime.maxMemory() - runtime.totalMemory() - runtime.freeMemory();
        long cacheUseBytes = (long) (freeBytes * CACHE_FREE_MEMORY_PERCENTAGE);
        long perBitmapBytes = THUMBNAIL_EDGE * THUMBNAIL_EDGE * 4;

        int cacheCount = (int) (cacheUseBytes / perBitmapBytes);
        Log.i(TAG, "free bytes: " + freeBytes +
                ", use " + (int) (CACHE_FREE_MEMORY_PERCENTAGE * 100) + "% for cache: " + cacheUseBytes +
                ", per bitmap occupy: " + perBitmapBytes +
                ", cache count: " + cacheCount);
        return cacheCount;
    }

    private void init(String videoPath) {
        setContentView(R.layout.activity_make_gif);
        mMediaFile = new PLMediaFile(videoPath);
        mShortVideoComposer = new PLShortVideoComposer(this);
        mSelectedFrameIndex = new ArrayList<>();

        mBitmapCache = new LruCache<>(calculateCacheCount());
        mOngoingTasks = new Hashtable<>();

        mGIFZoomFactorText = (EditText) findViewById(R.id.zoom_factor);
        mGIFTotalFrameCountText = (EditText) findViewById(R.id.total_frame_count);
        mGIFFrameRateText = (EditText) findViewById(R.id.gif_framerate);

        mGIFStartTimeText = (EditText) findViewById(R.id.start_time);
        mGIFEndTimeText = (EditText) findViewById(R.id.end_time);

        mGridView = (GridView) findViewById(R.id.key_frame_grid);
        final FrameAdapter adapter = new FrameAdapter(this);
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int color;
                if (!mSelectedFrameIndex.contains(i)) {
                    mSelectedFrameIndex.add(i);
                    color = R.color.colorAccent;
                } else {
                    mSelectedFrameIndex.remove((Integer) i);
                    color = R.color.white;
                }
                view.setBackgroundColor(getResources().getColor(color));
            }
        });
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisibleItem = firstVisibleItem + visibleItemCount - 1;
                Iterator<Integer> i = mOngoingTasks.keySet().iterator();
                while (i.hasNext()) {
                    int p = i.next();
                    LoadFrameTask task = mOngoingTasks.get(p);
                    boolean willCancelTask = task != null && !(p >= firstVisibleItem && p <= lastVisibleItem);
                    if (willCancelTask) {
                        task.cancel(true);
                        i.remove();
                        Log.i(TAG, "cancel task position: " + p);
                    }
                }
            }
        });

        mProcessingDialog = new ProgressDialog(MakeGIFActivity.this);
        mProcessingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProcessingDialog.setCancelable(false);
        mProcessingDialog.setCanceledOnTouchOutside(false);
        mProcessingDialog.setMessage("正在生成");
    }

    public void onClickExtrackGIFCover(View v) {
        int outputWeight = mMediaFile.getVideoWidth();
        int outputHeight = mMediaFile.getVideoHeight();
        float zoomFactor = Float.parseFloat(mGIFZoomFactorText.getText().toString());
        if (zoomFactor < 0) {
            zoomFactor = 1;
        } else if ((zoomFactor > 2) || (zoomFactor < 0.5)) {
            ToastUtils.showShortToast(this, "缩放倍数异常，建议范围[0.5, 2]");
            return;
        } else {
            outputWeight *= zoomFactor;
            outputHeight *= zoomFactor;
        }
        int totalFrameCount = Integer.parseInt(mGIFTotalFrameCountText.getText().toString());
        if (totalFrameCount < 0) {
            totalFrameCount = 20;
        } else if (totalFrameCount > 50) {
            ToastUtils.showShortToast(this, "GIF帧数过大，系统运行缓慢，请减小帧数设置");
            return;
        }
        int gifFrameRate = Integer.parseInt(mGIFFrameRateText.getText().toString());
        if ((gifFrameRate < 0) || (gifFrameRate > 120)) {
            ToastUtils.showShortToast(this, "GIF输出帧率异常，请选择1~120之间的整数");
            return;
        }
        int startTime = Integer.parseInt(mGIFStartTimeText.getText().toString());
        if (startTime < 0) {
            startTime = 0;
        }
        int endTime = Integer.parseInt(mGIFEndTimeText.getText().toString());
        if ((endTime < 0) || (endTime < startTime)) {
            ToastUtils.showShortToast(this, "时间参数异常，请重新设置");
            return;
        }

        mProcessingDialog.show();
        mShortVideoComposer.extractVideoToGIF(mInputFilePath, startTime * 1000, endTime * 1000, totalFrameCount,
                outputWeight, outputHeight, gifFrameRate, true, OUTPUT_GIF_COVER_PATH, new PLVideoSaveListener() {
                    @Override
                    public void onSaveVideoSuccess(String s) {
                        mProcessingDialog.dismiss();
                        Intent intent = new Intent(MakeGIFActivity.this, ShowGIFActivity.class);
                        intent.putExtra(ShowGIFActivity.GIF_PATH, OUTPUT_GIF_COVER_PATH);
                        startActivity(intent);
                    }

                    @Override
                    public void onSaveVideoFailed(int i) {
                        mProcessingDialog.dismiss();
                        ToastUtils.showShortToast(MakeGIFActivity.this, "Failed");
                    }

                    @Override
                    public void onSaveVideoCanceled() {
                        mProcessingDialog.dismiss();
                        ToastUtils.showShortToast(MakeGIFActivity.this, "Canceled");
                    }

                    @Override
                    public void onProgressUpdate(float v) {

                    }
                });
    }

    public void onClickMakeGIF(View v) {
        if (mSelectedFrameIndex.size() <= 0) {
            ToastUtils.showShortToast(this, "请先选择帧");
            mProcessingDialog.dismiss();
            return;
        }
        mProcessingDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Bitmap> bitmaps = new ArrayList<>();
                for (int i = 0; i < mSelectedFrameIndex.size(); i++) {
                    bitmaps.add(mMediaFile.getVideoFrameByIndex(mSelectedFrameIndex.get(i), true).toBitmap());
                }
                mProcessingDialog.setCancelable(true);
                mProcessingDialog.setOnCancelListener(dialogInterface -> mShortVideoComposer.cancelComposeToGIF());
                mShortVideoComposer.composeToGIF(bitmaps, 500, true, GIF_SAVE_PATH, new PLVideoSaveListener() {
                    @Override
                    public void onSaveVideoSuccess(String filepath) {
                        MediaStoreUtils.storeImage(MakeGIFActivity.this, new File(filepath), "iamge/gif");
                        mProcessingDialog.dismiss();
                        Intent intent = new Intent(MakeGIFActivity.this, ShowGIFActivity.class);
                        intent.putExtra(ShowGIFActivity.GIF_PATH, GIF_SAVE_PATH);
                        startActivity(intent);
                    }

                    @Override
                    public void onSaveVideoFailed(int i) {
                        mProcessingDialog.dismiss();
                        ToastUtils.showShortToast(MakeGIFActivity.this, "Failed");
                    }

                    @Override
                    public void onSaveVideoCanceled() {
                        ToastUtils.showShortToast(MakeGIFActivity.this, "Canceled");

                    }

                    @Override
                    public void onProgressUpdate(float v) {

                    }
                });
            }
        }).start();
    }

    private class FrameAdapter extends BaseAdapter {
        private Context mContext;

        public FrameAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return mMediaFile.getVideoFrameCount(true);
        }

        @Override
        public Object getItem(int position) {
            return mMediaFile.getVideoFrameByIndex(position, true);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(300, 300));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setTag(position);
            imageView.setImageDrawable(null);
            imageView.setBackgroundColor(getResources().getColor(mSelectedFrameIndex.contains(position) ? R.color.colorAccent : R.color.white));

            Bitmap cached = mBitmapCache.get(position);
            if (cached != null) {
                imageView.setImageBitmap(cached);
            } else {
                LoadFrameTask task = mOngoingTasks.get(position);
                if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
                    task.cancel(true);
                }
                task = new LoadFrameTask(position, imageView);
                mOngoingTasks.put(position, task);
                task.execute();
            }
            return imageView;
        }
    }

    private class LoadFrameTask extends AsyncTask<Void, Void, Bitmap> {
        private int mIndex;
        private ImageView mImageView;

        public LoadFrameTask(int index, ImageView imageView) {
            mIndex = index;
            mImageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... a) {
            Bitmap bmp = mMediaFile.getVideoFrameByIndex(mIndex, true, THUMBNAIL_EDGE, THUMBNAIL_EDGE).toBitmap();
            mBitmapCache.put(mIndex, bmp);
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (((Integer) mImageView.getTag()) == mIndex) {
                mImageView.setImageBitmap(result);
            }
            mOngoingTasks.remove(mIndex);
        }
    }
}
