package com.gpstrace.dlrc.activity;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.gpstrace.dlrc.R;
import com.gpstrace.dlrc.bluetooth.ClsUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by luk on 2017/7/13.
 */

public class FragmentBond extends Fragment{

    View v = null;
    Context mContext;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if( v!= null)
            return v;

        v = inflater.inflate(R.layout.fragment_bond, container, false);
        ImageView iv= (ImageView)v.findViewById(R.id.mac_qr_iv);
        String macString = ClsUtils.getMacAddress(mContext);
        Bitmap qrCodeBitmap = generateBitmap(macString.toUpperCase(),256,256);
        iv.setImageBitmap(qrCodeBitmap);
        Log.d("luk","mac="+macString);
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }
    private Bitmap generateBitmap(String content,int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (encode.get(j, i)) {
                        pixels[i * width + j] = 0x00000000;
                    } else {
                        pixels[i * width + j] = 0xffffffff;
                    }
                }
            }
            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public void onAttach(Context activity){
        super.onAttach(activity);
        this.mContext = activity;
    }
}
