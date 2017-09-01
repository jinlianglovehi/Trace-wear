
package com.gpstrace.dlrc.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.dlrc.service.SensorService;
import com.dlrc.utils.DZip;
import com.gpstrace.dlrc.R;
import com.gpstrace.dlrc.bluetooth.BluetoothChatService;
import com.gpstrace.dlrc.bluetooth.BluetoothFileTransfer;
import com.gpstrace.dlrc.bluetooth.ClsUtils;
import com.gpstrace.dlrc.bluetooth.Constants;
import com.gpstrace.dlrc.tools.ProductRouteFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;

/**
 * Created by wangz on 2017/3/9.
 */

public class MainActivityAnd extends Activity implements
        //   OnStartStopListener,
        FragmentSendDiscard.OnSendDiscardListener,
        FragmentDeleteOrCancel.OnCancelDeleteListener,
        FragmentStart.OnBtnStartListener,
        FragmentStop.OnBtnStopListener,
        FragmentInfo.OnBtnInfoListener,
/*        FragmentMotionSelect.OnMotionSelectListener,*/
        DZip.DZipListener{

    private BluetoothFileTransfer _bft;
    private Handler _handler = new Handler();
    private final int WHAT_FILE_UPLOAD = 4;// 头像上传
    private final int WHAT_FILE_ERROR = 5;// 头像上传失败

    private static final String TAG = "MainAcitivy";
    private static final String SENSOR_DATA_CAPABILITY_NAME = "imu_and_gps";
    private static final String TRANSFER_FILE_PATH = "/transfer-file";
    private static final String START_ACTIVITY_PATH = "/start-activity";
    FrameLayout _topContainer = null;
    FrameLayout _bottomContainer = null;
    private Context mContext;
    private ColorStateList mColorStateList;


    private String mAbsolutePath = null;
    private String mZipFilePath = null;
    private String mFileName = null;

    private String headMessage;
    private int countGPS;
    private int countIMU;
    private int serviceSecond;

    private FileNameReceiver mFileNameReceiver = null;
    private SensorServiceReceiver SensorServiceReceiver;
    //其他
    private boolean updateMode=false;
/*    private SharedPreferences macPreferences;*/
    private Intent serviceIntent = null;

  //  private GoogleApiClient mGoogleApiClient;
  //  private String mSensorDataNodeId;

    private Handler mHandler = new Handler();
    private String _dataPath = null;
    boolean isBond=false;
    //  Fragment _fragmentStartStop;
    Fragment _currentFragment;//Luk add currentFragment for judge which one can show
    Fragment _fragmentSendDiscard;
    Fragment _fragmentUploading;
    Fragment _fragmentWalking;
    Fragment _fragmentInfo;
    Fragment _fragmentCancelDelete;
    Fragment _fragmentUploadSuccess;
    Fragment _fragmentStart;
    Fragment _fragmentStop;
    Fragment _fragmentBond;
/*    Fragment _fragmentMotionSelect;*/
    static final int GMS_INIT = 0;
    static final int GMS_CONNECT_FAILED = -1;
    static final int GMS_OK = 1;
    int _gmsStatus = GMS_INIT;

    int _btState = BluetoothChatService.STATE_NONE;

    int _motionMode = FragmentMotionSelect.MOTION_AUTO;

    String macString="";
    String nameString="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main_d);

        _topContainer = (FrameLayout) findViewById(R.id.top_container);
        _bottomContainer = (FrameLayout) findViewById(R.id.bottom_container);

        initBaseData();

        //   _fragmentStartStop = new FragmentStartStop();
        _fragmentSendDiscard = new FragmentSendDiscard();
        ((FragmentSendDiscard)(_fragmentSendDiscard)).setOnSendDiscardListener(this);

        _fragmentUploading = new FragmentUploading();
        _fragmentWalking = new FragmentWalkingAnim();

        _fragmentInfo = new FragmentInfo();
        ((FragmentInfo)_fragmentInfo).setOnBtnInfoListener(this);

        _fragmentCancelDelete = new FragmentDeleteOrCancel();
        ((FragmentDeleteOrCancel)_fragmentCancelDelete).setOnCancelDeleteListener(this);

        _fragmentUploadSuccess = new FragmentUploadSuccess();
        _fragmentStart = new FragmentStart();
        ((FragmentStart)_fragmentStart).setOnBtnStartListener(this);
        _fragmentStop = new FragmentStop();
        ((FragmentStop)_fragmentStop).setOnBtnStopListener(this);

        _fragmentBond = new FragmentBond();
/*        _fragmentMotionSelect = new FragmentMotionSelect();
        ((FragmentMotionSelect)_fragmentMotionSelect).set_onMotionSelectListener(this);*/
        SharedPreferences macPreferences = getApplicationContext().getSharedPreferences(Constants.SAVE_MAC, Context.MODE_PRIVATE);
        macString = macPreferences.getString("mac","");
        nameString = macPreferences.getString("name","");
        initPermission();
        switchBluetooth(this,true);
        if(macString.length()==0) {
            BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> devices = ba.getBondedDevices();
            BluetoothDevice destDevice = null;
            if(devices.size()==1){


                for(Iterator<BluetoothDevice> iterator = devices.iterator(); iterator.hasNext();)
                {
                    BluetoothDevice device = (BluetoothDevice)iterator.next();
                    //Log.e("wzh",device.getAddress()+ "  name="+device.getName()+" state="+device.getBondState());

                    //  if(device.getAddress().compareTo("20:08:ED:31:34:09")==0){
                    destDevice = device;
                    break;

                }
                SharedPreferences.Editor sp = getApplicationContext().getSharedPreferences(Constants.SAVE_MAC, Context.MODE_PRIVATE).edit();
                sp.putString("mac",destDevice.getAddress());
                sp.putString("name",destDevice.getName());
                sp.commit();
                isBond=true;
                macString = destDevice.getAddress();
                nameString = destDevice.getName();
            }else {
                ((FragmentInfo) _fragmentInfo).setInfo("未配对手机,\r\n请使用 Trace+ 配对");
//            ((FragmentInfo) _fragmentInfo).setInfoWithFontSize(
//                    this.getResources().getString(R.string.main_start_collect_data_btn_tip_str),
//                    16.f, mContext, mColorStateList);
                FragmentTransaction trans = getFragmentManager().beginTransaction();
                //trans.add(R.id.bottom_container, _fragmentStart);
                trans.add(R.id.bottom_container, _fragmentBond);
                trans.add(R.id.top_container, _fragmentInfo);
                trans.commitAllowingStateLoss();
                getFragmentManager().executePendingTransactions();
                _currentFragment=_fragmentBond;
                registerBondChangeReceiver();
            }


        } else {
            isBond=true;

        }
        if(isBond) {
            BluetoothDevice destDevice = null;
            headMessage = "已配对 " + nameString +"\r\n版本:"+this.getResources().getString(R.string.app_version)+",您可以:";
            Log.d("luk","macString=["+macString+"]");
            destDevice=BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macString);

            if(destDevice.getBondState()!=BluetoothDevice.BOND_BONDED) {
                try {
                    ClsUtils.createBond(destDevice.getClass(),destDevice);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(destDevice != null) {
                if (_bft != null) {
                    _bft.stop();
                }

                _bft = new BluetoothFileTransfer(MainActivityAnd.this, MainActivityAnd.this._onClientListener, BluetoothChatService.ROLE_CLIENT);
                _bft.asClient(destDevice);
            }
            ArrayList<String> files = listDataFile();
            if (files.size() == 0) {
                //"欢迎使用Track,您可以：

                ((FragmentInfo) _fragmentInfo).setInfo(headMessage);
//            ((FragmentInfo) _fragmentInfo).setInfoWithFontSize(
//                    this.getResources().getString(R.string.main_start_collect_data_btn_tip_str),
//                    16.f, mContext, mColorStateList);
                FragmentTransaction trans = getFragmentManager().beginTransaction();
                //trans.add(R.id.bottom_container, _fragmentStart);
                trans.add(R.id.bottom_container, _fragmentStart);
                trans.add(R.id.top_container, _fragmentInfo);
                trans.commitAllowingStateLoss();
                getFragmentManager().executePendingTransactions();
                _currentFragment=_fragmentStart;
            } else {
                FragmentTransaction trans = getFragmentManager().beginTransaction();

                String nameInfo = files.get(0);
                String fresultWithoutEx = nameInfo.substring(0, nameInfo.length() - 3);
                // String ext = nameInfo.substring(nameInfo.length()-3,nameInfo.length());
                if (fresultWithoutEx.length() > 13 ) {
                    String year = fresultWithoutEx.substring(0, 4);
                    String month = fresultWithoutEx.substring(4, 6);
                    String day = fresultWithoutEx.substring(6, 8);
                    String hour = fresultWithoutEx.substring(8, 10);
                    String min = fresultWithoutEx.substring(10, 12);
                    String sec = fresultWithoutEx.substring(12, 14);


                    String info = "采集时间： " + month + "-" + day + "," + hour + ":" + min + ":" + sec;
                    ((FragmentInfo) _fragmentInfo).setInfo("您有一条轨迹数据未上传,\n" + info);
                    mZipFilePath = _dataPath + "/" + files.get(0);
                    mFileName = fresultWithoutEx;
                    trans.add(R.id.bottom_container, _fragmentSendDiscard);
                    trans.add(R.id.top_container, _fragmentInfo);
                    trans.commitAllowingStateLoss();
                    getFragmentManager().executePendingTransactions();
                    _currentFragment=_fragmentSendDiscard;
                }

            }
        }






        startSensorService();
           // initService();
    }
    public void switchBluetooth(Context context,boolean b) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if(b) {
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(enableBtIntent);
                }
            } else {
                if (bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.disable();
                }
            }

        } else {
            Log.i("blueTooth", "该手机不支持蓝牙");
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,permission))
                return false;
        }
        return true;
    }
    private void initPermission() { //Manifest.permission.ACCESS_FINE_LOCATION
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("GPS");
        if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("Read SD");
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("Write SD");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
               /* showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivityAnd.this,permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return;*/
            }
            ActivityCompat.requestPermissions(MainActivityAnd.this,permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }
    }

    /**
     * @function 初始化基本数据
     */
    private void initBaseData() {
        mContext = this.getApplicationContext();
        mColorStateList = ColorStateList.valueOf(0xFFfefefe);
    }



    private ArrayList<String> listDataFile() {
        ArrayList<String> result = new ArrayList<>();
        _dataPath = Environment.getExternalStorageDirectory().getPath() + "/TraceDataCollection";

        File fp = new File(_dataPath);
        if (fp.isDirectory()) {
            File[] files = fp.listFiles();
            if (files != null && files.length > 0) {
                if (files != null && files.length > 0) {
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].isFile()) {
                            String fName = files[i].getName();
                            if (fName.endsWith(".gz") && fName.length() > 9) {

                                String fresultWithoutEx = fName.substring(0, fName.length() - 3);
                                try {
                                    long t = Long.valueOf(fresultWithoutEx);
                                } catch (NumberFormatException e) {
                                    continue;
                                }
                                result.add(fName);
                            }
                        }
                    }
                }
            }
        }
        return result;

    }

    protected void onResume() {
        super.onResume();
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
       // uninitService();
        Log.d("luk","onDestroy");
        if(_bft!=null){
            _bft.stop();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    private long pastTime;
    private int count=10;
    public void showMyToast(final Toast toast, final int cnt) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toast.show();
            }
        }, 0, 3000);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toast.cancel();
                timer.cancel();
            }
        }, cnt );
    }
    @Override
    public void onBtnInfo() {
        if(isBond && _currentFragment==_fragmentStart) {
            long time = System.currentTimeMillis();
            if(time-pastTime>1000) {
                count=10;
            } else {
                count--;
            }
            pastTime=time;
            if(count==0) {
                count=10000;
                Toast.makeText(MainActivityAnd.this,"取消配对成功",Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor sp = getApplicationContext().getSharedPreferences(Constants.SAVE_MAC, Context.MODE_PRIVATE).edit();
                sp.putString("mac","");
                sp.putString("name","");
                sp.commit();
                BluetoothDevice destDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macString);
                if(destDevice.getBondState()==BluetoothDevice.BOND_BONDED) {
                    try {
                        ClsUtils.removeBond(destDevice.getClass(),destDevice);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Intent intent1 = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent1);
            } else if(count<7) {
                Toast toast = Toast.makeText(MainActivityAnd.this,"再点击"+count+"次取消配对",Toast.LENGTH_LONG);
                showMyToast(toast,500);
            }
        }

    }

    @Override
    public void onBtnStart() {
        if(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macString).getBondState() == BluetoothDevice.BOND_BONDED) {
            if(_sensorService != null){
                _sensorService.startCollecttingData(_motionMode, new SensorService.OnEventListener() {
                    @Override
                    public void onEvent(long gpsCounter, long imuCounter, long elapse) {
                        countGPS = (int)gpsCounter;
                        countIMU = (int)imuCounter;
                        serviceSecond = (int)elapse;

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ((FragmentStop) _fragmentStop).setInfo(String.valueOf(countGPS));
                            }
                        });
                    }

                    @Override
                    public void onDataReady(String path, String filename) {

                        mAbsolutePath = path;
                        mFileName =filename;
                        mZipFilePath = getZipFilePath(mFileName);
                        DZip.zipAsync(mAbsolutePath, mZipFilePath, MainActivityAnd.this);
                    }
                });

                FragmentTransaction trans = getFragmentManager().beginTransaction();
                trans.replace(R.id.top_container, _fragmentWalking).commitAllowingStateLoss();
                getFragmentManager().executePendingTransactions();
                switchBottomFragment(_currentFragment,_fragmentStop);
