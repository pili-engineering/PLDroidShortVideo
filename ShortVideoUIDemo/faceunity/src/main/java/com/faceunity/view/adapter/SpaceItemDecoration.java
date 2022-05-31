package com.faceunity.view.adapter;

import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * @author Richie on 2017.10.20
 * RecyclerView 水平垂直边距装饰器
 */
public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int verticalSpace;
    private int horizontalSpace;
    // 首尾额外的边距
    private int additionalLT;
    private int additionalRB;

    public SpaceItemDecoration(int horizontalSpace, int verticalSpace) {
        this.horizontalSpace = horizontalSpace;
        this.verticalSpace = verticalSpace;
    }

    public SpaceItemDecoration(int horizontalSpace, int verticalSpace, int additionalLT, int additionalRB) {
        this.horizontalSpace = horizontalSpace;
        this.verticalSpace = verticalSpace;
        this.additionalLT = additionalLT;
        this.additionalRB = additionalRB;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        outRect.top = verticalSpace;
        outRect.left = horizontalSpace;
        outRect.right = horizontalSpace;
        outRect.bottom = verticalSpace;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            int orientation = ((LinearLayoutManager) layoutManager).getOrientation();
            int itemCount = parent.getAdapter().getItemCount();
            int childAdapterPosition = parent.getChildAdapterPosition(view);
            if (layoutManager instanceof GridLayoutManager) {
                int spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
                if (orientation == LinearLayoutManager.VERTICAL) {
                    if (childAdapterPosition < spanCount) {
                        outRect.top += additionalLT;
                    } else if (childAdapterPosition + spanCount >= itemCount) {
                        outRect.bottom += additionalRB;
                    }
                } else if (orientation == LinearLayoutManager.HORIZONTAL) {
                    if (childAdapterPosition < spanCount) {
                        outRect.left += additionalLT;
                    } else if (childAdapterPosition + spanCount >= itemCount) {
                        outRect.right += additionalRB;
                    }
                }
            } else {
                if (orientation == LinearLayoutManager.VERTICAL) {
                    if (childAdapterPosition == 0) {
                        outRect.top += additionalLT;
                    } else if (childAdapterPosition == itemCount - 1) {
                        outRect.bottom += additionalRB;
                    }
                } else if (orientation == LinearLayoutManager.HORIZONTAL) {
                    if (childAdapterPosition == 0) {
                        outRect.left += additionalLT;
                    } else if (childAdapterPosition == itemCount - 1) {
                        outRect.right += additionalRB;
                    }
                }
            }
        }
    }

}
