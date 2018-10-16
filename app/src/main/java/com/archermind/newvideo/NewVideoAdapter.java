package com.archermind.newvideo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class NewVideoAdapter extends RecyclerView.Adapter {
    private LayoutInflater mLayoutInflater;
    private ArrayList<FileInfo> mArrayList;
    private FileInfo mFileInfo;

    public NewVideoAdapter(Context context,ArrayList arrayList){
        mLayoutInflater = LayoutInflater.from(context);
        mArrayList = arrayList;
    }

    public void setData(ArrayList<FileInfo> arrayList){
        mArrayList = arrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = mLayoutInflater.inflate(R.layout.video_listview_content,null);
        MyViewHolder mViewHolder = new MyViewHolder(view);
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final MyViewHolder video= (MyViewHolder) viewHolder;
        mFileInfo = mArrayList.get(i);
        if (mFileInfo.isFile){
            video.videoPic.setImageBitmap(mFileInfo.bitmap);
            video.videoName.setText(mFileInfo.name);
        }else {
            video.videoPic.setImageResource(R.drawable.folder);
            video.videoName.setText(mFileInfo.name);
        }


    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    private class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView videoPic;
        TextView videoName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            videoPic = itemView.findViewById(R.id.icon);
            videoName = itemView.findViewById(R.id.name);
        }


    }
}
