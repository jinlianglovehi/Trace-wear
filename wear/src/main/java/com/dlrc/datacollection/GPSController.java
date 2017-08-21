package com.dlrc.datacollection;

import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import java.util.Iterator;
import java.util.List;



public class GPSController implements GpsStatus.Listener, LocationListener {

    static final String TAG="GPSController";
    public interface GPSControllerListener {
        void onLocationChanged(Location location);
        void onGPSStatusChanged(int satellite_num);
    }

    private GPSControllerListener mListener = null;

    private LocationManager mLocationManager = null;

    public GPSController(LocationManager manager, GPSControllerListener listener) {
        mLocationManager = manager;
        mListener = listener;
    }

    public boolean isGPSEnabled() {
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void start() {
        mLocationManager.addGpsStatusListener(this);
        boolean hasGps = hasGpsDevice();
        if (hasGps)
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
    }

    public void stop() {
        mLocationManager.removeGpsStatusListener(this);
        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onGpsStatusChanged(int event) {
        switch (event) {
            // 第一次定位
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                Log.i(TAG, "第一次定位");
                break;
            // 卫星状态改变
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                Log.i(TAG, "卫星状态改变");
                // 获取当前状态
                GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                // 获取卫星颗数的默认最大值
                int maxSatellites = gpsStatus.getMaxSatellites();
                // 创建一个迭代器保存所有卫星
                Iterator<GpsSatellite> iters = gpsStatus.getSatellites()
                        .iterator();
                int count = 0;
                while (iters.hasNext() && count <= maxSatellites) {
                    GpsSatellite s = iters.next();
                    count++;
                }
                System.out.println("搜索到：" + count + "颗卫星");
                break;
            // 定位启动
            case GpsStatus.GPS_EVENT_STARTED:
                Log.i(TAG, "定位启动");
                break;
            // 定位结束
            case GpsStatus.GPS_EVENT_STOPPED:
                Log.i(TAG, "定位结束");
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "GPS data get");
        if (mListener != null)
            mListener.onLocationChanged(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            // GPS状态为可见时
            case LocationProvider.AVAILABLE:
                Log.i(TAG, "当前GPS状态为可见状态");
                break;
            // GPS状态为服务区外时
            case LocationProvider.OUT_OF_SERVICE:
                Log.i(TAG, "当前GPS状态为服务区外状态");
                break;
            // GPS状态为暂停服务时
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.i(TAG, "当前GPS状态为暂停服务状态");
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private boolean hasGpsDevice() {
        if (mLocationManager == null)
            return false;

        final List<String> providers = mLocationManager.getAllProviders();
        if (providers == null)
            return false;

        return providers.contains(LocationManager.GPS_PROVIDER);
    }

}
