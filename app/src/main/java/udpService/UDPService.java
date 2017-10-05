
package UDPService;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import datafile.SaveWifiData;

public class UDPService extends Service implements Runnable {

    private static final String TAG = "UDPService";
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
        public void sendData(byte[] data, InetAddress remoteIPAddress, int remotePort){
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
        byte[] receiveData = new byte[1024];
        UDPThreadRunning = true;
        while (UDPThreadRunning.booleanValue()) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                localSocket.receive(receivePacket);
                String sentence = new String(receiveData, 0, receivePacket.getLength());

                //get UDPClient ip and port
                remoteIPAddress = receivePacket.getAddress();
                remotePort = receivePacket.getPort();

                //Log.d(TAG,sentence);
                if (sentence.length()!=0) {
                    calculate(sentence);
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void calculate(String data){
        long sendTime = Long.parseLong(data.substring(0,13));
        Log.d(TAG,String.valueOf(sendTime));
        long packageSize = Long.parseLong(data.substring(13));
        Log.d(TAG,String.valueOf(packageSize));
        long period = sendTime-preTime;
        Log.d(TAG,"period is " + String.valueOf(period));
        if (period <= 1000){
            secondLatency += System.currentTimeMillis() - sendTime;
        } else {
            Log.d(TAG, String.valueOf(period) + " second Latency is: "+ String.valueOf(secondLatency));
            preTime = sendTime;
        }

        long roundLatency = System.currentTimeMillis() - sendTime;
        Log.d(TAG,"round-trip "+ roundLatency);
        SaveWifiData saveWifiData = new SaveWifiData();
        saveWifiData.saveWifiData(sendTime,roundLatency,packageSize);
    }


    //send data back to UDPClient
    public void send(byte[] data, InetAddress remoteIPAddress, int remotePort) {
        //Log.d(TAG,String.valueOf(remoteIPAddress));
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, remoteIPAddress, remotePort);
        try {
            localSocket.send(sendPacket);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
