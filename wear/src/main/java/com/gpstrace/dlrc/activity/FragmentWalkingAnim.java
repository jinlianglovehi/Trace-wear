package com.gpstrace.dlrc.activity;

import android.app.Fragment;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.gpstrace.dlrc.R;

/**
 * Created by wangz on 2017/3/9.
 */

public class FragmentWalkingAnim extends Fragment {


    View v = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(v!=null)
            return v;

        v = inflater.inflate(R.layout.fragment_walking, container, false);
        ImageView iv = (ImageView)v.findViewById(R.id.walkingiv);

        iv.setBackgroundResource(R.drawable.collect_process);
        AnimationDrawable anim = (AnimationDrawable) iv.getBackground();
        anim.start();
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }
}
