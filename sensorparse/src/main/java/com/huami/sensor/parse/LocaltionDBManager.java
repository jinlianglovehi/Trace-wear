package com.huami.sensor.parse;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jinliang on 17-8-22.
 */

public class LocaltionDBManager {

    private static final String TAG = LocaltionDBManager.class.getSimpleName();
    private String DB_NAME = "sport_data.db";
    private Context mContext;

    public List<SportLocationData> getSportLocationDatabyTrackId(SQLiteDatabase db, String trackId ){

        StringBuilder stringBuilder = new StringBuilder() ;
        stringBuilder.append(" select * from " + SportLocationDataEntry.TABLE_NAME  + "  where " ) ;
        stringBuilder.append( SportLocationDataEntry.COLUMN_TRACK_ID +"=?" ) ;

        Cursor cursor = db.rawQuery(stringBuilder.toString(),
                new String[]{trackId});
        List<SportLocationData> result = new LinkedList<>();
        while (cursor.moveToNext()) {
            SportLocationData data = new SportLocationData();
            data.mTrackId = cursor.getLong(cursor.getColumnIndex(SportLocationDataEntry.COLUMN_TRACK_ID));
            data.mLatitude = cursor.getFloat(cursor.getColumnIndex(SportLocationDataEntry.COLUMN_LATITUDE));
            data.mLongitude = cursor.getFloat(cursor.getColumnIndex(SportLocationDataEntry.COLUMN_LONGITUDE));
            data.mGPSAccuracy = cursor.getFloat(cursor.getColumnIndex(SportLocationDataEntry.COLUMN_ACCURACY));
            data.mAltitude = cursor.getFloat(cursor.getColumnIndex(SportLocationDataEntry.COLUMN_ALTITUDE));
            data.mTimestamp = cursor.getLong(cursor.getColumnIndex(SportLocationDataEntry.COLUMN_TIMESTAMP));
            data.mPointType = cursor.getInt(cursor.getColumnIndex(SportLocationDataEntry.COLUMN_POINT_TYPE));
            data.mSpeed = cursor.getFloat(cursor.getColumnIndex(SportLocationDataEntry.COLUMN_SPEED));
            data.mPointIndex = cursor.getInt(cursor.getColumnIndex(SportLocationDataEntry.COLUMN_INDEX));
            data.mBar = cursor.getInt(cursor.getColumnIndex(SportLocationDataEntry.COLUMN_BAR));
            data.mCourse = cursor.getFloat(cursor.getColumnIndex(SportLocationDataEntry.COLUMN_COURSE));
            data.mAlgoPointType = cursor.getInt(cursor.getColumnIndex(SportLocationDataEntry.COLUMN_ALGO_POINT_TYPE));
            result.add(data);
        }
        cursor.close();
        Log.i(TAG,"-- resultSize:"+ result.size());
        return result;
    }

    public LocaltionDBManager(Context mContext) {
        this.mContext = mContext;
    }
    //把assets目录下的db文件复制到dbpath下
    public SQLiteDatabase initDBManager(String packName) {

        String fileDocPath = "/data/data/" + packName
                + "/databases/";
        File file =  new File(fileDocPath);
        if(!file.isDirectory()){
            file.mkdirs();
        }

        String dbPath = "/data/data/" + packName
                + "/databases/" + DB_NAME;
        Log.i(TAG, " dbPath:"+ dbPath);
        if (!new File(dbPath).exists()) {
            try {
                FileOutputStream out = new FileOutputStream(dbPath);
                InputStream in = mContext.getAssets().open("db/"+DB_NAME);
                byte[] buffer = new byte[1024];
                int readBytes = 0;
                while ((readBytes = in.read(buffer)) != -1){
                    Log.i(TAG," db  insert batch ");
                    out.write(buffer, 0, readBytes);
                }

                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return SQLiteDatabase.openOrCreateDatabase(dbPath, null);
    }


    public static class SportLocationDataEntry implements BaseColumns {
        public static final String COLUMN_TRACK_ID = "track_id";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_INDEX = "point_index";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_ALTITUDE = "altitude";
        public static final String COLUMN_ACCURACY = "accuracy";
        public static final String COLUMN_POINT_TYPE = "point_type";
        public static final String COLUMN_SPEED = "speed";
        public static final String COLUMN_BAR = "bar";
        public static final String COLUMN_EXTRA = "extra";
        public static final String COLUMN_COURSE = "course";
        public static final String COLUMN_ALGO_POINT_TYPE = "algo_point_type";
        public static final String TABLE_NAME = "location_data";
    }

}
