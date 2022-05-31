/**
 * TuSDK
 * <p>
 * TuSDKEditorBarFragment.java
 *
 * @author H.ys
 * @Date 2019/4/30 15:38
 * @Copyright (c) 2019 tusdk.com. All rights reserved.
 */
package org.lasque.tusdkdemohelper;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import org.lasque.tusdk.api.engine.TuSdkFilterEngine;
import org.lasque.tusdk.api.video.preproc.filter.TuSDKVideoProcesser;
import org.lasque.tusdk.core.TuSdkContext;
import org.lasque.tusdk.core.activity.TuSdkFragment;
import org.lasque.tusdk.core.seles.SelesParameters;
import org.lasque.tusdk.core.utils.TLog;
import org.lasque.tusdk.core.utils.ThreadHelper;
import org.lasque.tusdk.core.utils.json.JsonHelper;
import org.lasque.tusdk.modules.view.widget.sticker.StickerGroup;
import org.lasque.tusdk.video.editor.TuSdkMediaComicEffectData;
import org.lasque.tusdk.video.editor.TuSdkMediaEffectData;
import org.lasque.tusdk.video.editor.TuSdkMediaFilterEffectData;
import org.lasque.tusdk.video.editor.TuSdkMediaPlasticFaceEffect;
import org.lasque.tusdk.video.editor.TuSdkMediaSkinFaceEffect;
import org.lasque.tusdk.video.editor.TuSdkMediaStickerEffectData;
import org.lasque.tusdkdemohelper.tusdk.BeautyPlasticRecyclerAdapter;
import org.lasque.tusdkdemohelper.tusdk.BeautyRecyclerAdapter;
import org.lasque.tusdkdemohelper.tusdk.FilterRecyclerAdapter;
import org.lasque.tusdkdemohelper.tusdk.MonsterFaceFragment;
import org.lasque.tusdkdemohelper.tusdk.StickerFragment;
import org.lasque.tusdkdemohelper.tusdk.StickerGroupCategories;
import org.lasque.tusdkdemohelper.tusdk.TabPagerIndicator;
import org.lasque.tusdkdemohelper.tusdk.TabViewPagerAdapter;
import org.lasque.tusdkdemohelper.tusdk.filter.FilterConfigSeekbar;
import org.lasque.tusdkdemohelper.tusdk.filter.FilterConfigView;
import org.lasque.tusdkdemohelper.tusdk.model.PropsItemMonster;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.lasque.tusdk.video.editor.TuSdkMediaEffectData.TuSdkMediaEffectDataType.TuSdkMediaEffectDataTypeFilter;
import static org.lasque.tusdk.video.editor.TuSdkMediaEffectData.TuSdkMediaEffectDataType.TuSdkMediaEffectDataTypePlasticFace;
import static org.lasque.tusdk.video.editor.TuSdkMediaEffectData.TuSdkMediaEffectDataType.TuSdkMediaEffectDataTypeSkinFace;

/**
 * 底部特效编辑栏
 */
public class TuSDKEditorBarFragment extends TuSdkFragment {

