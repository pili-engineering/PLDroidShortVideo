package com.qiniu.shortvideo.app.tusdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.tusdk.playview.TuSdkMovieScrollView;
import com.qiniu.shortvideo.app.tusdk.playview.TuSdkRangeSelectionBar;
import com.qiniu.shortvideo.app.tusdk.playview.rangeselect.TuSdkMovieColorGroupView;
import com.qiniu.shortvideo.app.tusdk.playview.rangeselect.TuSdkMovieColorRectView;

/**
 * 作者： MirsFang on 2018/12/6 11:07
 * 邮箱： mirsfang@163.com
 * 类描述：视频滚动播放
 */
public class TuSdkMovieScrollPlayLineView extends FrameLayout {
    private static final String TAG = "TuSdkMovieScrollPlayLineView";
    private TuSdkMovieScrollView mMoviePlayScrollView;

    public TuSdkMovieScrollPlayLineView(Context context) {
        super(context);
        initView();
    }

    public TuSdkMovieScrollPlayLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.lsq_play_line_view,null);
        mMoviePlayScrollView = (TuSdkMovieScrollView) view.findViewById(R.id.lsq_play_scroll_view);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(view,layoutParams);
    }

    /** 设置进度改变监听 **/
    public void setOnProgressChangedListener(TuSdkMovieScrollView.OnProgressChangedListener progressChangedListener){
        mMoviePlayScrollView.setProgressChangedListener(progressChangedListener);
    }

    /** 设置选择区域监听 **/
    public void setSelectRangeChangedListener(TuSdkRangeSelectionBar.OnSelectRangeChangedListener changedListener) {
        mMoviePlayScrollView.setSelectRangeChangedListener(changedListener);
    }

    /** 设置区间临界值回调 **/
    public void setExceedCriticalValueListener(TuSdkRangeSelectionBar.OnExceedCriticalValueListener exceedValueListener){
        mMoviePlayScrollView.setExceedCriticalValueListener(exceedValueListener);
    }

    public void setOnTouchSelectBarListener(TuSdkRangeSelectionBar.OnTouchSelectBarListener onTouchSelectBarListener){
        mMoviePlayScrollView.setOnTouchSelectBarListener(onTouchSelectBarListener);
    }

    /** 设置颜色选择监听 **/
    public void setOnSelectColorRectListener(TuSdkMovieColorGroupView.OnSelectColorRectListener onSelectColorRectListener){
        mMoviePlayScrollView.setOnSelectColorRectListener(onSelectColorRectListener);
    }

    public void setOnBackListener(TuSdkMovieScrollView.OnColorGotoBackListener onBackListener) {
        this.mMoviePlayScrollView.setOnBackListener(onBackListener);
    }

    /** 设置当前类型 0 没Bar  1有Bar **/
    public void setType(int type){
        mMoviePlayScrollView.setType(type);
    }

    /** 添加视图封面(单张) **/
    public void addBitmap(Bitmap bitmap){
        mMoviePlayScrollView.addBitmap(bitmap);
    }

    /** 跳转到指定进度 **/
    public void seekTo(float progress){
        mMoviePlayScrollView.seekTo(progress);
    }

    public float getCurrentPercent() {
        return mMoviePlayScrollView.getCurrentPercent();
    }

    /** 添加颜色区块视图 **/
    public void addColorRect(int colorId){
        mMoviePlayScrollView.startAddColorRect(colorId);
    }

    /** 取消添加一个颜色色块 **/
    public void endAddColorRect(){
        mMoviePlayScrollView.endAddColorRect();
    }

    /** 删除一个颜色色块 **/
    public void deletedColorRect() {
        mMoviePlayScrollView.deletedColorRect();
    }

    public void deletedColorRect(TuSdkMovieColorRectView rectView){
        mMoviePlayScrollView.deletedColorRect(rectView);
    }

    /** 最小区间占比 **/
    public void setMinWidth(float minPercent) {
        mMoviePlayScrollView.setMinWidth(minPercent);
    }

    /** 最大区间占比 **/
    public void setMaxWidth(float maxPercent) {
        mMoviePlayScrollView.setMaxWidth(maxPercent);
    }

    /** 移动左边Bar的位置 **/
    public void setLeftBarPosition(float percent) {
        mMoviePlayScrollView.setLeftBarPosition(percent);
    }

    /** 移动右边Bar的位置 **/
    public void setRightBarPosition(float percent){
        mMoviePlayScrollView.setRightBarPosition(percent);
    }

    /** 获取左边Bar的进度 **/
    public float getLeftBarPercent(){
        return mMoviePlayScrollView.getLeftBarPercent();
    }

    /** 获取右边Bar的进度 **/
    public float getRightBarPercent(){
        return mMoviePlayScrollView.getRightBarPercent();
    }

    /**
     * 是否展示选择控件
     * @param isShow true 显示 false 不显示
     */
    public void setShowSelectBar(boolean isShow){
        mMoviePlayScrollView.setShowSelectBar(isShow);
    }

    /**
     * 设施时间类型
     * @param timeEffectType  0 正常   1 倒序
     */
    public void setTimeEffectType(int timeEffectType){
        mMoviePlayScrollView.setTimeEffectType(timeEffectType);
    }

    /**
     * 当前选择控件
     * @return
     */
    public boolean isShowSelectBar(){
        return mMoviePlayScrollView.isShowSelectBar();
    }

    /**
     * 回复上次的颜色
     * @param color
     * @param startPercent
     * @param endPercent
     */
    public TuSdkMovieColorRectView recoverColorRect(int color, float startPercent, float endPercent) {
        return mMoviePlayScrollView.recoverColorRect(color,startPercent,endPercent);
    }

    public void changeColorRect(TuSdkMovieColorRectView rectView,float startPercent, float endPercent){
        mMoviePlayScrollView.changeColorRect(rectView,startPercent,endPercent);
    }

    public void clearAllColorRect() {
        mMoviePlayScrollView.clearAllColorRect();
    }
}
