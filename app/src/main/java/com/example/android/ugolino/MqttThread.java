package com.example.android.ugolino;

import android.content.Context;
import android.util.Base64;
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
    //private String broker;
    private String id;
    //private String mask;
    private MqttAndroidClient mqttAndroidClient;
    private MqttAndroidClient secureMqttAndroidClient;
    //private MqttAndroidClient secureCertMqttAndroidClient;
    private Context context;
    private boolean secure;
    //TODO safe storage
    /*private String user;
    private String password;
    private byte[] iv;*/

    //Constructor
    //Password is used only if initialized and only in secure mode
    /*MqttThread(String broker, Context context, String mMask, String password, String user, String id, byte[] iv, boolean secure) {
        this.broker = broker;
        this.mask = mMask;
        this.mask = mMask;
        this.context = context;
        this.password = password;
        this.user = user;
        this.secure = secure;
        this.id = id;
        this.iv = iv;
        mqttAndroidClient = new MqttAndroidClient(context, "tcp://" + broker, id + "@Ugolino");
        secureMqttAndroidClient = new MqttAndroidClient(context, "ssl://" + broker + ":8883", id + "@Ugolino");
        //secureCertMqttAndroidClient = new MqttAndroidClient(context, "ssl://" + broker + ":8884", id);


    }*/
    MqttThread(Context context, String id, String broker, boolean secure){
        this.context = context;
        this.id = id;
        this.secure = secure;
        mqttAndroidClient = new MqttAndroidClient(context, "tcp://" + broker, id + "@Ugolino");
        secureMqttAndroidClient = new MqttAndroidClient(context, "ssl://" + broker + ":8883", id + "@Ugolino");
    }


    //GET METHODS
    /*String getBroker() {return this.broker;}
    String getMask() {return this.mask;}*/
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
    void connect(Device device) {
        if (secure)
            this.sslConnect(device);
        else
            this.unsecureConnect(device);
    }

    //Disconnect
    //Auto-detect if secure or unsecure mode was used
    void close() {
        try {
            if (secure)
                secureMqttAndroidClient.disconnect();
            else
                mqttAndroidClient.disconnect();
            updateReadDeviceStatus(id, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }



    //private method for secure connect
    private void sslConnect(Device device) {

        String mask = device.getmMask();
        String user = device.getUser();
        String password = device.getPassword();
        String topic = device.getmRead_topic();

        byte[] iv = device.getIv();
        final String mMask;
        if(mask.equals(""))
            mMask = topic;
        else
            mMask = mask + "/" + topic;

        secureMqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.e("Message arrived", "mqtt thread");
                updateData(message);
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

                String decryptedPassword = null;
                try {
                    decryptedPassword = (decryptor
                            .decryptData(id, Base64.decode(password, Base64.DEFAULT), iv));
                } catch (UnrecoverableEntryException | NoSuchAlgorithmException |
                        KeyStoreException | NoSuchPaddingException | NoSuchProviderException |
                        IOException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                }
                if (decryptedPassword == null){
                    Toast toast = Toast.makeText(context, "Password decryption error - logging without credentials", Toast.LENGTH_SHORT);
                    toast.show();
                }else{
                    options.setPassword(decryptedPassword.toCharArray());
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
                        updateReadDeviceStatus(id, true);
                    } catch (MqttException e) {
                        e.printStackTrace();
                        updateReadDeviceStatus(id, false);
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    updateReadDeviceStatus(id, false);
                }
            });
        } catch (MqttException e) {

            e.printStackTrace();
        }
        if (isConnected())
            updateReadDeviceStatus(id, true);
        else
            updateReadDeviceStatus(id, false);
    }

    //private method for unsecure connect
    private void unsecureConnect(Device device) {

        String mask = device.getmMask();
        String topic = device.getmRead_topic();

        final String mMask;
        if(mask.equals(""))
            mMask = topic;
        else
            mMask = mask + "/" + topic;
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.e("Message arrived", "mqtt thread");
                updateData(message);
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
                        updateReadDeviceStatus(id, true);
                    } catch (MqttException e) {
                        e.printStackTrace();
                        updateReadDeviceStatus(id, false);
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    updateReadDeviceStatus(id, false);
                }
            });
        } catch (MqttException e) {

            e.printStackTrace();
        }
    }



    //------------------DATA UPDATE-----------------------------

    //Notify new messages
    private void updateData(MqttMessage message) {
        int length = MainActivity.read_devices.size();
        ArrayList<Device> devices = MainActivity.read_devices;
        for (int i = 0; i < length; i++) {
            devices.get(i);
            if (devices.get(i).getId().equals(this.id))
                devices.get(i).setmRead(message.toString());
        }
        ReadFragment.dataNotify(read_devices);
    }

    //Notify connection status
    private void updateReadDeviceStatus(String id, boolean on) {
        ArrayList<Device> devices = MainActivity.read_devices;
        for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).getId().equals(id)) {
                if (on)
                    devices.get(i).setmStatus(true);
                else
                    devices.get(i).setmStatus(false);
            }
        }
        ReadFragment.dataNotify(devices);
    }

}
