package com.dlrc.service;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
//import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.dlrc.datacollection.GPSController;
import com.dlrc.datacollection.SensorController;
import com.gpstrace.dlrc.activity.FragmentMotionSelect;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by LC127 on 2017/1/26.
 */

public class SensorService extends Service implements GPSController.GPSControllerListener, SensorController.SensorListener{

    private static final String TAG = SensorService.class.getSimpleName();
    public static boolean isTestMode = true;

    private GPSController mGPSController = null;
    private SensorController mSensorController = null;

    private Location mLocation = null;
    private float[] mAcceData = null;
    private float[] mGravData = null;
    private float[] mMagnData = new float[3];
    private float[] mGyroData = null;
    private boolean mbStart =false;


    private final byte IMU_DATA = 0;
    private final byte GPS_DATA = 1;
    private final byte TIME_AND_BATTERY = 2;
    private final byte FREQ = 3;
    private final byte MODE = 4;
    private final byte IMU_DATA_TEST = 10;
    private final byte GPS_DATA_TEST = 11;

    private DataOutputStream mOutStream = null;
    private String mAbsolutePath = null;
    private String mFileName = null;

//    static BufferedWriter gpsFile = null;
//    static BufferedWriter imuFile = null;

    private int countIMU = 0;
    private int countGPS = 0;
    private int countSatellite = 0;

    private Intent batteryStatus = null;
    private Timer mTimer = null;
//    private Timer mTimer2 = null;

    private PowerManager.WakeLock cpuWakeLock = null;

    private int serviceSecond = 0;

    private long timestamp0 = 0;
    private int imuCount = 0;
    private int samplerate = 0;
    private int downSamplerate = 20;
    private boolean isRecording = false;
    private boolean isCountSample=false;
    private float[] mAcceDataMod2 = new float[3];
    private float[] mMagnDataMod2 = new float[3];

//    private boolean startLocating = false;
    private LocalBinder _localBinder;
    private int _mode = FragmentMotionSelect.MOTION_AUTO;



    private OnEventListener _onEventLister;
    public interface OnEventListener{
        public void onEvent( long gpsCounter, long imuCounter, long elapse);
        public void onDataReady(String path, String filename);
    }
    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            batteryStatus = intent;
        }
    };


    @Override
    public IBinder onBind(Intent intent) {

        return _localBinder;
    }

    public void startCollecttingData(int mode, OnEventListener evtListener){
        Log.i(TAG, " writeToFile:StartCollecttingData");
        _onEventLister = evtListener;
        _mode = mode;
        if (mbStart) {
            return ;
        }

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        cpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "gps_service");
        cpuWakeLock.acquire();

        mFileName = getNowDate();
        mAbsolutePath = getOutputFilePath(mFileName);
        try {
            if (mAbsolutePath != null)
                mOutStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mAbsolutePath)));
//            gpsFile = new BufferedWriter(new FileWriter(getOutputFilePath(getNowDate() + "_gps")));
//            imuFile = new BufferedWriter(new FileWriter(getOutputFilePath(getNowDate() + "_imu")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//        writeData(TIME_AND_BATTERY);

//        if (intent.getBooleanExtra("imuBoxChecked", false))
        mSensorController.register();
//        if (intent.getBooleanExtra("gpsBoxChecked", false))
        mGPSController.start();


        countGPS = 0;
        countIMU = 0;
        countSatellite = 0;
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
              //  Intent intent = new Intent();
              //  intent.setAction("com.dlrc.SensorService");
             //   intent.putExtra("countGPS", countGPS);
              //  intent.putExtra("countIMU", countIMU);
              //  intent.putExtra("serviceSecond", serviceSecond);
              //  sendBroadcast(intent);
                if(_onEventLister != null)
                    _onEventLister.onEvent(countGPS,countIMU, serviceSecond);
            }
        }, 0, 1000);

