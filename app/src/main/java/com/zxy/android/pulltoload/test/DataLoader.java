package com.zxy.android.pulltoload.test;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhengxiaoyong on 2017/7/20.
 */
public class DataLoader {
    private volatile static DataLoader sInstance;

    private DataLoader() {
    }

    public static DataLoader getInstance() {
        if (sInstance == null) {
            synchronized (DataLoader.class) {
                if (sInstance == null) {
                    sInstance = new DataLoader();
                }
            }
        }
        return sInstance;
    }

    public void loadData(final OnDataLoadCallback<String> callback) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (callback != null)
                    callback.onSuccess(makeData());
            }
        }, 2000);
    }

    private List<String> makeData() {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            data.add("The number -> " + i);
        }
        return data;
    }

    public interface OnDataLoadCallback<T> {
        void onSuccess(List<T> data);

        void onFailed();
    }

}
