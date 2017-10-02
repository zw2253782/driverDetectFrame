package datafile;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by wei on 10/1/17.
 */

public class SaveWifiData {
    private static final String TAG = "SaveWifiData";

    public void saveWifiData(long sendTime, long latency, long packageSize){
        String filename = Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/Latencywifi/latency_dataSize.txt";
        Log.d(TAG,filename);

        try {
            FileOutputStream outputStream = new FileOutputStream (new File(filename), true); // true will be same as Context.MODE_APPEND
            outputStream.write((String.valueOf(sendTime)+ ","+ String.valueOf(latency)+ ","+ String.valueOf(packageSize)).getBytes());
            outputStream.write('\n');
            outputStream.close();
            Log.d(TAG,"file saved");

        } catch (Exception e) {
            Log.d(TAG,"save data error ");
            e.printStackTrace();
        }
    }

}
