/**
 * TuSDKLiveDemo
 * FilterConfigView.java
 *
 * @author 		Yanlin
 * @Date 		2016-4-15 上午10:36:28
 * @Copyright 	(c) 2016 tusdk.com. All rights reserved.
 *
 */
package org.lasque.tusdkdemohelper.tusdk.filter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.lasque.tusdk.core.TuSdkContext;
import org.lasque.tusdk.core.seles.SelesParameters.FilterArg;
import org.lasque.tusdk.core.view.TuSdkRelativeLayout;
import org.lasque.tusdk.core.view.TuSdkViewHelper;
import org.lasque.tusdk.core.view.TuSdkViewHelper.OnSafeClickListener;
import org.lasque.tusdk.video.editor.TuSdkMediaEffectData;

import java.util.ArrayList;
import java.util.List;

/**
 * 滤镜配置视图
 *
 * @author Clear
 */
public class FilterConfigView extends TuSdkRelativeLayout
{

    private TuSdkMediaEffectData mMediaEffect;

    /**
     * 滤镜配置视图委托
     *
     * @author Clear
     */
    public interface FilterConfigViewDelegate
    {
        /**
         * 通知重新绘制
         *
         * @param configView
         */
        void onFilterConfigRequestRender(FilterConfigView configView);
    }

    /**
     * 滤镜配置视图委托
     */
    private FilterConfigViewDelegate mDelegate;

    /**
     * 滤镜配置视图委托
     *
     * @return the mDelegate
     */
    public FilterConfigViewDelegate getDelegate()
    {
        return mDelegate;
    }

    /**
     * 滤镜配置视图委托
     *
     * @param mDelegate
     *            the mDelegate to set
     */
    public void setDelegate(FilterConfigViewDelegate mDelegate)
    {
        this.mDelegate = mDelegate;
    }

    public FilterConfigView(Context context)
    {
        super(context);
    }

    public FilterConfigView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public FilterConfigView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    // 配置包装
    private LinearLayout mConfigWrap;

    /**
     * 配置包装
     *
     * @return
     */
    public LinearLayout getConfigWrap()
    {
        if (mConfigWrap == null)
        {
            mConfigWrap = this.getViewById("lsq_configWrap");
        }
        return mConfigWrap;
    }

    /**
     * 设置拖动条高度
     *
     * @param seekbarHeight
     */
    public void setSeekBarHeight(int seekbarHeight)
    {
        this.mSeekHeigth = seekbarHeight;
    }


    /**
     * 设置显示的参数信息
     *
     * @param filterArgs 参数列表
     */
    public void setFilterArgs(TuSdkMediaEffectData effectData, List<FilterArg> filterArgs) {

        LinearLayout configWrap = this.getConfigWrap();

        if (configWrap == null) return;
        // 删除所有视图
        configWrap.removeAllViews();

        this.mMediaEffect = effectData;
        mSeekbars = new ArrayList<FilterConfigSeekbar>(0);

        if (filterArgs == null) return;


        for (FilterArg arg : filterArgs)
        {
            // 可过滤不需要的调节参数
            if (checkIgnoredKey(arg, getIgnoredKeys())) continue;

            FilterConfigSeekbar seekbar = this.buildAppendSeekbar(configWrap, this.mSeekHeigth);
            if (seekbar != null)
            {
                // 设置滤镜配置参数
                seekbar.setFilterArg(arg);
                seekbar.setDelegate(mFilterConfigSeekbarDelegate);
                mSeekbars.add(seekbar);
            }
        }
    }

    /**
     * 检查key是否需要忽略
     * @param arg
     *          滤镜参数
     * @param ignoredKeys
     *            忽略的Key
     * @return
     */
    private boolean checkIgnoredKey(FilterArg arg, String[] ignoredKeys)
    {
        for (String key : ignoredKeys)
        {
            if (arg.equalsKey(key)) return true;
        }

        return false;
    }

    String[] ignoredKeys = new String[]{"eyeSize", "chinSize", "noseSize","mouthWidth","archEyebrow","jawSize","eyeAngle","eyeDis"};

    private String[] getIgnoredKeys()
    {
        return ignoredKeys;
    }

    public void setIgnoredKeys(String[] strings)
    {
        ignoredKeys = strings;
    }

    /**
     * 滤镜配置拖动栏列表
     */
    private ArrayList<FilterConfigSeekbar> mSeekbars;

    /**
     * 拖动条高度
     */
    private int mSeekHeigth = TuSdkContext.dip2px(32);

    /**
     * 创建并添加滤镜配置拖动栏
     *
     * @param parent
     *            父视图
     * @return
     */
    public FilterConfigSeekbar buildAppendSeekbar(LinearLayout parent, int height)
    {
        if (parent == null) return null;

        FilterConfigSeekbar seekbar = TuSdkViewHelper.buildView(this.getContext(), FilterConfigSeekbar.getLayoutId(), parent);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
        parent.addView(seekbar, params);
        return seekbar;
    }

    /**
     * 滤镜配置拖动栏委托
     */
    protected FilterConfigSeekbar.FilterConfigSeekbarDelegate mFilterConfigSeekbarDelegate = new FilterConfigSeekbar.FilterConfigSeekbarDelegate()
    {
        /**
         * 配置数据改变
         *
         * @param seekbar
         *            滤镜配置拖动栏
         * @param arg
         *            滤镜参数
         */
        public void onSeekbarDataChanged(FilterConfigSeekbar seekbar, FilterArg arg)
        {
            if (getSeekBarDelegate() != null) {
                getSeekBarDelegate().onSeekbarDataChanged(seekbar, arg);
            }

            requestRender();
        }
    };

    /**
     * 滤镜配置拖动栏状态委托
     *
     * @author Clear
     */
    public interface FilterConfigViewSeekBarDelegate
    {
        /**
         * 配置数据改变
         *
         * @param seekbar
         *            滤镜配置拖动栏
         * @param arg
         *            滤镜参数
         */
        public void onSeekbarDataChanged(FilterConfigSeekbar seekbar, FilterArg arg);

    }
    
    private FilterConfigViewSeekBarDelegate mSeekBarDelegate;
    
    public void setSeekBarDelegate(FilterConfigViewSeekBarDelegate seekBarDelegate)
    {
    	this.mSeekBarDelegate = seekBarDelegate;
    }
    
    public FilterConfigViewSeekBarDelegate getSeekBarDelegate()
    {
    	return this.mSeekBarDelegate;
    }
    
    /**
     * 按钮点击事件
     */
    protected OnSafeClickListener mOnClickListener = new OnSafeClickListener()
    {
        @Override
        public void onSafeClick(View v)
        {
        }
    };
    
    /**
     * 设置滤镜配置选线显示状态
     */
    protected void handleShowStateAction()
    {
    }

    /**
     * 请求渲染
     */
    protected void requestRender()
    {
        if (mDelegate != null)
        {
            mDelegate.onFilterConfigRequestRender(this);
        }

        if (this.mMediaEffect != null)
            this.mMediaEffect.submitParameters();
    }
}
