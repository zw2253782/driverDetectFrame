package services;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.net.InetAddress;

import utility.FramePacket;

public class TCPClientServiceConnection implements ServiceConnection {

    private TCPClientService.TCPClientBinder binder = null;
    private static final String TAG = "UDPServiceConnection";


    public void onServiceConnected(ComponentName className, IBinder service) {
        Log.d(TAG, "connected");
        binder = ((TCPClientService.TCPClientBinder) service);
    }
    public void onServiceDisconnected(ComponentName className) {
        binder = null;
        Log.d(TAG, "distconnected");
    }
    public boolean isRunning() {
        if (binder == null) {
            return false;
        }
        return binder.isRunning();
    }
    public void sendData(FramePacket data) {
        if (data != null){
            binder.sendData(data);
        }
    }
}
