package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.qiniu.pili.droid.shortvideo.PLDraft;
import com.qiniu.pili.droid.shortvideo.PLDraftBox;
import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.pili.droid.shortvideo.PLVideoFrame;
import com.qiniu.pili.droid.shortvideo.demo.R;

import java.io.File;
import java.util.List;

public class DraftBoxActivity extends AppCompatActivity {

    private TextView mNullView;
    private ListView mListView;

    private DraftAdapter mAdapter;
    private List<PLDraft> mDrafts;

    private PLDraftBox mDraftBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft_box);
        mDraftBox = PLDraftBox.getInstance(this);
        mDrafts = mDraftBox.getAllDrafts();
        mAdapter = new DraftAdapter(this);

        mNullView = findViewById(R.id.tv_null_view);
        mListView = findViewById(R.id.draft_list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent = new Intent(DraftBoxActivity.this, VideoRecordActivity.class);
            PLDraft draft = mDrafts.get(i);
            intent.putExtra(VideoRecordActivity.DRAFT, draft.getTag());
            startActivity(intent);
        });
        mListView.setOnItemLongClickListener((adapterView, view, pos, l) -> {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(DraftBoxActivity.this).setTitle(R.string.dlg_delete_draft).setPositiveButton(getString(R.string.dlg_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    PLDraft draft = mDrafts.get(pos);
                    PLDraftBox.getInstance(DraftBoxActivity.this).removeDraftByTag(draft.getTag(), true);
                    updateView();
                }
            });
            alertDialog.show();
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateView();
    }

    private void updateView() {
        if (mAdapter != null) {
            mDrafts = PLDraftBox.getInstance(DraftBoxActivity.this).getAllDrafts();
            if (mDrafts.size() == 0) {
                mListView.setVisibility(View.GONE);
                mNullView.setVisibility(View.VISIBLE);
            } else {
                mListView.setVisibility(View.VISIBLE);
                mNullView.setVisibility(View.GONE);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private class DraftAdapter extends BaseAdapter {
        Context mContext;

        public DraftAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return mDrafts.size();
        }

        @Override
        public Object getItem(int pos) {
            return mDrafts.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            return 0;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            TextView textView;
            ImageView imageView;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_draft, parent, false);
                textView = convertView.findViewById(R.id.draft_tag);
                textView.setText(mDrafts.get(pos).getTag());
                imageView = convertView.findViewById(R.id.draft_thumbnail);
                File file = new File(mDrafts.get(pos).getSectionFilePath(0));
                if (file.exists()) {
                    PLMediaFile mediaFile = new PLMediaFile(file.getAbsolutePath());
                    PLVideoFrame frame = mediaFile.getVideoFrameByTime(100, false);
                    mediaFile.release();
                    if (frame != null) {
                        imageView.setImageBitmap(frame.toBitmap());
                    } else {
                        imageView.setBackgroundResource(R.drawable.bg_invalid_draft);
                    }
                }
            }
            return convertView;
        }
    }
}
