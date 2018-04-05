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

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.example.android.ugolino.MainActivity.decryptor;
import static com.example.android.ugolino.MainActivity.read_devices;


class MqttThread {
    private String broker;
    private String id;
    private String mask;
    private MqttAndroidClient mqttAndroidClient;
    private MqttAndroidClient secureMqttAndroidClient;
    //private MqttAndroidClient secureCertMqttAndroidClient;
    private Context context;
    private boolean secure;
    //TODO safe storage
    private String user;
    private String password;

    //Constructor
    //Password is used only if initialized and only in secure mode
    MqttThread(String broker, Context context, String mMask, String password, String user, String id, boolean secure) {
        this.broker = broker;
        this.mask = mMask;
        this.mask = mMask;
        this.context = context;
        this.password = password;
        this.user = user;
        this.secure = secure;
        this.id = id;
        mqttAndroidClient = new MqttAndroidClient(context, "tcp://" + broker, id + "@Ugolino");
        secureMqttAndroidClient = new MqttAndroidClient(context, "ssl://" + broker + ":8883", id + "@Ugolino");
        //secureCertMqttAndroidClient = new MqttAndroidClient(context, "ssl://" + broker + ":8884", id);


    }


    //GET METHODS
    String getBroker() {return this.broker;}
    String getMask() {return this.mask;}
    String getId() {return this.id;}

    private boolean isConnected() {
        if (secure)
            return mqttAndroidClient.isConnected();
        else
            return secureMqttAndroidClient.isConnected();
    }

    boolean isSecure() {return secure;}


    //-----------------------CONNECTIONS-----------------------------

    //Connect
    //Auto-detect if secure or unsecure mode was used
    void connect() {
        if (secure)
            this.sslConnect();
        else
            this.unsecureConnect();
    }

    //Disconnect
    //Auto-detect if secure or unsecure mode was used
    void close() {
        try {
            if (secure)
                secureMqttAndroidClient.disconnect();
            else
                mqttAndroidClient.disconnect();
            updateReadDeviceStatus(broker, mask, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }



    //private method for secure connect
    private void sslConnect() {
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
                Log.e("Message arrived", "mqtt thread");
                updateData(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        try {
            SslUtil.newInstance(context);
            MqttConnectOptions options = new MqttConnectOptions();
            //options.setSocketFactory(SslUtil.getInstance().getSocketFactory(R.raw.raw_key_file, "mykeystorePassword"));
            if (password == null || password.equals("")) {
                Toast toast = Toast.makeText(context, "Password not set - ignoring auth", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                //Password safe handling by AndroidKeyStore

                String password = null;
                try {
                    password = (decryptor
                            .decryptData(id, MainActivity.encryptor.getEncryption(), MainActivity.encryptor.getIv()));
                } catch (UnrecoverableEntryException | NoSuchAlgorithmException |
                        KeyStoreException | NoSuchPaddingException | NoSuchProviderException |
                        IOException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                }
                if (password == null){
                    Toast toast = Toast.makeText(context, "Password decryption error - logging without credentials", Toast.LENGTH_SHORT);
                    toast.show();
                }else{
                    options.setPassword(password.toCharArray());
                    options.setUserName(user);
                }
            }
            options.setCleanSession(true);
            options.setUserName("admin");
            options.setPassword("admin".toCharArray());
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
        } catch (MqttException e) {

            e.printStackTrace();
        }
        if (isConnected())
            updateReadDeviceStatus(broker, mask, true);
        else
            updateReadDeviceStatus(broker, mask, false);
    }

    //private method for unsecure connect
    private void unsecureConnect() {
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
                Log.e("Message arrived", "mqtt thread");
                updateData(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        try {

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
        } catch (MqttException e) {

            e.printStackTrace();
        }
    }



    //------------------DATA UPDATE-----------------------------

    //Notify new messages
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

    //Notify connection status
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
