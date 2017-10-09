package main;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import streaming.R;
import com.google.gson.Gson;

import database.DatabaseHelper;
import udpService.UDPServiceConnection;
import udpService.UDPService;
import sensor.SensorService;
import utility.Constants;
import utility.FrameData;
import utility.Trace;


public class MainActivity extends Activity implements SurfaceHolder.Callback, PreviewCallback {
	private final static String TAG = MainActivity.class.getSimpleName();

	private final static String SP_CAM_WIDTH = "cam_width";
	private final static String SP_CAM_HEIGHT = "cam_height";
	private final static String SP_DEST_IP = "dest_ip";
	private final static String SP_DEST_PORT = "dest_port";

	// skype frame rate 5-30
	// skype bit rate 30kbps - 950kbps
	// skype resolution 	640*480, 320*240, 160*120
	private final static int DEFAULT_FRAME_RATE = 10;
	private final static int DEFAULT_BIT_RATE = 500000;

	Camera camera;
	SurfaceHolder previewHolder;
	byte[] previewBuffer;
	boolean isStreaming = false;
	AvcEncoder encoder;

//	private String ip = "192.168.8.5";
	private String ip = "192.168.10.101";

	public InetAddress address;
	public int port = 55555;

	ArrayList<FrameData> encDataList = new ArrayList<FrameData>();
	ArrayList<Integer> encDataLengthList = new ArrayList<Integer>();


	private static Intent mSensor = null;
	public DatabaseHelper dbHelper_ = null;

