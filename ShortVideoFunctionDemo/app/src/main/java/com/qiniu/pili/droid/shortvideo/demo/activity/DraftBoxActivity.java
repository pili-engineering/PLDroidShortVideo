package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.PLDraft;
import com.qiniu.pili.droid.shortvideo.PLDraftBox;
import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.pili.droid.shortvideo.PLVideoFrame;
import com.qiniu.pili.droid.shortvideo.demo.R;

import java.util.List;

public class DraftBoxActivity extends AppCompatActivity {

    private ListView mListView;

    private List<PLDraft> mDrafts;
    private DraftAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft_box);
        mDrafts = PLDraftBox.getInstance(this).getAllDrafts();
        mAdapter = new DraftAdapter(this);

        mListView = (ListView) findViewById(R.id.draft_list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(DraftBoxActivity.this, VideoRecordActivity.class);
                PLDraft draft = mDrafts.get(i);
                intent.putExtra(VideoRecordActivity.DRAFT, draft.getTag());
                startActivity(intent);
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int pos, long l) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(DraftBoxActivity.this)
                        .setTitle(R.string.dlg_delete_draft)
                        .setPositiveButton(getString(R.string.dlg_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                PLDraft draft = mDrafts.get(pos);
                                PLDraftBox.getInstance(DraftBoxActivity.this).removeDraftByTag(draft.getTag(), true);
                                mAdapter.refresh();
                            }
                        });
                alertDialog.show();
                return true;
            }
        });
    }

    private class DraftAdapter extends BaseAdapter {
        Context mContext;
        boolean mIsNotifyDataChanged = false;

        public DraftAdapter(Context context) {
            mContext = context;
        }

        public void refresh() {
            mDrafts = PLDraftBox.getInstance(DraftBoxActivity.this).getAllDrafts();
            mIsNotifyDataChanged = true;
            notifyDataSetChanged();
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
            if (convertView == null || mIsNotifyDataChanged) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_draft, parent, false);
                textView = (TextView) convertView.findViewById(R.id.draft_tag);
                textView.setText(mDrafts.get(pos).getTag());
                imageView = (ImageView) convertView.findViewById(R.id.draft_thumbnail);
                PLVideoFrame frame = new PLMediaFile(mDrafts.get(pos).getSectionFilePath(0)).getVideoFrameByTime(100, false);
                if (frame != null) {
                    imageView.setImageBitmap(frame.toBitmap());
                } else {
                    imageView.setBackgroundResource(R.drawable.bg_invalid_draft);
                }
            }
            if (pos == mDrafts.size()) {
                mIsNotifyDataChanged = false;
            }
            return convertView;
        }
    }
}
