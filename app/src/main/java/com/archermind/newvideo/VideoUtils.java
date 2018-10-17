package com.archermind.newvideo;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;



public class VideoUtils {
    public static final String localPath = Environment.getExternalStorageDirectory().getPath() + "/Movies";
    public static ArrayList<FileInfo> data = new ArrayList<FileInfo>();
    public static MediaMetadataRetriever mMediaMetadataRetriever;
    private static Cursor cursor;

    public static ArrayList scanLocalFile(String str){
        data.clear();
        File mfile = new File(str);
        File[] files = mfile.listFiles();
        if (files == null){
            //no processing
        }else {
            for (File file : files) {
                FileInfo fileInfo = new FileInfo();
                if (file.isFile()) {
                    mMediaMetadataRetriever.setDataSource(file.getPath());
                    fileInfo.bitmap = mMediaMetadataRetriever.getFrameAtTime();
                    fileInfo.isFile = true;
                    fileInfo.name = file.getName();
                    fileInfo.path = file.getPath();

                } else {
                    fileInfo.isFile = false;
                    fileInfo.name = file.getName();
                    fileInfo.path = file.getPath();

                }
                data.add(fileInfo);
            }
        }

        return data;
    }

    public static ArrayList getDataOrderByTime(Context context,String path){
        data.clear();
        if (path.equals("external")){
            cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,null,
                    MediaStore.Video.VideoColumns.DATA+" like "+"'/storage/emulated/0%'",null,MediaStore.Video.Media.DATE_ADDED);
            while (cursor.moveToNext()){
                FileInfo fileInfo = new FileInfo();
                fileInfo.isFile = true;
                fileInfo.name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                fileInfo.path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                try {
                    fileInfo.bitmap = ThumbnailUtils.createVideoThumbnail(fileInfo.path, MediaStore.Video.Thumbnails.MICRO_KIND);
                    data.add(fileInfo);
                }catch (Exception e){

                }
            }
        }else {
            cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,null,
                    MediaStore.Video.VideoColumns.DATA+" like "+"'"+path+"%'",null,MediaStore.Video.Media.DATE_ADDED);
            while (cursor.moveToNext()){
                FileInfo fileInfo = new FileInfo();
                fileInfo.isFile = true;
                fileInfo.name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                fileInfo.path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                try {
                    fileInfo.bitmap = ThumbnailUtils.createVideoThumbnail(fileInfo.path, MediaStore.Video.Thumbnails.MICRO_KIND);
                    data.add(fileInfo);
                }catch (Exception e){

                }
            }
        }
        cursor.close();
        return data;
    }

    public static ArrayList getDataOrderByName(Context context,String path){
        data.clear();
        if (path.equals("external")){
            cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,null,
                    MediaStore.Video.VideoColumns.DATA+" like "+"'/storage/emulated/0%'",null,MediaStore.Video.Media.TITLE);
            while (cursor.moveToNext()){
                FileInfo fileInfo = new FileInfo();
                fileInfo.isFile = true;
                fileInfo.name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                fileInfo.path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                try {
                    fileInfo.bitmap = ThumbnailUtils.createVideoThumbnail(fileInfo.path, MediaStore.Video.Thumbnails.MICRO_KIND);
                    data.add(fileInfo);
                }catch (Exception e){

                }
            }
        }else {
            cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,null,
                    MediaStore.Video.VideoColumns.DATA+" like "+"'"+path+"%'",null,MediaStore.Video.Media.TITLE);
            while (cursor.moveToNext()){
                FileInfo fileInfo = new FileInfo();
                fileInfo.isFile = true;
                fileInfo.name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                fileInfo.path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                try {
                    fileInfo.bitmap = ThumbnailUtils.createVideoThumbnail(fileInfo.path, MediaStore.Video.Thumbnails.MICRO_KIND);
                    data.add(fileInfo);
                }catch (Exception e){

                }
            }
        }
        cursor.close();
        return data;
    }

}
