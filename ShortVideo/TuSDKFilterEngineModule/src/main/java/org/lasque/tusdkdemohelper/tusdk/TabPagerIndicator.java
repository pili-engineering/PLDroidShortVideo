package org.lasque.tusdkdemohelper.tusdk;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.tusdkdemohelper.R;

import org.lasque.tusdk.core.TuSdkContext;

import java.util.List;

/**
 * 贴纸TAB
 * @author xujie
 * @Date 2018/9/20
 */

public class TabPagerIndicator extends LinearLayout {
    public static final int DEFAULT_VISIBLE_COUNTS = 3;

    /** 线画笔 */
    private Paint mLinePaint;
    /** 线高度 */
    private int mLineHeight;
    /** 线宽度 */
    private int mLineWidth;
    /** 线颜色 */
    private int mLineColor;
    /** 文字大小 */
    private int mTextSize;
    /** 普通文字颜色 */
    private int mNormalTextColor;
    /** 高亮文字颜色 */
    private int mHighLightTextColor;

    /** tab item默认数量 */
    private int mDefaultVisibleCounts;
    /** 移动位置 */
    private int mMoveX;

    private ViewPager mViewPager;

    public TabPagerIndicator(Context context) {
        this(context,null);
    }

    public TabPagerIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TabPagerIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttrs(context,attrs);
        init();
    }

    /**
     * 必须先设置ViewPager
     * @param viewPager
     * @param currentPos
     */
    public void setViewPager(ViewPager viewPager, int currentPos){
        mViewPager = viewPager;
        viewPager.addOnPageChangeListener(mListener);
        viewPager.setCurrentItem(currentPos);
    }

    /**
     * 设置显示的tab个数
     * @param counts
     */
    public void setDefaultVisibleCounts(int counts){
        mDefaultVisibleCounts = counts;
        if(counts < DEFAULT_VISIBLE_COUNTS)
            mDefaultVisibleCounts = DEFAULT_VISIBLE_COUNTS;

        mLineWidth = getScreenWidth() / mDefaultVisibleCounts;
    }

    /**
     * 设置Tab数据集
     * @param stickerGroupCategories
     */
    public void setTabItems(List<String> stickerGroupCategories){
        if(stickerGroupCategories == null)
            return;
        this.removeAllViews();
        for (String categories : stickerGroupCategories){
            this.addView(generateTitleView(categories));
        }
        setItemClickListener();
        setHighLightText(mViewPager.getCurrentItem());
    }

    // 初始化
    private void init(){
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setStrokeWidth(mLineHeight);
    }

    /**
     * 获取自定义属性
     * @param context
     * @param attributeSet
     */
    private void getAttrs(Context context, AttributeSet attributeSet){
        TypedArray attributes = context.obtainStyledAttributes(attributeSet, R.styleable.TabPagerIndicator);
        mDefaultVisibleCounts = attributes.getInt(R.styleable.TabPagerIndicator_default_display_counts,DEFAULT_VISIBLE_COUNTS);
        mDefaultVisibleCounts = Math.max(DEFAULT_VISIBLE_COUNTS,mDefaultVisibleCounts);
        mTextSize = attributes.getInt(R.styleable.TabPagerIndicator_text_size,16);
        mLineColor = attributes.getColor(R.styleable.TabPagerIndicator_line_color, Color.WHITE);
        mNormalTextColor = attributes.getColor(R.styleable.TabPagerIndicator_normal_text_color, Color.WHITE);
        mHighLightTextColor = attributes.getColor(R.styleable.TabPagerIndicator_high_light_text_color, Color.WHITE);
        mLineHeight = attributes.getInt(R.styleable.TabPagerIndicator_line_size,10);
        mLineWidth = (int) attributes.getDimension(R.styleable.TabPagerIndicator_line_width, TuSdkContext.px2dip(getScreenWidth() / mDefaultVisibleCounts));
        mLineWidth = TuSdkContext.dip2px(mLineWidth);
        // 直线宽度
        attributes.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(getMeasuredWidth() > 0){ onFinishInflate(); }
    }

    /**
     * 水平滚动
     * @param position
     * @param offset
     */
    private void scroll(int position,float offset){
        int tabWidth = getWidth() / mDefaultVisibleCounts;
        // tab移动距离
        mMoveX = (int) (tabWidth * (position + offset));

        // 移动Tab
        if(position >= (mDefaultVisibleCounts - 2) && offset > 0
                && getChildCount() > mDefaultVisibleCounts
                && position != getChildCount() -2)
        {
            if(mDefaultVisibleCounts != 1){
                int scrollX = (position - (mDefaultVisibleCounts - 2)) * tabWidth
                        + (int)(tabWidth * offset);
                        this.scrollTo(scrollX,0);
            }else{
                this.scrollTo((position * tabWidth) + (int)(tabWidth + offset),0);
            }
        }
        invalidate();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int counts = getChildCount();
        if(counts == 0)
            return;
        for (int i = 0;i < counts; i++){
            View view = getChildAt(i);
            LayoutParams params = (LayoutParams) view.getLayoutParams();
            params.weight = 0;
            params.width = getMeasuredWidth() / mDefaultVisibleCounts;
            view.setLayoutParams(params);
        }
        setItemClickListener();
        mLineWidth = getMeasuredWidth() / mDefaultVisibleCounts;

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.drawLine(mMoveX + dip2px(10),getHeight(),mMoveX + mLineWidth - dip2px(10),getHeight(),mLinePaint);
        canvas.restore();
        super.dispatchDraw(canvas);
    }

    /**
     * 生成Tab
     * @param title
     */
    private View generateTitleView(String title){
        TextView tv = new TextView(getContext());
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.width = getScreenWidth() / mDefaultVisibleCounts;
        tv.setGravity(Gravity.CENTER);
        tv.setText(title);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,mTextSize);
        tv.setTextColor(mNormalTextColor);
        tv.setLayoutParams(params);
        return tv;
    }

    /**
     * 设置高亮文字
     * @param position
     */
    private void setHighLightText(int position){
        for (int i = 0;i < getChildCount();i++){
            View view = getChildAt(i);
            if(view instanceof TextView){
                if(i == position) {
                    ((TextView) view).setTypeface(Typeface.DEFAULT_BOLD);
                    ((TextView) view).setTextColor(mHighLightTextColor);
                }
                else {
                    ((TextView) view).setTypeface(Typeface.DEFAULT);
                    ((TextView) view).setTextColor(mNormalTextColor);
                }
            }
        }
    }

    /**
     * viewPager滑动
     */
    private ViewPager.OnPageChangeListener mListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float offset, int i1) {
            scroll(i,offset);
        }

        @Override
        public void onPageSelected(int i) {
            setHighLightText(i);
            // 必须刷新不然getItemPosition不会更新
            mViewPager.getAdapter().notifyDataSetChanged();
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    /**
     * Tab点击事件
     */
    private void setItemClickListener(){
        for (int i = 0;i < getChildCount();i++){
            final int clickPosition = i;
            View view = getChildAt(i);
            if(view instanceof TextView){
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewPager.setCurrentItem(clickPosition);
                    }
                });
            }
        }
    }

    private int getScreenWidth(){
        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels - dip2px(40);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
