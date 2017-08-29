package com.gpstrace.dlrc.tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.dlrc.service.SensorService;
import com.gpstrace.dlrc.test.TestActivity;
import com.huami.sensor.parse.AccSensorCsvDataParse;
import com.huami.sensor.parse.LocaltionDBManager;
import com.huami.sensor.parse.SportLocationData;

import java.util.List;

/**
 * Created by jinliang on 17-8-22.
 */

public class ProductRouteFile {

    private static ProductRouteFile instance ;

    private static String sportTrackId = "1503987805000" ;
    public static ProductRouteFile getInstance(){
        if(instance==null){
            synchronized (ProductRouteFile.class){
                if(instance==null){
                    instance = new ProductRouteFile();
                }
            }
        }
        return instance;
    }

    public  void productFile(Context mContext,SensorService.OnEventListener onEventListener){
        RouteDataProduct routeDataProduct = RouteDataProduct.getInstance();
        routeDataProduct.startData(onEventListener);
        routeDataProduct.writeModeToFile();
        routeDataProduct.writeFREQToFile();

        List<float[]> accList = AccSensorCsvDataParse.getInstance().
                getSensorDataFromFile(
                        mContext, "sensor_acc.csv"
                );

        List<float[]> magList = AccSensorCsvDataParse.getInstance().
                getSensorDataFromFile(mContext, "sensor_mag.csv");


        int count = Math.min(accList.size(),magList.size());

        // location data
        LocaltionDBManager localtionDBManager = new LocaltionDBManager(mContext);
        SQLiteDatabase db = localtionDBManager.initDBManager(mContext.getPackageName());

        List<SportLocationData> listData = localtionDBManager.getSportLocationDatabyTrackId(db,sportTrackId);

        SportLocationData currentLocaltionData;
        for (int i = 0; i < listData.size(); i++) {
            currentLocaltionData = listData.get(i);
            routeDataProduct.writeGPSDataToFile(currentLocaltionData.getmLatitude(),currentLocaltionData.getmLongitude());
        }

        List<float[]> mGyroList = AccSensorCsvDataParse.getInstance().
                getSensorDataFromFile(
                        mContext, "sensor_gyr.csv"
                );

        count = Math.min(count,mGyroList.size());
        for (int i = 0; i <count ; i++) {
            routeDataProduct.writeIMUToFile(accList.get(i),magList.get(i),mGyroList.get(i));
        }

        routeDataProduct.stopData();

    }
}
