package com.example.serfk.myapplication;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

// https://stackoverflow.com/a/39786708/4311829
public class CustomLayoutManager extends LinearLayoutManager {
    private final int parentWidth;
    private final int itemWidth;
    private final int numItems;

    private final float factor;

    public CustomLayoutManager(
            final Context context,
            final int orientation,
            final boolean reverseLayout,
            final int parentWidth,
            final int itemWidth,
            final int numItems,
            final float factor) {
        super(context, orientation, reverseLayout);
        this.parentWidth = parentWidth;
        this.itemWidth = itemWidth;
        this.numItems = numItems;
        this.factor = factor;
    }

    @Override
    public int getPaddingLeft() {
        final int totalItemWidth = itemWidth * numItems;
        if (totalItemWidth >= parentWidth) {
            return super.getPaddingLeft(); // do nothing
        } else {
            return Math.round(parentWidth / 2f - totalItemWidth / 2f);
        }
    }

    @Override
    public int getPaddingRight() {
        return getPaddingLeft();
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {

        final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {

            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return CustomLayoutManager.this.computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return super.calculateSpeedPerPixel(displayMetrics) * factor;
            }
        };

        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }
}
