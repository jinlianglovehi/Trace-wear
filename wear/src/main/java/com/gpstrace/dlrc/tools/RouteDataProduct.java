package com.gpstrace.dlrc.tools;

import android.os.Environment;

import com.dlrc.service.SensorService;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jinliang on 17-8-22.
 */

public class RouteDataProduct {

    private static RouteDataProduct instance ;

    private SensorService.OnEventListener _onEventLister;
    public static RouteDataProduct getInstance(){
        if(instance==null){
            synchronized (RouteDataProduct.class){
                if(instance==null){
                    instance = new RouteDataProduct();
                }
            }
        }
        return instance;
    }

    private DataOutputStream mOutStream = null;
    private String mAbsolutePath = null;
    private String mFileName = null;
    private int _mode =  0;
    int samplerate  =50 ; // test
    public void startData(SensorService.OnEventListener evtListener){
        _onEventLister  =evtListener;
        mFileName = getNowDate();
        mAbsolutePath = getOutputFilePath(mFileName);
        try {
            if (mAbsolutePath != null)
                mOutStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mAbsolutePath)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // first step
    public void writeModeToFile(){
        try {
            mOutStream.writeByte(_mode);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // second step

    public void writeFREQToFile(){


        try {
            mOutStream.writeByte(samplerate);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private  byte GPS_DATA_TEST = 11;

    public void writeGPSDataToFile(double latitude ,double longitude){

        try {
            mOutStream.writeByte(GPS_DATA_TEST);
            mOutStream.writeDouble(latitude);
            mOutStream.writeDouble(longitude);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    // acc
    public void writeIMUToFile(float[] mAcceData ,float[] mMagnData,float[] mGyroData){
         byte IMU_DATA_TEST = 10;
        try {
            mOutStream.writeByte(IMU_DATA_TEST);
            mOutStream.writeFloat(mAcceData[0]);
            mOutStream.writeFloat(mAcceData[1]);
            mOutStream.writeFloat(mAcceData[2]);
            mOutStream.writeFloat(mMagnData[0]);
            mOutStream.writeFloat(mMagnData[1]);
            mOutStream.writeFloat(mMagnData[2]);
            if(mGyroData!=null && mGyroData.length>0){
                mOutStream.writeFloat(mGyroData[0]);
                mOutStream.writeFloat(mGyroData[1]);
                mOutStream.writeFloat(mGyroData[2]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopData(){
        try {
            if (mOutStream != null) {
                mOutStream.close();
                mOutStream = null;
            }
            try {
                RandomAccessFile raFile = new RandomAccessFile(mAbsolutePath, "rw");
                raFile.writeByte(_mode);
                raFile.writeByte(samplerate);
                raFile.close();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }catch (Exception e){
           e.printStackTrace();
        }

        if(_onEventLister!=null)
            _onEventLister.onDataReady(mAbsolutePath, mFileName);
    }

    // common method

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

}
