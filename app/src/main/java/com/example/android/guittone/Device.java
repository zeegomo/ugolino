package com.example.android.guittone;

import static android.view.View.GONE;

/**
 * Created by ${Giacomo} on ${26/10/2016}
 */

 class Device {


    private String mName;
    private boolean mStatus;
    private String mOnUrl;
    private String mOffUrl;
    private String mCheckUrl;

    Device(String name, String OnUrl, String OffUrl, String CheckUrl) {
        mName = name;
        mStatus = false;
        mOffUrl = OffUrl;
        mOnUrl = OnUrl;
        mCheckUrl = CheckUrl;
    }

    //GET METHODS
    String getmName() {
        return mName;
    }

    String getOnUrl() {
        return mOnUrl;
    }

    String getOffUrl() {
        return mOffUrl;
    }

    String getCheckUrl() {
        return mCheckUrl;
    }

    boolean getmStatus() {
        return mStatus;
    }



    //SET METHODS
    void setmName(String name) {
        mName = name;
    }

    void setmStatus(Boolean status) {
        mStatus = status;
    }



    public void on(String url) {
        MainFragment.webView.loadUrl(url);
        MainFragment.webView.setVisibility(GONE);
        setmStatus(true);
    }

    void off(String url) {
        MainFragment.webView.loadUrl(url);
        MainFragment.webView.setVisibility(GONE);
        setmStatus(false);
    }


}