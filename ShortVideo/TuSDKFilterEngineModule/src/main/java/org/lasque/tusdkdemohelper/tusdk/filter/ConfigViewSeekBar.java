/**
 * TuSDKVideoDemo
 * ConfigViewSeekBar.java
 *
 * @author  LiuHang
 * @Date  Jul 4, 2017 2:24:33 PM
 * @Copright (c) 2017 tusdk.com. All rights reserved.
 *
 */
package org.lasque.tusdkdemohelper.tusdk.filter;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import org.lasque.tusdk.core.TuSdkContext;
import org.lasque.tusdk.core.view.TuSdkRelativeLayout;
import org.lasque.tusdk.impl.view.widget.TuSeekBar;
import org.lasque.tusdk.impl.view.widget.TuSeekBar.TuSeekBarDelegate;

/**
 * 调节栏SeekBar
 * 
 * @author LiuHang
 *
 */
public class ConfigViewSeekBar extends TuSdkRelativeLayout
{
	// SeekBar默认布局
	private static String mResID = "tusdk_config_seekbar_one";

	public ConfigViewSeekBar(Context context)
	{
		super(context);
	}

	public ConfigViewSeekBar(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public ConfigViewSeekBar(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	/**
	 * 拖动栏委托
	 * 
	 */
	public interface ConfigSeekbarDelegate
	{
		/**
		 * 配置数据改变
		 * 
		 * @param seekbar
		 *            拖动栏
		 * @param arg
		 *            参数
		 */
		void onSeekbarDataChanged(ConfigViewSeekBar seekbar, ConfigViewParams.ConfigViewArg arg);
	}

	/**
	 * 布局ID
	 * 
	 * @return
	 */
	public static int getLayoutId()
	{
		return TuSdkContext
				.getLayoutResId(mResID);
	}

	/**
	 * 设置布局ID
	 *
	 * @param resID
	 */
	public static void setLayoutId(String resID)
	{
		mResID = resID;
	}

	/** 百分比控制条 */
	private TuSeekBar mSeekbar;
	
	/** 标题视图 */
	private TextView mTitleView;
	
	/** 配置参数 */
	private ConfigViewParams.ConfigViewArg mConfigViewArg;
	
	/** 拖动栏委托 */
	private ConfigSeekbarDelegate mDelegate;
	
	/**
	 * 调节栏值
	 */
	private TextView mConfigValueView;
	
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
		this.setProgress(progress);
		if (mDelegate != null)
		{
			mDelegate.onSeekbarDataChanged(this, mConfigViewArg);
		}
		if(this.getConfigValueView()!=null)
		{
			this.getConfigValueView().setText((int)(progress*100)+"%");
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
	 * 调节栏强度值
	 * 
	 * @return the mConfigValueView
	 */
	public final TextView getConfigValueView()
	{
		if (mConfigValueView == null)
		{
			mConfigValueView = this.getViewById("lsq_configValueView");
		}
		return mConfigValueView;
	}

	/**
	 * 拖动栏委托
	 * 
	 * @return the mDelegate
	 */
	public ConfigSeekbarDelegate getDelegate()
	{
		return mDelegate;
	}

	/**
	 * 拖动栏委托
	 * 
	 * @param mDelegate
	 *            the mDelegate to set
	 */
	public void setDelegate(ConfigSeekbarDelegate mDelegate)
	{
		this.mDelegate = mDelegate;
	}

	/**
	 * 设置调节栏配置参数
	 * -
	 * @param arg
	 */
	public void setConfigViewArg(ConfigViewParams.ConfigViewArg arg)
	{
		mConfigViewArg = arg;
		if (mConfigViewArg == null) return;

		TuSeekBar seekBar = this.getSeekbar();
		if (seekBar == null) return;
		seekBar.setProgress(arg.getPercentValue());

		if (this.getTitleView() != null)
		{
			this.getTitleView().setText(
					TuSdkContext.getString("lsq_congfigview_set_" + arg.getKey()));
		}
		if(this.getConfigValueView()!=null)
		{
			this.getConfigValueView().setText((int)(arg.getPercentValue()*100)+"%");
		}
		this.setProgress(arg.getPercentValue());
	}

	/**
	 * 设置百分比信息
	 * 
	 * @param progress
	 */
	public void setProgress(float progress)
	{
		if (mConfigViewArg != null)
		{
			mConfigViewArg.setPercentValue(progress);
		}
		getSeekbar().setProgress(progress);
		
		if(this.getConfigValueView()!=null)
		{
			this.getConfigValueView().setText((int)(progress*100)+"%");
		}
	}

	/**
	 * 重置参数
	 */
	public void reset()
	{
		if (mConfigViewArg == null) return;
		mConfigViewArg.reset();
		this.setConfigViewArg(mConfigViewArg);
	}
}
