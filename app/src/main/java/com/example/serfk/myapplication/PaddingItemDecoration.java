package com.example.serfk.myapplication;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class PaddingItemDecoration extends RecyclerView.ItemDecoration {

    private int mPaddingPx;
    private int mPaddingEdgesPx;

    public PaddingItemDecoration(Activity activity) {
        //final Resources resources = activity.getResources();
        //mPaddingPx = (int) resources.getDimension(R.dimen.paddingItemDecorationDefault);
        //mPaddingEdgesPx = (int) resources.getDimension(R.dimen.paddingItemDecorationEdge);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        final int itemPosition = parent.getChildAdapterPosition(view);
        if (itemPosition == RecyclerView.NO_POSITION) {
            return;
        }

        //final int itemCount = state.getItemCount(); // 5
                                                    // 1

        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            if (i == itemPosition) {
                //outRect.set(0, 400, 0, 0);
                params.bottomMargin = 400;
            } else {
                //outRect.set(0, 0, 0, 0);

                params.bottomMargin = 0;
            }
        }
      /*  int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;

        // all positions
        top = mPaddingPx;
        bottom = mPaddingPx;

        // first position
        if (itemPosition == 0) {
            top += mPaddingEdgesPx;
        }
        // last position
        else if (itemCount > 0 && itemPosition == itemCount - 1) {
            bottom += mPaddingEdgesPx;
        }

        if (!isReverseLayout(parent)) {
            outRect.set(left, top, right, bottom);
        } else {
            outRect.set(right, bottom, left, top);
        }*/
    }

}