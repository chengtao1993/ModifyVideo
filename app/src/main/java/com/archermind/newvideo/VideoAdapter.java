package com.archermind.newvideo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by archermind on 1/15/18.
 */

public class VideoAdapter extends BaseAdapter {
    private ArrayList<FileInfo> mArrayList;
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private ViewHolder mViewHolder;
    private FileInfo mFileInfo;

    public void setData(ArrayList<FileInfo> arrayList){
        mArrayList = arrayList;
    }

    public VideoAdapter(Context context,ArrayList arrayList){
        mLayoutInflater = LayoutInflater.from(context);
        mArrayList = arrayList;
    }
    @Override
    public int getCount() {
        return mArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return mArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null){
            mViewHolder = new ViewHolder();
            view = mLayoutInflater.inflate(R.layout.video_listview_content,null);
            mViewHolder.imageView = view.findViewById(R.id.icon);
            mViewHolder.textView = view.findViewById(R.id.name);
            view.setTag(mViewHolder);

        }else {
            mViewHolder = (ViewHolder) view.getTag();
        }
        mFileInfo = mArrayList.get(i);
        if (mFileInfo.isFile){
            mViewHolder.imageView.setImageBitmap(mFileInfo.bitmap);
            mViewHolder.textView.setText(mFileInfo.name);
        }else {
            mViewHolder.imageView.setImageResource(R.drawable.folder);
            mViewHolder.textView.setText(mFileInfo.name);
        }
        return view;
    }

    private class ViewHolder{
        ImageView imageView;
        TextView textView;

    }
}
