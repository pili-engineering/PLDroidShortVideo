package com.qiniu.pili.droid.shortvideo.demo.view.tusdk.filter;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import org.lasque.tusdk.core.TuSdkContext;
import org.lasque.tusdk.core.seles.SelesParameters.FilterArg;
import org.lasque.tusdk.core.seles.SelesParameters.FilterParameterInterface;
import org.lasque.tusdk.core.seles.sources.SelesOutInput;
import org.lasque.tusdk.core.view.TuSdkRelativeLayout;
import org.lasque.tusdk.impl.view.widget.TuSeekBar;
import org.lasque.tusdk.impl.view.widget.TuSeekBar.TuSeekBarDelegate;

/**
 * 滤镜配置拖动栏
 */
public class FilterConfigSeekbar extends TuSdkRelativeLayout {
    /**
     * 滤镜配置拖动栏委托
     *
     * @author Clear
     */
    public interface FilterConfigSeekbarDelegate {
        /**
         * 配置数据改变
         *
         * @param seekbar 滤镜配置拖动栏
         * @param arg     滤镜参数
         */
        void onSeekbarDataChanged(FilterConfigSeekbar seekbar, FilterArg arg);
    }

    /**
     * 布局ID
     *
     * @return
     */
    public static int getLayoutId() {
        return TuSdkContext
                .getLayoutResId("filter_config_seekbar");
    }

    public FilterConfigSeekbar(Context context) {
        super(context);
    }

    public FilterConfigSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FilterConfigSeekbar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // 百分比控制条
    private TuSeekBar mSeekbar;
    // 标题视图
    private TextView mTitleView;
    // 计数视图
    private TextView mNumberView;
    // 滤镜对象
    private FilterParameterInterface mFilter;
    // 滤镜配置参数
    private FilterArg mFilterArg;
    // 滤镜配置拖动栏委托
    private FilterConfigSeekbarDelegate mDelegate;
    /**
     * 滤镜强度值
     */
    private TextView mFilterValueView;

    /**
     * 百分比控制条
     *
     * @return the mSeekbar
     */
    public TuSeekBar getSeekbar() {
        if (mSeekbar == null) {
            mSeekbar = this.getViewById("lsq_seekView");
            if (mSeekbar != null) {
                mSeekbar.setDelegate(mTuSeekBarDelegate);
            }
        }
        return mSeekbar;
    }

    /**
     * 百分比控制条委托
     */
    private TuSeekBarDelegate mTuSeekBarDelegate = new TuSeekBarDelegate() {
        /**
         * 进度改变
         *
         * @param seekBar
         *            百分比控制条
         * @param progress
         *            进度百分比
         */
        public void onTuSeekBarChanged(TuSeekBar seekBar, float progress) {
            onSeekbarDataChanged(progress);
        }
    };

    /**
     * 百分比控制条数据改变
     *
     * @param progress
     */
    private void onSeekbarDataChanged(float progress) {
        this.setProgress(progress);
        if (mDelegate != null) {
            mDelegate.onSeekbarDataChanged(this, mFilterArg);
        }
        if (this.getFilterValueView() != null) {
            this.getFilterValueView().setText((int) (progress * 100) + "%");

        }
    }

    /**
     * 标题视图
     *
     * @return the mTitleView
     */
    public final TextView getTitleView() {
        if (mTitleView == null) {
            mTitleView = this.getViewById("lsq_titleView");
        }
        return mTitleView;
    }

    /**
     * 滤镜强度值
     *
     * @return the mFilterValueView
     */
    public final TextView getFilterValueView() {
        if (mFilterValueView == null) {
            mFilterValueView = this.getViewById("lsq_filterValueView");
        }
        return mFilterValueView;
    }

    /**
     * 计数视图
     *
     * @return the mNumberView
     */
    public final TextView getNumberView() {
        if (mNumberView == null) {
            mNumberView = this.getViewById("lsq_numberView");
        }
        return mNumberView;
    }

    /**
     * 滤镜配置拖动栏委托
     *
     * @return the mDelegate
     */
    public FilterConfigSeekbarDelegate getDelegate() {
        return mDelegate;
    }

    /**
     * 滤镜配置拖动栏委托
     *
     * @param mDelegate the mDelegate to set
     */
    public void setDelegate(FilterConfigSeekbarDelegate mDelegate) {
        this.mDelegate = mDelegate;
    }

    /**
     * 设置滤镜配置参数
     *
     * @param arg
     */
    public void setFilterArg(FilterArg arg) {
        mFilterArg = arg;
        if (mFilterArg == null) return;

        TuSeekBar seekBar = this.getSeekbar();
        if (seekBar == null) return;
        seekBar.setProgress(arg.getPrecentValue());

        if (this.getTitleView() != null) {
            this.getTitleView().setText(
                    TuSdkContext.getString("lsq_filter_set_" + arg.getKey()));
        }
        if (this.getFilterValueView() != null) {
            this.getFilterValueView().setText((int) (arg.getPrecentValue() * 100) + "%");
        }
        this.setProgress(arg.getPrecentValue());

    }

    /**
     * 设置百分比信息
     *
     * @param progress
     */
    private void setProgress(float progress) {
        if (mFilterArg != null) {
            mFilterArg.setPrecentValue(progress);
        }

        if (this.getNumberView() != null) {
            this.getNumberView().setText(
                    String.format("%02d", (int) (progress * 100)));
        }
    }

    /**
     * 重置参数
     */
    public void reset() {
        if (mFilterArg == null) return;
        mFilterArg.reset();
        this.setFilterArg(mFilterArg);
    }

    /**
     * 设置滤镜
     *
     * @param filter
     */
    public void setSelesFilter(SelesOutInput filter) {
        if (filter == null || !(filter instanceof FilterParameterInterface))
            return;

        this.mFilter = (FilterParameterInterface) filter;

        mFilter.submitParameter();
    }
}
