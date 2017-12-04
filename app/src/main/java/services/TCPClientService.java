package services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import utility.FrameData;
import utility.FramePacket;
import utility.JsonWraper;

public class TCPClientService extends Service implements Runnable {
    private static String TAG = TCPClientService.class.getSimpleName();

    private final Binder binder_ = new TCPClientService.TCPClientBinder();
    private InetAddress address = null;
    private String ip = "192.168.10.101";
    private int serverPort = 55555;
    private boolean mRun = false;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    public class TCPClientBinder extends Binder {
        public TCPClientService getService() {
            return TCPClientService.this;
        }
        public boolean isRunning() {return mRun;}
        public void sendData(FramePacket framePacket) {
            sendMessage(framePacket);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");
        return binder_;
    }

    WifiManager.WifiLock lockHigh;
    public int onStartCommand(Intent intent, int flags, int startId) {
        // wait serverip to be set

        WifiManager wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        lockHigh = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "HIGH_WIFI");
        lockHigh.acquire();

        try {
            this.address = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        (new Thread(this)).start();
        return START_STICKY;
    }

    public void onDestroy() {
        Log.d(TAG,"tcp client connection is closed");
        stopSelf();
        mRun = false;
        if (dataOutputStream != null) {
            try {
                dataOutputStream.flush();
                dataOutputStream.close();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        if (dataInputStream != null) {
            try {
                dataInputStream.close();
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }
        dataInputStream = null;
        dataOutputStream = null;
        lockHigh.release();
    }


    public void run() {
        mRun = true;
        try {
            Log.d(TAG, "C: Connecting...");
            Socket socket = new Socket(this.address, this.serverPort);

            try {
                dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                byte [] buffer = new byte[2000];
                while (mRun) {
                    int len = dataInputStream.read(buffer);
                    String receivedData = new String(buffer, 0, len);
                    Gson gson = new Gson();
                    JsonWraper jsonWraper = gson.fromJson(receivedData, JsonWraper.class);
                    if (jsonWraper.type.compareTo("frame_data_from_server") == 0) {
                        processFrameData(receivedData);
                    } else if (jsonWraper.type.compareTo("control_message_from_server") == 0) {
                        processControllerData(receivedData);
                    } else {
                        Log.d(TAG,"unknow data type: " + jsonWraper.type);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                socket.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void sendMessage(FramePacket framePacket) {
        byte [] payload = framePacket.toBytePacket();
        // Gson gson = new Gson();
        // Log.d(TAG, gson.toJson(framePacket));
        if (dataOutputStream != null) {
            try {
                dataOutputStream.write(payload);
                dataOutputStream.flush();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            Log.e(TAG, "not connected");
        }
    }

    //send received frame data to main
    private void processFrameData(String frame) {
        Gson gson = new Gson();
        FrameData frameData = gson.fromJson(frame, FrameData.class);
        frameData.roundLatency = System.currentTimeMillis() - frameData.getFrameSendTime();
        Intent intent = new Intent("udp");
        intent.putExtra("latency", gson.toJson(frameData));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    //parse controller data
    private void processControllerData(String controllerData){

        Intent intent = new Intent("control");
        intent.putExtra("control", controllerData);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
