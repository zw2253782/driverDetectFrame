package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.security.acl.LastOwnerException;

import utility.Constants;
import utility.FrameData;

/**
 * Created by wei on 3/15/18.
 */


public class DatabaseHelperSensor extends SQLiteOpenHelper {
    // Logcat tag
    private static final String TAG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;
    /*
       // Database Name
       private static final String DATABASE_NAME = "obd.db";
    */
    // Table Names
    private static final String TABLE_RECORDS = "records";


    private static final String TABLE_GPS = "gps";
    private static final String TABLE_ACCELEROMETER = "accelerometer";
    private static final String TABLE_GYROSCOPE = "gyroscope";
    private static final String TABLE_MAGNETIC = "magnetic";
    private static final String TABLE_ORIENTATION = "orientation";
    private static final String TABLE_EVENT = "events";
    private static final String SENSOR_COLS[] = {"x", "y", "z"};
    private static final String GPS_COLS[] = {"lat", "lon", "speed"};
    private static final String EVENT_COLS[] = {"start", "end", "type"};

    private static final String KEY_TIME = "time";
    private static long starttime = 0;
    /*rotation matrix*/
    private static final String RECORD_COLS[] = {"lat", "lon", "speed", "totalpassenger", "onpassenger", "offpassenger", "onstreet", "atstop"};


    // Table Create Statements
    // Todo table create statement
    private static final String CREATE_TABLE_ACCELEROMETER = "CREATE TABLE "
            + TABLE_ACCELEROMETER + "(" + KEY_TIME + " INTEGER PRIMARY KEY," + SENSOR_COLS[0]
            + " REAL," + SENSOR_COLS[1] + " REAL," +  SENSOR_COLS[2] + " REAL" + ")";
    private static final String CREATE_TABLE_GYROSCOPE = "CREATE TABLE "
            + TABLE_GYROSCOPE + "(" + KEY_TIME + " INTEGER PRIMARY KEY," + SENSOR_COLS[0]
            + " REAL," + SENSOR_COLS[1] + " REAL," +  SENSOR_COLS[2] + " REAL" + ")";
    private static final String CREATE_TABLE_ORIENTATION = "CREATE TABLE "
            + TABLE_ORIENTATION+ "(" + KEY_TIME + " INTEGER PRIMARY KEY," + SENSOR_COLS[0]
            + " REAL," + SENSOR_COLS[1] + " REAL," +  SENSOR_COLS[2] + " REAL" + ")";
    private static final String CREATE_TABLE_MAGNETIC = "CREATE TABLE "
            + TABLE_MAGNETIC + "(" + KEY_TIME + " INTEGER PRIMARY KEY," + SENSOR_COLS[0]
            + " REAL," + SENSOR_COLS[1] + " REAL," +  SENSOR_COLS[2] + " REAL" + ")";
    private static final String CREATE_TABLE_GPS = "CREATE TABLE "
            + TABLE_GPS + "(" + KEY_TIME + " INTEGER PRIMARY KEY," + GPS_COLS[0]
            + " REAL," + GPS_COLS[1] + " REAL," + GPS_COLS[2] + " REAL" + ")";
    private static final String CREATE_TABLE_EVENT = "CREATE TABLE "
            + TABLE_EVENT + "(" + KEY_TIME + " INTEGER PRIMARY KEY," + EVENT_COLS[0]
            + " INTEGER," + EVENT_COLS[1] + " INTEGER," + EVENT_COLS[2] + " STRING" + ")";

    public DatabaseHelperSensor(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        starttime = Long.parseLong(name.substring(0,name.length()-3));

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //db = SQLiteDatabase.openOrCreateDatabase(Constants.sensorDBFolder + String.valueOf(starttime).concat(".db"), null, null);
        Log.d(TAG,"creat database");

        db.execSQL(CREATE_TABLE_ACCELEROMETER);
        db.execSQL(CREATE_TABLE_GYROSCOPE);
        db.execSQL(CREATE_TABLE_MAGNETIC);
        db.execSQL(CREATE_TABLE_ORIENTATION);
        db.execSQL(CREATE_TABLE_GPS);
        db.execSQL(CREATE_TABLE_EVENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCELEROMETER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GYROSCOPE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAGNETIC);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORIENTATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT);
        // create new tables
        onCreate(db);
    }

    public void addGPSData(long time, double latitude, double longitude, double speed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TIME, time);
        values.put(GPS_COLS[0], latitude);
        values.put(GPS_COLS[1], longitude);
        values.put(GPS_COLS[2], speed);
        db.insert(TABLE_GPS, null, values);
        db.close();
    }

    public void addAcceData(long time, double x, double y, double z) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TIME, time);
        values.put(SENSOR_COLS[0], x);
        values.put(SENSOR_COLS[1], y);
        values.put(SENSOR_COLS[2], z);
        db.insert(TABLE_ACCELEROMETER, null, values);
        db.close();
    }

    public void addGyroData(long time, double x, double y, double z) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TIME, time);
        values.put(SENSOR_COLS[0], x);
        values.put(SENSOR_COLS[1], y);
        values.put(SENSOR_COLS[2], z);
        db.insert(TABLE_GYROSCOPE, null, values);
        db.close();
    }

    public void addMagData(long time, double x, double y, double z) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TIME, time);
        values.put(SENSOR_COLS[0], x);
        values.put(SENSOR_COLS[1], y);
        values.put(SENSOR_COLS[2], z);
        db.insert(TABLE_MAGNETIC, null, values);
        db.close();
    }

    public void addOrientData(long time, double x, double y, double z) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TIME, time);
        values.put(SENSOR_COLS[0], x);
        values.put(SENSOR_COLS[1], y);
        values.put(SENSOR_COLS[2], z);
        db.insert(TABLE_ORIENTATION, null, values);
        db.close();
    }

    public void addEventData(long time, long start, long end, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TIME, time);
        values.put(EVENT_COLS[0], start);
        values.put(EVENT_COLS[1], end);
        values.put(EVENT_COLS[2], type);
        db.insert(TABLE_EVENT, null, values);

        //zw
        /*FrameData frameData = new FrameData();
        frameData.eventType = type;
        frameData.eventStart = start;
        frameData.eventEnd = end;*/
        //
        db.close();
    }

    //exporting database to sd card
    public static boolean exportDB(String path, String dbname) {

        try {

            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            File dir = new File(path);
            if (!dir.exists()){
                if (!dir.mkdir()){
                    // directory creation failed
                    return false;
                }
            }
            if (sd.canWrite()) {
                String  currentDBPath= "//data//" + "selfdriving.streaming"
                        + "//databases//" + dbname;

                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(dir, dbname);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                return true;
            }
        } catch (Exception e) {
            Log.e("DbHelper", "Error in exporting database : " + e.getMessage());
        }
        return false;
    }


    public Long getStarttime(){
        return starttime;
    }

}