//        if (!intent.getBooleanExtra("imuBoxChecked", false) && !intent.getBooleanExtra("gpsBoxChecked", false)) {
//            mTimer2.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    serviceSecond++;
//                    writeData(TIME_AND_BATTERY);
//                }
//            }, 0, 20);
//        }

        //writeData(MODE);
        mbStart = true;
    }

    public void stopCollecttingData(){
        Log.i(TAG, " writeToFile:stopCollecttingData");
        try {
            if (mOutStream != null) {
                mOutStream.close();
                mOutStream = null;
            }
            /*
            Intent intent = new Intent();
            intent.setAction("com.dlrc.SensorService.FileName");
            intent.putExtra("fileName", mFileName);
            intent.putExtra("absolutePath", mAbsolutePath);
            sendBroadcast(intent);
*/
            try {
                RandomAccessFile raFile = new RandomAccessFile(mAbsolutePath,"rw");
                raFile.writeByte(_mode);
                raFile.writeByte(samplerate);
                raFile.close();
            }catch (NullPointerException e) {
                e.printStackTrace();
            }


            if(_onEventLister!=null)
                _onEventLister.onDataReady(mAbsolutePath, mFileName);

            if (mSensorController != null)
                mSensorController.unregister();

            if (mGPSController != null)
                mGPSController.stop();

//            if (gpsFile != null) {
//                gpsFile.close();
//                gpsFile = null;
//            }
//
//            if (imuFile != null) {
//                imuFile.close();
//                imuFile = null;
//            }

            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }

//            if (mTimer2 != null) {
//                mTimer2.cancel();
//                mTimer2 = null;
//            }

//            DZip.zipAsync(mAbsolutePath, mZipFilePath, this);
        } catch (IOException e) {
            e.printStackTrace();
        }


        mbStart = false;
      //  mGPSController = null;
      //  mSensorController.setListener(null);
      //  mSensorController = null;
     //   unregisterReceiver(mBatteryReceiver);

        //cpuWakeLock.release();
        //cpuWakeLock = null;timestamp0 = 0
        timestamp0 = 0;
        imuCount = 0;
        countGPS = 0;
        isRecording = false;
        isCountSample=false;
        _onEventLister = null;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
      //  batteryStatus = registerReceiver(mBatteryReceiver, ifilter);
        mGPSController = new GPSController((LocationManager) getSystemService(Context.LOCATION_SERVICE), this);
        mSensorController = new SensorController(this);
        mSensorController.setListener(this);

//        mTimer2 = new Timer();

        // 判断GPS是否正常启动
        if (!mGPSController.isGPSEnabled()) {
            Toast.makeText(this, "请开启GPS导航...", Toast.LENGTH_SHORT).show();
            // 返回开启GPS导航设置界面
//            Intent settingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivityForResult(settingIntent, 0);
//            return;
        }
        _localBinder = new LocalBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (mbStart) {
            try {
//                imuFile.write("service restart\r\n");
//                gpsFile.write("service restart\r\n");
                mOutStream.writeChars("service restart");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Service.START_STICKY;
        }

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        cpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "gps_service");
        cpuWakeLock.acquire();

        mFileName = getNowDate();
        mAbsolutePath = getOutputFilePath(mFileName);
        try {
            if (mAbsolutePath != null)
                mOutStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mAbsolutePath)));
//            gpsFile = new BufferedWriter(new FileWriter(getOutputFilePath(getNowDate() + "_gps")));
//            imuFile = new BufferedWriter(new FileWriter(getOutputFilePath(getNowDate() + "_imu")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//        writeData(TIME_AND_BATTERY);

//        if (intent.getBooleanExtra("imuBoxChecked", false))
            mSensorController.register();
//        if (intent.getBooleanExtra("gpsBoxChecked", false))
            mGPSController.start();


        countGPS = 0;
        countIMU = 0;
        countSatellite = 0;

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                /*
                Intent intent = new Intent();
                intent.setAction("com.dlrc.SensorService");
                intent.putExtra("countGPS", countGPS);
                intent.putExtra("countIMU", countIMU);
                intent.putExtra("serviceSecond", serviceSecond);
                sendBroadcast(intent);
                */
                if(_onEventLister!=null){
                    _onEventLister.onEvent(countGPS, countIMU, serviceSecond);
                }

            }
        }, 0, 1000);

//        if (!intent.getBooleanExtra("imuBoxChecked", false) && !intent.getBooleanExtra("gpsBoxChecked", false)) {
//            mTimer2.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    serviceSecond++;
//                    writeData(TIME_AND_BATTERY);
//                }
//            }, 0, 20);
//        }

        mbStart = true;
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopCollecttingData();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        Log.d(TAG+" location lat:", mLocation.getLatitude() + ",lon:" + mLocation.getLongitude() +
                ",accuracy:"+ mLocation.getAccuracy() );
