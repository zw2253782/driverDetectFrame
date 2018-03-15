package services;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import database.DatabaseHelperSensor;

/**
 * Created by bozhao on 2/20/18.
 */

public class ActionDetectionServiceConnection implements ServiceConnection {
    private  ActionDetectionService.ActionDetectionBinder _service = null;
    private DatabaseHelperSensor _dbHelper = null;

    public ActionDetectionServiceConnection(DatabaseHelperSensor dbhelper) {
        _dbHelper = dbhelper;
    }

    @Override
    public void onServiceConnected(ComponentName arg0, IBinder binder) {
        // TODO Auto-generated method stub
        _service = (ActionDetectionService.ActionDetectionBinder) binder;
        _service.setDatabaseHelper(_dbHelper);
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        // TODO Auto-generated method stub
        _service = null;
    }

    public void setDatabaseHelper(DatabaseHelperSensor dbhelper) {
        _dbHelper = dbhelper;
    }

}