	Runnable senderRun = new Runnable() {
		@Override
		public void run() {
			while (isStreaming) {
				boolean empty = false;
				FrameData frameData = null;

				synchronized (encDataList) {
					if (encDataList.size() == 0) {
						empty = true;
					} else
						frameData = encDataList.remove(0);
				}
				if (empty) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				frameData.setVideoSendTime();
				if (dbHelper_.isOpen()) {
					dbHelper_.updateFrameData(frameData);
					Log.d(TAG,"update sendtime: " + dbHelper_.updateFrameData(frameData));
				}
				//we can start 2 thread, one is with time header send to one server and get time back
				// the other thread will send without header and directly show the video.
				if (mUDPConnection != null && mUDPService != null && mUDPConnection.isRunning()) {
					mUDPConnection.sendData(frameData, address, port);
				}


				//Log.d(TAG, "send data length is: " + String.valueOf(encData.length));
			}
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.activity_main);

		/*
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if (Build.MODEL.equals("Nexus 5X")){
			//Nexus 5X's screen is reversed, ridiculous! the image sensor does not fit in corrent orientation
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		*/


		this.findViewById(R.id.btntest).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showSettingsDlg();
					}
				});

		this.findViewById(R.id.btnstart).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (isStreaming) {
							((Button) v).setText("Stream");
							stopStream();
							stopServices();
						} else {
							startStream();
							startServices();
						}
					}
				});

		SurfaceView svCameraPreview = (SurfaceView) this.findViewById(R.id.svCameraPreview);
		this.previewHolder = svCameraPreview.getHolder();
		this.previewHolder.addCallback(this);

		setupFolders();

	}

	private void startServices() {
		startUDPService();
		mSensor = new Intent(this, SensorService.class);
		startService(mSensor);
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("sensor"));
		LocalBroadcastManager.getInstance(this).registerReceiver(LMessageReceiver, new IntentFilter("udp"));

		long time = System.currentTimeMillis();
		dbHelper_ = new DatabaseHelper();
		dbHelper_.createDatabase(time);
	}

	private void setupFolders () {
		File dbDir = new File(Constants.kDBFolder);
		//File videoDir = new File(Constants.kVideoFolder);
		if (!dbDir.exists()) {
			dbDir.mkdirs();
		}
		/*if(!videoDir.exists()) {
			videoDir.mkdir();
		}*/
	}

	protected void onDestroy() {
		super.onDestroy();
		stopServices();
	}

	private void stopServices() {
		stopUDPService();
		if (mSensor!= null){
			stopService(mSensor);
			mSensor = null;
		}
		if (dbHelper_!= null) {
			dbHelper_.closeDatabase();
		}
		if (mMessageReceiver!= null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
		}
		if (LMessageReceiver!=null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(LMessageReceiver);
		}
	}

	@Override
	protected void onPause() {
		this.stopStream();
		if (encoder != null)
			encoder.close();

		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings)
			return true;
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		this.camera.addCallbackBuffer(this.previewBuffer);

		if (this.isStreaming) {
			if (this.encDataLengthList.size() > 100) {
				Log.e(TAG, "OUT OF BUFFER");
				return;
			}
			FrameData frameData = this.encoder.offerEncoder(data);
			dbHelper_.insertFrameData(frameData);
			if (frameData.getDataSize() > 0) {
				synchronized (this.encDataList) {
					this.encDataList.add(frameData);
				}
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		startCamera();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		stopCamera();
	}

	private void startStream() {

		SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);
		int width = sp.getInt(SP_CAM_WIDTH, 0);
		int height = sp.getInt(SP_CAM_HEIGHT, 0);

		this.encoder = new AvcEncoder();
		this.encoder.init(width, height, DEFAULT_FRAME_RATE, DEFAULT_BIT_RATE);

		try {
			this.address = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		sp.edit().putString(SP_DEST_IP, ip).commit();
		sp.edit().putInt(SP_DEST_PORT, port).commit();

		this.isStreaming = true;
		Thread thrd = new Thread(senderRun);
		thrd.start();

		((Button) this.findViewById(R.id.btnstart)).setText("Stop");
		this.findViewById(R.id.btntest).setEnabled(false);
	}

	private void stopStream() {
		this.isStreaming = false;

		if (this.encoder != null)
			this.encoder.close();
		this.encoder = null;

		this.findViewById(R.id.btntest).setEnabled(true);
	}

	private void startCamera() {
		SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);
		int width = sp.getInt(SP_CAM_WIDTH, 0);
		int height = sp.getInt(SP_CAM_HEIGHT, 0);
		if (width == 0) {
			Camera tmpCam = Camera.open();
			Camera.Parameters params = tmpCam.getParameters();
			final List<Size> prevSizes = params.getSupportedPreviewSizes();
			int i = prevSizes.size() - 1;
			width = prevSizes.get(i).width;
			height = prevSizes.get(i).height;
			sp.edit().putInt(SP_CAM_WIDTH, width).commit();
			sp.edit().putInt(SP_CAM_HEIGHT, height).commit();
			tmpCam.release();
			tmpCam = null;
		}

		this.previewHolder.setFixedSize(width, height);

		int stride = (int) Math.ceil(width / 16.0f) * 16;
		int cStride = (int) Math.ceil(width / 32.0f) * 16;
		final int frameSize = stride * height;
		final int qFrameSize = cStride * height / 2;

		this.previewBuffer = new byte[frameSize + qFrameSize * 2];

		try {
			camera = Camera.open();
			camera.setPreviewDisplay(this.previewHolder);
			Camera.Parameters params = camera.getParameters();
			params.setPreviewSize(width, height);
			params.setPreviewFormat(ImageFormat.YV12);
			camera.setParameters(params);
			camera.addCallbackBuffer(previewBuffer);
			camera.setPreviewCallbackWithBuffer(this);
			camera.startPreview();
		} catch (IOException e) {
			//TODO:
		} catch (RuntimeException e) {
			//TODO:
		}
	}

	private void stopCamera() {
		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}


	private void showSettingsDlg() {
		Camera.Parameters params = camera.getParameters();
		final List<Size> prevSizes = params.getSupportedPreviewSizes();
		String[] choiceStrItems = new String[prevSizes.size()];
		ArrayList<String> choiceItems = new ArrayList<String>();
		for (Size s : prevSizes) {
			choiceItems.add(s.width + "x" + s.height);
		}
		choiceItems.toArray(choiceStrItems);

		AlertDialog.Builder dlgBld = new AlertDialog.Builder(this);
		dlgBld.setTitle(R.string.app_name);
		dlgBld.setSingleChoiceItems(choiceStrItems, 0, null);
		dlgBld.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int pos = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
						Size s = prevSizes.get(pos);
						SharedPreferences sp = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
						sp.edit().putInt(SP_CAM_WIDTH, s.width).commit();
						sp.edit().putInt(SP_CAM_HEIGHT, s.height).commit();

						stopCamera();
						startCamera();
					}
				});
		dlgBld.setNegativeButton(android.R.string.cancel, null);
		dlgBld.show();
	}

	//initial UDPConnetion
	private static Intent mUDPService = null;
	private static UDPServiceConnection mUDPConnection = null;

	private void startUDPService() {
		Log.d(TAG, "startUDPService");
		mUDPService = new Intent(this, UDPService.class);
		mUDPConnection = new UDPServiceConnection();
		bindService(mUDPService, mUDPConnection, Context.BIND_AUTO_CREATE);
		startService(mUDPService);
	}

	private void stopUDPService() {
		if (mUDPService != null && mUDPConnection != null) {
			unbindService(mUDPConnection);
			stopService(mUDPService);
			mUDPService = null;
			mUDPConnection = null;
		}
	}


	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String message = intent.getStringExtra("trace");
			Trace trace = new Trace();
			trace.fromJson(message);

			if (dbHelper_.isOpen()) {
				dbHelper_.insertSensorData(trace);
			}
		}

	};

	private BroadcastReceiver LMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String message = intent.getStringExtra("latency");

			Gson gson = new Gson();
			FrameData frameData = gson.fromJson(message, FrameData.class);

			if (dbHelper_.isOpen()) {
				dbHelper_.updateFrameData(frameData);
				Log.d(TAG,"updateFrameData " + dbHelper_.updateFrameData(frameData));
			}
		}

	};

}
