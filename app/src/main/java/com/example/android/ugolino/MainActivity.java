package com.example.android.ugolino;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;

import static com.example.android.ugolino.R.menu.toolbar;


public class MainActivity extends AppCompatActivity {

    //List of devices meant to send data
    public static ArrayList<Device> interact_devices = new ArrayList<>();
    //List of devices meant to retrieve data
    public static ArrayList<Device> read_devices = new ArrayList<>();
    public static WebView webView;
    public static MqttHandler mqttHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar3);
        myToolbar.setTitleTextAppearance(getApplicationContext(), R.style.MyTitleTextAppearance);
        setSupportActionBar(myToolbar);

        mqttHandler = new MqttHandler(getApplicationContext());
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
        if (!json_interact.equals("")) {
            interact_devices = gson.fromJson(json_interact, new TypeToken<ArrayList<Device>>() {
            }.getType());
        }

        String json_read = appSharedPrefs.getString("read_devices", "");
        read_devices.clear();
        if (!json_interact.equals("")) {
            read_devices = gson.fromJson(json_read, new TypeToken<ArrayList<Device>>() {
            }.getType());
        }

        //MqttThread mqtt = new MqttThread("mosquitto.ddns.net",getApplicationContext(), "read_devices");
        //mqtt.sslConnect(false);



        //mqttHandler.updateConnections();
        //View initialization
        webView = (WebView) findViewById(R.id.webview);
    }

    /*
    void updateData(String topic, MqttMessage message){
        int length = read_devices.size();
        for(int i = 0; i < length; i++){

            String deviceTopic;
            Device currentDevice = read_devices.get(i);
            if(currentDevice.getmMask().equals(""))
                deviceTopic = currentDevice.getmRead_topic();
            else
                deviceTopic = currentDevice.getmMask() + '/' + currentDevice.getmRead_topic();

            Log.e("deviceTopic" + deviceTopic, "updateData");
            Log.e("topic" + topic, "updateData");
            if((deviceTopic).equals(topic)) //TODO control if effective
                read_devices.get(i).setmRead(message.toString());
        }
        mqttHandler.updateConnections();
        ReadFragment.dataNotify();
    }*/


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
                //reload();
                InteractFragment.dataNotify(interact_devices);
                return true;

            case R.id.action_info:

                return true;

            case R.id.action_add_read:
                AddReadDevice();
                //reload();
                ReadFragment.dataNotify(read_devices);

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
                mqttHandler.addConnection(read_devices.get(read_devices.size()));
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
                Save();
                InteractFragment.dataNotify(interact_devices);
                Log.d("intereat_device" + interact_devices, "AddDevice");
                Log.e("topic: " +topic,"ADD INTERACT");

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });
        alert.show();
    }
    @Override
    protected void onPause() {
        Save();
        super.onPause();
    }

    public void reload(){
        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();

        //Loading interact_devices from memory
        String interact_json = appSharedPrefs.getString("interact_devices", "");
        interact_devices.clear();
        if (!interact_json.equals("")) {
            interact_devices = gson.fromJson(interact_json, new TypeToken<ArrayList<Device>>() {
            }.getType());
        }
        Log.e("interact_size: " + interact_devices.size(), "MainActivity");
        //Loading read_devices from memory
        String read_json = appSharedPrefs.getString("read_devices", "");
        read_devices.clear();
        if (!read_json.equals("")) {
            read_devices = gson.fromJson(read_json, new TypeToken<ArrayList<Device>>() {
            }.getType());
        }
        mqttHandler.updateConnections();
        Log.e("read_size: " + read_devices.size(), "MainActivity");
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
        if (!interact_json.equals("")) {
            interact_devices = gson.fromJson(interact_json, new TypeToken<ArrayList<Device>>() {
            }.getType());
        }
        Log.e("interact_size: " + interact_devices.size(), "MainActivity");
        //Loading read_devices from memory
        String read_json = appSharedPrefs.getString("read_devices", "");
        read_devices.clear();
        if (!read_json.equals("")) {
            read_devices = gson.fromJson(read_json, new TypeToken<ArrayList<Device>>() {
            }.getType());
        }
        Log.e("read_size: " + read_devices.size(), "MainActivity");
        mqttHandler.updateConnections();
        //mqttHandler.init();
        //InteractFragment.dataNotify(interact_devices);
        //ReadFragment.dataNotify(read_devices);
        for(int i = 0; i < mqttHandler.getSize(); i++){
            Log.e("mqttHandler: " + mqttHandler.connections.get(i).getBroker() + mqttHandler.connections.get(i).getMask(),"Main Activity");

        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
