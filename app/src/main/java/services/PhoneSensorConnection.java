package services;

/**
 * Created by bozhao on 4/30/17.
 */
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import database.DatabaseHelperSensor;

public class PhoneSensorConnection implements ServiceConnection {
    private PhoneSensorService.SensorBinder _service = null;
    private DatabaseHelperSensor _dbHelper = null;

    public PhoneSensorConnection(DatabaseHelperSensor dbhelper) {
        _dbHelper = dbhelper;
    }
    @Override
    public void onServiceConnected(ComponentName arg0, IBinder binder) {
        // TODO Auto-generated method stub
        _service = (PhoneSensorService.SensorBinder) binder;
        _service.setDatabaseHelper(_dbHelper);
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        // TODO Auto-generated method stub
        _service = null;
    }

    /**
     * Sets a callback in the service.
     *
     * @param listener
     */
    public void setDatabaseHelper(DatabaseHelperSensor dbhelper) {
        _dbHelper = dbhelper;
    }

    public boolean isRunning() {
        if (_service == null) {
            return false;
        }

        return _service.isRunning();
    }
}
