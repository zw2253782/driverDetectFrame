
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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

import utility.FrameData;
import utility.FramePacket;
import utility.JsonWraper;

public class UDPService extends Service implements Runnable {

    private static final String TAG = "UDPservices";
    private final Binder binder_ = new UDPService.UDPBinder();
    public DatagramSocket localSocket = null;
    public int localPort = 4444;
    //this IP need to be changed if you change your WiFi connection
    private Boolean UDPThreadRunning = null;


    public class UDPBinder extends Binder {
        public UDPService getService() {
            return UDPService.this;
        }
        public boolean isRunning() {
            return UDPThreadRunning;
        }
        public void sendData(FramePacket data, InetAddress remoteIPAddress, int remotePort){
            send(data, remoteIPAddress, remotePort);
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
        Log.d(TAG, "UDP Service onStartCommand");

        WifiManager wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        lockHigh = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "HIGH_WIFI");
        lockHigh.acquire();

        Log.d(TAG,"Start UDP server");
        try {
            localSocket = new DatagramSocket(localPort);
            (new Thread(this)).start();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return START_STICKY;
    }

    public void onDestroy() {
        Log.d(TAG,"udpserver connection is closed");
        stopSelf();
        UDPThreadRunning = false;

        localSocket.close();
        localSocket = null;

        lockHigh.release();
    }


    public void run() {
        // TODO Auto-generated method stub
        Log.d(TAG, "start receiving thread");
        byte[] buffer = new byte[65555];
        UDPThreadRunning = true;
        while (UDPThreadRunning.booleanValue()) {
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
             Log.d(TAG, "received data");

            try {
                localSocket.receive(receivePacket);

                String receivedData = new String(buffer, 0, receivePacket.getLength());
                //get UDPClient ip and port
                InetAddress remoteIPAddress = receivePacket.getAddress();
                int remotePort = receivePacket.getPort();

                if (receivedData.length()!=0) {
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

            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                break;
            }
        }
        Log.d(TAG, "stop UDP receiving thread");
    }

    //send data back to UDPClient
    private static int lastIndex = -1;
    private static int streamingExtraLatency = 1; // > 0
    private static Random rand = new Random();

    private static long lastTimeStamp = 0;
    private static int accumulatedSize = 0;
    public void send(FramePacket framePacket, InetAddress remoteIPAddress, int remotePort) {
        try {
            byte[] payload = framePacket.toBytePacket();
            Log.d(TAG,"send data to server");

            if (framePacket.index != lastIndex) {
                lastIndex = framePacket.index;
                Thread.sleep(rand.nextInt(streamingExtraLatency));
            }

            if (lastTimeStamp == 0) {
                lastTimeStamp = System.currentTimeMillis();
            }
            accumulatedSize += payload.length;
            long now = System.currentTimeMillis();
            if (now - lastTimeStamp > 1000) {
                //Log.d(TAG, "udp bitrate: " + accumulatedSize * 8 * 1000.0 / (now - lastTimeStamp) / 1000000.0 + "mbps");
                lastTimeStamp = now;
                accumulatedSize = 0;
            }
            DatagramPacket sendPacket = new DatagramPacket(payload, payload.length, remoteIPAddress, remotePort);
            if (localSocket != null) {
                localSocket.send(sendPacket);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
