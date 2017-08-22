/**
 * Copyright 2015 Huami Inc.  All rights reserved.
 */
package com.huami.sensor.parse;

/**
 * Created by zhangfan on 15-7-9.
 */
public class SportLocationData {
    public static final int POINT_TYPE_MASK_RUNNING = 0x0001;//运动中点
    public static final int POINT_TYPE_MASK_RESUME = 0x0002;//恢复之后第一个点
    public static final int POINT_TYPE_MASK_IS_AUTO_RESUME = 0x0004;//是否是自动恢复
    public static final int POINT_TYPE_MASK_GPS_FIND = 0x0008;//GPS找到后第一个点
    public static final int POINT_TYPE_FINAL = 0x1000;//优化以后的点
    public static final int POINT_TYPE_INVALID = 0x8000;//无效的点
    public static final int DEFAULT_ALGO_POINT_TYPE = 0;//invalid point
    public long mTrackId = -1;
    public long mLocationId = -1;
    public long mTimestamp = -1;
    public float mLongitude = 0;
    public float mLatitude = 0;
    public float mAltitude = 0;
    public float mGPSAccuracy = 0;
    public int mPointType = 0;
    public float mSpeed = 0;
    public boolean mIsAutoPause = false;
    public int mPointIndex = -1;
    public int mBar = 0;
    public float mCourse = 0;
    public int mAlgoPointType = DEFAULT_ALGO_POINT_TYPE;


    public long getmTrackId() {
        return mTrackId;
    }

    public void setmTrackId(long mTrackId) {
        this.mTrackId = mTrackId;
    }

    public long getmLocationId() {
        return mLocationId;
    }

    public void setmLocationId(long mLocationId) {
        this.mLocationId = mLocationId;
    }

    public long getmTimestamp() {
        return mTimestamp;
    }

    public void setmTimestamp(long mTimestamp) {
        this.mTimestamp = mTimestamp;
    }

    public float getmLongitude() {
        return mLongitude;
    }

    public void setmLongitude(float mLongitude) {
        this.mLongitude = mLongitude;
    }

    public float getmLatitude() {
        return mLatitude;
    }

    public void setmLatitude(float mLatitude) {
        this.mLatitude = mLatitude;
    }

    public float getmAltitude() {
        return mAltitude;
    }

    public void setmAltitude(float mAltitude) {
        this.mAltitude = mAltitude;
    }

    public float getmGPSAccuracy() {
        return mGPSAccuracy;
    }

    public void setmGPSAccuracy(float mGPSAccuracy) {
        this.mGPSAccuracy = mGPSAccuracy;
    }

    public int getmPointType() {
        return mPointType;
    }

    public void setmPointType(int mPointType) {
        this.mPointType = mPointType;
    }

    public float getmSpeed() {
        return mSpeed;
    }

    public void setmSpeed(float mSpeed) {
        this.mSpeed = mSpeed;
    }

    public boolean ismIsAutoPause() {
        return mIsAutoPause;
    }

    public void setmIsAutoPause(boolean mIsAutoPause) {
        this.mIsAutoPause = mIsAutoPause;
    }

    public int getmPointIndex() {
        return mPointIndex;
    }

    public void setmPointIndex(int mPointIndex) {
        this.mPointIndex = mPointIndex;
    }

    public int getmBar() {
        return mBar;
    }

    public void setmBar(int mBar) {
        this.mBar = mBar;
    }

    public float getmCourse() {
        return mCourse;
    }

    public void setmCourse(float mCourse) {
        this.mCourse = mCourse;
    }

    public int getmAlgoPointType() {
        return mAlgoPointType;
    }

    public void setmAlgoPointType(int mAlgoPointType) {
        this.mAlgoPointType = mAlgoPointType;
    }

    @Override
    public String toString() {
        return "SportLocationData{" +
                "mTrackId=" + mTrackId +
                ", mLocationId=" + mLocationId +
                ", mTimestamp=" + mTimestamp +
                ", mLongitude=" + mLongitude +
                ", mLatitude=" + mLatitude +
                ", mAltitude=" + mAltitude +
                ", mGPSAccuracy=" + mGPSAccuracy +
                ", mPointType=" + mPointType +
                ", mSpeed=" + mSpeed +
                ", mIsAutoPause=" + mIsAutoPause +
                ", mPointIndex=" + mPointIndex +
                ", mBar=" + mBar +
                ", mCourse=" + mCourse +
                ", mAlgoPointType=" + mAlgoPointType +
                '}';
    }
}
