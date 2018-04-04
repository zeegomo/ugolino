package com.example.android.ugolino;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

import static com.example.android.ugolino.MainActivity.read_devices;



class MqttThread{
    private String broker;
    private String id = "ugolino";
    private String mask;
    private MqttAndroidClient mqttAndroidClient;
    private MqttAndroidClient secureMqttAndroidClient;
    private MqttAndroidClient secureCertMqttAndroidClient;
    private Context context;
    //TODO safe storage
    private String user;
    private String password;

    MqttThread(String broker, Context context, String mMask) {
        this.broker = broker;
        this.mask = mMask;
        this.mask = mMask;
        this.context = context;
        this.password = "";
        this.user = "";
        mqttAndroidClient =  new MqttAndroidClient(context, "tcp://" + broker, id);
        secureMqttAndroidClient = new MqttAndroidClient(context, "ssl://" + broker + ":8883", id);
        secureCertMqttAndroidClient = new MqttAndroidClient(context, "ssl://" + broker + ":8884", id);
    }

    MqttThread(String broker, Context context, String mMask, String password, String user) {
        this.broker = broker;
        this.mask = mMask;
        this.mask = mMask;
        this.context = context;
        this.password = password;
        this.user = user;
        mqttAndroidClient =  new MqttAndroidClient(context, "tcp://" + broker, id);
        secureMqttAndroidClient = new MqttAndroidClient(context, "ssl://" + broker + ":8883", id);
        secureCertMqttAndroidClient = new MqttAndroidClient(context, "ssl://" + broker + ":8884", id);
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
        }catch (MqttException e){
            e.printStackTrace();
        }
    }

    void sslConnect(boolean pass){
        final String mMask;
        if (this.mask.equals(""))
            mMask = "#";
        else
            mMask = this.mask + "/#";

        secureMqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.e("Message arrived","mqtt thread");
                updateData(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        try{
            SslUtil.newInstance(context);
            MqttConnectOptions options = new MqttConnectOptions();
            //options.setSocketFactory(SslUtil.getInstance().getSocketFactory(R.raw.raw_key_file, "mykeystorePassword"));
            if(pass){
                if(password.equals("")){
                    Toast toast = Toast.makeText(context, "Password not set - ignoring auth", Toast.LENGTH_SHORT);
                    toast.show();}
                    else{
                    options.setPassword(password.toCharArray());
                    options.setUserName(user);
                }
            }
            options.setCleanSession(true);
            options.setConnectionTimeout(60);
            options.setKeepAliveInterval(60);

            secureMqttAndroidClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    secureMqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    try {
                        secureMqttAndroidClient.subscribe(mMask, 0);
                        updateReadDeviceStatus(broker, mask, true);
                    } catch (MqttException e) {
                        e.printStackTrace();
                        updateReadDeviceStatus(broker, mask, false);
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    updateReadDeviceStatus(broker, mask, false);
                }
            });
        }catch(MqttException e){

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
                updateData(topic, message);
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
                        mqttAndroidClient.subscribe(mMask, 0);
                        updateReadDeviceStatus(broker, mask, true);
                    } catch (MqttException e) {
                        e.printStackTrace();
                        updateReadDeviceStatus(broker, mask, false);
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    updateReadDeviceStatus(broker, mask, false);
                }
            });
        }catch(MqttException e){

            e.printStackTrace();
        }
    }



    private void updateData(String topic, MqttMessage message) {
        int length = read_devices.size();
        for (int i = 0; i < length; i++) {

            String deviceTopic;
            Device currentDevice = read_devices.get(i);
            if (currentDevice.getmMask().equals(""))
                deviceTopic = currentDevice.getmRead_topic();
            else
                deviceTopic = currentDevice.getmMask() + '/' + currentDevice.getmRead_topic();

            //Log.e("deviceTopic" + deviceTopic, "updateData");
            //Log.e("topic" + topic, "updateData");
            if ((deviceTopic).equals(topic))
                read_devices.get(i).setmRead(message.toString());
        }
        ReadFragment.dataNotify(read_devices);
    }

    private void updateReadDeviceStatus(String broker, String mask, boolean on) {
        ArrayList<Device> devices = MainActivity.read_devices;
        for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).getmBroker().equals(broker) && devices.get(i).getmMask().equals(mask)) {
                if (on)
                    devices.get(i).setmStatus(true);
                else
                    devices.get(i).setmStatus(false);
            }
        }
        ReadFragment.dataNotify(devices);
    }

}
