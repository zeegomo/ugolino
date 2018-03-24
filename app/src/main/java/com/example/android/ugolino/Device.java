package com.example.android.ugolino;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


/**
 * Created by ${Giacomo} on ${26/10/2016}
 */

 class Device {


    private String mName;
    private boolean mStatus;
    private String mWrite_topic;
    //private String mW
    private String mRead_topic;
    private String mBroker;
    private String mMask;
    private boolean mType;
    private String mRead;

    Device(String name, String mask, String read_topic, String broker, String write_topic, boolean Type) {
        mName = name;
        mStatus = false;
        mRead_topic = read_topic;
        mWrite_topic = write_topic;
        mRead = "";
        mMask = mask;
        mBroker = broker;
        mType = Type;
    }

    //GET METHODS
    String getmName() {
        return mName;
    }

    String getmWrite_topic() {
        return mWrite_topic;
    }

    String getmRead_topic() {
        return mRead_topic;
    }

    String getmBroker() {return  mBroker;}

    String getmMask(){
        return mMask;
    }

    boolean getmStatus() {
        return mStatus;
    }

    boolean getmType() {
        return mType;
    }

    String getmRead() {
        return mRead;
    }



    //SET METHODS
    void setmName(String name) {
        mName = name;
    }

    void setmStatus(Boolean status) {
        mStatus = status;
    }

    void setmWrite_topic(String topic){
        mWrite_topic = topic;
    }

    void setmRead_topic(String topic){
        mRead_topic = topic;
    }

    public void setmBroker(String broker) {
        mBroker = broker;
    }

    void setmRead(String read) {mRead = read;
    }

    void setmMask(String mask){mMask = mask;
    }

    public void on() {
        int qos = 0;
        String clientId = "paho-java-client";
        String content = "1";

        Log.e("topic: " + mWrite_topic +"mask:  " + mMask + " broker: " + mBroker,"DEVICE");
        try {
            final MqttClient sampleClient = new MqttClient("tcp://"+mBroker, clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            sampleClient.connect(connOpts);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            String topic;
            if(mMask.equals(""))
                topic = mWrite_topic;
            else
                topic = mMask + '/' + mWrite_topic;
            sampleClient.publish(topic, message);
            sampleClient.disconnect();
        } catch (MqttException me) {
            me.printStackTrace();
        }


        setmStatus(true);
    }

    void off() {
        int qos = 0;
        String clientId = "paho-java-client";
        String content = "0";
        try {
            final MqttClient sampleClient = new MqttClient("tcp://"+ mBroker, clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            sampleClient.connect(connOpts);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            String topic;
            if(mMask.equals(""))
                topic = mWrite_topic;
            else
                topic = mMask + '/' + mWrite_topic;
            sampleClient.publish(topic, message);
            sampleClient.disconnect();
        } catch (MqttException me) {
            me.printStackTrace();
        }

        setmStatus(false);
    }


}