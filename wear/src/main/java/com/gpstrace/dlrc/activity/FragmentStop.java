package com.gpstrace.dlrc.activity;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.gpstrace.dlrc.R;

/**
 * Created by wangz on 2017/3/13.
 */

public class FragmentStop extends Fragment{

    public interface OnBtnStopListener{
        public void onBtnStop();
    }
    OnBtnStopListener _onBtnStopListener = null;
    String _info = "";
    Button iv2;
    View v = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if( v!= null)
            return v;

        v = inflater.inflate(R.layout.fragment_stop, container, false);

        Button iv= (Button)v.findViewById(R.id.btn_stop);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_onBtnStopListener != null)
                    _onBtnStopListener.onBtnStop();
            }
        });
        iv.setText("结束采集");

        iv2= (Button)v.findViewById(R.id.btn_count);
        iv2.setText(_info);


        return v;
    }

    public void setInfo(String info){
        _info = info;
        if(iv2 != null) {
            iv2.setText(_info);
            iv2.invalidate();
        }
    }

    public void onAttach(Context activity){
        super.onAttach(activity);
        _onBtnStopListener = (OnBtnStopListener)activity;
    }

    public void setOnBtnStopListener(OnBtnStopListener lis){
        _onBtnStopListener = lis;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
    }
}
