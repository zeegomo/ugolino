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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
        GuittoneFragmentPagerAdapter gadapter = new GuittoneFragmentPagerAdapter(getSupportFragmentManager());
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
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:

                AddDevice();
                //InteractFragment.dataNotify(interact_devices);
                //ReadFragment.dataNotify(read_devices);
                return true;

            case R.id.action_info:

                return true;

            case R.id.action_reconnect:
                android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(this);
                alert.setMessage("Make sure you are connected to GuittoneWiFi");
                alert.setTitle("Reconnect device");

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String url = "http://192.168.1.1";
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                        browserIntent.setData(Uri.parse(url));
                        startActivity(browserIntent);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });
                alert.show();
                return true;

            case R.id.power:


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    public void AddDevice() {

        android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(this);
        alert.setMessage("Choose the widget type");
        alert.setTitle("New Widget");

        alert.setPositiveButton("Switch", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                /*String url = "http://192.168.1.1";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse(url));
                startActivity(browserIntent);*/
                interact_devices.add(new Device("New Switch Widget", "", "", "", true));
                InteractFragment.dataNotify(interact_devices);
                Save();
                Log.d("interact_device" + interact_devices, "AddDevice");
            }
        });

        alert.setNegativeButton("Read", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
                read_devices.add(new Device("New Read Widget", "", "", "", false));
                ReadFragment.dataNotify(read_devices);
                Log.d("read_device" + read_devices, "AddDevice");
                Save();
            }
        });
        alert.show();

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
