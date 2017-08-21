package com.gpstrace.dlrc.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.gpstrace.dlrc.R;

/**
 * Created by wangz on 2017/4/20.
 */

public class FragmentMotionSelect extends Fragment {

    public final static int MOTION_AUTO = 0;
    public final static int MOTION_WALKING = 1;
    public final static int MOTION_HOLDING = 2;
    public final static int MOTION_RUNNING = 3;

    public interface OnMotionSelectListener{
        public void onMotionSelect(int motion);

    }
    View v;

    OnMotionSelectListener _onMotionSelectListener;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if( v!= null)
            return v;

        v = inflater.inflate(R.layout.fragment_motion_select, container, false);

        ImageView ivrunning= (ImageView)v.findViewById(R.id.running);
        ivrunning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_onMotionSelectListener!= null)
                    _onMotionSelectListener.onMotionSelect(MOTION_RUNNING);
            }
        });

        ImageView ivwalking= (ImageView)v.findViewById(R.id.walking);
        ivwalking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_onMotionSelectListener!= null)
                    _onMotionSelectListener.onMotionSelect(MOTION_WALKING);
            }
        });

        ImageView ivholding= (ImageView)v.findViewById(R.id.holding);
        ivholding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_onMotionSelectListener!= null)
                    _onMotionSelectListener.onMotionSelect(MOTION_HOLDING);
            }
        });
        return v;
    }

    public void set_onMotionSelectListener(OnMotionSelectListener lis){
        _onMotionSelectListener = lis;
    }
}
