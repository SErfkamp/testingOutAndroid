package com.example.serfk.myapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

public class CustomRecyclerView extends RecyclerView {

    public CustomRecyclerView(@NonNull Context context) {
        super(context);
    }

    @Override
    public int getHorizontalFadingEdgeLength() {
        return 100;
    }

}
