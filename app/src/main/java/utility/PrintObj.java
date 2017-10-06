package utility;

import android.util.Log;

import com.google.gson.Gson;

/**
 * Created by wei on 10/5/17.
 */

public class PrintObj {
    private final String TAG = "print";
    public void printObj(Object object){
        Gson gson = new Gson();
        Log.d(TAG,gson.toJson(object));

    }
}
