package com.gpstrace.dlrc.activity;

import android.app.Fragment;
import android.content.Context;
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

public class FragmentUploading extends Fragment{


    View v = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if( v!= null)
            return v;

        v = inflater.inflate(R.layout.fragment_uploading, container, false);

        ImageView iv= (ImageView)v.findViewById(R.id.uploadingiv);
        iv.setBackgroundResource(R.drawable.send_collect_uploading);
        AnimationDrawable anim = (AnimationDrawable) iv.getBackground();
        anim.start();
        return v;
    }
    public void onAttach(Context activity){
        super.onAttach(activity);
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

}
