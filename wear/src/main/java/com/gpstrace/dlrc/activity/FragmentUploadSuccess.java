package com.gpstrace.dlrc.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gpstrace.dlrc.R;

/**
 * Created by wangz on 2017/3/10.
 */

public class FragmentUploadSuccess extends Fragment {

    View v=null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(v!=null)
            return v;

        v = inflater.inflate(R.layout.fragment_success, container, false);

        return v;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
    }
}
