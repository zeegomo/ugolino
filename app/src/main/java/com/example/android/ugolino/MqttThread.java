package com.example.android.ugolino;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import static com.example.android.ugolino.MainActivity.read_devices;

/**
 * Created by zeegomo on 27/03/18.
 */

public class MqttThread extends Thread {
    private String broker;
    private String id = "ugolino";
    private String mask;
    private Context context;

    MqttThread(String broker, Context context, String mask) {
        this.broker = broker;
        this.context = context;
        if (mask.equals(""))
            this.mask = "#";
        else
            this.mask = mask;
    }

    public void run() {

        final MqttAndroidClient mqttAndroidClient;
        mqttAndroidClient = new MqttAndroidClient(context, broker, id);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.e("MESSAGE ARRIVED" + message, "MESSAGE ARRIVED");
                updateData(topic, message);
                //mqttAndroidClient.publish(publishTopic,new MqttMessage(publishMessage.getBytes()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);


        try {
            //addToHistory("Connecting to " + serverUri);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    try {
                        mqttAndroidClient.subscribe(mask, 0);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                }
            });


        } catch (MqttException ex) {
            ex.printStackTrace();
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
