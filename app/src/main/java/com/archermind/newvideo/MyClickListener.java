package com.archermind.newvideo;

import android.view.View;

public abstract class MyClickListener implements View.OnClickListener{
    private static final long DOUBLE_TIME = 1000;
    private static long lastClickTime = 0;

    @Override
    public void onClick(View v) {
        onSingleClick(v);
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastClickTime < DOUBLE_TIME) {
            onDoubleClick(v);
        }
        lastClickTime = currentTimeMillis;
    }
    public abstract void onSingleClick(View v);
    public abstract void onDoubleClick(View v);
}
