package com.archermind.newvideo;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_SETTINGS,
            Manifest.permission.MANAGE_DOCUMENTS,
    };
    private ArrayList<FileInfo> dataListView;
    private VideoAdapter mVideoAdapter;
    public  static int currentPosition = -1;
    private Intent intent;
    public static String current_source_path = "external";
    private int localFileId;
    private FileInfo mFileInfo;
    private VideoView mVideoView;
    private TextView time_current;
    private TextView time_total;
    private boolean isPlayingComplete = false;
    private SeekBar seekBar;
    private FrameLayout video_container;
    private int time_jump = 0;
    private Message message;
    private final int orientation_rewind = 1;
    private final int orientation_fastforward = 2;
    private int seekBarRatio;
    private ImageView playOrPause;
    private GridView videoList;
    private TextView videoName;
    private Context mContext;
    private final static int LOAD_COMPLETE_MESSAGE = 1;
    private ProgressBar mProgressbar;


    public VideoPlayController mVideoPlayController;

    private Handler loadVideoList = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == LOAD_COMPLETE_MESSAGE) {
                mVideoAdapter = new VideoAdapter(mContext,dataListView);
                videoList.setAdapter(mVideoAdapter);
                mProgressbar.setVisibility(View.GONE);
                if(dataListView.size() == 0){
                    Toast.makeText(mContext,"未找到视频",Toast.LENGTH_LONG).show();
                }else
                Toast.makeText(mContext,"加载完毕",Toast.LENGTH_LONG).show();

            }
        }
    };

    private Handler displayCurrentTime = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            time_current.setText(mVideoPlayController.getCurrentTime());
            seekBar.setProgress(mVideoPlayController.getCurrentSeekBarRatio());
            if (!isPlayingComplete) {
                message = new Message();
                message.what = 1;
                sendMessageDelayed(message, 1000);
            }
        }
    };

    private Handler fastJump = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.arg1 == orientation_fastforward){
                seekBarRatio = mVideoPlayController.jump(orientation_fastforward,time_jump);
                message = new Message();
                message.what = 2;
                message.arg1 = orientation_fastforward;
                sendMessageDelayed(message,1000);
            }else if (msg.arg1 == orientation_rewind){
                seekBarRatio = mVideoPlayController.jump(orientation_rewind,time_jump);
                if (seekBarRatio != 0) {
                    message = new Message();
                    message.what = 2;
                    message.arg1 = orientation_rewind;
                    sendMessageDelayed(message, 1000);
                }else {

                }
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        //requestPermissions(PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
        checkRequiredPermission(this);
        initViews();
        intent = getIntent();
    }



    private void initViews(){
        mProgressbar = findViewById(R.id.progressbar);
        videoName = findViewById(R.id.video_name);
        videoList = findViewById(R.id.videoList);
        playOrPause = findViewById(R.id.playOrPause);
        new Thread(new Runnable() {
            @Override
            public void run() {
                dataListView = VideoUtils.getDataOrderByTime(mContext,current_source_path);
                loadVideoList.sendEmptyMessage(LOAD_COMPLETE_MESSAGE);
            }
        }).start();
       videoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (dataListView.get(i).isFile){
                    currentPosition = i;
                    setData(dataListView,currentPosition );
                    startPlaying();
                }else {
                    dataListView = VideoUtils.scanLocalFile(dataListView.get(i).path);
                    if (dataListView.size() == 0){
                    }else {
                        mVideoAdapter.setData(dataListView);
                    }
                }
                mVideoAdapter.notifyDataSetChanged();
            }
        });



        seekBar = findViewById(R.id.seek);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mVideoView.seekTo(progress * mVideoView.getDuration() / 100);
                    time_current.setText(mVideoPlayController.getCurrentTime());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



        mVideoView = findViewById(R.id.videoView);
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                currentPosition = -1;
                return true;
            }
        });
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                time_total.setText(mVideoPlayController.getTotalTime());
                isPlayingComplete = false;

                message = new Message();
                message.what = 1;
                displayCurrentTime.sendMessage(message);

            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                isPlayingComplete = true;

            }
        });


        mVideoPlayController = new VideoPlayController(this, mVideoView);
        if(mFileInfo != null){
            if (mFileInfo.uri == null){
                mVideoPlayController.setVideoPath(mFileInfo.path);
            }else {
                mVideoPlayController.setVideoUri(mFileInfo.uri);
            }
            mVideoPlayController.start();

            if(localFileId > 0 && localFileId < dataListView.size()-1) {

            }else if(localFileId == 0){

                if(dataListView.size() > 1){

                }

            }else if(localFileId == dataListView.size()-1){

            }
        }
        time_current = findViewById(R.id.time_current);
        time_total = findViewById(R.id.time_total);




        video_container = findViewById(R.id.video_container);
        video_container.setOnTouchListener(new View.OnTouchListener() {
            int	count = 0;

            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                Log.d("hct","count = "+count);

                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    Log.d("hct","ACTION_DOWN");
                    if (count == 0) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (count == 2) {
                                   playOrpause(v);
                                } else if (count == 1) { // 单击
                                    Log.d("hct", "111111111111");
                                }
                                count = 0;
                            }
                        }, 600);
                    }
                    return true;
                }

                if (MotionEvent.ACTION_UP == event.getAction()) {
                    Log.d("hct","ACTION_UP");
                    count++;
                    return true;
                }
                return true;
            }

        });

        message = new Message();
        message.what = 1;
        displayCurrentTime.sendMessage(message);

    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private String[] permissionsArray=new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_SETTINGS,
            Manifest.permission.MANAGE_DOCUMENTS
    };
    private List<String> permissionList=new ArrayList<String>();
    //申请权限后的返回码
    public final int REQUEST_CODE_ASK_PERMISSIONS=1;
    private int number=0;
    private void checkRequiredPermission(Activity activity){
        for (String permission: permissionsArray) {
            if(ContextCompat.checkSelfPermission(activity,permission)!= PackageManager.PERMISSION_GRANTED){
                permissionList.add(permission);
            }
        }
        if (permissionList.size()>0) {
            number = permissionList.size();
            ActivityCompat.requestPermissions(activity, permissionList.toArray(new String[permissionList.size()]), REQUEST_CODE_ASK_PERMISSIONS);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch (requestCode){
            case REQUEST_CODE_ASK_PERMISSIONS:

                break;
            default:
                super.onRequestPermissionsResult(requestCode,permissions,grantResults);

        }
    }

    public void startPlaying(){
        videoName.setText(mFileInfo.name);
        mVideoPlayController.stop();
        mVideoPlayController.setVideoPath(mFileInfo.path);
        mVideoPlayController.start();
    }

    private void playOrpause(View view) {

        fastJump.removeMessages(2);
        if (view.isSelected()){
            view.setSelected(false);
            mVideoPlayController.pause();
            playOrPause.setVisibility(View.VISIBLE);
            displayCurrentTime.removeMessages(1);
        }else {
            view.setSelected(true);
            mVideoPlayController.resume();
            playOrPause.setVisibility(View.INVISIBLE);
            isPlayingComplete = false;
            message = new Message();
            message.what = 1;
            displayCurrentTime.sendMessage(message);

        }
    }





    public void setData(ArrayList<FileInfo> arrayList,int localFileId){
        this.localFileId = localFileId;
        if( null != arrayList && localFileId < arrayList.size()){
            mFileInfo = arrayList.get(localFileId);
        }
    }



}