//        startLocating = true;
        if (isTestMode)
            writeData(GPS_DATA_TEST);
        else
            writeData(GPS_DATA);
        countGPS++;


    }

    @Override
    public void onGPSStatusChanged(int satellite_num) {
        countSatellite = satellite_num;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER: {// 加速 x, y,z
                mAcceData = event.values.clone();
                Log.d(TAG + "  ACC", "acc="+ mAcceData[0] + " " + mAcceData[1] + " " + mAcceData[2]);
                /*
                imuCount++;
                if (timestamp0 == 0)
                    timestamp0 = event.timestamp;
                if ((event.timestamp - timestamp0 > 1e9) && !isRecording) {
                    calSamplerate();
                    isRecording = true;
                    writeData(FREQ);
                }
                writeData(IMU_DATA);*/
//
////                Log.e("samplerate", samplerate + "");
////                float alpha = 0.8f;
////                mGravData[0] = alpha * mGravData[0] + (1 - alpha) * mAcceData[0];
////                mGravData[1] = alpha * mGravData[1] + (1 - alpha) * mAcceData[1];
////                mGravData[2] = alpha * mGravData[2] + (1 - alpha) * mAcceData[2];
//
////                long ts = System.currentTimeMillis();
////                if (ts - imuTimestamp >= samplingInterval) {
////                    Log.d("acce", mAcceData[0] + "");
////                    writeData(IMU_DATA);
////                    countIMU++;
////                    imuTimestamp = ts;
////                }
//                int modulo = imuCount % 5;
//                switch (samplerate) {
//                    case 50:
//                        if (modulo == 2) {
//                            mAcceDataMod2[0] = mAcceData[0];
//                            mAcceDataMod2[1] = mAcceData[1];
//                            mAcceDataMod2[2] = mAcceData[2];
//                            mMagnDataMod2[0] = mMagnData[0];
//                            mMagnDataMod2[1] = mMagnData[1];
//                            mMagnDataMod2[2] = mMagnData[2];
//                        } else if (modulo == 3) {
//                            mAcceData[0] = (mAcceData[0] + mAcceDataMod2[0]) / 2;
//                            mAcceData[1] = (mAcceData[1] + mAcceDataMod2[1]) / 2;
//                            mAcceData[2] = (mAcceData[2] + mAcceDataMod2[2]) / 2;
//                            mMagnData[0] = (mMagnData[0] + mMagnDataMod2[0]) / 2;
//                            mMagnData[1] = (mMagnData[1] + mMagnDataMod2[1]) / 2;
//                            mMagnData[2] = (mMagnData[2] + mMagnDataMod2[2]) / 2;
//
//                            writeData(IMU_DATA);
//                            countIMU++;
//                        } else if (modulo == 0) {
//                            writeData(IMU_DATA);
//                            countIMU++;
//                        }
//                        break;
//                    case 100:
//                        if (modulo == 0) {
//                            writeData(IMU_DATA);
//                            countIMU++;
//                        }
//                        break;
//                    default:
//                }
                break;
            }
            case Sensor.TYPE_GRAVITY: {// 重力感应器
                mGravData = event.values.clone();
                Log.d(TAG + "  Grav", mGravData[0] + " " + mGravData[1] + " " + mGravData[2]);
                break;
            }
            case Sensor.TYPE_MAGNETIC_FIELD: { //磁场感应
                mMagnData = event.values.clone();
                Log.d(TAG +"  Magn", mMagnData[0] + " " + mMagnData[1] + " " + mMagnData[2]);

                if (timestamp0 == 0) {
                    timestamp0 = event.timestamp;
                    isRecording = true;
                    isCountSample = true;
                    writeData(MODE);
                    writeData(FREQ);
                }
                if(isCountSample) {
                    imuCount++;
                    if ((event.timestamp - timestamp0 > 1e9)) {
                        calSamplerate();
                        timestamp0 = event.timestamp;
                        isCountSample=false;
/*                    isRecording = true;*/
                    }
                }



                if (isTestMode)
                    writeData(IMU_DATA_TEST);
                else
                    writeData(IMU_DATA);

                break;
            }
            case Sensor.TYPE_GYROSCOPE: { //  //陀螺仪
                mGyroData = event.values.clone();
                Log.e(TAG +" Gyro", mGyroData[0] + " " + mGyroData[1] + " " + mGyroData[2]);
                break;
      }
        }

    }


    private void writeData(byte type) {

        Log.i(TAG, " writeData:"+ type);
        if (!isRecording)
            return;

        if (mOutStream == null)
            return;


        /*
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float)scale;
*/
        int precision = 100;

        switch (type) {
            case IMU_DATA:

                if (mAcceData == null || mMagnData == null)
                    return;

//                countIMU++;
//                Log.d("sensor", mAcceData[0] + " " + mMagnData[0]);
                try {
                    Log.i(TAG, " writeToFile:IMUData");
                    mOutStream.writeByte(type);
                    mOutStream.writeShort((int) (mAcceData[0] * precision));
                    mOutStream.writeShort((int) (mAcceData[1] * precision));
                    mOutStream.writeShort((int) (mAcceData[2] * precision));
//                    mOutStream.writeFloat(mGravData[0]);
//                    mOutStream.writeFloat(mGravData[1]);
//                    mOutStream.writeFloat(mGravData[2]);
                    mOutStream.writeShort((int) (mMagnData[0] * precision));
                    mOutStream.writeShort((int) (mMagnData[1] * precision));
                    mOutStream.writeShort((int) (mMagnData[2] * precision));
                    /*
                    mOutStream.writeShort((int) (mGravData[0] * precision));
                    mOutStream.writeShort((int) (mGravData[1] * precision));
                    mOutStream.writeShort((int) (mGravData[2] * precision));
                    mOutStream.writeShort((int) (mGyroData[0] * precision));
                    mOutStream.writeShort((int) (mGyroData[1] * precision));
                    mOutStream.writeShort((int) (mGyroData[2] * precision));
                    */
                //    Log.e("writing sensor", mAcceData[0] + " " + mMagnData[0] + " " + mGravData[0] + " " + mGyroData[0]);
//                    imuFile.write(System.currentTimeMillis() + " " + batteryPct + " " + mAcceData[0] + " " + mAcceData[1] + " " +
//                            mAcceData[2] + " " + mMagnData[0] + " " + mMagnData[1] + " " + mMagnData[2] + "\r\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case GPS_DATA:
            case GPS_DATA_TEST:

                if (mLocation == null)
                    return;

                Log.d("location lat:", mLocation.getLatitude() + ",lon:" + mLocation.getLongitude() +
                        ",accuracy:"+ mLocation.getAccuracy() );
//                countGPS++;
                try {
                    Log.i(TAG, " writeToFile:GPS_DATA_TEST");
                    mOutStream.writeByte(type);
                    mOutStream.writeDouble(mLocation.getLatitude());
                    mOutStream.writeDouble(mLocation.getLongitude());
//                    gpsFile.write(System.currentTimeMillis() + " " + batteryPct + " " + mLocation.getLatitude() + " " + mLocation.getLongitude() + " " + mLocation.getAccuracy() + " " +
//                            mLocation.getSpeed() + " " + mLocation.getBearing() + "\r\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case TIME_AND_BATTERY:
                /*
                try {
  //                  mOutStream.writeLong(System.currentTimeMillis());
//                    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
//                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
//                    float batteryPct = level / (float)scale;
  //                  mOutStream.writeFloat(batteryPct);
//                    imuFile.write(System.currentTimeMillis() + " " + batteryPct + "\r\n");
//                    gpsFile.write(System.currentTimeMillis() + " " + batteryPct + "\r\n");
//                    Log.e("time and battery", System.currentTimeMillis() + " " + batteryPct);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                */
                break;
            case FREQ:
                try {
                    Log.i(TAG, " writeToFile:FREQ");
//                    mOutStream.writeByte(type);
                    mOutStream.writeByte(samplerate);
                    Log.e(TAG + "  freq", samplerate + "");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case MODE:
                try {
                    Log.i(TAG, " writeToFile:MODE");
//                    mOutStream.writeByte(type);
                    mOutStream.writeByte(_mode);
                    Log.e(TAG + " freq", samplerate + "");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case IMU_DATA_TEST:

                if (mAcceData == null || mMagnData == null || mGyroData == null)
                    return;

                try {
                    Log.i(TAG, " writeToFile:IMU_DATA_TEST");
                    mOutStream.writeByte(type);
                    mOutStream.writeFloat(mAcceData[0]);
                    mOutStream.writeFloat(mAcceData[1]);
                    mOutStream.writeFloat(mAcceData[2]);
                    mOutStream.writeFloat(mMagnData[0]);
                    mOutStream.writeFloat(mMagnData[1]);
                    mOutStream.writeFloat(mMagnData[2]);
                    mOutStream.writeFloat(mGyroData[0]);
                    mOutStream.writeFloat(mGyroData[1]);
                    mOutStream.writeFloat(mGyroData[2]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
        }

    }

    private void calSamplerate() {
        int d50 = Math.abs(imuCount - 50);
        int d100 = Math.abs(imuCount - 100);
        samplerate = d50 < d100? 50 : 100;
/*        samplerate = 50;*/
    }

    private String getOutputFilePath(String name) {
        String directoryPath = Environment.getExternalStorageDirectory().getPath() + "/TraceDataCollection";
        File directory = new File(directoryPath);
        if (!directory.exists())
            directory.mkdir();
//        String path = String.format("%s/%s_%s.bin", directoryPath, getNowDate(), name);
        String path = String.format("%s/%s.bin", directoryPath, name);
        return path;
    }

    protected String getNowDate() {
            Date dt = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            return sdf.format(dt);
    }

    public class LocalBinder extends Binder {

        public SensorService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SensorService.this;
        }
    }
}
