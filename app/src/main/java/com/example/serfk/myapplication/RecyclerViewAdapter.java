package com.example.serfk.myapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>  {

    private static final String TAG = "RecyclerViewAdapter";
    private final int mListItem;

    private ArrayList<String> mLabels;
    private Context mContext;
    private int selectedPos = RecyclerView.NO_POSITION;

    public RecyclerViewAdapter(ArrayList<String> mLabels, Context mContext, int mListItem) {
        this.mLabels = mLabels;
        this.mContext = mContext;
        this.mListItem = mListItem;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mListItem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.name.setText(mLabels.get(position));
        holder.itemView.setSelected(selectedPos == position);
        if(position == 0 && mLabels.get(position).equals("")) {
            holder.menuChangeImage.setBackgroundResource(R.drawable.change_menu);
        }
    }

    @Override
    public int getItemCount() {
        return mLabels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        CardView menuChangeImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            menuChangeImage = itemView.findViewById(R.id.list_item);
        }
    }
}
