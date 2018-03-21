package com.example.android.guittone;

import org.eclipse.paho.android.service.MqttAndroidClient;

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
    private boolean mType;
    private int mRead;

    Device(String name, String OnUrl, String OffUrl, String CheckUrl, boolean Type) {
        mName = name;
        mStatus = false;
        mOffUrl = OffUrl;
        mOnUrl = OnUrl;
        mCheckUrl = CheckUrl;
        mRead = 0;
        mType = Type;
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

    boolean getmType() {
        return mType;
    }

    int getmRead() {
        return mRead;
    }



    //SET METHODS
    void setmName(String name) {
        mName = name;
    }

    void setmStatus(Boolean status) {
        mStatus = status;
    }

    void setmOnUrl(String url) {
        mOnUrl = url;
    }

    void setmOffUrl(String url) {
        mOffUrl = url;
    }

    void setmRead(int read) {
        mRead = read;
    }
    public void on(String url) {
        MqttAndroidClient mqttAndroidClient;

        final String serverUri = "tcp://iot.eclipse.org:1883";
        String clientId = "ExampleAndroidClient";
        final String subscriptionTopic = "exampleAndroidTopic";
        final String publishTopic = "exampleAndroidPublishTopic";
        final String publishMessage = "Hello World!";


        setmStatus(true);
    }

    void off(String url) {

        setmStatus(false);
    }


}