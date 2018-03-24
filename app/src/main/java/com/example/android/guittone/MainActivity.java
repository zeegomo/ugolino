package com.example.android.guittone;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import static com.example.android.guittone.R.menu.toolbar;


public class MainActivity extends AppCompatActivity {

    //List of devices meant to send data to Arduino
    public static ArrayList<Device> interact_devices = new ArrayList<>();
    //List of devices meant to retrieve data from Arduino
    public static ArrayList<Device> read_devices = new ArrayList<>();
    public static WebView webView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar3);
        myToolbar.setTitleTextAppearance(getApplicationContext(), R.style.MyTitleTextAppearance);
        setSupportActionBar(myToolbar);

        //Save();
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        UgolinoFragmentPagerAdapter gadapter = new UgolinoFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(gadapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json_interact = appSharedPrefs.getString("interact_devices", "");
        interact_devices.clear();
        if (json_interact.equals("")) {
        } else {
            interact_devices = gson.fromJson(json_interact, new TypeToken<ArrayList<Device>>() {
            }.getType());
        }

        String json_read = appSharedPrefs.getString("read_devices", "");
        read_devices.clear();
        if (json_interact.equals("")) {
        } else {
            read_devices = gson.fromJson(json_interact, new TypeToken<ArrayList<Device>>() {
            }.getType());
        }
        //View initialization
        webView = (WebView) findViewById(R.id.webview);

        //MQTT Callback

        final MqttAndroidClient mqttAndroidClient;
        final String serverUri = "tcp://test.mosquitto.org:1883";
        String clientId = "ExampleAndroidClient";
        final String subscriptionTopic = "read_devices/#";
        final String publishTopic = "banana";
        final String publishMessage = "I'm alive";
        String topic = "read_devices";

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
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

    }

    void updateData(String topic, MqttMessage message){
        int length = read_devices.size();
        for(int i = 0; i < length; i++){
            if((read_devices.get(i).getmMask() + '/' +read_devices.get(i).getmRead_topic()).equals(topic)) //TODO control if effective
                read_devices.get(i).setmRead(message.toString());
        }
        ReadFragment.dataNotify(read_devices);
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_switch:
                AddWriteDevice();
                //AddDevice();
                //InteractFragment.dataNotify(interact_devices);
                //ReadFragment.dataNotify(read_devices);
                return true;

            case R.id.action_info:

                return true;

            case R.id.action_add_read:
                AddReadDevice();

            case R.id.power:


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void AddReadDevice(){
        android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(this);
        alert.setMessage("Specify read address");
        alert.setTitle("New Widget");

        //Creating Dialog layout
        final LinearLayout linear = new LinearLayout(MainActivity.this);
        linear.setOrientation(LinearLayout.VERTICAL);

        final EditText topicEditText = new EditText(MainActivity.this);
        topicEditText.setHint("topic");
        topicEditText.setGravity(Gravity.CENTER);

        final EditText maskEditText = new EditText(MainActivity.this);
        maskEditText.setHint("mask");
        maskEditText.setGravity(Gravity.CENTER);

        final EditText brokerEditText = new EditText(MainActivity.this);
        brokerEditText.setHint("broker");
        brokerEditText.setGravity(Gravity.CENTER);


        linear.addView(topicEditText);
        linear.addView(maskEditText);
        linear.addView(brokerEditText);

        alert.setView(linear);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String topic = topicEditText.getText().toString();
                String mask = maskEditText.getText().toString();
                //topic = mask + '/' + topic;
                String broker = brokerEditText.getText().toString();
                read_devices.add(new Device("New Read Widget",mask, topic , broker, "",false));
                ReadFragment.dataNotify(read_devices);
                Log.d("read_device" + read_devices, "AddDevice");
                Log.e("server: " +topic,"ADD READ");
                Save();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });
        alert.show();
    }


    public void AddWriteDevice(){
        android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(this);
        alert.setMessage("Specify write address");
        alert.setTitle("New Widget");

        //Creating Dialog layout
        final LinearLayout linear = new LinearLayout(MainActivity.this);
        linear.setOrientation(LinearLayout.VERTICAL);
        //linear.setHorizontalGravity(0);

        final EditText topicEditText = new EditText(MainActivity.this);
        topicEditText.setHint("topic");
        topicEditText.setGravity(Gravity.CENTER);

        final EditText maskEditText = new EditText(MainActivity.this);
        maskEditText.setHint("mask");
        maskEditText.setGravity(Gravity.CENTER);

        final EditText brokerEditText = new EditText(MainActivity.this);
        brokerEditText.setHint("broker");
        brokerEditText.setGravity(Gravity.CENTER);


        linear.addView(topicEditText);
        linear.addView(maskEditText);
        linear.addView(brokerEditText);

        alert.setView(linear);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String topic = topicEditText.getText().toString();
                String mask = maskEditText.getText().toString();
                String broker = brokerEditText.getText().toString();
                //topic = mask + '/' + topic;
                interact_devices.add(new Device("New Write Widget",mask, "" , broker, topic,true));
                InteractFragment.dataNotify(read_devices);
                Log.d("intereat_device" + interact_devices, "AddDevice");
                Log.e("topic: " +topic,"ADD INTERACT");
                Save();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });
        alert.show();
    }

    public void AddDevice() {
        /*
        android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(this);
        alert.setMessage("Choose the widget type");
        alert.setTitle("New Widget");

        alert.setPositiveButton("Switch", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                /*String url = "http://192.168.1.1";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse(url));
                startActivity(browserIntent);*//*
                interact_devices.add(new Device("New Switch Widget", "mario", "test.mosquitto.org", "banana", true));
                InteractFragment.dataNotify(interact_devices);
                Save();
                Log.d("interact_device" + interact_devices, "AddDevice");
            }
        });

        alert.setNegativeButton("Read", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
                read_devices.add(new Device("New Read Widget", "read_devices", "test.mosquitto.org", "banana", false));
                ReadFragment.dataNotify(read_devices);
                Log.d("read_device" + read_devices, "AddDevice");
                Save();
            }
        });
        alert.show();
    */
    }

    @Override
    protected void onPause() {
        Save();
        super.onPause();
    }

    public void Save() {
        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        Gson gson = new Gson();


        //Saving interact_devices
        String interact_json = gson.toJson(interact_devices);
        prefsEditor.putString("interact_devices", interact_json);

        //Saving read_devices
        String read_json = gson.toJson(read_devices);
        prefsEditor.putString("read_devices", read_json);
        prefsEditor.apply();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(this, "Could not connect to server - Check your internet connection", Toast.LENGTH_SHORT);
            toast.show();
        }
        Log.e("Reasume", "MainActivity");
        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();

        //Loading interact_devices from memory
        String interact_json = appSharedPrefs.getString("interact_devices", "");
        interact_devices.clear();
        if (interact_json.equals("")) {
        } else {
            interact_devices = gson.fromJson(interact_json, new TypeToken<ArrayList<Device>>() {
            }.getType());
        }
        Log.e("interact_size: " + interact_devices.size(), "MainActivity");
        //Loading read_devices from memory
        String read_json = appSharedPrefs.getString("read_devices", "");
        read_devices.clear();
        if (read_json.equals("")) {
        } else {
            read_devices = gson.fromJson(read_json, new TypeToken<ArrayList<Device>>() {
            }.getType());
        }
        Log.e("read_size: " + read_devices.size(), "MainActivity");
        //InteractFragment.dataNotify(interact_devices);
        //ReadFragment.dataNotify(read_devices);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
