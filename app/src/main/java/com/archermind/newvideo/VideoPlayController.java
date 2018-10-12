package com.archermind.newvideo;

import android.app.Activity;
import android.net.Uri;
import android.widget.VideoView;

import java.util.Formatter;
import java.util.Locale;

/*import android.os.SystemProperties;*/

/**
 * Created by archermind on 1/16/18.
 */

public class VideoPlayController {
    private VideoView mVideoView;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private int CurrentPosition;
    private int timeJump;
    private final int orientation_rewind = 1;
    private final int orientation_fastforward = 2;
    private int duration = 0;
    private int ratio;


    protected VideoPlayController(Activity activity, VideoView videoView){
        mVideoView = videoView;
    }

    protected void setVideoPath(String path){
        mVideoView.setVideoPath(path);
    }

    protected void setVideoUri(Uri uri){
        mVideoView.setVideoURI(uri);
    }

    protected boolean isPlaying(){
        return mVideoView.isPlaying();
    }

    protected void start(){
        mVideoView.start();
    }

    protected void pause(){
        mVideoView.pause();

    }

    protected void resume(){
        mVideoView.start();
    }

    public void stop(){
        mVideoView.stopPlayback();
    }

    protected void seekto(int time){
        mVideoView.seekTo(time);
    }

    protected int getCurrentPosition(){
        return mVideoView.getCurrentPosition();
    }

    protected int getDuration(){
        return mVideoView.getDuration();
    }

    protected String getTotalTime(){
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        duration = mVideoView.getDuration();
        int totalSeconds = duration/1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds/60)%60;
        int hours = totalSeconds/3600;
        mFormatBuilder.setLength(0);
        if(hours>0){
            return mFormatter.format("%d:%02d:%02d",hours,minutes,seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d",minutes,seconds).toString();
        }

    }

    protected int getCurrentSeekBarRatio(){
        if (duration == 0){
            duration = mVideoView.getDuration();
        }
        if (CurrentPosition == 0){
            CurrentPosition = mVideoView.getCurrentPosition();
        }
        return 100*CurrentPosition/duration;
    }

    protected String getCurrentTime(){
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        CurrentPosition = mVideoView.getCurrentPosition();
        int totalSeconds = CurrentPosition/1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds/60)%60;
        int hours = totalSeconds/3600;
        mFormatBuilder.setLength(0);
        if(hours>0){
            return mFormatter.format("%d:%02d:%02d",hours,minutes,seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d",minutes,seconds).toString();
        }
    }


    protected int jump(int orientation,int timeJump){
        this.timeJump = timeJump;
        if(duration == 0) {
            duration = mVideoView.getDuration();
        }
        CurrentPosition = mVideoView.getCurrentPosition();
        if (orientation == orientation_fastforward){
            CurrentPosition += timeJump;
            if (CurrentPosition > duration){
                CurrentPosition = duration;
            }
        }else if (orientation == orientation_rewind){
            CurrentPosition -= timeJump;
            if (CurrentPosition < 0){
                CurrentPosition = 0;
            }
        }
        mVideoView.seekTo(CurrentPosition);


        return ratio = 100*CurrentPosition/duration;

    }

}
