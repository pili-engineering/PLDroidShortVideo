/**
 *  TuSDK
 *
 *  MonsterFaceFragment.java
 *  @author  H.ys
 *  @Date    2019/5/6 18:46
 *  @Copyright 	(c) 2019 tusdk.com. All rights reserved.
 *
 *
 */
package org.lasque.tusdkdemohelper.tusdk;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tusdkdemohelper.R;

import org.lasque.tusdk.core.TuSdkContext;
import org.lasque.tusdkdemohelper.tusdk.model.PropsItemMonster;
import org.lasque.tusdkdemohelper.tusdk.model.PropsItemMonsterCategory;


/**
 * 哈哈镜选项页
 */
public class MonsterFaceFragment extends Fragment {

    /**哈哈镜视图*/
    private RecyclerView mMonsterFaceListView;
    /**
     * 哈哈镜列表适配器
     */
    private MonsterFaceRecyclerAdapter mMonsterRecyclerAdapter;

    // 点击反馈
    public MonsterFaceFragment.OnMonsterItemClickListener listener;

    public void setOnStickerItemClickListener(MonsterFaceFragment.OnMonsterItemClickListener listener){
        this.listener = listener;
    }

    public interface OnMonsterItemClickListener{
        void onMonsterItemClick(PropsItemMonster itemData);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view =inflater.inflate(TuSdkContext.getLayoutResId("tusdk_sticker_listview_layout"),null);
        getMonsterListView(view);
        return view;
    }

    public static MonsterFaceFragment newInstance(){
        Bundle bundle = new Bundle();
        MonsterFaceFragment fragment = new MonsterFaceFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * 哈哈镜视图
     */
    public RecyclerView getMonsterListView(View view) {
        mMonsterFaceListView = (RecyclerView) view.findViewById(R.id.lsq_sticker_list_view);
        mMonsterRecyclerAdapter = new MonsterFaceRecyclerAdapter(getActivity());
        mMonsterFaceListView.setAdapter(mMonsterRecyclerAdapter);
        mMonsterRecyclerAdapter.setItemClickListener(mStickerClickListener);
        mMonsterRecyclerAdapter.setPropsItemMonsterList(PropsItemMonsterCategory.allCategories().get(0).getItems());
        GridLayoutManager grid = new GridLayoutManager(getActivity(), 5);
        mMonsterFaceListView.setLayoutManager(grid);
        mMonsterFaceListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.bottom = 30;
            }
        });
        return mMonsterFaceListView;
    }

    /** 哈哈镜点击事件 */
    private MonsterFaceRecyclerAdapter.ItemClickListener mStickerClickListener = new MonsterFaceRecyclerAdapter.ItemClickListener() {
        @Override
        public void onItemClick(int position, PropsItemMonster itemData) {
            if (listener!=null) listener.onMonsterItemClick(itemData);
        }
    };
}
