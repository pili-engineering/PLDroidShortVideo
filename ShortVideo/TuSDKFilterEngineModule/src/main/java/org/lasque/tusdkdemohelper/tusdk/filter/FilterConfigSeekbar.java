/** 
 * TuSDKLiveDemo
 * FilterConfigSeekbar.java
 *
 * @author 		Yanlin
 * @Date 		2016-4-15 上午10:36:28
 * @Copyright 	(c) 2016 tusdk.com. All rights reserved.
 *
 */
package org.lasque.tusdkdemohelper.tusdk.filter;

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

import java.math.BigDecimal;

/**
 * 滤镜配置拖动栏
 * 
 * @author Yanlin
 */
public class FilterConfigSeekbar extends TuSdkRelativeLayout
{
	/**
	 * 滤镜配置拖动栏委托
	 * 
	 * @author Clear
	 */
	public interface FilterConfigSeekbarDelegate
	{
		/**
		 * 配置数据改变
		 * 
		 * @param seekbar
		 *            滤镜配置拖动栏
		 * @param arg
		 *            滤镜参数
		 */
		void onSeekbarDataChanged(FilterConfigSeekbar seekbar, FilterArg arg);
	}

	/**
	 * 布局ID
	 * 
	 * @return
	 */
	public static int getLayoutId()
	{
		return TuSdkContext
				.getLayoutResId("tusdk_filter_config_seekbar");
	}

	public FilterConfigSeekbar(Context context)
	{
		super(context);
	}

	public FilterConfigSeekbar(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public FilterConfigSeekbar(Context context, AttributeSet attrs, int defStyle)
	{
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
	public TuSeekBar getSeekbar()
	{
		if (mSeekbar == null)
		{
			mSeekbar = this.getViewById("lsq_seekView");
			if (mSeekbar != null)
			{
				mSeekbar.setDelegate(mTuSeekBarDelegate);
			}
		}
		return mSeekbar;
	}

	/**
	 * 百分比控制条委托
	 */
	private TuSeekBarDelegate mTuSeekBarDelegate = new TuSeekBarDelegate()
	{
		/**
		 * 进度改变
		 * 
		 * @param seekBar
		 *            百分比控制条
		 * @param progress
		 *            进度百分比
		 */
		public void onTuSeekBarChanged(TuSeekBar seekBar, float progress)
		{
			onSeekbarDataChanged(progress);
		}
	};

	/**
	 * 百分比控制条数据改变
	 * 
	 * @param progress
	 */
	private void onSeekbarDataChanged(float progress)
	{
		this.setProgress(mFilterArg.getKey(),progress);

		if (mDelegate != null)
		{
			mDelegate.onSeekbarDataChanged(this, mFilterArg);
		}
	}

	/**
	 * 标题视图
	 * 
	 * @return the mTitleView
	 */
	public final TextView getTitleView()
	{
		if (mTitleView == null)
		{
			mTitleView = this.getViewById("lsq_titleView");
		}
		return mTitleView;
	}
	/**
	 * 滤镜强度值
	 * 
	 * @return the mFilterValueView
	 */
	public final TextView getFilterValueView()
	{
		if (mFilterValueView == null)
		{
			mFilterValueView = this.getViewById("lsq_filterValueView");
		}
		return mFilterValueView;
	}

	/**
	 * 计数视图
	 * 
	 * @return the mNumberView
	 */
	public final TextView getNumberView()
	{
		if (mNumberView == null)
		{
			mNumberView = this.getViewById("lsq_numberView");
		}
		return mNumberView;
	}

	/**
	 * 滤镜配置拖动栏委托
	 * 
	 * @return the mDelegate
	 */
	public FilterConfigSeekbarDelegate getDelegate()
	{
		return mDelegate;
	}

	/**
	 * 滤镜配置拖动栏委托
	 * 
	 * @param mDelegate
	 *            the mDelegate to set
	 */
	public void setDelegate(FilterConfigSeekbarDelegate mDelegate)
	{
		this.mDelegate = mDelegate;
	}

	/**
	 * 设置滤镜配置参数
	 * 
	 * @param arg
	 */
	public void setFilterArg(FilterArg arg)
	{
		mFilterArg = arg;
		if (mFilterArg == null) return;

		TuSeekBar seekBar = this.getSeekbar();
		if (seekBar == null) return;
		seekBar.setProgress(arg.getPrecentValue());

		if (this.getTitleView() != null)
		{
			this.getTitleView().setText(
					TuSdkContext.getString("lsq_filter_set_" + arg.getKey()));
		}
		this.setProgress(arg.getKey(),arg.getPrecentValue());
		
	}

	/**
	 * 设置百分比信息
	 *
	 * @param key
	 * @param progress
	 */
	private void setProgress(String key,float progress)
	{
		if (mFilterArg != null)
		{
			mFilterArg.setPrecentValue(progress);
		}

		if (this.getNumberView() != null)
		{
			this.getNumberView().setText(
					String.format("%02d", (int) (progress * 100)));
		}

		if(this.getFilterValueView()!=null)
		{
			switch (key) {
				// 以下为改变显示进度
				case "mouthWidth":
					progress = progress - 0.5f;
					break;
				case "archEyebrow":
					progress = progress - 0.5f;
					break;
				case "jawSize":
					progress = progress - 0.5f;
					break;
				case "eyeAngle":
					progress = progress - 0.5f;
					break;
				case "eyeDis":
					progress = progress - 0.5f;
					break;
			}
			BigDecimal bigDecimal = new BigDecimal(progress);
			progress = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
			this.getFilterValueView().setText((int) (progress * 100)+"%");
		}
	}

	/**
	 * 重置参数
	 */
	public void reset()
	{
		if (mFilterArg == null) return;
		mFilterArg.reset();
		this.setFilterArg(mFilterArg);
	}
	
    /**
     * 设置滤镜
     *
     * @param filter
     */
    public void setSelesFilter(SelesOutInput filter)
    {
        if (filter == null || !(filter instanceof FilterParameterInterface))
            return;
        
       this.mFilter = (FilterParameterInterface) filter;
       
       mFilter.submitParameter();
    }
}