//                // TODO: 17-8-22 custom
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ProductRouteFile.getInstance().productFile(MainActivityAnd.this, new SensorService.OnEventListener() {
//                            @Override
//                            public void onEvent(long gpsCounter, long imuCounter, long elapse) {
//
//                            }
//                            @Override
//                            public void onDataReady(String path, String filename) {
//                                mAbsolutePath = path;
//                                mFileName =filename;
//                                mZipFilePath = getZipFilePath(mFileName);
//                                DZip.zipAsync(mAbsolutePath, mZipFilePath, MainActivityAnd.this);
//                            }
//                        });
//                    }
//                }).start();
            }
        } else {
            Toast.makeText(this,"请配对手机",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBtnStop() {

       // serviceIntent = new Intent(MainActivityAnd.this, SensorService.class);
       // stopService(serviceIntent);

        _sensorService.stopCollecttingData();
        ((FragmentInfo) _fragmentInfo).setInfo("压缩中，请稍后...");
        FragmentTransaction trans = getFragmentManager().beginTransaction();
        trans.replace(R.id.top_container, _fragmentInfo);
        trans.commitAllowingStateLoss();
    }

    @Override
    public void onBtnUpload() {
        sendFile();
    }

    @Override
    public void onBtnDiscard() {
        ((FragmentInfo) _fragmentInfo).setInfo("确认放弃当前轨迹吗？\n确认的话将删除当前轨迹");
        FragmentTransaction trans = getFragmentManager().beginTransaction();
        trans.replace(R.id.top_container, _fragmentInfo).commitAllowingStateLoss();
        getFragmentManager().executePendingTransactions();
        switchBottomFragment(_currentFragment,_fragmentCancelDelete);
    }


    @Override
    public void onBtnCancelDelete() {

        ((FragmentInfo) _fragmentInfo).setInfo("轨迹采集已完成,请选择：");
//        ((FragmentInfo) _fragmentInfo).setInfoWithFontSize(
//                this.getResources().getString(R.string.main_collect_finish_tip_str),
//                16.f, this.getApplication(), mColorStateList);
        FragmentTransaction trans = getFragmentManager().beginTransaction();
        trans.replace(R.id.top_container, _fragmentInfo).commitAllowingStateLoss();
        getFragmentManager().executePendingTransactions();
        switchBottomFragment(_currentFragment,_fragmentSendDiscard);

    }

    @Override
    public void onBtnDelete() {

        // delete file
        if (mZipFilePath != null) {
            if (mZipFilePath != null) {
                String filePath = mZipFilePath.substring(0, mZipFilePath.length() - 3);
                deleteDataFile(filePath);
            }
        }

//		((FragmentInfo) _fragmentInfo).setInfo("欢迎使用Track,您可以：");
        ((FragmentInfo) _fragmentInfo).setInfoWithFontSize(
                headMessage,
                16.f, mContext, mColorStateList);
        FragmentTransaction trans = getFragmentManager().beginTransaction();
        trans.replace(R.id.top_container, _fragmentInfo).commitAllowingStateLoss();
        getFragmentManager().executePendingTransactions();
        switchBottomFragment(_currentFragment,_fragmentStart);

    }

    protected String getNowDate() {
        Date dt = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(dt);
    }

    @Override
    public void onComplete(int error, String input, String output) {
        // go to sendordiscard

        mHandler.post(new Runnable() {
            @Override
            public void run() {

                ((FragmentInfo) _fragmentInfo).setInfo("轨迹采集已完成,请选择：");
//                ((FragmentInfo) _fragmentInfo).setInfoWithFontSize(
//                        getString(R.string.main_collect_finish_tip_str),
//                        16.f, mContext, mColorStateList);
                FragmentTransaction trans = getFragmentManager().beginTransaction();
                trans.replace(R.id.top_container, _fragmentInfo).commitAllowingStateLoss();
                getFragmentManager().executePendingTransactions();
                switchBottomFragment(_currentFragment,_fragmentSendDiscard);
            }
        });
    }

    @Override
    public void onProgress(int progress) {

    }


    private String getZipFilePath(String name) {
        String directoryPath = Environment.getExternalStorageDirectory().getPath() + "/TraceDataCollection";
        File directory = new File(directoryPath);
        if (!directory.exists())
            directory.mkdir();
//        String path = String.format("%s/%s_%s.bin", directoryPath, getNowDate(), name);
        String path = String.format("%s/%s.gz", directoryPath, name);
        return path;
    }


    private void sendActionTip(String tipStr) {
        FragmentTransaction trans = getFragmentManager().beginTransaction();
        ((FragmentInfo) _fragmentInfo).setInfo(tipStr);
        trans.replace(R.id.top_container, _fragmentInfo);
//        trans.replace(R.id.bottom_container, _fragmentSendDiscard);
        trans.hide(_currentFragment);
        trans.commitAllowingStateLoss();
        getFragmentManager().executePendingTransactions();
    }

    /**
     * add switch Bottom Fragment function
     *@param from:fragment which will be hidden
     *@param to:fragment which will be show
     */
    public void switchBottomFragment(Fragment from,Fragment to) {
        FragmentTransaction trans = getFragmentManager().beginTransaction();
        if(_currentFragment!=to) {
            _currentFragment=to;
            if(!_currentFragment.isAdded()) {
                trans.hide(from).add(R.id.bottom_container,_currentFragment);
                if(_currentFragment.isHidden()) {
                    trans.show(_currentFragment);
                }
                trans.commitAllowingStateLoss();
                getFragmentManager().executePendingTransactions();
            } else {
                trans.hide(from).show(_currentFragment).commitAllowingStateLoss();
                getFragmentManager().executePendingTransactions();
            }
        } else {
            if(!_currentFragment.isAdded()) {
                trans.add(R.id.bottom_container,_currentFragment);
            }
            if(_currentFragment.isHidden()) {
                trans.show(_currentFragment);
            }
            trans.commitAllowingStateLoss();
            getFragmentManager().executePendingTransactions();
        }
    }

    private void initService() {

        IntentFilter intentFilter2 = new IntentFilter("com.dlrc.SensorService.FileName");
        mFileNameReceiver = new FileNameReceiver();
        registerReceiver(mFileNameReceiver, intentFilter2);

        IntentFilter SensorServiceFilter = new IntentFilter("com.dlrc.SensorService");
        SensorServiceReceiver = new SensorServiceReceiver();
        registerReceiver(SensorServiceReceiver, SensorServiceFilter);

    }

    private void uninitService() {
        if (mFileNameReceiver != null)
            unregisterReceiver(mFileNameReceiver);

        if (SensorServiceReceiver != null)
            unregisterReceiver(SensorServiceReceiver);

        serviceIntent = new Intent(MainActivityAnd.this, SensorService.class);
        stopService(serviceIntent);
    }


    private void sendFile() {
            //mZipFilePath
        int res=-99;
        try {
            res = _bft.sendFile(mZipFilePath);
            if(res !=0 )
            {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ((FragmentInfo) _fragmentInfo).setInfo("请打开已配对手机\n并等待配对");
//                    ((FragmentInfo) _fragmentInfo).setInfoWithFontSize(
//                            "请打开已配对手机\n并等待配对",
//                            10.f, mContext, mColorStateList);
                        FragmentTransaction trans = getFragmentManager().beginTransaction();
                        trans.replace(R.id.top_container, _fragmentInfo).commitAllowingStateLoss();
                        getFragmentManager().executePendingTransactions();
                        switchBottomFragment(_currentFragment,_fragmentSendDiscard);
                    }
                });
                return;
            }else{
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        sendActionTip("轨迹上传中...");
                    }
                });
            }
        }catch (NullPointerException e) {
            e.printStackTrace();
            Log.d("luk","send file null pointer error,mZipFilePath="+mZipFilePath+",res="+res);
            Toast.makeText(this,"null pointer",Toast.LENGTH_SHORT).show();
        }

    }

    void deleteDataFile(String name) {
        String binFile = name + ".bin";
        String zipFile = name + ".gz";

        File fbin = new File(binFile);
        File fzip = new File(zipFile);

        if (fbin.isFile())
            fbin.delete();
        if (fzip.isFile())
            fzip.delete();
    }

    void errorHandler(String info, Fragment bottom, boolean bRestart) {
        if (!bRestart) {
            FragmentTransaction trans = getFragmentManager().beginTransaction();
            ((FragmentInfo) _fragmentInfo).setInfo(info);
            trans.replace(R.id.top_container, _fragmentInfo);
            trans.replace(R.id.bottom_container, bottom);
            trans.commitAllowingStateLoss();
        }
    }

    /*@Override
    public void onMotionSelect(int motion) {

        _motionMode = motion;

        if(_sensorService != null){
            _sensorService.startCollecttingData(_motionMode, new SensorService.OnEventListener() {
                @Override
                public void onEvent(long gpsCounter, long imuCounter, long elapse) {
                    countGPS = (int)gpsCounter;
                    countIMU = (int)imuCounter;
                    serviceSecond = (int)elapse;

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ((FragmentStop) _fragmentStop).setInfo(String.valueOf(countGPS));
                        }
                    });
                }

                @Override
                public void onDataReady(String path, String filename) {

                    mAbsolutePath = path;
                    mFileName =filename;
                    mZipFilePath = getZipFilePath(mFileName);
                    DZip.zipAsync(mAbsolutePath, mZipFilePath, MainActivityAnd.this);
                }
            });

            FragmentTransaction trans = getFragmentManager().beginTransaction();
            trans.replace(R.id.top_container, _fragmentWalking).commitAllowingStateLoss();
            getFragmentManager().executePendingTransactions();
            switchBottomFragment(_currentFragment,_fragmentStop);
        }
    }*/
    ServiceConnection _serviceConnection;
    SensorService _sensorService;
    private void startSensorService(){
       // serviceIntent = new Intent(MainActivityAnd.this, SensorService.class);
      //  startService(serviceIntent);

        _serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                _sensorService = ((SensorService.LocalBinder)service).getService();
                int xx= 0;
                xx++;

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        Intent it = new Intent(this, SensorService.class);

        bindService(it,_serviceConnection, Context.BIND_AUTO_CREATE);
    }
    private void stopSensorService(){

        _sensorService.stopCollecttingData();
        //unbindService(_serviceConnection);
    }

    private class FileNameReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mAbsolutePath = intent.getStringExtra("absolutePath");
            mFileName = intent.getStringExtra("fileName");
            mZipFilePath = getZipFilePath(mFileName);
            DZip.zipAsync(mAbsolutePath, mZipFilePath, MainActivityAnd.this);
        }
    }

    public class SensorServiceReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            countGPS = intent.getIntExtra("countGPS", 0);
            countIMU = intent.getIntExtra("countIMU", 0);
            serviceSecond = intent.getIntExtra("serviceSecond", 0);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ((FragmentStop) _fragmentStop).setInfo(String.valueOf(countGPS));
                }
            });
        }
    }
    BluetoothFileTransfer.OnFileTransListener _onClientListener = 	new BluetoothFileTransfer.OnFileTransListener() {

        @Override
        public void onStatusChanged(int status) {
            // TODO Auto-generated method stub

            _btState = status;
            _handler.post(new Runnable(){

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    if( BluetoothChatService.STATE_CONNECTED==_btState){
                       // mStatusView.setText("connected");
                        //dlrc_report.pdf
                        Vibrator vg  =(Vibrator)MainActivityAnd.this.getSystemService(Service.VIBRATOR_SERVICE);
                        vg.vibrate(1000);

                    }else if(BluetoothChatService.STATE_CONNECTING== _btState){
                        //mStatusView.setText("connecting");
                    }else if(BluetoothChatService.STATE_LISTEN == _btState){
                        //mStatusView.setText("listen");
                    }else if(BluetoothChatService.STATE_NONE==_btState){
                        //mStatusView.setText("unknow");
                    }else{
                        //mStatusView.setText("shit");
                    }
                }});

        }

        @Override
        public void onMessage(String msg) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onFileTransferCompleted(int error) {
            // TODO Auto-generated method stub
            if(error==0){
                _handler.post(new Runnable(){

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        // mStatusView.setText("file sent ok");
                        Toast.makeText(mContext, "上传成功", Toast.LENGTH_SHORT).show();
                        ((FragmentInfo) _fragmentInfo).setInfo("上传成功!");
                        FragmentTransaction trans = getFragmentManager().beginTransaction();
                        trans.replace(R.id.top_container, _fragmentInfo).commitAllowingStateLoss();
                        getFragmentManager().executePendingTransactions();
                        switchBottomFragment(_currentFragment,_fragmentUploadSuccess);

                        if (mZipFilePath != null) {
                            String filePath = mZipFilePath.substring(0, mZipFilePath.length() - 3);
                            deleteDataFile(filePath);
                        }

                        Timer t = new Timer();
                        t.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
//									((FragmentInfo) _fragmentInfo).setInfo("欢迎使用Track,您可以：");
                                        FragmentTransaction trans = getFragmentManager().beginTransaction();
                                        ((FragmentInfo) _fragmentInfo).setInfo(headMessage);
                                        trans.replace(R.id.top_container, _fragmentInfo).commitAllowingStateLoss();
                                        getFragmentManager().executePendingTransactions();
                                        switchBottomFragment(_currentFragment,_fragmentStart);


                                    }
                                });
                            }
                        }, 1000);
                    }
                });
            }else{

                // error
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        FragmentTransaction trans = getFragmentManager().beginTransaction();
                        ((FragmentInfo) _fragmentInfo).setInfo("上传出错，请重试");
                        trans.replace(R.id.top_container, _fragmentInfo).commitAllowingStateLoss();
                        getFragmentManager().executePendingTransactions();
                        switchBottomFragment(_currentFragment,_fragmentSendDiscard);
                        //getFragmentManager().beginTransaction().show(_fragmentSendDiscard);
                    }
                });
            }

        }

        @Override
        public void onFileReceived(String path) {
            // TODO Auto-generated method stub

            _handler.post(new Runnable(){

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                   // mStatusView.setText("file received : "+fileCounter);
                }});

        }
    };

    private void registerBondChangeReceiver() {

        IntentFilter stateChangeFilter = new IntentFilter();
        stateChangeFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        stateChangeFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        registerReceiver(bondStateChangeReceiver, stateChangeFilter);
    }
    private void unregisterBondChangeReceiver() {
        if (bondStateChangeReceiver != null)
            unregisterReceiver(bondStateChangeReceiver);
    }
    private BroadcastReceiver bondStateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                switch (state) {
                    case BluetoothDevice.BOND_NONE:
                        Log.d("luk", "BOND_NONE 删除配对");
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.d("luk", "BOND_BONDING 正在配对");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.d("luk", "BOND_BONDED 配对成功");
                        Toast.makeText(MainActivityAnd.this,"配对成功",Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor sp = getApplicationContext().getSharedPreferences(Constants.SAVE_MAC, Context.MODE_PRIVATE).edit();
                        sp.putString("mac",device.getAddress());
                        sp.putString("name",device.getName());
                        sp.commit();
                        Intent intent1 = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent1);
                        break;
                }
            } else if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                try {
                    ClsUtils.setPairingConfirmation(device.getClass(), device, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
