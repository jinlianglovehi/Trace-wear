package com.gpstrace.dlrc.activity;

import android.app.Fragment;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gpstrace.dlrc.R;


/**
 * Created by wangz on 2017/3/9.
 */

public class FragmentInfo extends Fragment {
    public interface OnBtnInfoListener{
        public void onBtnInfo();
    }
    TextView _infoView = null;
    OnBtnInfoListener _onBtnInfoListener = null;
    String _info = "欢迎使用Track,您可以：";
    View v = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if( v!= null)
            return v;
        v = inflater.inflate(R.layout.fragment_info, container, false);
        _infoView = (TextView)v.findViewById(R.id.infotv);
        _infoView.setText(_info);
        _infoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_onBtnInfoListener != null)
                    _onBtnInfoListener.onBtnInfo();
            }
        });
        return v;
    }
    public void setOnBtnInfoListener(OnBtnInfoListener lis){
        _onBtnInfoListener = lis;
    }
    public void onAttach(Context activity){
        super.onAttach(activity);
        _onBtnInfoListener = (OnBtnInfoListener)activity;
    }
    public void setInfo(String info){
        _info = info;
        if(_infoView!=null) {
            _infoView.setText(_info);
            _infoView.invalidate();
        }
    }

    /**
     * @function 设置页面显示字体的大小以及颜色，默认为黑色
     * @param info 要设置显示的字体
     * @param mFontSize 字体的大小
     * @param mContext 上下文消息
     * @param mColor 设置不透明的颜色值
     * */
    public void setInfoWithFontSize(String info, Float mFontSize,
                                    Context mContext,ColorStateList mColor){
        _info = info;
        if(_infoView!=null) {
            _infoView.setText(info);
            _infoView.invalidate();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }
}
