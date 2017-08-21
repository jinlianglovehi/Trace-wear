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
 * Created by wangz on 2017/3/10.
 */

public class FragmentDeleteOrCancel extends Fragment{

    OnCancelDeleteListener _onCancelDeleteListener = null;
    View v = null;
    public interface OnCancelDeleteListener{
        public  void onBtnCancelDelete();
        public  void onBtnDelete();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(v!=null)
            return v;

        v = inflater.inflate(R.layout.fragment_delete_cancel, container, false);

        ImageView btnCancelDelete= (ImageView)v.findViewById(R.id.btnCancelDelete);
        ImageView btnDelete = (ImageView)v.findViewById(R.id.btnDelete);

        btnCancelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_onCancelDeleteListener!=null)
                    _onCancelDeleteListener.onBtnCancelDelete();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_onCancelDeleteListener!=null)
                    _onCancelDeleteListener.onBtnDelete();
            }
        });
        return v;
    }
    public void onAttach(Context activity){
        super.onAttach(activity);
        _onCancelDeleteListener = (OnCancelDeleteListener)activity;

    }

    public void setOnCancelDeleteListener(OnCancelDeleteListener lis){
        _onCancelDeleteListener = lis;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
    }
}
