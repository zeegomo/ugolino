package com.example.android.guittone;

import android.os.Parcel;
import android.os.Parcelable;
import android.webkit.WebView;

import java.io.Serializable;

import static android.view.View.GONE;

/**
 * Created by Giacomo on 26/12/2016.
 */

public class Device {


    private String mName;
    private boolean mStatus;
    private String mOnUrl;
    private String mOffUrl;
    private String mCheckUrl;

    public Device(String name, String OnUrl, String OffUrl, String CheckUrl) {
        mName = name;
        mStatus = false;
        mOffUrl = OffUrl;
        mOnUrl = OnUrl;
        mCheckUrl = CheckUrl;
    }

    //GET METHODS
    public String getmName() {
        return mName;
    }

    public String getOnUrl() {
        return mOnUrl;
    }

    public String getOffUrl() {
        return mOffUrl;
    }

    public String getCheckUrl() {
        return mCheckUrl;
    }

    public boolean getmStatus() {
        return mStatus;
    }



    //SET METHODS
    public void setmName(String name) {
        mName = name;
    }

    public void setmStatus(Boolean status) {
        mStatus = status;
    }



    public void on(String url) {
        MainActivity.webView.loadUrl(url);
        MainActivity.webView.setVisibility(GONE);
        setmStatus(true);
    }

    public void off(String url) {
        MainActivity.webView.loadUrl(url);
        MainActivity.webView.setVisibility(GONE);
        setmStatus(false);
    }


}