    public static TuSDKEditorBarFragment newInstance(String[] mFilterGroup, String[] mCartoonFilterGroup,boolean hasMonsterFace) {
        TuSDKEditorBarFragment fragment = new TuSDKEditorBarFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArray("FilterGroup", mFilterGroup);
        bundle.putStringArray("CartoonFilterGroup", mCartoonFilterGroup);
        bundle.putBoolean("hasMonsterFace",hasMonsterFace);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static TuSDKEditorBarFragment newInstance(String[] mFilterGroup, String[] mCartoonFilterGroup){
        return newInstance(mFilterGroup,mCartoonFilterGroup,false);
    }

    private String[] mFilterGroup;

    private String[] mCartoonFilterGroup;

    private boolean mHasMonsterFace = false;

    public void setFilterEngine(TuSdkFilterEngine filterEngine) {
        this.mFilterEngine = filterEngine;

        ThreadHelper.postDelayed(new Runnable() {
            @Override
            public void run() {
                changeFilter(0);
            }
        }, 500);
    }

    // TuSDK Filter Engine
    private TuSdkFilterEngine mFilterEngine;

    // 参数调节视图
    private FilterConfigView mFilterConfigView;

    // 滤镜栏视图
    private RecyclerView mFilterRecyclerView;

    // 滤镜列表Adapter
    private FilterRecyclerAdapter mFilterAdapter;

    // 滤镜底部栏
    private View mFilterBottomView;

    // 贴纸视图
    private RelativeLayout mStickerLayout;
    // 取消贴纸
    private ImageView mStickerCancel;
    // 贴纸分类pager页
    private ViewPager mViewPager;
    // 贴纸Tab
    private TabPagerIndicator mTabPagerIndicator;
    // 贴纸Tab适配器
    private TabViewPagerAdapter mStickerPagerAdapter;
    // 贴纸数据
    private List<StickerGroupCategories> mStickerGroupCategoriesList;

    // 漫画列表视图
    private RecyclerView mCartoonRecyclerView;

    // 漫画布局
    private View mCartoonLayout;

    // 漫画列表Adapter
    private FilterRecyclerAdapter mCartoonAdapter;

    //微整形布局
    private View mBeautyPlasticBottomView;
    //微整形列表
    private RecyclerView mBeautyPlasticRecyclerView;
    // 微整形调节栏
    private FilterConfigView mBeautyConfigView;

    private View.OnClickListener mCartoonButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mCartoonLayout.setVisibility(mCartoonLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            mFilterBottomView.setVisibility(View.GONE);
            mBeautyPlasticBottomView.setVisibility(View.GONE);
            mStickerLayout.setVisibility(View.GONE);
            mFilterConfigView.setVisibility(View.GONE);
            mBeautyConfigView.setVisibility(View.GONE);
        }
    };

