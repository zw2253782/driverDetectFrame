
package udpService;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import utility.FrameData;
import utility.Trace;

public class UDPService extends Service implements Runnable {

    private static final String TAG = "udpService";
    private final Binder binder_ = new UDPService.UDPBinder();
    public DatagramSocket localSocket = null;
    public InetAddress remoteIPAddress = null;
    public int remotePort = 5000;
    public int localPort = 55555;
    //this IP need to be changed if you change your WiFi connection
    String remoteIPName = "";
    private Boolean UDPThreadRunning = null;
    long preTime = System.currentTimeMillis()+500;
    long secondLatency = 0;


    public class UDPBinder extends Binder {
        public UDPService getService() {
            return UDPService.this;
        }
        public void sendData(FrameData data, InetAddress remoteIPAddress, int remotePort){
            send(data, remoteIPAddress, remotePort);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");
        return binder_;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        startService();
        return START_STICKY;
    }

    WifiManager wifiManager;
    WifiManager.WifiLock lockHigh;

    private void startService() {

        wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        lockHigh = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "HIGH_WIFI");
        lockHigh.acquire();

        Log.d(TAG,"Start UDP server");
        try {
            localSocket = new DatagramSocket(localPort);
            remoteIPAddress = InetAddress.getByName(remoteIPName);
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        (new Thread(this)).start();
    }



    public void onDestroy() {
        Log.d(TAG,"udpserver connection is closed");
        stopSelf();
        UDPThreadRunning = false;

        lockHigh.release();
    }


    public void run() {
        // TODO Auto-generated method stub
        Log.d(TAG, "start receiving thread");
        byte[] receiveData = new byte[65555];
        UDPThreadRunning = true;
        while (UDPThreadRunning.booleanValue()) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                localSocket.receive(receivePacket);
                String sentence = new String(receiveData, 0, receivePacket.getLength());

                //get UDPClient ip and port
                remoteIPAddress = receivePacket.getAddress();
                remotePort = receivePacket.getPort();

                //Log.d(TAG,"received value is: "+ sentence);
                if (sentence.length()!=0) {
                    dataProcess(sentence);
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void dataProcess(String data){
        long timeStamp = 0;
        long sequenceNo = 0;
        long oraginalSize = 0;
        boolean isIFrame = false;
        long PCtime = 0;
        long comDataSize = 0;
        long PCReceivedDataSize = 0;

        try {
            JSONObject obj = new JSONObject(data);
            timeStamp = Long.parseLong(obj.getString("timeStamp_"));
            sequenceNo = Long.parseLong(obj.getString("SequenceNo_"));
            oraginalSize = Long.parseLong(obj.getString("originalDataSize_"));
            PCtime = Long.parseLong(obj.getString("PCtime_"));
            comDataSize = Long.parseLong(obj.getString("comDataSize_"));
            PCReceivedDataSize = Long.parseLong(obj.getString("PCReceivedDataSize_"));
            isIFrame = Boolean.parseBoolean(obj.getString("isIFrame_"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        long roundLatency = System.currentTimeMillis() - timeStamp;
        //Log.d(TAG,"roundLatency is: " + String.valueOf(roundLatency));

        Trace trace = new Trace();
        trace.time = System.currentTimeMillis();
        trace.videoSendTime = timeStamp;
        trace.sequenceNo = sequenceNo;
        trace.roundLatency = roundLatency;
        trace.oraginalSize = oraginalSize;
        trace.PCtime = PCtime;
        trace.comDataSize = comDataSize;
        trace.PCReceivedDataSize = PCReceivedDataSize;
        trace.type = Trace.LATENCY;
        trace.isIFrame = isIFrame;
        sendTrace(trace);
    }



    //send data back to UDPClient
    public void send(FrameData sendData, InetAddress remoteIPAddress, int remotePort) {
        Gson gson = new Gson();
        DatagramPacket sendPacket = new DatagramPacket(gson.toJson(sendData).getBytes(), gson.toJson(sendData).getBytes().length, remoteIPAddress, remotePort);
        //Log.d(TAG,"gson.toJson(sendData) " + gson.toJson(sendData).toString());
        try {
            localSocket.send(sendPacket);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    private void sendTrace(Trace trace) {
        //Log.d(TAG, trace.toJson());
        Intent intent = new Intent("udp");
        intent.putExtra("latency", trace.toJson());

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
