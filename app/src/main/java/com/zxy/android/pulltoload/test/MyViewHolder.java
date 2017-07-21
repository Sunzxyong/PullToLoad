package com.zxy.android.pulltoload.test;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by zhengxiaoyong on 2017/7/20.
 */

public class MyViewHolder extends RecyclerView.ViewHolder {
    TextView mTextView;

    MyViewHolder(View itemView) {
        super(itemView);
        mTextView = (TextView) itemView.findViewById(android.R.id.text1);
    }
}