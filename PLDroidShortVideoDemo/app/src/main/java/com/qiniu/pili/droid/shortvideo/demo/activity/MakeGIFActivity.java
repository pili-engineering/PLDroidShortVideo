package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.pili.droid.shortvideo.PLShortVideoComposer;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.GetPathFromUri;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        setTitle(R.string.title_gif);

        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("video/*");
        }
        startActivityForResult(Intent.createChooser(intent, "选择要导入的视频"), 0);
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
            String selectedFilepath = GetPathFromUri.getPath(this, data.getData());
            Log.i(TAG, "Select file: " + selectedFilepath);
            if (selectedFilepath != null && !"".equals(selectedFilepath)) {
                init(selectedFilepath);
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
                    mSelectedFrameIndex.remove(mSelectedFrameIndex.indexOf(i));
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
                    if (task != null && !(p >= firstVisibleItem && p <= lastVisibleItem)) {
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

    public void onClickMakeGIF(View v) {
        if (mSelectedFrameIndex.size() <= 0) {
            ToastUtils.s(this, "请先选择帧");
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
                mProcessingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        mShortVideoComposer.cancelComposeToGIF();
                    }
                });
                mShortVideoComposer.composeToGIF(bitmaps, 500, true, GIF_SAVE_PATH, new PLVideoSaveListener() {
                    @Override
                    public void onSaveVideoSuccess(String s) {
                        mProcessingDialog.dismiss();
                        startActivity(new Intent(MakeGIFActivity.this, ShowGIFActivity.class));
                        finish();
                    }

                    @Override
                    public void onSaveVideoFailed(int i) {
                        mProcessingDialog.dismiss();
                        ToastUtils.s(MakeGIFActivity.this, "Failed");
                    }

                    @Override
                    public void onSaveVideoCanceled() {
                        ToastUtils.s(MakeGIFActivity.this, "Canceled");

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
