package com.gpstrace.dlrc.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.gpstrace.dlrc.R;

/**
 * Created by wangz on 2017/3/9.
 */

public class FragmentStartStop extends Fragment {


    private int _status = 0;
    View v = null;
    OnStartStopListener _startstopListener = null;
    public interface OnStartStopListener{
        public void onBtnStart();
        public void onBtnStop();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        if(v!=null)
            return v;

        v =  inflater.inflate(R.layout.fragment_startstop, container, false);

        Button buttonStartStop = (Button)v.findViewById(R.id.btn_startstop);
        _status = 0;
        buttonStartStop.setTag(_status);
        buttonStartStop.setText("开始采集");
        buttonStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int status = (int)v.getTag();
                if(status==0) // start
                {
                    _status = 1;
                    Button btn = (Button)v;
                    v.setTag(_status);
                    btn.setText("结束采集");
                    if(_startstopListener!= null)
                        _startstopListener.onBtnStart();

                }
                else if( status==1) //stop
                {
                    _status = 0;
                    v.setTag(_status);
                    ((Button)v).setText("开始采集");

                    if(_startstopListener!= null)
                        _startstopListener.onBtnStop();


                }
            }
        });
        return v;
    }

    public void onAttach(Context activity){
        super.onAttach(activity);

        _startstopListener = (OnStartStopListener)activity;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }
}
