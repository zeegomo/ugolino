package com.example.android.ugolino;


import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.example.android.ugolino.MainActivity.decryptor;


/**
 * Created by ${Giacomo} on ${26/10/2016}
 */

class Device {


    private String mName;
    private boolean mStatus;
    private String mWrite_topic;
    private String id;
    private String mRead_topic;
    private String mBroker;
    private String mMask;
    private boolean mType;
    private String mRead;
    private boolean secure;

    //MQTT credentials
    private String password;
    private String user;
    private byte[] iv;


    Device(String name, String mask, String read_topic, String broker, String write_topic, boolean Type) {
        mName = name;
        id = String.valueOf(Math.random() % 1000000);
        mStatus = false;
        mRead_topic = read_topic;
        mWrite_topic = write_topic;
        mRead = "";
        mMask = mask;
        mBroker = broker;
        mType = Type;
        secure = false;
        user = "";
        password = null;
        iv = null;
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

    String getmBroker() {
        return mBroker;
    }

    String getmMask() {
        return mMask;
    }

    boolean getmStatus() {
        return mStatus;
    }

    boolean getmType() {
        return mType;
    }

    boolean isSecure() {
        return secure;
    }

    String getmRead() {
        return mRead;
    }

    String getPassword() {
        return password;
    }

    String getUser() {
        return user;
    }

    String getId() {
        return id;
    }

    byte[] getIv(){
        return this.iv;
    }


    //SET METHODS
    void setmName(String name) {
        mName = name;
    }

    void setmStatus(Boolean status) {
        mStatus = status;
    }

    void setmWrite_topic(String topic) {
        mWrite_topic = topic;
    }

    void setmRead_topic(String topic) {
        mRead_topic = topic;
    }

    void setmBroker(String broker) {
        mBroker = broker;
    }

    void setIv(byte[] iv){this.iv = iv;}

    void setmRead(String read) {
        mRead = read;
    }

    void setmMask(String mask) {
        mMask = mask;
    }

    void setSecure(boolean sec) {
        secure = sec;
    }

    void setUser(String newUser) {
        user = newUser;
    }

    void setPassword(String password) {

        this.password = password;
    }


    void on(Context context) {
        if(secure)
            sslPublish(context,"1");
        else
            publish("1");

        setmStatus(true);
    }

    void off(Context context) {
       if(secure)
           sslPublish(context,"0");
       else
           publish("0");

        setmStatus(false);
    }


    void sslPublish(Context context, String content){
        int qos = 0;
        String clientId = id + "@Ugolino";
        //Log.e("topic: " + mWrite_topic + "mask:  " + mMask + " broker: " + mBroker, "DEVICE");
        try {
            final MqttClient sampleClient = new MqttClient("ssl://" + mBroker +":8883", clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            if (this.password == null || this.password.equals("")) {
                Toast toast = Toast.makeText(context, "Password not set - ignoring auth", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                //Password safe handling by AndroidKeyStore

                String decryptedPassword = null;
                try {
                    decryptedPassword = (decryptor
                            .decryptData(id, Base64.decode(this.password, Base64.DEFAULT), this.iv));
                } catch (UnrecoverableEntryException | NoSuchAlgorithmException |
                        KeyStoreException | NoSuchPaddingException | NoSuchProviderException |
                        IOException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                }
                if (decryptedPassword == null || decryptedPassword.equals("")){
                    Toast toast = Toast.makeText(context, "Password decryption error - logging without credentials", Toast.LENGTH_SHORT);
                    toast.show();
                }else{
                    connOpts.setPassword(decryptedPassword.toCharArray());
                    connOpts.setUserName(user);
                }
            }

            sampleClient.connect(connOpts);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            String topic;
            if (mMask.equals(""))
                topic = mWrite_topic;
            else
                topic = mMask + '/' + mWrite_topic;
            sampleClient.publish(topic, message);
            sampleClient.disconnect();
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }

    void publish(String content){
        int qos = 0;
        String clientId = id + "@Ugolino";
        Log.e("topic: " + mWrite_topic + "mask:  " + mMask + " broker: " + mBroker, "DEVICE");
        try {
            final MqttClient sampleClient = new MqttClient("tcp://" + mBroker, clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            sampleClient.connect(connOpts);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            String topic;
            if (mMask.equals(""))
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



}