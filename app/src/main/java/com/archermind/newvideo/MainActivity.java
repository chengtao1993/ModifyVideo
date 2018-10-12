package com.archermind.newvideo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_SETTINGS,
            Manifest.permission.MANAGE_DOCUMENTS,
    };

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";



    private TextView noFile;
    private ListView dataDisplay;
    private FrameLayout videoFragmentView;
    private ArrayList<FileInfo> dataListView;
    private VideoAdapter mVideoAdapter;
    private String videocontrol;
    private String videosetting;
    private BroadcastReceiver usb_out;
    private String type;
    public  static int currentPosition =0;
    private AudioManager audioManager;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<FileInfo> videoArraryList;
    private Intent intent;
    public static String current_source_name = "本地";
    public static String current_source_path = "external";
    public static HashMap<String,String> name_path = new HashMap();
    private ArrayList<FileInfo> dataList;


    private ArrayList<FileInfo> data;
    private int localFileId;
    private FileInfo mFileInfo;
    private FrameLayout videoPlayLayout;
    private VideoView mVideoView;
    private TextView time_current;
    private TextView time_total;
    private ImageView videoplaystate;
    private boolean isPlayingComplete = false;
    private ImageView rewind;
    private ImageView last;
    private ImageView next;
    private ImageView fastforward;
    private ImageView close_video;
    private LinearLayout toastLayout;
    private ImageView toastIcon;
    private TextView toastTime;
    private SeekBar seekBar;
    private FrameLayout video_container;
    private Toast toast;
    private int num_click_rewind = 0;
    private int num_click_fastforward = 0;
    private int time_jump = 0;
    private Message message;
    private final int orientation_rewind = 1;
    private final int orientation_fastforward = 2;
    private int seekBarRatio;
    private LinearLayout volum_layout;
    private LinearLayout intensity_layout;
    private AlertDialog dialog;
    private Window dialogWindow;
    private WindowManager.LayoutParams lp;
    private VerticalSeekBar verticalSeekBar;
    private int num_intensity;
    private int num_volume;
    private Window window;
    private WindowManager.LayoutParams lp_activity;
    private int base_distance = 400;
    private int currentTime;
    private LayoutInflater mInflate;


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
                    num_click_rewind = 0;
                }
            }

        }
    };
    private GestureDetector gestureDetector;
    private GestureDetector.OnGestureListener onGestureListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            float x = e.getX();
            dialog = new AlertDialog.Builder(MainActivity.this).create();
            dialogWindow = dialog.getWindow();
            lp = dialogWindow.getAttributes();
            if (x < 540){
                num_volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
               volum_layout =(LinearLayout) mInflate.inflate(R.layout.volum_layout,null);
                verticalSeekBar = volum_layout.findViewById(R.id.volume_seekBar);
                verticalSeekBar.setProgress(100*num_volume/15);
                lp.gravity = Gravity.START | Gravity.TOP;
                lp.x = 30;
                lp.y = 380;
                dialog.setView(volum_layout);
            }else {
                num_intensity = getBrightness(MainActivity.this);
                intensity_layout = (LinearLayout)mInflate.inflate(R.layout.intensity_layout,null);
                verticalSeekBar = intensity_layout.findViewById(R.id.intensity_seekBar);
                verticalSeekBar.setProgress(num_intensity);
                lp.gravity = Gravity.END | Gravity.TOP;
                lp.x = -30;
                lp.y = 380;
                dialog.setView(intensity_layout);
            }
            currentTime = mVideoView.getCurrentPosition();
            return true;
        }


        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float x_first = e1.getX();
            float x_last = e2.getX();
            float y_first = e1.getY();
            float y_last = e2.getY();
            if (Math.abs(distanceX) > 5 && Math.abs(distanceY) < 5){
                //快进、快退
                int time;
                if (x_last > x_first){
                    time = Math.round(60*1000*(x_last - x_first)/base_distance);
                    mVideoView.seekTo(currentTime + time);
                }else {
                    time = Math.round(60*1000*(x_first - x_last)/base_distance);
                    mVideoView.seekTo(currentTime - time);
                }


            }else if (Math.abs(distanceY) > 5 && Math.abs(distanceX) < 5){
                if (x_first < 540){
                    //音量调节
                    if (!dialog.isShowing()){
                        dialog.show();
                    }
                    int num;
                    if (y_last > y_first){
                        num = 100*num_volume/15 - Math.round(100*(y_last - y_first)/base_distance);
                        if (num < 0){
                            num = 0;
                        }
                    }else {
                        num = 100*num_volume/15 + Math.round(100*(y_first - y_last)/base_distance);
                        if (num > 100){
                            num = 100;
                        }
                    }
                    verticalSeekBar.setProgress(num);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,num*15/100,AudioManager.FLAG_PLAY_SOUND);

                }else {
                    //亮度调节
                    stopAutoBrightness(MainActivity.this);
                    int num;
                    if (!dialog.isShowing()){
                        dialog.show();
                    }
                    if (y_last > y_first){
                        num = num_intensity - Math.round(100*(y_last - y_first)/base_distance);
                        if (num < 0){
                            num = 0;
                        }
                    }else {
                        num = num_intensity + Math.round(100*(y_first - y_last)/base_distance);
                        if (num > 100){
                            num = 100;
                        }
                    }
                    verticalSeekBar.setProgress(num);
                    window = getWindow();
                    lp_activity = window.getAttributes();
                    lp_activity.screenBrightness = num;
                    window.setAttributes(lp_activity);
                    saveBrightness(MainActivity.this,num);
                }
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            return false;
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
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        audioManager =(AudioManager) getSystemService(AUDIO_SERVICE);
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
            videocontrol = savedInstanceState.getString("video_control");
            videosetting = savedInstanceState.getString("video_setting");
            Log.i("ccc","videocontrol"+videocontrol);
        }
    }



    private void initViews(){
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
                        noFile = videoFragmentView.findViewById(R.id.noFile);
                        noFile.setVisibility(View.VISIBLE);
                    }else {
                        Log.d("hct","2bbb");
                        mVideoAdapter.setData(dataListView);
                        mVideoAdapter.notifyDataSetChanged();
                    }
                }
            }
        });


        volum_layout = (LinearLayout) mInflate.inflate(R.layout.volum_layout,null);
        intensity_layout = (LinearLayout) mInflate.inflate(R.layout.intensity_layout,null);
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
        toastLayout = (LinearLayout) mInflate.inflate(R.layout.toast_layout,null);
        toastIcon = toastLayout.findViewById(R.id.toastIcon);
        toastTime = toastLayout.findViewById(R.id.toastTime);

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
                videoplaystate.setSelected(true);
                message = new Message();
                message.what = 1;
                displayCurrentTime.sendMessage(message);

            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoplaystate.setSelected(false);
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

        videoplaystate = findViewById(R.id.videoplaystate);
        videoplaystate.setSelected(true);
        videoplaystate.setOnClickListener(this);

        rewind = findViewById(R.id.rewind);
        rewind.setOnClickListener(this);

        last = findViewById(R.id.last);
        last.setOnClickListener(this);

        next = findViewById(R.id.next);
        next.setOnClickListener(this);

        fastforward = findViewById(R.id.fastforward);
        fastforward.setOnClickListener(this);
        gestureDetector = new GestureDetector(this,onGestureListener);

        video_container = findViewById(R.id.video_container);

        video_container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
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
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.videoplaystate:
                Log.i("ccc","点击了");
                playOrpause(view);
                break;
            case R.id.rewind:
                if(videoplaystate.isSelected()) {
                    num_click_rewind += 1;
                    if (num_click_rewind == 1) {
                        num_click_fastforward = 0;
                        time_jump = 10 * 1000;
                        toastIcon.setImageResource(R.drawable.rewind);
                        toastTime.setText("10s");
                        toast = new Toast(this);
                        toast.setGravity(Gravity.TOP, 0, 560);
                        toast.setView(toastLayout);
                        toast.show();
                        fastJump.removeMessages(2);
                        num_click_fastforward = 0;
                        message = new Message();
                        message.what = 2;
                        message.arg1 = orientation_rewind;
                        fastJump.sendMessage(message);
                    } else if (num_click_rewind == 2) {
                        time_jump = 20 * 1000;
                        toastTime.setText("20s");
                        toast = new Toast(this);
                        toast.setGravity(Gravity.TOP, 0, 560);
                        toast.setView(toastLayout);
                        toast.show();
                    } else if (num_click_rewind == 3) {
                        time_jump = 30 * 1000;
                        toastTime.setText("30s");
                        toast = new Toast(this);
                        toast.setGravity(Gravity.TOP, 0, 560);
                        toast.setView(toastLayout);
                        toast.show();
                    } else {
                        num_click_rewind = 0;
                        time_jump = 0;
                        fastJump.removeMessages(2);
                    }
                }
                break;
            case R.id.last:
                playlast();
                break;
            case R.id.next:
                playNext();
                break;
            case R.id.fastforward:
                if (videoplaystate.isSelected()) {
                    num_click_fastforward += 1;
                    if (num_click_fastforward == 1) {
                        num_click_rewind = 0;
                        time_jump = 10 * 1000;
                        toastIcon.setImageResource(R.drawable.fastforward);
                        toastTime.setText("10s");
                        toast = new Toast(this);
                        toast.setGravity(Gravity.TOP, 0, 560);
                        toast.setView(toastLayout);
                        toast.show();
                        fastJump.removeMessages(2);
                        num_click_rewind = 0;
                        message = new Message();
                        message.what = 2;
                        message.arg1 = orientation_fastforward;
                        fastJump.sendMessage(message);
                    } else if (num_click_fastforward == 2) {
                        time_jump = 20 * 1000;
                        toastTime.setText("20s");
                        toast = new Toast(this);
                        toast.setGravity(Gravity.TOP, 0, 560);
                        toast.setView(toastLayout);
                        toast.show();
                    } else if (num_click_fastforward == 3) {
                        time_jump = 30 * 1000;
                        toastTime.setText("30s");
                        toast = new Toast(this);
                        toast.setGravity(Gravity.TOP, 0, 560);
                        toast.setView(toastLayout);
                        toast.show();
                    } else {
                        num_click_fastforward = 0;
                        time_jump = 0;
                        fastJump.removeMessages(2);
                    }
                }
                break;
        }
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
        num_click_fastforward = 0;
        num_click_rewind = 0;
        if (view.isSelected()){
            view.setSelected(false);
            mVideoPlayController.pause();
            displayCurrentTime.removeMessages(1);
        }else {
            view.setSelected(true);
            mVideoPlayController.resume();

            isPlayingComplete = false;
            message = new Message();
            message.what = 1;
            displayCurrentTime.sendMessage(message);

        }
    }

    public void pauseOrPlay(){
        if("pause".equals(message)){
            if (videoplaystate.isSelected()){
                Log.i("ccc","pause");
                videoplaystate.setSelected(false);
                mVideoPlayController.pause();

            }
        }else if("play".equals(message)){
            if(!videoplaystate.isSelected()) {
                videoplaystate.setSelected(true);
                mVideoPlayController.resume();
                isPlayingComplete = false;
                Log.i("ccc","play");
            }
        }else if("prev".equals(message)){
            playlast();
            Log.i("ccc","prev");
        }else if ("next".equals(message)){
            playNext();
            Log.i("ccc","next");
        }

    }

    private void playlast(){
        fastJump.removeMessages(2);
        num_click_fastforward = 0;
        num_click_rewind = 0;
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
        num_click_fastforward = 0;
        num_click_rewind = 0;
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



    public static int getBrightness(Activity activity) {
        int brightValue = 0;
        ContentResolver contentResolver = activity.getContentResolver();
        try {
            brightValue = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return brightValue;
    }

    public static void stopAutoBrightness(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                //有了权限，具体的动作
                Settings.System.putInt(context.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        }


    }

    public static void saveBrightness(Context context, int brightness) {

        Uri uri = android.provider.Settings.System
                .getUriFor(Settings.System.SCREEN_BRIGHTNESS);

        android.provider.Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, brightness);

        context.getContentResolver().notifyChange(uri, null);
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
