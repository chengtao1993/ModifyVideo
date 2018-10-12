package com.archermind.newvideo;

import android.app.Service;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.widget.Toast;

public class UsbListenerService extends Service {
    private UsbManager usbManager;
    public UsbListenerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        String str = intent.getStringExtra("usb");
        if (usbManager == null){
            usbManager = (UsbManager) getSystemService(USB_SERVICE);
        }
        if (("in").equals(str)){
            Toast.makeText(this,"in",Toast.LENGTH_SHORT).show();
        }else if (("out").equals(str)){
            Toast.makeText(this,"out",Toast.LENGTH_SHORT).show();
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
