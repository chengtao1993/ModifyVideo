package com.archermind.newvideo;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_SETTINGS,
            Manifest.permission.MANAGE_DOCUMENTS,
    };
    private TextView noFile;
    private ListView dataDisplay;
    private ArrayList<FileInfo> dataListView;
    private VideoAdapter mVideoAdapter;
    private String videocontrol;
    private BroadcastReceiver usb_out;
    public  static int currentPosition =0;
    private Intent intent;
    public static String current_source_name = "本地";
    public static String current_source_path = "external";
    public static HashMap<String,String> name_path = new HashMap();
    private ArrayList<FileInfo> dataList;
    private ArrayList<FileInfo> data;
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
    private LayoutInflater mInflate;
    private ImageView playOrPause;




    public VideoPlayController mVideoPlayController;

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
        requestPermissions(PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
        checkRequiredPermission(this);
        mInflate = getLayoutInflater();
        initViews();
        intent = getIntent();
        IntentFilter filter = new IntentFilter("com.archermind.media.USBOUT");
        usb_out = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (name_path.containsKey(intent.getStringExtra("name"))){
                    name_path.remove(intent.getStringExtra("name"));
                    Map.Entry<String,String> entry = name_path.entrySet().iterator().next();
                    current_source_name = entry.getKey();
                    current_source_path = entry.getValue();
                }

            }
        };
        registerReceiver(usb_out,filter);
        if (savedInstanceState != null) {

        }
    }



    private void initViews(){
        playOrPause = findViewById(R.id.playOrPause);
        dataListView = VideoUtils.getDataOrderByTime(this,current_source_path);
        dataDisplay = findViewById(R.id.dataDisplay);
        mVideoAdapter = new VideoAdapter(this,dataListView);
        dataDisplay.setAdapter(mVideoAdapter);
        dataDisplay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("hct","item_click = "+i);
                if (dataListView.get(i).isFile){
                    Log.d("hct","1111");
                    currentPosition = i;
                    setData(dataList,currentPosition );
                    startPlaying();
                }else {
                    Log.d("hct","2222");
                    dataListView = VideoUtils.scanLocalFile(dataListView.get(i).path);
                    if (dataListView.size() == 0){
                        Log.d("hct","2aaaaa");
                        Toast.makeText(MainActivity.this,"aaaaa",Toast.LENGTH_SHORT).show();
                        noFile = findViewById(R.id.noFile);
                        noFile.setVisibility(View.VISIBLE);
                    }else {
                        Log.d("hct","2bbb");
                        mVideoAdapter.setData(dataListView);
                        mVideoAdapter.notifyDataSetChanged();
                    }
                }
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

            if(localFileId > 0 && localFileId < data.size()-1) {

            }else if(localFileId == 0){

                if(data.size() > 1){

                }

            }else if(localFileId == data.size()-1){

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
        try {
            dataList = VideoUtils.getDataOrderByTime(this,current_source_path);
        }catch (Exception e){
            Log.i("ccc","---MediaActivity---"+e);
        }
        IntentActionPlay();
        setData(dataList,currentPosition);
        startPlaying();
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



    private void playlast(){
        fastJump.removeMessages(2);
        for (int i = localFileId - 1; i <= data.size();i--){
            if (i == -1){
                i = data.size();
                continue;
            }
            if(data.get(i).isFile){
                mFileInfo = data.get(i);
                localFileId = i;
               currentPosition=i;
                mVideoPlayController.stop();
                if (mFileInfo.uri == null){
                    mVideoPlayController.setVideoPath(mFileInfo.path);
                }else {
                    mVideoPlayController.setVideoUri(mFileInfo.uri);
                }
                mVideoPlayController.start();
                break;
            }

        }
    }
    private void playNext(){
        fastJump.removeMessages(2);
        for (int i = localFileId + 1; i <= data.size();i++){
            if (i == data.size()){
                i = -1;
                continue;
            }
            if(data.get(i).isFile){
                mFileInfo = data.get(i);
                localFileId = i;
                currentPosition=i;
                mVideoPlayController.stop();
                if (mFileInfo.uri == null){
                    mVideoPlayController.setVideoPath(mFileInfo.path);
                }else {
                    mVideoPlayController.setVideoUri(mFileInfo.uri);
                }
                mVideoPlayController.start();
                break;
            }

        }

    }


    public void setData(ArrayList<FileInfo> arrayList,int localFileId){
        data = arrayList;
        this.localFileId = localFileId;
        mFileInfo = arrayList.get(localFileId);
        Log.i("ccc","......"+data+"----"+localFileId+"****"+mFileInfo);

    }
    private void IntentActionPlay() {
        if(getIntent() != null && getIntent().getData() != null) {

            FileInfo info = new FileInfo();
            info.isFile = true;
            info.uri = intent.getData();
            info.name = getFileName(this, info.uri);
            ArrayList<FileInfo> arrayList = new ArrayList<FileInfo>();
            arrayList.add(info);

        }
    }

    private String getFileName(final Context context, final Uri uri) {
        if (null == uri)
            return null;
        //
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme) ) {
            Cursor cursor = context.getContentResolver().query(uri, new String[] {MediaStore.Images.ImageColumns.DATA }, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1){
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        //
        if(data == null){
            data = uri.toString();
        }
        String fileName = data.substring(data.lastIndexOf("/") + 1, data.length());
        return fileName;
    }

}
