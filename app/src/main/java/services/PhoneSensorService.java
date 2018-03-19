package services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.util.concurrent.atomic.AtomicBoolean;

import database.DatabaseHelperSensor;
import utility.TraceSensor;

/**
 * Created by bozhao on 4/30/17.
 */

public class PhoneSensorService extends Service implements SensorEventListener {
    private DatabaseHelperSensor _dbHelper = null;

    private final Binder _binder = new SensorBinder();
    private AtomicBoolean _isRunning = new AtomicBoolean(false);

    private SensorManager sensorManager;
    private WindowManager mWindowManager;

    int numberOfSensors = 4;
    int[] sensorType = {Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE, Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_ROTATION_VECTOR
    };

    /*Marked: for orientation*/
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];


    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private float[] mR = new float[9];

    private long tLastGyroscope = 0;
    private long tLastAccelerometer = 0;
    private long tLastMagnetic = 0;
    private long tLastRotation = 0;
    private long starttime = 0;

    private final String TAG = "Sensor Service";


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
       if(_isRunning.get()==false || _dbHelper == null) {
            return;
        }
        int type = event.sensor.getType();
        long time = System.currentTimeMillis();
        if(type== Sensor.TYPE_MAGNETIC_FIELD && (time - tLastMagnetic) >= 80) {
            tLastMagnetic = time;
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
            TraceSensor tmp = new TraceSensor(3);
            tmp.type = TraceSensor.MAGNETOMETER;
            tmp.time = time - starttime;
            tmp.values[0] = event.values[0];
            tmp.values[1] = event.values[1];
            tmp.values[2] = event.values[2];
            if(tmp.time < 11000) {
                sendTrace(tmp);
            }
            _dbHelper.addMagData(time, event.values[0], event.values[1], event.values[2]);
        } else if(type== Sensor.TYPE_ACCELEROMETER && (time - tLastAccelerometer) >= 90) {
            tLastAccelerometer = time;
            mLastAccelerometerSet = true;
            TraceSensor tmp = new TraceSensor(3);
            tmp.time = time - starttime;
            tmp.type = TraceSensor.ACCELEROMETER;
            tmp.values[0] = event.values[0];
            tmp.values[1] = event.values[1];
            tmp.values[2] = event.values[2];
            if(tmp.time < 11000) {
                sendTrace(tmp);
            }
            _dbHelper.addAcceData(time, event.values[0], event.values[1], event.values[2]);
        } else if (type == Sensor.TYPE_GYROSCOPE && (time - tLastGyroscope) >= 90) {
            tLastGyroscope = time;
            TraceSensor tmp = new TraceSensor(3);
            tmp.type = TraceSensor.GYROSCOPE;
            tmp.time = time - starttime;
            tmp.values[0] = event.values[0];
            tmp.values[1] = event.values[1];
            tmp.values[2] = event.values[2];
            Log.i(TAG, String.valueOf(tmp.time));
            sendTrace(tmp);
            _dbHelper.addGyroData(time, event.values[0], event.values[1], event.values[2]);

        } else if (type == Sensor.TYPE_ROTATION_VECTOR && (time - tLastRotation) >= 80) {
            tLastRotation = time;
            float[] values = updateOrientation(event.values);
            TraceSensor tmp = new TraceSensor(3);
            tmp.type = TraceSensor.ORIENTATION;
            tmp.time = time - starttime;
            tmp.values[0] = values[0];
            tmp.values[1] = values[1];
            tmp.values[2] = values[2];
            sendTrace(tmp);
            _dbHelper.addOrientData(time, values[0], values[1], values[2]);

        } else {

        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return _binder;
    }

    public class SensorBinder extends Binder {
        public void setDatabaseHelper(DatabaseHelperSensor dbhelper) {
            _dbHelper = dbhelper;
            starttime = _dbHelper.getStarttime();

        }
        public boolean isRunning() {
            return _isRunning.get();
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        startService();
        return START_STICKY;
    }

    public void onDestroy() {
        Log.d(TAG, "stop service");
        sensorManager.unregisterListener(this);
        _isRunning.set(false);
        _dbHelper = null;
        starttime = 0;
        stopSelf();
    }

    private void startService() {
        Log.d(TAG, "start service");
        _isRunning.set(true);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        for(int i = 0; i < numberOfSensors; ++i) {
            Sensor sensor = sensorManager.getDefaultSensor(sensorType[i]);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        _isRunning.set(true);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private float[] updateOrientation(float[] rotationVector) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);

        final int worldAxisForDeviceAxisX;
        final int worldAxisForDeviceAxisY;

        // Remap the axes as if the device screen was the instrument panel,
        // and adjust the rotation matrix for the device orientation.
        switch (mWindowManager.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
            default:
                worldAxisForDeviceAxisX = SensorManager.AXIS_X;
                worldAxisForDeviceAxisY = SensorManager.AXIS_Z;
                break;
            case Surface.ROTATION_90:
                worldAxisForDeviceAxisX = SensorManager.AXIS_Z;
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X;
                break;
            case Surface.ROTATION_180:
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X;
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Z;
                break;
            case Surface.ROTATION_270:
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Z;
                worldAxisForDeviceAxisY = SensorManager.AXIS_X;
                break;
        }

        float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX,
                worldAxisForDeviceAxisY, adjustedRotationMatrix);

        // Transform rotation matrix into azimuth/pitch/roll
        float[] orientation = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);

        // Convert radians to degrees
        float pitch = orientation[1] * (float) -57.2958;
        float roll = orientation[2] * (float) -57.2958;
        float yaw = orientation[0] * (float) -57.2958;
        float[] values = new float[9];
        values[0] = orientation[0] * (float) -57.2958; // yaw
        values[1] = orientation[1] * (float) -57.2958; // pitch
        values[2] = orientation[2] * (float) -57.2958; //roll
        //Log.i("debug", "pitch: " + pitch + " roll: " + roll + " yaw: " + yaw);
       // Log.i("debug", "pitch: " + orientation[1] + " roll: " + orientation[2] + " yaw: " + orientation[0]);
        return values;
    }

    private void sendTrace(TraceSensor trace) {
        Intent intent = new Intent("sensor");
        intent.putExtra("trace", trace);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        //Log.i(TAG, "Sending trace");
    }
}
