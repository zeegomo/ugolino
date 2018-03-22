package com.example.android.guittone;

/**
 * Created by Giacomo on 21/03/2018.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReadFragment extends Fragment {

    public ReadFragment() {
        // Required empty public constructor
    }

    public static ArrayList<Device> read_devices = MainActivity.read_devices;

    //Device visualizer
    public static ReadAdapter adapter;
    public static WebView webView;
    public static ListView listView;
    public static TextView instructionsTextView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_main, container, false);

        //View initialization
        webView = (WebView) rootView.findViewById(R.id.webview);
        adapter = new ReadAdapter(getActivity(), read_devices);
        listView = (ListView) rootView.findViewById(R.id.list_item);
        listView.setAdapter(adapter);
        instructionsTextView = (TextView) rootView.findViewById(R.id.instructions_textView);

        final MqttAndroidClient mqttAndroidClient;
        final String serverUri = "tcp://test.mosquitto.org:1883";
        String clientId = "ExampleAndroidClient";
        final String subscriptionTopic = "read_devices";
        final String publishTopic = "banana";
        final String publishMessage = "I'm alive";

        mqttAndroidClient = new MqttAndroidClient(getContext(), serverUri, clientId);
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
                updateData(topic,message);
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
                        mqttAndroidClient.subscribe(subscriptionTopic, 0);
                    } catch (MqttException e){
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
        return rootView;
    }

    void updateData(String topic, MqttMessage message){
        int length = read_devices.size();
        for(int i = 0; i < length; i++){
            if(read_devices.get(i).getmRead_topic().equals(topic))
                read_devices.get(i).setmRead(message.toString());
        }
        dataNotify(read_devices);
    }

    public static void dataNotify(ArrayList<Device> device) {
        read_devices = MainActivity.read_devices;
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Log.e("notify", "ReadFrament");
        Log.e("read_device" + read_devices, "ReadFrament");
        Log.e("read_devices size: " + read_devices.size(), "ReadFrament");
    }

    @Override
    public void onResume() {
        super.onResume();
        //dataNotify(MainActivity.read_devices);
        read_devices = MainActivity.read_devices;
        adapter = new ReadAdapter(getActivity(), read_devices);
        listView.setClickable(true);
        listView.setAdapter(adapter);

        adapter.notifyDataSetChanged();
        listView.requestFocus();
        if (read_devices.size() > 0) {
            listView.setVisibility(View.VISIBLE);
            instructionsTextView.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.GONE);
            instructionsTextView.setVisibility(View.VISIBLE);
        }

    }

}
