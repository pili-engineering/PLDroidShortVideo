package com.faceunity.view.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.faceunity.OnMultiClickListener;

import java.util.List;

/**
 * @author Richie on 2017.10.02
 * RecyclerView 通用适配器
 */
public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<BaseRecyclerAdapter.BaseViewHolder> {
    /**
     * 不可选
     */
    public static final int NO_CHOICE_MODE = 0;
    /**
     * 单选
     */
    public static final int SINGLE_CHOICE_MODE = 1;
    /**
     * 多选
     */
    public static final int MULTI_CHOICE_MODE = 2;
    // 单选默认选中的位置
    private static final int DEFAULT_SELECTED_POSITION = Integer.MIN_VALUE;
    // 单选上次选中的 view 位置
    protected int mLastSelected = DEFAULT_SELECTED_POSITION;
    // 数据集
    protected List<T> mData;
    // item 布局资源
    protected int mLayoutResId;
    // item 点击事件
    private OnItemClickListener<T> mOnItemClickListener;
    private OnItemLongClickListener<T> mOnItemLongClickListener;
    // 选中的 view 包含的数据，子 view 可点击时无效
    private SparseArray<T> mSelectedItems;

    public BaseRecyclerAdapter(@NonNull List<T> data, @LayoutRes int layoutResId) {
        mData = data;
        mLayoutResId = layoutResId;
        mSelectedItems = new SparseArray<>();
    }

    @Override
    @NonNull
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final BaseViewHolder viewHolder = BaseViewHolder.createViewHolder(parent, mLayoutResId);
        View itemView = viewHolder.getItemView();
        itemView.setOnClickListener(new InnerItemViewClickListener(viewHolder));
        itemView.setOnLongClickListener(new InnerItemLongClickListener(viewHolder));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder viewHolder, int position) {
        T data = mData.get(position);
        bindViewHolder(viewHolder, data);
        int choiceMode = choiceMode();
        if (choiceMode == SINGLE_CHOICE_MODE) {
            handleSelectedState(viewHolder, data, position == mLastSelected);
        } else if (choiceMode == MULTI_CHOICE_MODE) {
            boolean selected = mSelectedItems.get(position) != null;
            handleSelectedState(viewHolder, data, selected);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public List<T> getData() {
        return mData;
    }

    @Nullable
    public T getItem(@IntRange(from = 0) int position) {
        if (isValidPosition(position)) {
            return mData.get(position);
        } else {
            return null;
        }
    }

    /**
     * 设定元素选择的模式，默认单选
     *
     * @return
     */
    protected @ChoiceMode
    int choiceMode() {
        return SINGLE_CHOICE_MODE;
    }

    /**
     * 处理选中元素的状态
     *
     * @param viewHolder
     * @param data
     * @param selected
     */
    protected void handleSelectedState(BaseViewHolder viewHolder, T data, boolean selected) {
        viewHolder.setViewSelected(selected);
    }

    /**
     * 绑定 ViewHolder
     *
     * @param viewHolder
     * @param item
     */
    protected abstract void bindViewHolder(BaseViewHolder viewHolder, T item);

    public void setOnItemClickListener(OnItemClickListener<T> onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public OnItemClickListener<T> getOnItemClickListener() {
        return mOnItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<T> onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    /**
     * 设置某个选项选中
     *
     * @param data
     */
    public void setItemSelected(@NonNull T data) {
        int lastSelected = mLastSelected;
        mLastSelected = indexOf(data);
        if (mLastSelected >= 0) {
            mSelectedItems.put(mLastSelected, data);
            notifyItemChanged(mLastSelected);
        }
        mSelectedItems.remove(lastSelected);
        notifyItemChanged(lastSelected);
    }

    /**
     * 设置某个选项选中
     *
     * @param pos
     */
    public void setItemSelected(@IntRange(from = 0) int pos) {
        if (isValidPosition(pos)) {
            setItemSelected(mData.get(pos));
        }
    }

    /**
     * 设置某些选项选中
     *
     * @param data
     */
    public void setItemsSelected(@NonNull List<T> data) {
        if (data.size() > 0) {
            for (T datum : data) {
                setItemSelected(datum);
            }
        }
    }

    /**
     * 多选状态，设置全部选中状态
     */
    public void setAllItemSelected() {
        if (choiceMode() == MULTI_CHOICE_MODE) {
            for (T item : mData) {
                int index = indexOf(item);
                mSelectedItems.put(index, item);
                notifyItemChanged(index);
            }
        }
    }

    /**
     * 清除单选状态
     */
    public void clearSingleItemSelected() {
        mSelectedItems.clear();
        if (isValidPosition(mLastSelected)) {
            notifyItemChanged(mLastSelected);
        }
        mLastSelected = DEFAULT_SELECTED_POSITION;
    }

    /**
     * 清除多选状态
     */
    public void clearMultiItemSelected() {
        int size = mSelectedItems.size();
        for (int i = 0; i < size; i++) {
            T t = mSelectedItems.valueAt(i);
            int index = indexOf(t);
            if (index >= 0) {
                notifyItemChanged(index);
            }
        }
        mSelectedItems.clear();
    }

    /**
     * 添加元素
     */
    public void add(@NonNull T data) {
        mData.add(data);
        int index = indexOf(data);
        notifyItemInserted(index);
    }

    /**
     * 添加元素集合
     */
    public void addAll(@NonNull List<T> data) {
        mData.addAll(data);
        for (T datum : data) {
            int index = indexOf(datum);
            notifyItemInserted(index);
        }
    }

    /**
     * 清空旧元素，并添加新元素
     */
    public void replaceAll(@NonNull List<T> data) {
        mSelectedItems.clear();
        mData.clear();
        mData.addAll(data);
        mLastSelected = DEFAULT_SELECTED_POSITION;
        notifyDataSetChanged();
    }

    /**
     * 向某个位置，添加一个元素
     *
     * @param position
     * @param data
     */
    public void add(@IntRange(from = 0) int position, @NonNull T data) {
        mData.add(position, data);
        notifyItemInserted(position);
    }

    /**
     * 更新元素
     *
     * @param data
     */
    public void update(@NonNull T data) {
        int index = indexOf(data);
        if (index >= 0) {
            mData.set(index, data);
            if (mSelectedItems.get(index) != null) {
                mSelectedItems.put(index, data);
            }
            notifyItemChanged(index);
        }
    }

    /**
     * 清空所有元素
     */
    public void removeAll() {
        mData.clear();
        mSelectedItems.clear();
        notifyDataSetChanged();
    }

    /**
     * 更新特定位置的元素
     *
     * @param position
     * @param data
     */
    public void update(@IntRange(from = 0) int position, @NonNull T data) {
        mData.set(position, data);
        if (mSelectedItems.get(position) != null) {
            mSelectedItems.put(position, data);
        }
        notifyItemChanged(position);
    }

    /**
     * 移除元素
     *
     * @param data
     */
    public void remove(@NonNull T data) {
        int index = indexOf(data);
        if (isValidPosition(index)) {
            mSelectedItems.remove(index);
            mData.remove(index);
            notifyItemRemoved(index);
        }
    }

    /**
     * 移除特定位置的元素
     *
     * @param position
     */
    public void remove(@IntRange(from = 0) int position) {
        if (isValidPosition(position)) {
            mData.remove(position);
            mSelectedItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public boolean isAllItemSelected() {
        return mSelectedItems.size() == mData.size();
    }

    /**
     * 获取选中的元素
     *
     * @return
     */
    public SparseArray<T> getSelectedItems() {
        return mSelectedItems;
    }

    private boolean isValidPosition(int position) {
        return position >= 0 && position < mData.size();
    }

    public int indexOf(@NonNull T data) {
        return mData.indexOf(data);
    }

    @IntDef({NO_CHOICE_MODE, SINGLE_CHOICE_MODE, MULTI_CHOICE_MODE})
    public @interface ChoiceMode {
    }

    /**
     * View 点击事件监听器
     */
    public interface OnItemClickListener<T> {
        /**
         * 点击选项
         *
         * @param adapter
         * @param view
         * @param position
         */
        void onItemClick(BaseRecyclerAdapter<T> adapter, View view, int position);
    }

    /**
     * Interface definition for a callback to be invoked when an item in this
     * view has been clicked and held.
     */
    public interface OnItemLongClickListener<T> {
        /**
         * callback method to be invoked when an item in this view has been
         * click and held
         *
         * @param adapter  the adpater
         * @param view     The view whihin the RecyclerView that was clicked and held.
         * @param position The position of the view int the adapter
         * @return true if the callback consumed the long click ,false otherwise
         */
        boolean onItemLongClick(BaseRecyclerAdapter<T> adapter, View view, int position);
    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder {
        private SparseArray<View> mViews;

        private BaseViewHolder(View itemView) {
            super(itemView);
            mViews = new SparseArray<>();
        }

        public static BaseViewHolder createViewHolder(ViewGroup parent, int layoutResId) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(layoutResId, parent, false);
            return new BaseViewHolder(view);
        }

        public View getItemView() {
            return itemView;
        }

        /**
         * 通过viewId获取控件
         *
         * @param viewId
         * @return
         */
        @SuppressWarnings("unchecked")
        public <T extends View> T getViewById(@IdRes int viewId) {
            View view = mViews.get(viewId);
            if (view == null) {
                view = itemView.findViewById(viewId);
                mViews.put(viewId, view);
            }
            return (T) view;
        }

        /*----以下为辅助方法----*/

        /**
         * 设置 Item 布局的选中状态
         *
         * @param selected
         * @return
         */
        public BaseViewHolder setViewSelected(boolean selected) {
            getItemView().setSelected(selected);
            return this;
        }

        /**
         * 设置文字
         */
        public BaseViewHolder setText(@IdRes int id, String text) {
            View view = getViewById(id);
            if (view instanceof TextView) {
                ((TextView) view).setText(text);
            }
            return this;
        }

        /**
         * ImageView 设置图片 Bitmap
         *
         * @param id
         * @param bitmap
         * @return
         */
        public BaseViewHolder setImageBitmap(@IdRes int id, Bitmap bitmap) {
            View view = getViewById(id);
            if (view instanceof ImageView) {
                ((ImageView) view).setImageBitmap(bitmap);
            }
            return this;
        }

        /**
         * ImageView 设置图片 res
         *
         * @param id
         * @param drawable
         * @return
         */
        public BaseViewHolder setImageResource(@IdRes int id, @DrawableRes int drawable) {
            View view = getViewById(id);
            if (view instanceof ImageView) {
                ((ImageView) view).setImageResource(drawable);
            }
            return this;
        }

        /**
         * ImageView 设置图片 drawable
         *
         * @param id
         * @param drawable
         * @return
         */
        public BaseViewHolder setImageDrawable(@IdRes int id, Drawable drawable) {
            View view = getViewById(id);
            if (view instanceof ImageView) {
                ((ImageView) view).setImageDrawable(drawable);
            }
            return this;
        }

        /**
         * 设置 View 的点击监听器
         *
         * @param id
         * @param listener
         * @return
         */
        public BaseViewHolder setOnClickListener(@IdRes int id, View.OnClickListener listener) {
            getViewById(id).setOnClickListener(listener);
            return this;
        }

        /**
         * 设置 View 的可见性
         *
         * @param id
         * @param visible
         * @return
         */
        public BaseViewHolder setVisibility(@IdRes int id, int visible) {
            View view = getViewById(id);
            if (view != null && view.getVisibility() != visible) {
                view.setVisibility(visible);
            }
            return this;
        }

        /**
         * 设置 View 的标签
         *
         * @param id
         * @param obj
         * @return
         */
        public BaseViewHolder setTag(@IdRes int id, Object obj) {
            getViewById(id).setTag(obj);
            return this;
        }

        /**
         * 设置 Item View 的标签
         *
         * @param obj
         * @return
         */
        public BaseViewHolder setTag(Object obj) {
            getItemView().setTag(obj);
            return this;
        }

        /**
         * 设置 View 的选中状态
         *
         * @param id
         * @param selected
         * @return
         */
        public BaseViewHolder setViewSelected(@IdRes int id, boolean selected) {
            View view = getViewById(id);
            view.setSelected(selected);
            return this;
        }

        /**
         * 设置 View 是否可用
         *
         * @param id
         * @param enabled
         * @return
         */
        public BaseViewHolder setEnabled(@IdRes int id, boolean enabled) {
            View view = getViewById(id);
            view.setEnabled(enabled);
            return this;
        }

        /**
         * 设置 TextView 字体样式
         *
         * @param id
         * @param textStype
         * @return
         */
        public BaseViewHolder setTextStyle(@IdRes int id, int textStype) {
            View view = getViewById(id);
            if (view instanceof TextView) {
                ((TextView) view).setTypeface(null, textStype);
            }
            return this;
        }

        /**
         * 设置 TextView 字体颜色
         *
         * @return
         */
        public BaseViewHolder setTextColor(@IdRes int id, @ColorInt int color) {
            View view = getViewById(id);
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(color);
            }
            return this;
        }

        /**
         * 设置 View 背景
         *
         * @param id
         * @param drawable
         * @return
         */
        public BaseViewHolder setBackground(@IdRes int id, @DrawableRes int drawable) {
            View view = getViewById(id);
            view.setBackgroundResource(drawable);
            return this;
        }

        //其他方法可自行扩展

    }

    public class InnerItemLongClickListener implements View.OnLongClickListener {
        private BaseViewHolder mViewHolder;

        public InnerItemLongClickListener(BaseViewHolder viewHolder) {
            mViewHolder = viewHolder;
        }

        @Override
        public boolean onLongClick(View v) {
            if (mOnItemLongClickListener != null) {
                return mOnItemLongClickListener.onItemLongClick(BaseRecyclerAdapter.this, v, mViewHolder.getAdapterPosition());
            } else {
                return false;
            }
        }
    }

    /**
     * Item 点击事件监听器
     */
    public class InnerItemViewClickListener extends OnMultiClickListener {
        private BaseViewHolder mViewHolder;

        public InnerItemViewClickListener(BaseViewHolder viewHolder) {
            mViewHolder = viewHolder;
        }

        @Override
        protected void onMultiClick(View view) {
            int position = mViewHolder.getAdapterPosition();
            if (position < 0) {
                return;
            }
            int choiceMode = choiceMode();
            T selectedData = getItem(position);
            if (choiceMode == SINGLE_CHOICE_MODE) {
                mSelectedItems.put(position, selectedData);
                if (isValidPosition(mLastSelected) && mLastSelected != position) {
                    mSelectedItems.remove(mLastSelected);
                }
                notifyItemChanged(mLastSelected);
                notifyItemChanged(position);
                mLastSelected = position;
            } else if (choiceMode == MULTI_CHOICE_MODE) {
                boolean isClicked = !view.isSelected();
                if (isClicked) {
                    mSelectedItems.put(position, selectedData);
                } else {
                    mSelectedItems.remove(position);
                }
                notifyItemChanged(position);
            }

            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(BaseRecyclerAdapter.this, view, position);
            }
        }
    }

}
