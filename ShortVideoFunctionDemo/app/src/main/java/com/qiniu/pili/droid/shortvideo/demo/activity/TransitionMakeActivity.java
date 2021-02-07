package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.transition.Transition0;
import com.qiniu.pili.droid.shortvideo.demo.transition.Transition1;
import com.qiniu.pili.droid.shortvideo.demo.transition.Transition2;
import com.qiniu.pili.droid.shortvideo.demo.transition.Transition3;
import com.qiniu.pili.droid.shortvideo.demo.transition.Transition4;
import com.qiniu.pili.droid.shortvideo.demo.transition.Transition5;
import com.qiniu.pili.droid.shortvideo.demo.transition.TransitionBase;
import com.qiniu.pili.droid.shortvideo.demo.utils.MediaStoreUtils;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;
import com.qiniu.pili.droid.shortvideo.demo.view.CustomProgressDialog;
import com.qiniu.pili.droid.shortvideo.demo.view.TransitionEditView;

import java.io.File;
import java.lang.reflect.Constructor;

import static com.qiniu.pili.droid.shortvideo.demo.activity.VideoFrameActivity.DATA_EXTRA_PATH;
import static com.qiniu.pili.droid.shortvideo.demo.utils.Config.VIDEO_STORAGE_DIR;

public class TransitionMakeActivity extends Activity {
    private static final String TAG = "TransitionMakeActivity";

    private static String[] TRANSITION_TITLE = {
            "大标题", "章节", "简约", "引用", "标题与副标题", "片尾"
    };
    private static Class[] TRANSITION_CLASS = {
            Transition0.class, Transition1.class, Transition2.class, Transition3.class, Transition4.class, Transition5.class
    };

    private RecyclerView mTransListView;
    private CustomProgressDialog mProcessingDialog;

    private TransitionBase[] mTransitions = new TransitionBase[6];
    private TransitionBase mCurTransition;
    private TransitionEditView mTransEditView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transition);

        initTransitions();

        mTransListView = (RecyclerView) findViewById(R.id.recycler_transition);
        mTransEditView = (TransitionEditView) findViewById(R.id.transition_edit_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mTransListView.setLayoutManager(layoutManager);
        mTransListView.setAdapter(new TransListAdapter());

        //consumed the event
        mTransEditView.setOnTouchListener((v, event) -> true);

        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(dialog -> mCurTransition.cancelSave());
    }

    private void initTransitions() {
        final PLVideoEncodeSetting setting = new PLVideoEncodeSetting(TransitionMakeActivity.this);
        setting.setEncodingSizeLevel(PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL.VIDEO_ENCODING_SIZE_LEVEL_720P_3);
        for (int i = 0; i < mTransitions.length; i++) {
            final ViewGroup viewGroup = (ViewGroup) findViewById(getResources().getIdentifier("transition_container" + i, "id", getPackageName()));
            final int index = i;
            viewGroup.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Constructor constructor = TRANSITION_CLASS[index].getConstructor(ViewGroup.class, PLVideoEncodeSetting.class);
                        TransitionBase transition = (TransitionBase) constructor.newInstance(viewGroup, setting);

                        mTransitions[index] = transition;

                        //default show first transition
                        if (index == 0) {
                            transition.setVisibility(View.VISIBLE);
                            mCurTransition = transition;
                            mTransEditView.setTransition(mCurTransition);
                        } else {
                            transition.setVisibility(View.INVISIBLE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtils.showShortToast(TransitionMakeActivity.this, "Can not init Transition : " + "Transition" + index);
                    }
                }
            });
        }
    }

    public void onBackClicked(View view) {
        finish();
    }

    public void onSaveClicked(View view) {
        mProcessingDialog.show();
        String path = VIDEO_STORAGE_DIR + "pl-transition-" + System.currentTimeMillis() + ".mp4";

        mCurTransition.save(path, new PLVideoSaveListener() {
            @Override
            public void onSaveVideoSuccess(final String destFile) {
                Log.i(TAG, "save success: " + destFile);
                runOnUiThread(() -> {
                    mProcessingDialog.dismiss();
                    MediaStoreUtils.storeVideo(TransitionMakeActivity.this, new File(destFile), "video/mp4");
                    Intent intent = new Intent();
                    intent.putExtra(DATA_EXTRA_PATH, destFile);
                    setResult(VideoFrameActivity.TRANSITION_REQUEST_CODE, intent);
                    finish();
                });
            }

            @Override
            public void onSaveVideoFailed(int errorCode) {
            }

            @Override
            public void onSaveVideoCanceled() {
            }

            @Override
            public void onProgressUpdate(final float percentage) {
                runOnUiThread(() -> mProcessingDialog.setProgress((int) (100 * percentage)));
            }
        });
    }

    public void onEditClicked(View view) {
        mTransEditView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < mTransitions.length; i++) {
            mTransitions[i].destroy();
        }
    }

    @Override
    public void onBackPressed() {
        if (mTransEditView.getVisibility() == View.VISIBLE) {
            mTransEditView.setVisibility(View.GONE);
        } else {
            finish();
        }
    }

    private class TransViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitle;
        public View mItemView;

        public TransViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            mTitle = (TextView) itemView.findViewById(R.id.title_text);
        }
    }

    private class TransListAdapter extends RecyclerView.Adapter<TransViewHolder> {
        private View mSelectedView;

        @Override
        public TransViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View contactView = inflater.inflate(R.layout.item_transition_selector, parent, false);
            TransViewHolder viewHolder = new TransViewHolder(contactView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final TransViewHolder holder, final int position) {
            holder.mTitle.setText(TRANSITION_TITLE[position]);
            holder.mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectedView != null) {
                        mSelectedView.setSelected(false);
                    }
                    holder.mItemView.setSelected(true);
                    mSelectedView = holder.mItemView;

                    for (int i = 0; i < mTransitions.length; i++) {
                        mTransitions[i].setVisibility(View.GONE);
                    }
                    mTransitions[position].setVisibility(View.VISIBLE);
                    mTransitions[position].play();
                    mCurTransition = mTransitions[position];
                    mTransEditView.setTransition(mCurTransition);
                }
            });
        }

        @Override
        public int getItemCount() {
            return TRANSITION_TITLE.length;
        }
    }
}
