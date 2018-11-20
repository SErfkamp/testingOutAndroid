package com.example.serfk.myapplication;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;

public class CustomLayoutManager extends LinearLayoutManager {

    private static final String TAG = "CustomLayoutManager";
    private  int parentWidth;
    private  int itemWidth;
    private  int numItems;

    private static float factor;


    public CustomLayoutManager (
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

    public CustomLayoutManager(Context context) {
        super(context);
    }

    public CustomLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public CustomLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public int getPaddingLeft() {
        final int totalItemWidth = itemWidth * numItems;
        if (totalItemWidth >= parentWidth) {
            return super.getPaddingLeft(); // do nothing
        } else {
            return Math.round(parentWidth / 2f - totalItemWidth / 2f);// - itemWidth / 2f);
        }
    }

    @Override
    public int getPaddingRight() {
        return getPaddingLeft();
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        RecyclerView.SmoothScroller smoothScroller = new CenterSmoothScroller(recyclerView.getContext());
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
        //Log.d(TAG, "startSmoothScrollToPosition: " + position);
    }

    private static class CenterSmoothScroller extends LinearSmoothScroller {

        CenterSmoothScroller(Context context) {
            super(context);
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
        }

        @Override
        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            return super.calculateSpeedPerPixel(displayMetrics) * factor;
        }
    }
}