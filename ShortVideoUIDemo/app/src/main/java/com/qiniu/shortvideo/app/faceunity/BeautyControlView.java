package com.qiniu.shortvideo.app.faceunity;

import android.animation.ValueAnimator;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.faceunity.FURenderer;
import com.faceunity.OnFUControlListener;
import com.faceunity.OnMultiClickListener;
import com.faceunity.entity.CartoonFilter;
import com.faceunity.entity.CartoonFilterEnum;
import com.faceunity.entity.Effect;
import com.faceunity.entity.EffectEnum;
import com.faceunity.entity.FaceMakeup;
import com.faceunity.entity.FaceMakeupEnum;
import com.faceunity.entity.Filter;
import com.faceunity.entity.FilterEnum;
import com.faceunity.entity.MakeupItem;
import com.faceunity.view.BeautyBox;
import com.faceunity.view.BeautyBoxGroup;
import com.faceunity.view.CheckGroup;
import com.faceunity.view.CircleImageView;
import com.faceunity.view.adapter.BaseRecyclerAdapter;
import com.faceunity.view.adapter.EffectRecyclerAdapter;
import com.faceunity.view.adapter.StickerOptionAdapter;
import com.faceunity.view.adapter.StickerOptionItem;
import com.faceunity.view.adapter.VHSpaceItemDecoration;
import com.qiniu.shortvideo.app.R;
import com.faceunity.view.seekbar.DiscreteSeekBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.qiniu.shortvideo.app.faceunity.BeautyParameterModel.STR_FILTER_LEVEL;
import static com.qiniu.shortvideo.app.faceunity.BeautyParameterModel.getValue;
import static com.qiniu.shortvideo.app.faceunity.BeautyParameterModel.isHeightPerformance;
import static com.qiniu.shortvideo.app.faceunity.BeautyParameterModel.isOpen;
import static com.qiniu.shortvideo.app.faceunity.BeautyParameterModel.sFilterLevel;
import static com.qiniu.shortvideo.app.faceunity.BeautyParameterModel.sFilterName;
import static com.qiniu.shortvideo.app.faceunity.BeautyParameterModel.sHairLevel;
import static com.qiniu.shortvideo.app.faceunity.BeautyParameterModel.sHeavyBlur;
import static com.qiniu.shortvideo.app.faceunity.BeautyParameterModel.sSkinDetect;
import static com.qiniu.shortvideo.app.faceunity.BeautyParameterModel.setValue;

// TODO: 2019/3/25 0025 fix static import!

/**
 * Created by tujh on 2017/8/15.
 */
public class BeautyControlView extends FrameLayout {
    private static final String TAG = "BeautyControlView";
    private static final int DEFAULT_ANIMOJI_INDEX = 0;
    private static final int DEFAULT_CARTOON_FILTER_INDEX = 0;

    private Context mContext;

    private OnFUControlListener mOnFUControlListener;
    private static final List<Integer> FACE_SHAPE_ID_LIST = Arrays.asList(R.id.face_shape_0_nvshen, R.id.face_shape_1_wanghong, R.id.face_shape_2_ziran, R.id.face_shape_3_default, R.id.face_shape_4);
    private RecyclerView mRvMakeupItems;

    public void setOnFUControlListener(@NonNull OnFUControlListener onFUControlListener) {
        mOnFUControlListener = onFUControlListener;
    }

    private CheckGroup mBottomCheckGroup;

    private HorizontalScrollView mSkinBeautySelect;
    private ImageView mClearFaceShapeBtn;
    private BeautyBoxGroup mSkinBeautyBoxGroup;
    private BeautyBox mBoxSkinDetect;
    private BeautyBox mBoxHeavyBlur;
    private BeautyBox mBoxBlurLevel;
    private BeautyBox mBoxEyeBright;
    private BeautyBox mBoxToothWhiten;

    private HorizontalScrollView mFaceShapeSelect;
    private BeautyBoxGroup mFaceShapeBeautyBoxGroup;
    private BeautyBox mBoxIntensityChin;
    private BeautyBox mBoxIntensityForehead;
    private BeautyBox mBoxIntensityNose;
    private BeautyBox mBoxIntensityMouth;

    private RecyclerView mFilterRecyclerView;
    private FilterRecyclerAdapter mFilterRecyclerAdapter;
    private List<Filter> mFilters;

    private DiscreteSeekBar mBeautySeekBar;
    private FaceMakeupAdapter mFaceMakeupAdapter;
    private RelativeLayout mFaceShapeLayout;
    private View mFaceShapeCheckedLine;
    private RadioGroup mFaceShapeRadioGroup;
    private RadioButton mFaceShape4Radio;
    private boolean isShown;

    /**
     * animoji
     */
    private RecyclerView mRvAnim;
    /**
     * 动漫滤镜
     */
    private RecyclerView mRvCartoonFilter;
    /**
     * 美发道具
     */
    private RecyclerView mRvHair;
    /**
     * 美发道具特效
     */
    private ArrayList<Effect> mEffects;
    private HairAdapter mHairAdapter;
    private int mHairGradientCount;

    /**
     * AR 面具
     */
    private RecyclerView mRvArMask;

    /**
     * 手势识别
     */
    private RecyclerView mRvGesture;

    /**
     * 基础道具
     */
    private RecyclerView mRvEffectNormal;

    /**
     * 换脸
     */
    private RecyclerView mRvFaceChange;

    /**
     * 哈哈镜
     */
    private RecyclerView mRvHahaMirror;

    /**
     * 表情识别
     */
    private RecyclerView mRvExpression;

    /**
     * 背景分割
     */
    private RecyclerView mRvBackground;

    /**
     * 人像驱动
     */
    private RecyclerView mRvPortraitDrive;

    private RecyclerView mRvStickerOption;
    private StickerOptionAdapter mStickerOptionAdapter;
    private ArrayList<StickerOptionItem> mStickerOptionItems;

    /**
     * 记录当前美肤、美型所选中的 item id
     */
    private int mCheckedId = NO_ID;

    public BeautyControlView(Context context) {
        this(context, null);
    }

    public BeautyControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    // 默认选中第三个粉嫩
    private int mFilterPositionSelect = 2;

    public BeautyControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mFilters = FilterEnum.getFiltersByFilterType();

        LayoutInflater.from(context).inflate(R.layout.layout_beauty_control, this);

