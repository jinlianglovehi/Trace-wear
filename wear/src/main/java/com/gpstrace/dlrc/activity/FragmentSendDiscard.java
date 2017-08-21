package com.gpstrace.dlrc.activity;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.gpstrace.dlrc.R;

/**
 * Created by wangz on 2017/3/9.
 */

public class FragmentSendDiscard extends Fragment {

    public interface OnSendDiscardListener{
        public void onBtnUpload();
        public void onBtnDiscard();
    }

    OnSendDiscardListener _onSendDiscardListener = null;
    View v = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        if( v!=null)
            return v;
        v = inflater.inflate(R.layout.fragment_upload, container, false);
        ImageView ivUpload = (ImageView)v.findViewById(R.id.btnUpload);
        ImageView ivDiscard = (ImageView)v.findViewById(R.id.btnDiscard);

        ivUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_onSendDiscardListener != null)
                    _onSendDiscardListener.onBtnUpload();
            }
        });

        ivDiscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_onSendDiscardListener!=null)
                    _onSendDiscardListener.onBtnDiscard();
            }
        });
        return v;
    }

    public void onAttach(Context activity){
        super.onAttach(activity);

        _onSendDiscardListener = (OnSendDiscardListener)activity;

    }

    public void setOnSendDiscardListener(OnSendDiscardListener lis){
        _onSendDiscardListener = lis;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
    }
}
