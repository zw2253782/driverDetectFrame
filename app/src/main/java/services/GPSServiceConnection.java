package services;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import database.DatabaseHelperSensor;


public class GPSServiceConnection implements ServiceConnection {

    private GPSService.GPSBinder _service = null;
    private DatabaseHelperSensor _dbHelper = null;

    public GPSServiceConnection(DatabaseHelperSensor dbhelper) {
        _dbHelper = dbhelper;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        _service = (GPSService.GPSBinder) service;
        _service.setDatabaseHelper(_dbHelper);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        _service = null;
    }


    public void setDatabaseHelper(DatabaseHelperSensor dbhelper) {
        _dbHelper = dbhelper;
    }

    public double getLatitude() {
        return _service.getLatitude();
    }
    public double getLongitude() {
        return _service.getLontitude();
    }
    public double getSpeed() {
        return _service.getSpeed();
    }

}