        initView();
    }

    private void initView() {
        initStickerOptionsRadio();

        initViewSkinBeauty();
        initViewFaceShape();
        initViewFilterRecycler();
        initMakeupView();
        initAnimojiView();
        initCartoonView();
        initHairView();
        initArMask();
        initGesture();
        initNormalEffect();
        initExpression();
        initHahaMirror();
        initBackground();
        // 换脸和人像驱动已废弃
        // initFaceChange();
        // initPortraitDrive();
        initViewTop();
    }

    public void onResume() {
        updateViewSkinBeauty();
        updateViewFaceShape();
        updateViewFilterRecycler();
    }

    private void initMakeupView() {
        mRvMakeupItems = (RecyclerView) findViewById(R.id.rv_face_makeup);
        mRvMakeupItems.setHasFixedSize(true);
        ((SimpleItemAnimator) mRvMakeupItems.getItemAnimator()).setSupportsChangeAnimations(false);
        mRvMakeupItems.setLayoutManager(new GridLayoutManager(mContext, 5));
        mRvMakeupItems.addItemDecoration(new VHSpaceItemDecoration(0, getResources().getDimensionPixelSize(R.dimen.x15)));
        mFaceMakeupAdapter = new FaceMakeupAdapter(FaceMakeupEnum.getBeautyFaceMakeup());
        OnFaceMakeupClickListener onMpItemClickListener = new OnFaceMakeupClickListener();
        mFaceMakeupAdapter.setOnItemClickListener(onMpItemClickListener);
        mRvMakeupItems.setAdapter(mFaceMakeupAdapter);
        mFaceMakeupAdapter.setItemSelected(0);
    }

    private void initAnimojiView() {
        mRvAnim = findViewById(R.id.rv_animoji);
        mRvAnim.setHasFixedSize(true);
        mRvAnim.setLayoutManager(new GridLayoutManager(mContext, 5));
        ((SimpleItemAnimator) mRvAnim.getItemAnimator()).setSupportsChangeAnimations(false);
        AnimojiAdapter animojiAdapter = new AnimojiAdapter(EffectEnum.getEffectsByEffectType(Effect.EFFECT_TYPE_ANIMOJI));
        animojiAdapter.setOnItemClickListener(new OnAnimojiItemClickListener());
        animojiAdapter.setItemSelected(DEFAULT_ANIMOJI_INDEX);
        mRvAnim.setAdapter(animojiAdapter);
    }

    private void initCartoonView() {
        mRvCartoonFilter = findViewById(R.id.rv_cartoon_filter);
        mRvCartoonFilter.setHasFixedSize(true);
        mRvCartoonFilter.setLayoutManager(new GridLayoutManager(mContext, 5));
        ((SimpleItemAnimator) mRvCartoonFilter.getItemAnimator()).setSupportsChangeAnimations(false);
        CartoonFilterAdapter cartoonFilterAdapter = new CartoonFilterAdapter(CartoonFilterEnum.getAllCartoonFilters());
        cartoonFilterAdapter.setOnItemClickListener(new OnCartoonFilterItemClickListener());
        cartoonFilterAdapter.setItemSelected(DEFAULT_CARTOON_FILTER_INDEX);
        mRvCartoonFilter.setAdapter(cartoonFilterAdapter);
    }

    private void initHairView() {
        mRvHair = findViewById(R.id.rv_hair);
        mRvHair.setHasFixedSize(true);
        mRvHair.setLayoutManager(new GridLayoutManager(mContext, 5));
        mHairAdapter = new HairAdapter();
        mRvHair.setAdapter(mHairAdapter);
        ((SimpleItemAnimator) mRvHair.getItemAnimator()).setSupportsChangeAnimations(false);
        Arrays.fill(sHairLevel, 0.6f);

        ArrayList<Effect> hairEffects = EffectEnum.getEffectsByEffectType(Effect.EFFECT_TYPE_HAIR_NORMAL);
        ArrayList<Effect> hairGradientEffects = EffectEnum.getEffectsByEffectType(Effect.EFFECT_TYPE_HAIR_GRADIENT);
        mHairGradientCount = hairGradientEffects.size() - 1;
        mEffects = new ArrayList<>(mHairGradientCount + hairEffects.size());
        mEffects.addAll(hairGradientEffects);
        hairEffects.remove(0);
        mEffects.addAll(hairEffects);
    }

    private void initArMask() {
        mRvArMask = findViewById(R.id.rv_ar_mask);
        mRvArMask.setLayoutManager(new GridLayoutManager(mContext, 5));
        mRvArMask.setHasFixedSize(true);
        EffectRecyclerAdapter arMaskAdapter = new EffectRecyclerAdapter(mContext, Effect.EFFECT_TYPE_AR);
        arMaskAdapter.setOnEffectSelectListener(mOnEffectSelectListener);
        mRvArMask.setAdapter(arMaskAdapter);
        ((SimpleItemAnimator) mRvArMask.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private void initGesture() {
        mRvGesture = findViewById(R.id.rv_effect_gesture);
        mRvGesture.setLayoutManager(new GridLayoutManager(mContext, 5));
        mRvGesture.setHasFixedSize(true);
        EffectRecyclerAdapter gestureAdapter = new EffectRecyclerAdapter(mContext, Effect.EFFECT_TYPE_GESTURE);
        gestureAdapter.setOnEffectSelectListener(mOnEffectSelectListener);
        gestureAdapter.setOnDescriptionChangeListener(mOnDescriptionChangeListener);
        mRvGesture.setAdapter(gestureAdapter);
        ((SimpleItemAnimator) mRvGesture.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private void initNormalEffect() {
        mRvEffectNormal = findViewById(R.id.rv_effect_normal);
        mRvEffectNormal.setLayoutManager(new GridLayoutManager(mContext, 5));
        mRvEffectNormal.setHasFixedSize(true);
        EffectRecyclerAdapter normalEffectAdapter = new EffectRecyclerAdapter(mContext, Effect.EFFECT_TYPE_NORMAL);
        normalEffectAdapter.setOnEffectSelectListener(mOnEffectSelectListener);
        normalEffectAdapter.setOnDescriptionChangeListener(mOnDescriptionChangeListener);
        mRvEffectNormal.setAdapter(normalEffectAdapter);
        ((SimpleItemAnimator) mRvEffectNormal.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private void initExpression() {
        mRvExpression = findViewById(R.id.rv_effect_expression);
        mRvExpression.setLayoutManager(new GridLayoutManager(mContext, 5));
        mRvExpression.setHasFixedSize(true);
        EffectRecyclerAdapter expressionAdapter = new EffectRecyclerAdapter(mContext, Effect.EFFECT_TYPE_EXPRESSION);
        expressionAdapter.setOnEffectSelectListener(mOnEffectSelectListener);
        expressionAdapter.setOnDescriptionChangeListener(mOnDescriptionChangeListener);
        mRvExpression.setAdapter(expressionAdapter);
        ((SimpleItemAnimator) mRvExpression.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private void initHahaMirror() {
        mRvHahaMirror = findViewById(R.id.rv_effect_haha_mirror);
        mRvHahaMirror.setLayoutManager(new GridLayoutManager(mContext, 5));
        mRvHahaMirror.setHasFixedSize(true);
        EffectRecyclerAdapter hahaMirrorAdapter = new EffectRecyclerAdapter(mContext, Effect.EFFECT_TYPE_FACE_WARP);
        hahaMirrorAdapter.setOnEffectSelectListener(mOnEffectSelectListener);
        hahaMirrorAdapter.setOnDescriptionChangeListener(mOnDescriptionChangeListener);
        mRvHahaMirror.setAdapter(hahaMirrorAdapter);
        ((SimpleItemAnimator) mRvHahaMirror.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private void initFaceChange() {
        mRvFaceChange = findViewById(R.id.rv_effect_face_change);
        mRvFaceChange.setLayoutManager(new GridLayoutManager(mContext, 5));
        mRvFaceChange.setHasFixedSize(true);
        EffectRecyclerAdapter faceChangeAdapter = new EffectRecyclerAdapter(mContext, Effect.EFFECT_TYPE_FACE_CHANGE);
        faceChangeAdapter.setOnEffectSelectListener(mOnEffectSelectListener);
        faceChangeAdapter.setOnDescriptionChangeListener(mOnDescriptionChangeListener);
        mRvFaceChange.setAdapter(faceChangeAdapter);
        ((SimpleItemAnimator) mRvFaceChange.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private void initBackground() {
        mRvBackground = findViewById(R.id.rv_effect_background);
        mRvBackground.setLayoutManager(new GridLayoutManager(mContext, 5));
        mRvBackground.setHasFixedSize(true);
        EffectRecyclerAdapter backgroundAdapter = new EffectRecyclerAdapter(mContext, Effect.EFFECT_TYPE_BACKGROUND);
        backgroundAdapter.setOnEffectSelectListener(mOnEffectSelectListener);
        backgroundAdapter.setOnDescriptionChangeListener(mOnDescriptionChangeListener);
        mRvBackground.setAdapter(backgroundAdapter);
        ((SimpleItemAnimator) mRvBackground.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private void initPortraitDrive() {
        mRvPortraitDrive = findViewById(R.id.rv_effect_portrait_drive);
        mRvPortraitDrive.setLayoutManager(new GridLayoutManager(mContext, 5));
        mRvPortraitDrive.setHasFixedSize(true);
        EffectRecyclerAdapter portraitDriveAdapter = new EffectRecyclerAdapter(mContext, Effect.EFFECT_TYPE_PORTRAIT_DRIVE);
        portraitDriveAdapter.setOnEffectSelectListener(mOnEffectSelectListener);
        portraitDriveAdapter.setOnDescriptionChangeListener(mOnDescriptionChangeListener);
        mRvPortraitDrive.setAdapter(portraitDriveAdapter);
        ((SimpleItemAnimator) mRvPortraitDrive.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    @Override
    public boolean isShown() {
        return isShown;
    }

    private void initStickerOptionsRadio() {
        mStickerOptionItems = new ArrayList<>();
        mStickerOptionItems.add(new StickerOptionItem(mContext.getResources().getString(R.string.beauty_radio_skin_beauty)));
        mStickerOptionItems.add(new StickerOptionItem(mContext.getResources().getString(R.string.beauty_radio_face_shape)));
        mStickerOptionItems.add(new StickerOptionItem(mContext.getResources().getString(R.string.beauty_radio_filter)));
        mStickerOptionItems.add(new StickerOptionItem(mContext.getResources().getString(R.string.beauty_radio_beauty)));
        mStickerOptionItems.add(new StickerOptionItem(mContext.getResources().getString(R.string.home_function_name_normal)));
        mStickerOptionItems.add(new StickerOptionItem(mContext.getResources().getString(R.string.home_function_name_ar)));
        mStickerOptionItems.add(new StickerOptionItem(mContext.getResources().getString(R.string.home_function_name_gesture)));
        mStickerOptionItems.add(new StickerOptionItem(mContext.getResources().getString(R.string.home_function_name_animoji)));
        mStickerOptionItems.add(new StickerOptionItem(mContext.getResources().getString(R.string.home_function_name_face_warp)));
        mStickerOptionItems.add(new StickerOptionItem(mContext.getResources().getString(R.string.home_function_name_expression)));
        mStickerOptionItems.add(new StickerOptionItem(mContext.getResources().getString(R.string.cartoon_filter)));
        mStickerOptionItems.add(new StickerOptionItem(mContext.getResources().getString(R.string.home_function_name_background)));
        mStickerOptionItems.add(new StickerOptionItem(mContext.getResources().getString(R.string.home_function_name_hair)));
//        mStickerOptionItems.add(new StickerOptionItem(mContext.getResources().getString(R.string.home_function_name_face_change)));
//        mStickerOptionItems.add(new StickerOptionItem(mContext.getResources().getString(R.string.home_function_name_portrait_drive)));

        mStickerOptionAdapter = new StickerOptionAdapter(mContext, mStickerOptionItems);
        mStickerOptionAdapter.setOnStickerOptionsClickedListener(new StickerOptionAdapter.OnStickerOptionsClickedListener() {
            @Override
            public void onStickerOptionClicked(StickerOptionItem item) {
                clickStickerOptionsRadio(item.getStickerType());
            }
        });
        mRvStickerOption = findViewById(R.id.sticker_option_rv);
        mRvStickerOption.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mRvStickerOption.setAdapter(mStickerOptionAdapter);

        mClearFaceShapeBtn = findViewById(R.id.clear_face_shape_btn);
        mClearFaceShapeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSkinBeautySelect.getVisibility() == VISIBLE) {
                    resetSkinBeauty();
                    seekToSeekBar(mCheckedId);
                    if (mOnFUControlListener != null) {
                        mOnFUControlListener.onSkinBeautyReset();
                    }
                } else if (mFaceShapeSelect.getVisibility() == VISIBLE) {
                    resetFaceShape();
                    seekToSeekBar(mCheckedId);
                    if (mOnFUControlListener != null) {
                        mOnFUControlListener.onFaceShapeReset();
                    }
                }
            }
        });
    }

    private void updateViewSkinBeauty() {
        mBoxSkinDetect.setVisibility(isHeightPerformance ? GONE : VISIBLE);
        mBoxHeavyBlur.setVisibility(isHeightPerformance ? GONE : VISIBLE);
        mBoxBlurLevel.setVisibility(isHeightPerformance ? VISIBLE : GONE);
        mBoxEyeBright.setVisibility(isHeightPerformance ? GONE : VISIBLE);
        mBoxToothWhiten.setVisibility(isHeightPerformance ? GONE : VISIBLE);
        if (mOnFUControlListener != null) {
            mOnFUControlListener.onHeavyBlurSelected(isHeightPerformance ? 1 : sHeavyBlur);
        }
        onChangeFaceBeautyLevel(R.id.beauty_box_skin_detect);
        if (isHeightPerformance) {
            onChangeFaceBeautyLevel(R.id.beauty_box_heavy_blur);
            onChangeFaceBeautyLevel(R.id.beauty_box_blur_level);
        } else {
            if (sHeavyBlur == 0) {
                onChangeFaceBeautyLevel(R.id.beauty_box_heavy_blur);
            } else {
                onChangeFaceBeautyLevel(R.id.beauty_box_blur_level);
            }
        }
        onChangeFaceBeautyLevel(R.id.beauty_box_color_level);
        onChangeFaceBeautyLevel(R.id.beauty_box_red_level);
        onChangeFaceBeautyLevel(R.id.beauty_box_eye_bright);
        onChangeFaceBeautyLevel(R.id.beauty_box_tooth_whiten);
    }

    private void initViewSkinBeauty() {
        mSkinBeautySelect = (HorizontalScrollView) findViewById(R.id.skin_beauty_select_block);

        mSkinBeautyBoxGroup = (BeautyBoxGroup) findViewById(R.id.beauty_group_skin_beauty);
        mSkinBeautyBoxGroup.setOnCheckedChangeListener(new BeautyBoxGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(BeautyBoxGroup group, int checkedId) {
                mCheckedId = checkedId;
                mFaceShapeLayout.setVisibility(GONE);
                mBeautySeekBar.setVisibility(INVISIBLE);
                if (checkedId != R.id.beauty_box_skin_detect) {
                    seekToSeekBar(checkedId);
                    onChangeFaceBeautyLevel(checkedId);
                }
            }
        });
        mBoxSkinDetect = (BeautyBox) findViewById(R.id.beauty_box_skin_detect);
        mBoxSkinDetect.setOnOpenChangeListener(new BeautyBox.OnOpenChangeListener() {
            @Override
            public void onOpenChanged(BeautyBox beautyBox, boolean isOpen) {
                sSkinDetect = isOpen ? 1 : 0;
                setDescriptionShowStr(sSkinDetect == 0 ? R.string.beauty_box_skin_detect_close : R.string.beauty_box_skin_detect_open);
                onChangeFaceBeautyLevel(R.id.beauty_box_skin_detect);
            }
        });
        mBoxHeavyBlur = (BeautyBox) findViewById(R.id.beauty_box_heavy_blur);
        mBoxHeavyBlur.setOnDoubleChangeListener(new BeautyBox.OnDoubleChangeListener() {
            @Override
            public void onDoubleChanged(BeautyBox beautyBox, boolean isDouble) {
                sHeavyBlur = isDouble ? 1 : 0;
                setDescriptionShowStr(sHeavyBlur == 0 ? R.string.beauty_box_heavy_blur_normal_text : R.string.beauty_box_heavy_blur_double_text);
                seekToSeekBar(R.id.beauty_box_heavy_blur);
                onChangeFaceBeautyLevel(R.id.beauty_box_heavy_blur);
                if (mOnFUControlListener != null) {
                    mOnFUControlListener.onHeavyBlurSelected(sHeavyBlur);
                }
            }
        });
        mBoxBlurLevel = (BeautyBox) findViewById(R.id.beauty_box_blur_level);
        BeautyBox boxColorLevel = (BeautyBox) findViewById(R.id.beauty_box_color_level);
        BeautyBox boxRedLevel = (BeautyBox) findViewById(R.id.beauty_box_red_level);
        mBoxEyeBright = (BeautyBox) findViewById(R.id.beauty_box_eye_bright);
        mBoxToothWhiten = (BeautyBox) findViewById(R.id.beauty_box_tooth_whiten);
    }

    private void updateViewFaceShape() {
        float faceShape = getValue(R.id.beauty_box_face_shape);

        mBoxIntensityChin.setVisibility(faceShape != 4 ? GONE : VISIBLE);
        mBoxIntensityForehead.setVisibility(faceShape != 4 ? GONE : VISIBLE);
        mBoxIntensityNose.setVisibility(faceShape != 4 ? GONE : VISIBLE);
        mBoxIntensityMouth.setVisibility(faceShape != 4 ? GONE : VISIBLE);
        mFaceShape4Radio.setVisibility(isHeightPerformance ? GONE : VISIBLE);
        if (isHeightPerformance && mFaceShapeRadioGroup.getCheckedRadioButtonId() == R.id.face_shape_4) {
            mFaceShapeRadioGroup.check(R.id.face_shape_3_default);
        }
        onChangeFaceBeautyLevel(R.id.beauty_box_face_shape);
        onChangeFaceBeautyLevel(R.id.beauty_box_eye_enlarge);
        onChangeFaceBeautyLevel(R.id.beauty_box_cheek_thinning);
        onChangeFaceBeautyLevel(R.id.beauty_box_intensity_chin);
        onChangeFaceBeautyLevel(R.id.beauty_box_intensity_forehead);
        onChangeFaceBeautyLevel(R.id.beauty_box_intensity_nose);
        onChangeFaceBeautyLevel(R.id.beauty_box_intensity_mouth);
    }

    private void initViewFilterRecycler() {
        mFilterRecyclerView = (RecyclerView) findViewById(R.id.filter_recycle_view);
        mFilterRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 5));
        mFilterRecyclerView.setAdapter(mFilterRecyclerAdapter = new FilterRecyclerAdapter());
        ((SimpleItemAnimator) mFilterRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private void updateViewFilterRecycler() {
        mFilterRecyclerAdapter.setFilter(sFilterName);
        mOnFUControlListener.onFilterNameSelected(sFilterName);
        float filterLevel = getFilterLevel(sFilterName.filterName());
        mOnFUControlListener.onFilterLevelSelected(filterLevel);
    }

    private void initViewFaceShape() {
        mFaceShapeSelect = (HorizontalScrollView) findViewById(R.id.face_shape_select_block);

        mFaceShapeBeautyBoxGroup = (BeautyBoxGroup) findViewById(R.id.beauty_group_face_shape);
        mFaceShapeBeautyBoxGroup.setOnCheckedChangeListener(new BeautyBoxGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(BeautyBoxGroup group, int checkedId) {
                mCheckedId = checkedId;
                mFaceShapeLayout.setVisibility(GONE);
                mBeautySeekBar.setVisibility(INVISIBLE);
                if (checkedId == R.id.beauty_box_face_shape) {
                    mFaceShapeLayout.setVisibility(VISIBLE);
                    float faceShape = getValue(R.id.beauty_box_face_shape);
                    updateFaceShapeCheckedLine(FACE_SHAPE_ID_LIST.get((int) faceShape));
                    mFaceShapeRadioGroup.check(FACE_SHAPE_ID_LIST.get((int) faceShape));
                } else {
                    seekToSeekBar(checkedId);
                }
                onChangeFaceBeautyLevel(checkedId);
            }
        });
        BeautyBox boxFaceShape = (BeautyBox) findViewById(R.id.beauty_box_face_shape);
        BeautyBox boxEyeEnlarge = (BeautyBox) findViewById(R.id.beauty_box_eye_enlarge);
        BeautyBox boxCheekThinning = (BeautyBox) findViewById(R.id.beauty_box_cheek_thinning);
        mBoxIntensityChin = (BeautyBox) findViewById(R.id.beauty_box_intensity_chin);
        mBoxIntensityForehead = (BeautyBox) findViewById(R.id.beauty_box_intensity_forehead);
        mBoxIntensityNose = (BeautyBox) findViewById(R.id.beauty_box_intensity_nose);
        mBoxIntensityMouth = (BeautyBox) findViewById(R.id.beauty_box_intensity_mouth);
    }

    private void updateFaceShapeCheckedLine(final int checkedId) {
        mFaceShapeCheckedLine.post(new Runnable() {
            @Override
            public void run() {
                RadioButton radioButton = (RadioButton) findViewById(checkedId);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFaceShapeCheckedLine.getLayoutParams();
                int textWidth = radioButton == null || radioButton.getVisibility() == GONE ? 0 : (int) radioButton.getPaint().measureText(radioButton.getText().toString());
                params.width = textWidth;
                params.leftMargin = radioButton == null || radioButton.getVisibility() == GONE ? 0 : (radioButton.getLeft() + (radioButton.getWidth() - textWidth) / 2);
                mFaceShapeCheckedLine.setLayoutParams(params);
            }
        });
    }

    private void onChangeFaceBeautyLevel(int viewId) {
        if (viewId == View.NO_ID) {
            return;
        }
        ((BeautyBox) findViewById(viewId)).setOpen(isOpen(viewId));
        if (mOnFUControlListener == null) {
            return;
        }
        switch (viewId) {
            case R.id.beauty_box_skin_detect:
                mOnFUControlListener.onSkinDetectSelected(getValue(viewId));
                break;
            case R.id.beauty_box_heavy_blur:
                mOnFUControlListener.onBlurLevelSelected(getValue(viewId));
                break;
            case R.id.beauty_box_blur_level:
                mOnFUControlListener.onBlurLevelSelected(getValue(viewId));
                break;
            case R.id.beauty_box_color_level:
                mOnFUControlListener.onColorLevelSelected(getValue(viewId));
                break;
            case R.id.beauty_box_red_level:
                mOnFUControlListener.onRedLevelSelected(getValue(viewId));
                break;
            case R.id.beauty_box_eye_bright:
                mOnFUControlListener.onEyeBrightSelected(getValue(viewId));
                break;
            case R.id.beauty_box_tooth_whiten:
                mOnFUControlListener.onToothWhitenSelected(getValue(viewId));
                break;
            case R.id.beauty_box_face_shape:
                mOnFUControlListener.onFaceShapeSelected(getValue(viewId));
                break;
            case R.id.beauty_box_eye_enlarge:
                mOnFUControlListener.onEyeEnlargeSelected(getValue(viewId));
                break;
            case R.id.beauty_box_cheek_thinning:
                mOnFUControlListener.onCheekThinningSelected(getValue(viewId));
                break;
            case R.id.beauty_box_intensity_chin:
                mOnFUControlListener.onIntensityChinSelected(getValue(viewId));
                break;
            case R.id.beauty_box_intensity_forehead:
                mOnFUControlListener.onIntensityForeheadSelected(getValue(viewId));
                break;
            case R.id.beauty_box_intensity_nose:
                mOnFUControlListener.onIntensityNoseSelected(getValue(viewId));
                break;
            case R.id.beauty_box_intensity_mouth:
                mOnFUControlListener.onIntensityMouthSelected(getValue(viewId));
                break;
            default:
        }
    }

    private void initViewTop() {
        mFaceShapeLayout = (RelativeLayout) findViewById(R.id.face_shape_radio_layout);
        mFaceShapeCheckedLine = findViewById(R.id.beauty_face_shape_checked_line);
        mFaceShapeRadioGroup = (RadioGroup) findViewById(R.id.face_shape_radio_group);
        mFaceShapeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mCheckedId = checkedId;
                if (checkedId == R.id.face_shape_4) {
                    mBoxIntensityChin.setVisibility(VISIBLE);
                    mBoxIntensityForehead.setVisibility(VISIBLE);
                    mBoxIntensityNose.setVisibility(VISIBLE);
                    mBoxIntensityMouth.setVisibility(VISIBLE);
                } else {
                    mBoxIntensityChin.setVisibility(GONE);
                    mBoxIntensityForehead.setVisibility(GONE);
                    mBoxIntensityNose.setVisibility(GONE);
                    mBoxIntensityMouth.setVisibility(GONE);
                }
                float value = FACE_SHAPE_ID_LIST.indexOf(checkedId);
                setValue(R.id.beauty_box_face_shape, value);
                onChangeFaceBeautyLevel(R.id.beauty_box_face_shape);
                onChangeFaceBeautyLevel(R.id.beauty_box_eye_enlarge);
                onChangeFaceBeautyLevel(R.id.beauty_box_cheek_thinning);
                updateFaceShapeCheckedLine(checkedId);
            }
        });
        mFaceShape4Radio = (RadioButton) findViewById(R.id.face_shape_4);

        mBeautySeekBar = (DiscreteSeekBar) findViewById(R.id.beauty_seek_bar);
        mBeautySeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnSimpleProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar SeekBar, int value, boolean fromUser) {
                if (!fromUser) {
                    return;
                }

                float valueF = 1.0f * (value - SeekBar.getMin()) / 100;
                int currentPos = mStickerOptionAdapter.getSelectedPosition();
                if (currentPos == 0) {
                    setValue(mSkinBeautyBoxGroup.getCheckedBeautyBoxId(), valueF);
                    onChangeFaceBeautyLevel(mSkinBeautyBoxGroup.getCheckedBeautyBoxId());
                } else if (currentPos == 1) {
                    setValue(mFaceShapeBeautyBoxGroup.getCheckedBeautyBoxId(), valueF);
                    onChangeFaceBeautyLevel(mFaceShapeBeautyBoxGroup.getCheckedBeautyBoxId());
                } else if (currentPos == 2) {
                    mFilterRecyclerAdapter.setFilterLevels(valueF);
                } else if (currentPos == 3) {
                    // 整体妆容调节
                    float level = 1.0f * value / 100;
                    FaceMakeup faceMakeup = mFaceMakeupAdapter.getSelectedItems().valueAt(0);
                    String name = getResources().getString(faceMakeup.getNameId());
                    BeautyParameterModel.sBatchMakeupLevel.put(name, level);
                    List<MakeupItem> makeupItems = faceMakeup.getMakeupItems();
                    /* 数学公式，哈哈
                     * 0.4        0.7
                     * strength  level
                     * --> strength = 0.4 * level / 0.7
                     *   if level = 1.0, then strength = 0.57
                     *   if level = 0.2, then strength = 0.11
                     *   so, float strength = item.defaultLevel * level / DEFAULT_BATCH_MAKEUP_LEVEL
                     * */
                    if (makeupItems != null) {
                        for (MakeupItem makeupItem : makeupItems) {
                            float lev = makeupItem.getDefaultLevel() * level / FaceMakeupEnum.MAKEUP_OVERALL_LEVEL.get(faceMakeup.getNameId());
                            makeupItem.setLevel(lev);
                        }
                    }
                    mOnFUControlListener.onLightMakeupOverallLevelChanged(level);
                    mOnFUControlListener.onFilterLevelSelected(level);
                } else if (currentPos == 12) {
                    if (mOnFUControlListener == null) {
                        return;
                    }
                    if (mHairAdapter.mPositionSelect <= mHairGradientCount) {
                        int hairIndex = mHairAdapter.mPositionSelect - 1;
                        mOnFUControlListener.onHairLevelSelected(FURenderer.HAIR_GRADIENT, hairIndex,
                                hairIndex < 0 ? 0 : (sHairLevel[mHairAdapter.mPositionSelect - 1] = 1.0f * value / 100));
                    } else {
                        int hairIndex = mHairAdapter.mPositionSelect - mHairGradientCount - 1;
                        mOnFUControlListener.onHairLevelSelected(FURenderer.HAIR_NORMAL, hairIndex,
                                sHairLevel[mHairAdapter.mPositionSelect - 1] = 1.0f * value / 100);
                    }
                }
            }
        });
    }

    private void clickStickerOptionsRadio(String stickerType) {
        mSkinBeautySelect.setVisibility(GONE);
        mFaceShapeSelect.setVisibility(GONE);
        mFilterRecyclerView.setVisibility(GONE);
        mRvMakeupItems.setVisibility(GONE);
        mRvAnim.setVisibility(GONE);
        mRvCartoonFilter.setVisibility(GONE);
        mRvHair.setVisibility(GONE);
        mRvArMask.setVisibility(GONE);
        mRvGesture.setVisibility(GONE);
        mRvEffectNormal.setVisibility(GONE);
        mRvExpression.setVisibility(GONE);
        mRvHahaMirror.setVisibility(GONE);
        mRvBackground.setVisibility(GONE);
//        mRvFaceChange.setVisibility(GONE);
//        mRvPortraitDrive.setVisibility(GONE);

        mFaceShapeLayout.setVisibility(GONE);
        mBeautySeekBar.setVisibility(INVISIBLE);
        if (stickerType.equals(mContext.getResources().getString(R.string.beauty_radio_skin_beauty))) {
            mSkinBeautySelect.setVisibility(VISIBLE);
            int id = mSkinBeautyBoxGroup.getCheckedBeautyBoxId();
            if (id != R.id.beauty_box_skin_detect) {
                seekToSeekBar(id);
            }
        } else if (stickerType.equals(mContext.getResources().getString(R.string.beauty_radio_face_shape))) {
            mFaceShapeSelect.setVisibility(VISIBLE);
            int id = mFaceShapeBeautyBoxGroup.getCheckedBeautyBoxId();
            if (id == R.id.beauty_box_face_shape) {
                mFaceShapeLayout.setVisibility(VISIBLE);
            } else {
                seekToSeekBar(id);
            }
        } else if (stickerType.equals(mContext.getResources().getString(R.string.beauty_radio_filter))) {
            mFilterRecyclerView.setVisibility(VISIBLE);
            mFilterRecyclerAdapter.setFilterProgress();
        } else if (stickerType.equals(mContext.getResources().getString(R.string.beauty_radio_beauty))) {
            mRvMakeupItems.setVisibility(VISIBLE);
            mBeautySeekBar.setVisibility(INVISIBLE);
            FaceMakeup faceMakeup = mFaceMakeupAdapter.getSelectedItems().valueAt(0);
            if (faceMakeup != null) {
                String name = getResources().getString(faceMakeup.getNameId());
                Float level = BeautyParameterModel.sBatchMakeupLevel.get(name);
                if (level == null) {
                    level = FaceMakeupEnum.MAKEUP_OVERALL_LEVEL.get(faceMakeup.getNameId());
                    BeautyParameterModel.sBatchMakeupLevel.put(name, level);
                }
                if (level != null) {
                    seekToSeekBar(level);
                }
            }
        } else if (stickerType.equals(mContext.getResources().getString(R.string.home_function_name_animoji))) {
            mRvAnim.setVisibility(VISIBLE);
        } else if (stickerType.equals(mContext.getResources().getString(R.string.cartoon_filter))) {
            mRvCartoonFilter.setVisibility(VISIBLE);
        } else if (stickerType.equals(mContext.getResources().getString(R.string.home_function_name_hair))) {
            mRvHair.setVisibility(VISIBLE);
        } else if (stickerType.equals(mContext.getResources().getString(R.string.home_function_name_ar))) {
            mRvArMask.setVisibility(VISIBLE);
        } else if (stickerType.equals(mContext.getResources().getString(R.string.home_function_name_gesture))) {
            mRvGesture.setVisibility(VISIBLE);
        } else if (stickerType.equals(mContext.getResources().getString(R.string.home_function_name_normal))) {
            mRvEffectNormal.setVisibility(VISIBLE);
        } else if (stickerType.equals(mContext.getResources().getString(R.string.home_function_name_expression))) {
            mRvExpression.setVisibility(VISIBLE);
        } else if (stickerType.equals(mContext.getResources().getString(R.string.home_function_name_face_warp))) {
            mRvHahaMirror.setVisibility(VISIBLE);
        } else if (stickerType.equals(mContext.getResources().getString(R.string.home_function_name_background))) {
            mRvBackground.setVisibility(VISIBLE);
        } else if (stickerType.equals(mContext.getResources().getString(R.string.home_function_name_face_change))) {
            mRvFaceChange.setVisibility(VISIBLE);
        } else if (stickerType.equals(mContext.getResources().getString(R.string.home_function_name_portrait_drive))) {
            mRvPortraitDrive.setVisibility(VISIBLE);
        }
    }

    private void seekToSeekBar(float value) {
        seekToSeekBar(value, 0, 100);
    }

    private void seekToSeekBar(float value, int min, int max) {
        mBeautySeekBar.setVisibility(VISIBLE);
        mBeautySeekBar.setMin(min);
        mBeautySeekBar.setMax(max);
        mBeautySeekBar.setProgress((int) (value * (max - min) + min));
    }

    private void seekToSeekBar(int checkedId) {
        if (checkedId == View.NO_ID) {
            return;
        }

        float value = getValue(checkedId);
        int min = 0;
        int max = 100;
        if (checkedId == R.id.beauty_box_intensity_chin || checkedId == R.id.beauty_box_intensity_forehead || checkedId == R.id.beauty_box_intensity_mouth) {
            min = -50;
            max = 50;
        }
        seekToSeekBar(value, min, max);
    }

    private void resetSkinBeauty() {
        BeautyParameterModel.resetSkinBeauty();
    }

    private void resetFaceShape() {
        BeautyParameterModel.resetFaceShape();
    }

    private void changeBottomLayoutAnimator(final int startHeight, final int endHeight) {
        if (mBottomLayoutAnimator != null && mBottomLayoutAnimator.isRunning()) {
            mBottomLayoutAnimator.end();
        }
        mBottomLayoutAnimator = ValueAnimator.ofInt(startHeight, endHeight).setDuration(150);
        mBottomLayoutAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int height = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = getLayoutParams();
                if (params == null) {
                    return;
                }
                params.height = height;
                setLayoutParams(params);
                if (mOnBottomAnimatorChangeListener != null) {
                    float showRate = 1.0f * (height - startHeight) / (endHeight - startHeight);
                    mOnBottomAnimatorChangeListener.onBottomAnimatorChangeListener(startHeight > endHeight ? 1 - showRate : showRate);
                }
            }
        });
        mBottomLayoutAnimator.start();
    }

    public interface OnBottomAnimatorChangeListener {
        void onBottomAnimatorChangeListener(float showRate);
    }

    public void setOnBottomAnimatorChangeListener(OnBottomAnimatorChangeListener onBottomAnimatorChangeListener) {
        mOnBottomAnimatorChangeListener = onBottomAnimatorChangeListener;
    }

    private OnBottomAnimatorChangeListener mOnBottomAnimatorChangeListener;

    private ValueAnimator mBottomLayoutAnimator;

    private void setDescriptionShowStr(int str) {
        if (mOnDescriptionShowListener != null) {
            mOnDescriptionShowListener.onDescriptionShowListener(str);
        }
    }

    public void hideBottomLayoutAnimator() {
        mBottomCheckGroup.check(View.NO_ID);
    }

    public interface OnDescriptionShowListener {
        void onDescriptionShowListener(int str);
    }

    public void setOnDescriptionShowListener(OnDescriptionShowListener onDescriptionShowListener) {
        mOnDescriptionShowListener = onDescriptionShowListener;
    }

    private OnDescriptionShowListener mOnDescriptionShowListener;

    public void setFilterLevel(String filterName, float faceBeautyFilterLevel) {
        sFilterLevel.put(STR_FILTER_LEVEL + filterName, faceBeautyFilterLevel);
        if (mOnFUControlListener != null) {
            mOnFUControlListener.onFilterLevelSelected(faceBeautyFilterLevel);
        }
    }

    public void setHeightPerformance(boolean isHP) {
        isHeightPerformance = isHP;
        updateViewSkinBeauty();
        updateViewFaceShape();
        mSkinBeautyBoxGroup.check(View.NO_ID);
        mFaceShapeBeautyBoxGroup.check(View.NO_ID);
    }

    public float getFilterLevel(String filterName) {
        String key = STR_FILTER_LEVEL + filterName;
        Float level = sFilterLevel.get(key);
        if (level == null) {
            level = Filter.DEFAULT_FILTER_LEVEL;
            sFilterLevel.put(key, level);
        }
        setFilterLevel(filterName, level);
        return level;
    }

    class FilterRecyclerAdapter extends RecyclerView.Adapter<FilterRecyclerAdapter.HomeRecyclerHolder> {

        @Override
        public FilterRecyclerAdapter.HomeRecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new FilterRecyclerAdapter.HomeRecyclerHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_beauty_control_recycler, parent, false));
        }

        @Override
        public void onBindViewHolder(FilterRecyclerAdapter.HomeRecyclerHolder holder, final int position) {
            final List<Filter> filters = mFilters;
            holder.filterImg.setImageResource(filters.get(position).resId());
            holder.filterName.setText(filters.get(position).description());
            if (mFilterPositionSelect == position) {
                holder.filterImg.setBackgroundResource(R.drawable.control_filter_select);
            } else {
                holder.filterImg.setBackgroundResource(0);
            }
            holder.itemView.setOnClickListener(new OnMultiClickListener() {
                @Override
                protected void onMultiClick(View v) {
                    mFilterPositionSelect = position;
                    mBeautySeekBar.setVisibility(position == 0 ? INVISIBLE : VISIBLE);
                    setFilterProgress();
                    notifyDataSetChanged();
                    if (mOnFUControlListener != null) {
                        sFilterName = filters.get(mFilterPositionSelect);
                        mOnFUControlListener.onFilterNameSelected(sFilterName);
                        if (mOnDescriptionShowListener != null) {
                            mOnDescriptionShowListener.onDescriptionShowListener(sFilterName.description());
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mFilters.size();
        }

        public void setFilterLevels(float filterLevels) {
            if (mFilterPositionSelect >= 0) {
                setFilterLevel(mFilters.get(mFilterPositionSelect).filterName(), filterLevels);
            }
        }

        public void setFilter(Filter filter) {
            mFilterPositionSelect = mFilters.indexOf(filter);
        }

        public void setFilter(int index) {
            mFilterPositionSelect = index;
            if (mOnFUControlListener != null) {
                sFilterName = mFilters.get(mFilterPositionSelect);
                mOnFUControlListener.onFilterNameSelected(sFilterName);
                if (mOnDescriptionShowListener != null) {
                    mOnDescriptionShowListener.onDescriptionShowListener(sFilterName.description());
                }
            }
            notifyDataSetChanged();
        }

        public int indexOf(Filter filter) {
            for (int i = 0; i < mFilters.size(); i++) {
                if (filter.filterName().equals(mFilters.get(i).filterName())) {
                    return i;
                }
            }
            return -1;
        }

        public void setFilterProgress() {
            if (mFilterPositionSelect >= 0) {
                seekToSeekBar(getFilterLevel(mFilters.get(mFilterPositionSelect).filterName()));
            }
        }

        class HomeRecyclerHolder extends RecyclerView.ViewHolder {

            ImageView filterImg;
            TextView filterName;

            public HomeRecyclerHolder(View itemView) {
                super(itemView);
                filterImg = (ImageView) itemView.findViewById(R.id.control_recycler_img);
                filterName = (TextView) itemView.findViewById(R.id.control_recycler_text);
            }
        }
    }

    // ----------- 新添加的美妆组合

    // 美妆列表点击事件
    private class OnFaceMakeupClickListener implements BaseRecyclerAdapter.OnItemClickListener<FaceMakeup> {

        @Override
        public void onItemClick(BaseRecyclerAdapter<FaceMakeup> adapter, View view, int position) {
            FaceMakeup faceMakeup = adapter.getItem(position);
            if (position == 0) {
                // 卸妆
                mBeautySeekBar.setVisibility(View.INVISIBLE);
                Filter origin = mFilters.get(0);
                mOnFUControlListener.onFilterNameSelected(origin);
                setFilterLevel(origin.filterName(), 0);
                int old = mFilterPositionSelect;
                mFilterPositionSelect = -1;
                mFilterRecyclerAdapter.notifyItemChanged(old);
            } else {
                // 各个妆容
                mBeautySeekBar.setVisibility(View.VISIBLE);
                String name = getResources().getString(faceMakeup.getNameId());
                Float level = BeautyParameterModel.sBatchMakeupLevel.get(name);
                boolean used = true;
                if (level == null) {
                    used = false;
                    level = FaceMakeupEnum.MAKEUP_OVERALL_LEVEL.get(faceMakeup.getNameId());
                    BeautyParameterModel.sBatchMakeupLevel.put(name, level);
                }
                seekToSeekBar(level);
                mOnFUControlListener.onLightMakeupOverallLevelChanged(level);

                Pair<Filter, Float> filterFloatPair = FaceMakeupEnum.MAKEUP_FILTERS.get(faceMakeup.getNameId());
                if (filterFloatPair != null) {
                    // 滤镜调整到对应的位置，没有就不做
                    Filter filter = filterFloatPair.first;
                    int i = mFilterRecyclerAdapter.indexOf(filter);
                    if (i >= 0) {
                        mFilterPositionSelect = i;
                        mFilterRecyclerAdapter.notifyItemChanged(i);
                        mFilterRecyclerView.scrollToPosition(i);
                    } else {
                        int old = mFilterPositionSelect;
                        mFilterPositionSelect = -1;
                        mFilterRecyclerAdapter.notifyItemChanged(old);
                    }
                    mOnFUControlListener.onFilterNameSelected(filter);
                    Float filterLevel = used ? level : filterFloatPair.second;
                    sFilterName = filter;
                    String filterName = filter.filterName();
                    sFilterLevel.put(STR_FILTER_LEVEL + filterName, filterLevel);
                    setFilterLevel(filterName, filterLevel);
                }
            }
            List<MakeupItem> makeupItems = faceMakeup.getMakeupItems();
            mOnFUControlListener.onLightMakeupBatchSelected(makeupItems);
        }
    }

    // 妆容组合适配器
    private class FaceMakeupAdapter extends BaseRecyclerAdapter<FaceMakeup> {

        FaceMakeupAdapter(@NonNull List<FaceMakeup> data) {
            super(data, R.layout.layout_rv_makeup);
        }

        @Override
        protected void bindViewHolder(BaseViewHolder viewHolder, FaceMakeup item) {
            viewHolder.setText(R.id.tv_makeup, getResources().getString(item.getNameId()))
                    .setImageResource(R.id.iv_makeup, item.getIconId());
        }

        @Override
        protected void handleSelectedState(BaseViewHolder viewHolder, FaceMakeup data, boolean selected) {
            ((TextView) viewHolder.getViewById(R.id.tv_makeup)).setTextColor(selected ?
                    getResources().getColor(R.color.main_color) : getResources().getColor(R.color.colorWhite));
            viewHolder.setBackground(R.id.iv_makeup, selected ? R.drawable.control_filter_select : 0);
        }
    }

    private class OnAnimojiItemClickListener implements BaseRecyclerAdapter.OnItemClickListener<Effect> {
        private int mLastPosition = DEFAULT_ANIMOJI_INDEX;

        @Override
        public void onItemClick(BaseRecyclerAdapter<Effect> adapter, View view, int position) {
            Effect effect = adapter.getItem(position);
            if (mLastPosition != position) {
                if (mOnFUControlListener != null) {
                    mOnFUControlListener.onEffectSelected(effect);
                }
            }
            mLastPosition = position;
        }
    }

    /**
     * Animoji 适配器
     */
    private class AnimojiAdapter extends BaseRecyclerAdapter<Effect> {

        public AnimojiAdapter(@NonNull List<Effect> data) {
            super(data, R.layout.layout_animoji_recycler);
        }

        @Override
        protected void bindViewHolder(BaseViewHolder viewHolder, Effect item) {
            viewHolder.setImageResource(R.id.iv_anim_item, item.resId());
        }

        @Override
        protected void handleSelectedState(BaseViewHolder viewHolder, Effect data, boolean selected) {
            viewHolder.setBackground(R.id.iv_anim_item, selected ? R.drawable.effect_select : 0);
        }
    }

    /**
     * 动漫滤镜适配器
     */
    private class CartoonFilterAdapter extends BaseRecyclerAdapter<CartoonFilter> {

        public CartoonFilterAdapter(@NonNull List<CartoonFilter> data) {
            super(data, R.layout.layout_animoji_recycler);
        }

        @Override
        protected void bindViewHolder(BaseViewHolder viewHolder, CartoonFilter item) {
            viewHolder.setImageResource(R.id.iv_anim_item, item.getImageResId());
        }

        @Override
        protected void handleSelectedState(BaseViewHolder viewHolder, CartoonFilter data, boolean selected) {
            viewHolder.setBackground(R.id.iv_anim_item, selected ? R.drawable.effect_select : 0);
        }
    }

    private class OnCartoonFilterItemClickListener implements BaseRecyclerAdapter.OnItemClickListener<CartoonFilter> {
        private int mLastPosition = DEFAULT_CARTOON_FILTER_INDEX;

        @Override
        public void onItemClick(BaseRecyclerAdapter<CartoonFilter> adapter, View view, int position) {
            CartoonFilter cartoonFilter = adapter.getItem(position);
            if (mLastPosition != position) {
                if (mOnFUControlListener != null) {
                    mOnFUControlListener.onCartoonFilterSelected(cartoonFilter.getStyle());
                }
            }
            mLastPosition = position;
        }
    }

    /**
     * 美发适配器
     */
    private class HairAdapter extends RecyclerView.Adapter<HairAdapter.HomeRecyclerHolder> {
        int mPositionSelect = 0;

        @Override
        @NonNull
        public HairAdapter.HomeRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new HairAdapter.HomeRecyclerHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.layout_effect_recycler, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull HairAdapter.HomeRecyclerHolder holder, int position) {

            holder.effectImg.setImageResource(mEffects.get(position).resId());
            final int pos = position;
            holder.effectImg.setOnClickListener(new OnMultiClickListener() {
                @Override
                protected void onMultiClick(View v) {
                    if (mPositionSelect == pos) {
                        return;
                    }
                    int lastPos = mPositionSelect;
                    mPositionSelect = pos;
                    int hairIndex;
                    float hairLevel;
                    if (mPositionSelect <= 0) {
                        hairIndex = mPositionSelect;
                        hairLevel = 0;
                    } else if (mPositionSelect > mHairGradientCount) {
                        // 正常
                        hairIndex = mPositionSelect - mHairGradientCount - 1;
                        hairLevel = sHairLevel[mPositionSelect - 1];
                    } else {
                        // 渐变
                        hairIndex = mPositionSelect - 1;
                        hairLevel = sHairLevel[mPositionSelect - 1];
                    }
                    if (mOnFUControlListener != null) {
                        if (mPositionSelect == 0) {
                            if (lastPos <= mHairGradientCount) {
                                mOnFUControlListener.onHairSelected(FURenderer.HAIR_GRADIENT, hairIndex, 0.0f);
                            } else {
                                mOnFUControlListener.onHairSelected(FURenderer.HAIR_NORMAL, hairIndex, 0.0f);
                            }
                        } else {
                            if (mPositionSelect <= mHairGradientCount) {
                                mOnFUControlListener.onHairSelected(FURenderer.HAIR_GRADIENT, hairIndex, hairLevel);
                            } else {
                                mOnFUControlListener.onHairSelected(FURenderer.HAIR_NORMAL, hairIndex, hairLevel);
                            }
                        }
                    }
                    if (mPositionSelect == 0) {
                        mBeautySeekBar.setVisibility(View.INVISIBLE);
                    } else {
                        mBeautySeekBar.setVisibility(View.VISIBLE);
                        mBeautySeekBar.setProgress((int) (hairLevel * 100));
                    }

                    notifyDataSetChanged();
                }
            });
            if (mPositionSelect == position) {
                holder.effectImg.setBackgroundResource(R.drawable.effect_select);
            } else {
                holder.effectImg.setBackgroundResource(0);
            }
        }

        @Override
        public int getItemCount() {
            return mEffects.size();
        }

        Effect getSelectEffect() {
            return mEffects.get(mPositionSelect);
        }

        class HomeRecyclerHolder extends RecyclerView.ViewHolder {

            CircleImageView effectImg;

            HomeRecyclerHolder(View itemView) {
                super(itemView);
                effectImg = itemView.findViewById(R.id.effect_recycler_img);
            }
        }
    }

    /**
     * 特效选择监听器
     */
    private EffectRecyclerAdapter.OnEffectSelectListener mOnEffectSelectListener = new EffectRecyclerAdapter.OnEffectSelectListener() {
        @Override
        public void onEffectSelected(Effect effect) {
            if (mOnFUControlListener != null) {
                mOnFUControlListener.onEffectSelected(effect);
            }
        }

        @Override
        public void onMusicFilterTime(long time) {
            if (mOnFUControlListener != null) {
                mOnFUControlListener.onMusicFilterTime(time);
            }
        }
    };

    private EffectRecyclerAdapter.OnDescriptionChangeListener mOnDescriptionChangeListener = new EffectRecyclerAdapter.OnDescriptionChangeListener() {
        @Override
        public void onDescriptionChangeListener(int description) {
            if (mOnDescriptionShowListener != null) {
                mOnDescriptionShowListener.onDescriptionShowListener(description);
            }
        }
    };
}