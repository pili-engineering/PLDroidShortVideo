package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.qiniu.pili.droid.shortvideo.PLComposeItem;
import com.qiniu.pili.droid.shortvideo.PLShortVideoComposer;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.Config;
import com.qiniu.pili.droid.shortvideo.demo.utils.GetPathFromUri;
import com.qiniu.pili.droid.shortvideo.demo.utils.RecordSettings;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;
import com.qiniu.pili.droid.shortvideo.demo.view.CustomProgressDialog;
import com.qiniu.pili.droid.shortvideo.demo.view.ImageListAdapter;

import java.util.List;

public class ImageComposeActivity extends AppCompatActivity {
    private static final String TAG = "ImageComposeActivity";

    public static final long DEFULT_DURATION = 3000;
    public static final long DEFULT_TRANS_TIME = 1000;

    private CustomProgressDialog mProcessingDialog;
    private PLShortVideoComposer mShortVideoComposer;
    private ImageListAdapter mImageListAdapter;
    private ListView mImageListView;
    private Spinner mEncodingSizeLevelSpinner;
    private Spinner mEncodingBitrateLevelSpinner;

    private int mDeletePosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_compose);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        setTitle(R.string.title_image_compose);

        mImageListView = (ListView) findViewById(R.id.ImageListView);
        mImageListAdapter = new ImageListAdapter(this);
        mImageListView.setAdapter(mImageListAdapter);

        mEncodingSizeLevelSpinner = (Spinner) findViewById(R.id.EncodingSizeLevelSpinner);
        mEncodingBitrateLevelSpinner = (Spinner) findViewById(R.id.EncodingBitrateLevelSpinner);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, RecordSettings.ENCODING_SIZE_LEVEL_TIPS_ARRAY);
        mEncodingSizeLevelSpinner.setAdapter(adapter1);
        mEncodingSizeLevelSpinner.setSelection(7);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, RecordSettings.ENCODING_BITRATE_LEVEL_TIPS_ARRAY);
        mEncodingBitrateLevelSpinner.setAdapter(adapter2);
        mEncodingBitrateLevelSpinner.setSelection(2);

        mShortVideoComposer = new PLShortVideoComposer(this);

        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mShortVideoComposer.cancelComposeVideos();
            }
        });

        mImageListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0, 0, 0, "删除");
            }
        });

        mImageListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mDeletePosition = position;
                return false;
            }
        });

        mImageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                createEditDialog(position);
            }
        });
    }

    public void createEditDialog(final int position) {
        final PLComposeItem item = (PLComposeItem) mImageListAdapter.getItem(position);
        if (item == null) {
            return;
        }

        View view = View.inflate(this, R.layout.dialog_edit_image_compose, null);
        final EditText editDuration = (EditText) view.findViewById(R.id.EditDuration);
        final EditText editTransTime = (EditText) view.findViewById(R.id.EditTransitionTime);

        editDuration.setText(String.valueOf(item.getDurationMs()));
        editTransTime.setText(String.valueOf(item.getTransitionTimeMs()));

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String durationStr = editDuration.getText().toString();
                String transTimeStr = editTransTime.getText().toString();
                if (durationStr.isEmpty() || transTimeStr.isEmpty()) {
                    return;
                }

                String path = item.getFilePath();
                long duration = Long.parseLong(durationStr);
                long transTime = Long.parseLong(transTimeStr);
                PLComposeItem newItem = new PLComposeItem(path);
                newItem.setDurationMs(duration);
                newItem.setTransitionTimeMs(transTime);

                mImageListAdapter.removeItem(position);
                mImageListAdapter.addItem(position, newItem);

                mImageListAdapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create().show();
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
                mImageListAdapter.removeItem(mDeletePosition);
                mImageListAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
        return true;
    }

    public void onClickAddImage(View v) {
        chooseImageFile();
    }

    public void onClickCompose(View v) {
        List<PLComposeItem> items = mImageListAdapter.getItemList();
        if (items.size() < 1) {
            ToastUtils.s(this, "请先添加至少 1 个图片");
            return;
        }
        mProcessingDialog.show();
        PLVideoEncodeSetting setting = new PLVideoEncodeSetting(this);
        setting.setEncodingSizeLevel(getEncodingSizeLevel(mEncodingSizeLevelSpinner.getSelectedItemPosition()));
        setting.setEncodingBitrate(getEncodingBitrateLevel(mEncodingBitrateLevelSpinner.getSelectedItemPosition()));
        mShortVideoComposer.composeImages(items, Config.IMAGE_COMPOSE_FILE_PATH, setting, mVideoSaveListener);
    }

    private PLVideoSaveListener mVideoSaveListener = new PLVideoSaveListener() {
        @Override
        public void onSaveVideoSuccess(String filepath) {
            mProcessingDialog.dismiss();
            PlaybackActivity.start(ImageComposeActivity.this, filepath);
        }

        @Override
        public void onSaveVideoFailed(int errorCode) {
            mProcessingDialog.dismiss();
        }

        @Override
        public void onSaveVideoCanceled() {
            mProcessingDialog.dismiss();
        }

        @Override
        public void onProgressUpdate(float percentage) {
            mProcessingDialog.setProgress((int) (100 * percentage));
        }
    };

    private void chooseImageFile() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
        }
        startActivityForResult(Intent.createChooser(intent, "选择要拼接的图片"), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String selectedFilepath = GetPathFromUri.getPath(this, data.getData());
            Log.i(TAG, "Select file: " + selectedFilepath);
            if (selectedFilepath != null && !"".equals(selectedFilepath)) {
                PLComposeItem item = new PLComposeItem(selectedFilepath);
                item.setDurationMs(DEFULT_DURATION).setTransitionTimeMs(DEFULT_TRANS_TIME);
                mImageListAdapter.addItem(item);
                mImageListAdapter.notifyDataSetChanged();
                ToastUtils.s(this, "单击条目可以进行编辑，长按可以删除");
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