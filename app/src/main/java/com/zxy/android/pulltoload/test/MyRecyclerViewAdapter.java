package com.zxy.android.pulltoload.test;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhengxiaoyong on 2017/7/20.
 */

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private List<String> mData = new ArrayList<>();

    public void setData(List<String> data) {
        if (data == null || data.isEmpty())
            return;
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void addData(String data) {
        mData.add(data);
        notifyItemInserted(mData.size() - 1);
    }

    public void addData(List<String> data) {
        if (data == null || data.isEmpty())
            return;
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        if (position < 0 || position >= getItemCount())
            return;
        mData.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.mTextView.setText(mData.get(position));
        holder.mTextView.setTextColor(Color.WHITE);
        holder.itemView.setBackgroundColor(0xffaa66cc);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}