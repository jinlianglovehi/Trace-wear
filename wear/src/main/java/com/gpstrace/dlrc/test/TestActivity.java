package com.gpstrace.dlrc.test;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.gpstrace.dlrc.R;
import com.huami.sensor.parse.AccSensorCsvDataParse;
import com.huami.sensor.parse.LocaltionDBManager;
import com.huami.sensor.parse.SensorMetaModel;
import com.huami.sensor.parse.SportLocationData;

import java.util.List;

/**
 * Created by jinliang on 17-8-22.
 */

public class TestActivity extends Activity {

    private static final String TAG = TestActivity.class.getSimpleName();
    private Button btnTestAccSensorParse;
    private Button btnTestGsonParse;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_sensor_parse);

        btnTestAccSensorParse = (Button) findViewById(R.id.btn_test_acc_parse);
        btnTestGsonParse  =(Button)findViewById(R.id.btn_test_gson_parse);

        btnTestAccSensorParse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(AccSensorCsvDataParse.TAG," onclick ");
                AccSensorCsvDataParse.getInstance().
                        getSensorDataFromFile(
                                TestActivity.this, "sensor_acc.csv"
                        );
            }
        });

        btnTestGsonParse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(AccSensorCsvDataParse.TAG," btnTestGsonParse onclick ");

                SensorMetaModel model =AccSensorCsvDataParse.getInstance().
                        getModelFromFile(TestActivity.this, "acc_meta.json");

                if(model!=null){
                    Log.i(AccSensorCsvDataParse.TAG," model Content:" + model.toString()) ;
                }else{
                    Log.i(AccSensorCsvDataParse.TAG," model Content: is null " ) ;
                }

            }
        });

        findViewById(R.id.btn_test_localtion_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LocaltionDBManager localtionDBManager = new LocaltionDBManager(TestActivity.this);
                SQLiteDatabase db = localtionDBManager.initDBManager(getPackageName());

                List<SportLocationData> listData = localtionDBManager.getSportLocationDatabyTrackId(db,"1501567477000");

                Log.i(TAG," sportLocalDataSize:"+ listData.size());

            }
        });


    }
}
