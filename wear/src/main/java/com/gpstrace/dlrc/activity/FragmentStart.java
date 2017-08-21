package com.gpstrace.dlrc.activity;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.gpstrace.dlrc.R;

/**
 * Created by wangz on 2017/3/13.
 */

public class FragmentStart extends Fragment{

    public interface OnBtnStartListener{
        public void onBtnStart();
    }
    OnBtnStartListener _onBtnStartListener = null;
    View v = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if( v!= null)
            return v;

        v = inflater.inflate(R.layout.fragment_start, container, false);

        Button iv= (Button)v.findViewById(R.id.btn_start);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_onBtnStartListener != null)
                    _onBtnStartListener.onBtnStart();
            }
        });
        iv.setText("开始采集");
        return v;
    }
    public void onAttach(Context activity){
        super.onAttach(activity);
        _onBtnStartListener = (OnBtnStartListener)activity;
    }

    public void setOnBtnStartListener(OnBtnStartListener lis){
        _onBtnStartListener = lis;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
    }
}
