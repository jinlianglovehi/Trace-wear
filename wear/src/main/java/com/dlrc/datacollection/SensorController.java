package com.dlrc.datacollection;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.List;

public class SensorController implements SensorEventListener {
	final private String TAG = "SensorController";

	private SensorManager mManager = null;
	private Sensor mAccelerometer = null;
	private Sensor mMagnSensor = null;
	private Sensor mGravSensor = null;
	private Sensor mGyroSensor = null;

	private int mRate = SensorManager.SENSOR_DELAY_GAME;

	private SensorController.SensorListener mListener = null;

	public SensorController(Context context) {
		if (mManager == null)
			mManager = (SensorManager) context
					.getSystemService(Context.SENSOR_SERVICE);


		mGravSensor = mManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		try {
			Log.d("sensor",mGravSensor.toString());
		} catch (NullPointerException e) {
			Log.d("sensor","not found " + "TYPE_GRAVITY");
		}

		mMagnSensor = mManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		try {
			Log.d("sensor",mMagnSensor.toString());
		} catch (NullPointerException e) {
			Log.d("sensor","not found " + "TYPE_MAGNETIC_FIELD");
		}
		mAccelerometer = mManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		try {
			Log.d("sensor",mAccelerometer.toString());
		} catch (NullPointerException e) {
			Log.d("sensor","not found " + "TYPE_ACCELEROMETER");
		}
//		Log.e("get vendor", mAccelerometer.getName() + " " + mAccelerometer.getFifoMaxEventCount() + " " + mAccelerometer.getFifoReservedEventCount() + " " +
//								mAccelerometer.getMaxDelay() + " " + mAccelerometer.getMinDelay() + " " + mAccelerometer.getMaximumRange() + " " +
//								mAccelerometer.getPower() + " " + mAccelerometer.getResolution() + " " + mAccelerometer.isWakeUpSensor());
		mGyroSensor = mManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		try {
			Log.d("sensor",mGyroSensor.toString());
		} catch (NullPointerException e) {
			Log.d("sensor","not found " + "TYPE_GYROSCOPE");
		}
//		Log.e("get vendor", mAccelerometer.getVendor() + " " + mGravSensor.getVendor());
	}

	public boolean register() {
		List sensorlist = mManager.getSensorList(Sensor.TYPE_ALL);


		boolean registerSucess = true;
//		mManager.registerListener(this,mAccelerometer,1000*20, 1000*100);
//		mManager.

		registerSucess = registerSucess
				&& mManager.registerListener(this,mAccelerometer, mRate);
        if (mMagnSensor != null) {
            registerSucess = registerSucess
                    && mManager.registerListener(this, mMagnSensor, mRate);
        }
		registerSucess = registerSucess
				&& mManager.registerListener(this, mGravSensor, mRate);
		registerSucess = registerSucess
				&& mManager.registerListener(this, mGyroSensor, mRate);
		if (registerSucess)
			Log.d(TAG, "register sensor all sucessfully");

		return registerSucess;
	}

	public void unregister() {
		mManager.unregisterListener(this);
		Log.d(TAG, "unregister sensor all sucessfully");
	}

	public void setListener(SensorController.SensorListener listener) {
		mListener = listener;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		Log.d(TAG, sensor.getName() + " accuracy change to " + accuracy);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (mListener != null)
			mListener.onSensorChanged(event);
	}

	public interface SensorListener {
		void onSensorChanged(SensorEvent event);
	}

}