    private View.OnClickListener mBeautyPlasticButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mBeautyPlasticRecyclerView.setAdapter(mBeautyPlasticRecyclerAdapter);
            mBeautyPlasticBottomView.setVisibility(mBeautyPlasticBottomView.getVisibility() == View.VISIBLE
                    ? View.GONE : View.VISIBLE);
            mCartoonLayout.setVisibility(View.GONE);
            mFilterBottomView.setVisibility(View.GONE);
            mStickerLayout.setVisibility(View.GONE);
            mFilterConfigView.setVisibility(View.GONE);
            mBeautyConfigView.setVisibility(View.GONE);
        }
    };

    private View.OnClickListener mFilterButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // 显示 隐藏滤镜栏
            mFilterBottomView.setVisibility(mFilterBottomView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            // 隐藏微整形布局
            mBeautyPlasticBottomView.setVisibility(View.GONE);
            // 隐藏贴纸栏布局
            mStickerLayout.setVisibility(View.GONE);
            // 隐藏动漫滤镜
            mCartoonLayout.setVisibility(View.GONE);
            // 隐藏滤镜调节栏
            mFilterConfigView.setVisibility(View.GONE);
            // 隐藏微整形调节栏
            mBeautyConfigView.setVisibility(View.GONE);
        }
    };

    private View.OnClickListener mBeautySkinButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mBeautyPlasticRecyclerView.setAdapter(mBeautySkinRecyclerAdapter);
            mBeautyPlasticBottomView.setVisibility(mBeautyPlasticBottomView.getVisibility() == View.VISIBLE
                    ? View.GONE : View.VISIBLE);
            mCartoonLayout.setVisibility(View.GONE);
            mFilterBottomView.setVisibility(View.GONE);
            mStickerLayout.setVisibility(View.GONE);
            mFilterConfigView.setVisibility(View.GONE);
            mBeautyConfigView.setVisibility(View.GONE);
        }
    };


    private View.OnClickListener mStickerButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mStickerLayout.setVisibility((mStickerLayout.getVisibility() == View.VISIBLE) ? View.INVISIBLE : View.VISIBLE);
            mFilterBottomView.setVisibility(View.GONE);
            mBeautyPlasticBottomView.setVisibility(View.GONE);
            mCartoonLayout.setVisibility(View.GONE);
            mFilterConfigView.setVisibility(View.GONE);
            mBeautyConfigView.setVisibility(View.GONE);
        }
    };


    /**
     * 微整形默认值  Float 为进度值
     */
    private HashMap<String, Float> mDefaultBeautyPercentParams = new HashMap<String, Float>() {
        {
            put("eyeSize", 0.3f);
            put("chinSize", 0.2f);
            put("noseSize", 0.2f);
            put("mouthWidth", 0.5f);
            put("archEyebrow", 0.5f);
            put("jawSize", 0.5f);
            put("eyeAngle", 0.5f);
            put("eyeDis", 0.5f);
        }
    };

    /**
     * 微整形参数
     */
    private List<String> mBeautyPlastics = new ArrayList() {
        {
            add("reset");
            add("eyeSize");
            add("chinSize");
            add("noseSize");
            add("mouthWidth");
            add("archEyebrow");
            add("jawSize");
            add("eyeAngle");
            add("eyeDis");
        }
    };

    /**
     * 微整形适配器
     */
    private BeautyPlasticRecyclerAdapter mBeautyPlasticRecyclerAdapter;

    /**
     * 美肤适配器
     */
    private BeautyRecyclerAdapter mBeautySkinRecyclerAdapter;

    public static int getLayoutId() {
        return TuSdkContext.getLayoutResId("tusdk_parent_wrap_layout");
    }

    private View mParentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.setRootViewLayoutId(getLayoutId());
        mFilterGroup = getArguments().getStringArray("FilterGroup");
        mCartoonFilterGroup = getArguments().getStringArray("CartoonFilterGroup");
        mHasMonsterFace = getArguments().getBoolean("hasMonsterFace",false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void initCreateView() {

    }

    @Override
    protected void loadView(ViewGroup viewGroup) {
        loadView();
    }

    @Override
    protected void viewDidLoad(ViewGroup viewGroup) {

    }

    private void loadView() {
        initTuSDKViews();
    }

    private void initTuSDKViews() {
        // 微整形布局
        mBeautyPlasticBottomView = this.getViewById("lsq_beauty_bottom_view");
        // 微整形列表
        mBeautyPlasticRecyclerView = this.getViewById("lsq_beauty_recyclerView");
        // 微整形调节栏
        mBeautyConfigView = this.getViewById("lsq_beauty_config_view");
        if (mBeautyConfigView != null)
            mBeautyConfigView.setIgnoredKeys(new String[]{});


        // 滤镜调节栏
        mFilterConfigView = this.getViewById("lsq_filter_config_view");
        // 滤镜列表
        mFilterRecyclerView = this.getViewById("lsq_filter_list_view");
        // 滤镜布局
        mFilterBottomView = this.getViewById("lsq_filter_group_bottom_view_wrap");
        mFilterConfigView.setSeekBarDelegate(mFilterConfigViewSeekBarDelegate);


        // 贴纸布局
        mStickerLayout = this.getViewById("lsq_record_sticker_layout");
        // 贴纸Pager页
        mViewPager = this.getViewById("lsq_viewPager");
        // 贴纸Tab
        mTabPagerIndicator = this.getViewById("lsq_TabIndicator");
        // 贴纸取消按钮
        mStickerCancel = this.getViewById("lsq_cancel_button");


        // 动漫滤镜布局
        mCartoonLayout = this.getViewById("lsq_cartoon_view");
        // 动漫滤镜列表
        mCartoonRecyclerView = this.getViewById("lsq_cartoon_recycler_view");

        // 准备贴纸视图
        prepareStickerViews();

        // 准备滤镜视图
        prepareFilterViews();

        // 准备美肤视图
        prepareBeautySkinViews();

        // 准备微整形视图
        prepareBeautyPlasticViews();

        // 准备漫画视图
        prepareCartoonViews();
    }

    /**
     * 准备动漫滤镜视图
     */
    private void prepareCartoonViews() {
        mCartoonRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mCartoonAdapter = new FilterRecyclerAdapter();
        mCartoonRecyclerView.setAdapter(mCartoonAdapter);
        mCartoonAdapter.isShowImageParameter(false);
        mCartoonAdapter.setFilterList(Arrays.asList(mCartoonFilterGroup));

        mCartoonAdapter.setItemCilckListener(new FilterRecyclerAdapter.ItemClickListener() {

            @Override
            public void onItemClick(int position) {

                // 移除不可叠加的特效
                mFilterEngine.removeMediaEffectsWithType(TuSdkMediaEffectDataTypeFilter);
                mFilterEngine.removeMediaEffectsWithType(TuSdkMediaEffectData.TuSdkMediaEffectDataType.TuSdkMediaEffectDataTypeComic);

                TuSdkMediaComicEffectData effectData = new TuSdkMediaComicEffectData(mCartoonAdapter.getFilterList().get(position));
                mFilterEngine.addMediaEffectData(effectData);

            }
        });

    }

    /********************** 微整形 ***********************

     /**
     * 初始化微整形视图
     */
    @UiThread
    public void prepareBeautyPlasticViews() {
        mBeautyPlasticRecyclerAdapter = new BeautyPlasticRecyclerAdapter(getContext(), mBeautyPlastics);
        mBeautyPlasticRecyclerAdapter.setOnBeautyPlasticItemClickListener(beautyPlasticItemClickListener);

        // 美型Bar
        mBeautyConfigView.setSeekBarDelegate(mBeautyConfigDelegate);
        mBeautyConfigView.setVisibility(View.GONE);
        mBeautyPlasticRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    /**
     * 微整形Item点击事件
     */
    BeautyPlasticRecyclerAdapter.OnBeautyPlasticItemClickListener beautyPlasticItemClickListener = new BeautyPlasticRecyclerAdapter.OnBeautyPlasticItemClickListener() {
        @Override
        public void onItemClick(View v, int position) {
            mBeautyConfigView.setVisibility(View.VISIBLE);
            switchBeautyPlasticConfig(position);
        }

        @Override
        public void onClear() {

            mBeautyConfigView.setVisibility(View.GONE);

            android.app.AlertDialog.Builder adBuilder = new android.app.AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
            adBuilder.setTitle("是否重置？");
            adBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            adBuilder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    clearBeautyPlastic();
                    dialog.dismiss();
                }
            });
            adBuilder.show();
        }
    };

    /**
     * 切换微整形值
     *
     * @param position
     */
    private void switchBeautyPlasticConfig(int position) {
        if (mFilterEngine.mediaEffectsWithType(TuSdkMediaEffectDataTypePlasticFace).size() == 0) {

            // 添加一个默认微整形特效
            TuSdkMediaPlasticFaceEffect plasticFaceEffect = new TuSdkMediaPlasticFaceEffect();
            mFilterEngine.addMediaEffectData(plasticFaceEffect);
            for (SelesParameters.FilterArg arg : plasticFaceEffect.getFilterArgs()) {
                if (arg.equalsKey("eyeSize")) {// 大眼
                    arg.setMaxValueFactor(0.85f);
                }
                if (arg.equalsKey("chinSize")) {// 瘦脸
                    arg.setMaxValueFactor(0.8f);
                }
                if (arg.equalsKey("noseSize")) {// 瘦鼻
                    arg.setMaxValueFactor(0.6f);
                }

            }
            for (String key : mDefaultBeautyPercentParams.keySet()) {
                TLog.e("key -- %s", mDefaultBeautyPercentParams.get(key));
                submitPlasticFaceParamter(key, mDefaultBeautyPercentParams.get(key));
            }
        }
        TuSdkMediaEffectData effectData = mFilterEngine.mediaEffectsWithType(TuSdkMediaEffectDataTypePlasticFace).get(0);
        SelesParameters.FilterArg filterArg = effectData.getFilterArg(mBeautyPlastics.get(position));

        TLog.e("filterArg -- %s", filterArg.getPrecentValue());

        mBeautyConfigView.setFilterArgs(null, Arrays.asList(filterArg));
        mBeautyConfigView.setVisibility(View.VISIBLE);

    }

    /**
     * 重置微整形
     */
    private void clearBeautyPlastic() {

        mFilterEngine.removeMediaEffectsWithType(TuSdkMediaEffectDataTypePlasticFace);
    }

    /**
     * 美肤
     */
    @UiThread
    private void prepareBeautySkinViews() {
        mBeautySkinRecyclerAdapter = new BeautyRecyclerAdapter(getContext());
        mBeautySkinRecyclerAdapter.setOnSkinItemClickListener(mOnBeautyItemClickListener);

        // 美型Bar
        mBeautyConfigView.setSeekBarDelegate(mBeautyConfigDelegate);
        mBeautyConfigView.setVisibility(View.GONE);
        mBeautyPlasticRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    /**
     * 美颜委托对象
     */
    private FilterConfigView.FilterConfigViewSeekBarDelegate mBeautyConfigDelegate = new FilterConfigView.FilterConfigViewSeekBarDelegate() {

        @Override
        public void onSeekbarDataChanged(FilterConfigSeekbar seekbar, SelesParameters.FilterArg arg) {
            submitPlasticFaceParamter(arg.getKey(), seekbar.getSeekbar().getProgress());
        }
    };

    /**
     * 应用整形值
     *
     * @param key
     * @param progress
     */
    private void submitPlasticFaceParamter(String key, float progress) {
        List<TuSdkMediaEffectData> filterEffects = mFilterEngine.mediaEffectsWithType(TuSdkMediaEffectDataTypePlasticFace);

        if (filterEffects.size() == 0) return;

        // 只能添加一个滤镜特效
        TuSdkMediaPlasticFaceEffect filterEffect = (TuSdkMediaPlasticFaceEffect) filterEffects.get(0);
        filterEffect.submitParameter(key, progress);
    }

    BeautyRecyclerAdapter.OnBeautyItemClickListener mOnBeautyItemClickListener = new BeautyRecyclerAdapter.OnBeautyItemClickListener() {
        @Override
        public void onChangeSkin(View v, String key, boolean useSkinNatural) {
            mBeautyConfigView.setVisibility(View.VISIBLE);
            switchConfigSkin(useSkinNatural);

            // 获取key值并显示到调节栏
            TuSdkMediaEffectData effectData = mFilterEngine.mediaEffectsWithType(TuSdkMediaEffectDataTypeSkinFace).get(0);
            SelesParameters.FilterArg filterArg = effectData.getFilterArg(key);
            mBeautyConfigView.setFilterArgs(effectData, Arrays.asList(filterArg));
        }

        @Override
        public void onClear() {
            mBeautyConfigView.setVisibility(View.GONE);
            mFilterEngine.removeMediaEffectsWithType(TuSdkMediaEffectDataTypeSkinFace);
        }
    };

    /**
     * 切换美颜预设按键
     *
     * @param useSkinNatural true 自然(精准)美颜 false 极致美颜
     */
    private void switchConfigSkin(boolean useSkinNatural) {
        TuSdkMediaSkinFaceEffect skinFaceEffect = new TuSdkMediaSkinFaceEffect(useSkinNatural);

        // 美白
        SelesParameters.FilterArg whiteningArgs = skinFaceEffect.getFilterArg("whitening");
        whiteningArgs.setMaxValueFactor(0.6f);//设置最大值限制
        // 磨皮
        SelesParameters.FilterArg smoothingArgs = skinFaceEffect.getFilterArg("smoothing");
        smoothingArgs.setMaxValueFactor(0.7f);//设置最大值限制

        if (mFilterEngine.mediaEffectsWithType(TuSdkMediaEffectDataTypeSkinFace).size() == 0) {

            whiteningArgs.setPrecentValue(0.3f);//设置默认显示

            smoothingArgs.setPrecentValue(0.6f);//设置默认显示
            mFilterEngine.addMediaEffectData(skinFaceEffect);
        } else {
            TuSdkMediaSkinFaceEffect oldSkinFaceEffect = (TuSdkMediaSkinFaceEffect) mFilterEngine.mediaEffectsWithType(TuSdkMediaEffectDataTypeSkinFace).get(0);
            mFilterEngine.addMediaEffectData(skinFaceEffect);

            for (SelesParameters.FilterArg filterArg : oldSkinFaceEffect.getFilterArgs()) {
                SelesParameters.FilterArg arg = skinFaceEffect.getFilterArg(filterArg.getKey());
                arg.setPrecentValue(filterArg.getPrecentValue());
            }

            skinFaceEffect.submitParameters();
        }

    }

    /**
     * 准备滤镜栏视图
     */
    private void prepareFilterViews() {

        // 设置滤镜布局方式
        mFilterRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        // 初始化滤镜适配器
        mFilterAdapter = new FilterRecyclerAdapter();
        // 设置滤镜适配器
        mFilterRecyclerView.setAdapter(mFilterAdapter);
        // 设置滤镜数据
        mFilterAdapter.setFilterList(Arrays.asList(mFilterGroup));
        // 滤镜栏点击事件
        mFilterAdapter.setItemCilckListener(new FilterRecyclerAdapter.ItemClickListener() {

            @Override
            public void onItemClick(int position) {
                changeFilter(position);
            }
        });

    }

    /**
     * 切换滤镜
     */
    public void changeFilter(int postion) {

        if (mFilterEngine == null || mFilterAdapter == null) return;

        mFilterAdapter.setCurrentPosition(postion);

        // 移除不可叠加的特效
        mFilterEngine.removeMediaEffectsWithType(TuSdkMediaEffectDataTypeFilter);
        mFilterEngine.removeMediaEffectsWithType(TuSdkMediaEffectData.TuSdkMediaEffectDataType.TuSdkMediaEffectDataTypeComic);

        TuSdkMediaFilterEffectData effectData = new TuSdkMediaFilterEffectData(mFilterAdapter.getFilterList().get(postion));
        mFilterEngine.addMediaEffectData(effectData);
    }

    // 准备贴纸view
    private void prepareStickerViews() {
        mStickerCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 取消贴纸
                mFilterEngine.removeMediaEffectsWithType(TuSdkMediaEffectData.TuSdkMediaEffectDataType.TuSdKMediaEffectDataTypeSticker);
                TabViewPagerAdapter.mStickerGroupId = 0;
                mViewPager.getAdapter().notifyDataSetChanged();
                mFilterEngine.removeMediaEffectsWithType(TuSdkMediaEffectData.TuSdkMediaEffectDataType.TuSdkMediaEffectDataTypeMonsterFace);
            }
        });

        TabViewPagerAdapter.mStickerGroupId = 0;
        List<Fragment> fragments = new ArrayList<>();
        mStickerGroupCategoriesList = getRawStickGroupList();
        List<String> tabTitles = new ArrayList<>();
        for (StickerGroupCategories categories : mStickerGroupCategoriesList) {
            StickerFragment stickerFragment = StickerFragment.newInstance(categories);
            stickerFragment.setOnStickerItemClickListener(onStickerItemClickListener);
            fragments.add(stickerFragment);
            tabTitles.add(categories.getCategoryName());
        }
        //哈哈镜选项页,仅短视频可使用哈哈镜功能,并需要开启权限
        if (mHasMonsterFace){
            MonsterFaceFragment monsterFaceFragment = MonsterFaceFragment.newInstance();
            monsterFaceFragment.setOnStickerItemClickListener(onMonsterItemClickListener);
            fragments.add(monsterFaceFragment);
            tabTitles.add("哈哈镜");
        }
        mStickerPagerAdapter = new TabViewPagerAdapter(getFragmentManager(),fragments);
        mViewPager.setAdapter(mStickerPagerAdapter);
        mTabPagerIndicator.setViewPager(mViewPager,0);
        mTabPagerIndicator.setDefaultVisibleCounts(tabTitles.size());
        mTabPagerIndicator.setTabItems(tabTitles);
    }

    /**
     * 获取贴纸
     *
     * @return
     */
    private List<StickerGroupCategories> getRawStickGroupList() {
        List<StickerGroupCategories> list = new ArrayList<StickerGroupCategories>();
        try {
            InputStream stream = getResources().openRawResource(TuSdkContext.getRawResId("customstickercategories"));

            if (stream == null) return null;

            byte buffer[] = new byte[stream.available()];
            stream.read(buffer);
            String json = new String(buffer, "UTF-8");

            JSONObject jsonObject = JsonHelper.json(json);
            JSONArray jsonArray = jsonObject.getJSONArray("categories");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                StickerGroupCategories categories = new StickerGroupCategories();
                categories.setCategoryName(item.getString("categoryName"));
                List<StickerGroup> groupList = new ArrayList<StickerGroup>();
                JSONArray jsonArrayGroup = item.getJSONArray("stickers");
                for (int j = 0; j < jsonArrayGroup.length(); j++) {
                    JSONObject itemGroup = jsonArrayGroup.getJSONObject(j);
                    StickerGroup group = new StickerGroup();
                    group.groupId = itemGroup.optLong("id");
                    group.previewName = itemGroup.optString("previewImage");
                    group.name = itemGroup.optString("name");
                    groupList.add(group);
                }
                categories.setStickerGroupList(groupList);
                list.add(categories);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 贴纸点击事件
     */
    private StickerFragment.OnStickerItemClickListener onStickerItemClickListener = new StickerFragment.OnStickerItemClickListener() {
        @Override
        public void onStickerItemClick(StickerGroup itemData) {

            mFilterEngine.removeAllLiveSticker();

            if (itemData != null) {
                TuSdkMediaStickerEffectData stickerEffectData = new TuSdkMediaStickerEffectData(itemData);
                mFilterEngine.addMediaEffectData(stickerEffectData);
            }
        }
    };

    /**
     * 哈哈镜点击事件
     */
    private MonsterFaceFragment.OnMonsterItemClickListener onMonsterItemClickListener = new MonsterFaceFragment.OnMonsterItemClickListener() {
        @Override
        public void onMonsterItemClick(PropsItemMonster itemData) {

            if (itemData!=null){
                mFilterEngine.addMediaEffectData(itemData.effect());
            }
        }
    };

    /**
     * 滤镜参数调节
     */
    private FilterConfigView.FilterConfigViewSeekBarDelegate mFilterConfigViewSeekBarDelegate = new FilterConfigView.FilterConfigViewSeekBarDelegate() {
        @Override
        public void onSeekbarDataChanged(FilterConfigSeekbar seekbar, SelesParameters.FilterArg arg) {

            List<TuSdkMediaEffectData> filterEffects = mFilterEngine.mediaEffectsWithType(TuSdkMediaEffectDataTypeFilter);

            float progress = seekbar.getSeekbar().getProgress();
            if (arg.getKey().equals("whitening")) {
                progress = progress * 0.6f;
            } else if (arg.equalsKey("mixied") || arg.equalsKey("smoothing")) {
                progress = progress * 0.7f;
            }

            // 只能添加一个滤镜特效
            TuSdkMediaFilterEffectData filterEffect = (TuSdkMediaFilterEffectData) filterEffects.get(0);
            filterEffect.submitParameter(arg.getKey(), progress);
        }
    };

    public View.OnClickListener getCartoonButtonClick() {
        return mCartoonButtonClick;
    }

    public View.OnClickListener getBeautyPlasticButtonClick() {
        return mBeautyPlasticButtonClick;
    }

    public View.OnClickListener getFilterButtonClick() {
        return mFilterButtonClick;
    }

    public View.OnClickListener getBeautySkinButtonClick() {
        return mBeautySkinButtonClick;
    }

    public View.OnClickListener getStickerButtonClick() {
        return mStickerButtonClick;
    }

    public TuSDKVideoProcesser.TuSDKVideoProcessorMediaEffectDelegate getMediaEffectDelegate() {
        return mMediaEffectDelegate;
    }

    /**
     * 特效事件委托
     */
    private TuSDKVideoProcesser.TuSDKVideoProcessorMediaEffectDelegate mMediaEffectDelegate = new TuSDKVideoProcesser.TuSDKVideoProcessorMediaEffectDelegate() {

        /**
         * 当前被应用的特效
         * @param mediaEffectData
         */
        @Override
        public void didApplyingMediaEffect(final TuSdkMediaEffectData mediaEffectData) {

            ThreadHelper.post(new Runnable() {

                @Override
                public void run() {

                    switch (mediaEffectData.getMediaEffectType())
                    {

                        case TuSdkMediaEffectDataTypeFilter: {

                            // 切换滤镜时刷新滤镜参数视图
                            mFilterConfigView.setFilterArgs(mediaEffectData, mediaEffectData.getFilterArgs());
                            mFilterConfigView.setVisibility(View.VISIBLE);

                        }
                        break;

                        default:
                            break;
                    }

                }
            });

        }

        @Override
        public void didRemoveMediaEffect(List<TuSdkMediaEffectData> list) {

        }
    };
}
