package com.example.android.ugolino;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import static com.example.android.ugolino.MainActivity.read_devices;

/**
 * Created by zeegomo on 27/03/18.
 */

public class MqttThread{
    private String broker;
    private String id = "ugolino";
    private String mask;
    private Context context;
    private MqttAndroidClient mqttAndroidClient;

    MqttThread(String broker, Context context, String mMask) {
        this.broker = broker;
        this.context = context;
        this.mask = mMask;
        this.mask = mMask;
        mqttAndroidClient =  new MqttAndroidClient(context, "tcp://" + broker, "id");
    }

    String getBroker(){
        return this.broker;
    }

    String getMask(){
        return  this.mask;
    }

    boolean isConnected(){
        return  mqttAndroidClient.isConnected();
    }


    void close(){
        try{
        mqttAndroidClient.disconnect();
        //mqttAndroidClient.close();
        }catch (MqttException e){
            e.printStackTrace();
        }
    }

    void connect(){
        final String mMask;
        if (this.mask.equals(""))
            mMask = "#";
        else
            mMask = this.mask + "/#";
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.e("Message arrived","mqtt thread");
                //updateData(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        try{
            mqttAndroidClient.connect(new MqttConnectOptions(), null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    try {
                        //Log.e("MASK:" +mask, "MQTT_THREAD");
                        //Log.e("MASK:" +mqttAndroidClient.getServerURI(), "MQTT_THREAD");
                        mqttAndroidClient.subscribe(mMask, 0);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                }
            });
        }catch(MqttException e){
            e.printStackTrace();
        }
    }



    void updateData(String topic, MqttMessage message) {
        int length = read_devices.size();
        for (int i = 0; i < length; i++) {

            String deviceTopic;
            Device currentDevice = read_devices.get(i);
            if (currentDevice.getmMask().equals(""))
                deviceTopic = currentDevice.getmRead_topic();
            else
                deviceTopic = currentDevice.getmMask() + '/' + currentDevice.getmRead_topic();

            Log.e("deviceTopic" + deviceTopic, "updateData");
            Log.e("topic" + topic, "updateData");
            if ((deviceTopic).equals(topic)) //TODO control if effective
                read_devices.get(i).setmRead(message.toString());
        }
        ReadFragment.dataNotify(read_devices);
    }

}
