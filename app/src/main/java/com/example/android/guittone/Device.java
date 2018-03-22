package com.example.android.guittone;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static android.view.View.GONE;

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
    private boolean mType;
    private String mRead;

    Device(String name, String read_topic, String broker, String write_topic, boolean Type) {
        mName = name;
        mStatus = false;
        mRead_topic = read_topic;
        mWrite_topic = write_topic;
        mRead = "";
        mBroker = broker;
        mType = Type;
    }

    //GET METHODS
    String getmName() {
        return this.mName;
    }

    String getmWrite_topic() {
        return this.mWrite_topic;
    }

    String getmRead_topic() {
        return this.mRead_topic;
    }

    String getmBroker() {return  this.mBroker;}

    boolean getmStatus() {
        return this.mStatus;
    }

    boolean getmType() {
        return this.mType;
    }

    String getmRead() {
        return this.mRead;
    }



    //SET METHODS
    void setmName(String name) {
        this.mName = name;
    }

    void setmStatus(Boolean status) {
        this.mStatus = status;
    }

    public void setmBroker(String mBroker) {
        this.mBroker = mBroker;
    }

    void setmRead(String read) {
        this.mRead = read;
    }

    public void on() {
        int qos = 0;
        String clientId = "paho-java-client";
        String content = "1";
        try {
            final MqttClient sampleClient = new MqttClient(mBroker, clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            //System.out.println("paho-client connecting to broker: " + broker);
            sampleClient.connect(connOpts);
            //System.out.println("paho-client connected to broker");
            //System.out.println("paho-client publishing message: " + content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(mWrite_topic, message);
            //System.out.println("paho-client message published");
            sampleClient.disconnect();
            //System.out.println("paho-client disconnected");
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
            final MqttClient sampleClient = new MqttClient(mBroker, clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            //System.out.println("paho-client connecting to broker: " + broker);
            sampleClient.connect(connOpts);
            //System.out.println("paho-client connected to broker");
            //System.out.println("paho-client publishing message: " + content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(mWrite_topic, message);
            //System.out.println("paho-client message published");
            sampleClient.disconnect();
            //System.out.println("paho-client disconnected");
        } catch (MqttException me) {
            me.printStackTrace();
        }

        setmStatus(false);
    }


}