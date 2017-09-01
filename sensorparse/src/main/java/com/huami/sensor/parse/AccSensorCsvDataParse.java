package com.huami.sensor.parse;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinliang on 17-8-22.
 */

public class AccSensorCsvDataParse {

    public static final String TAG = AccSensorCsvDataParse.class.getSimpleName();
   private List<float[]> accsensorcsvData =null;


    private static AccSensorCsvDataParse instance ;

    public static AccSensorCsvDataParse getInstance(){
         if(instance==null){
             synchronized (AccSensorCsvDataParse.class){
                 if(instance==null){
                     instance = new AccSensorCsvDataParse();
                 }
             }
         }
         return instance;
    }
    /**
     *  by fileName get sensorData
     * @param mContext
     * @param fileName
     * @return
     */
    public List<float[]> getSensorDataFromFile(Context mContext,String fileName){
        List<float[]> data = new ArrayList<float[]>();
        try {
            Log.i(TAG, " getSensorDataFromFile: "+ fileName);

            String path =mContext.getCacheDir().getPath() +"/"+fileName;
            Log.i(TAG," path:"+ path);
            InputStream in = new FileInputStream(path);//读取文件的数据。
            InputStreamReader inputReader = new InputStreamReader(in);//读取


//            InputStreamReader inputStreamReader = new InputStreamReader(new InputStream(mContext.getCacheDir()));
//            InputStreamReader inputReader = new InputStreamReader(mContext.getResources().getAssets().open(fileName) );
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            String[] itemData = null;
            float[] itemFloatData =null;
            int count = 0;
            while((line = bufReader.readLine()) != null){
                itemData = line.split(",");
                itemFloatData =strToFloat(itemData);
                count ++ ;
                if(itemFloatData[0]!=-1 && itemFloatData[1] !=-1){
                    data.add(itemFloatData);
                    Log.i(TAG, fileName+ " Line:"+ line.toString() + " ---"+printData(itemData) );
                }else{
                    Log.i(TAG," no need to add " + printData(itemData));
                }


            }
            Log.i(TAG, fileName+" line Count Size:"+ count +",dataSize:"+ data.size());
        } catch (Exception e) {
            Log.i(TAG," error ");
            e.printStackTrace();
        }
        return data;
    }

    public float[] strToFloat(String[] strs){
        float[] results = new float[strs.length];
        for (int i = 0; i <strs.length ; i++) {
           results[i] = Float.valueOf(strs[i]);
        }

        return results;

    }

    public  SensorMetaModel getModelFromFile(Context mContext ,String fileName){
        StringBuilder sb = new StringBuilder();
        try {
            Log.i(TAG, " getSensorDataFromFile: "+ fileName);
            InputStreamReader inputReader = new InputStreamReader(mContext.getResources().getAssets().open(fileName) );
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";

            int count = 0;
            while((line = bufReader.readLine()) != null){
                sb.append(line);
            }
            Log.i(TAG, fileName+ "  content:"+ sb.toString());
        } catch (Exception e) {
            Log.i(TAG," error ");
            e.printStackTrace();
        }

        if(sb.toString().length()<=0){
            return null;
        }else{
            SensorMetaModel model =new Gson().fromJson(sb.toString(),SensorMetaModel.class);
            return model;
        }
    }


    public static String printData(String[] data){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < data.length ; i++) {
            sb.append(data[i] +",");
        }
        sb.replace(sb.length()-1,sb.length(),"");
        sb.append("]");
        return sb.toString();
    }








}
