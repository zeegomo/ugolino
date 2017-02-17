package com.example.android.guittone;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.TextView;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    public String newName = "";
    public static ArrayList<Device> devices = new ArrayList<>();
    String SAVE = "sirup";
    public static DeviceAdapter adapter;
    public static WebView webView;
    public static ListView listView;
    public static TextView instructionsTextView;
    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_main, container, false);
        //Toolbar myToolbar = (Toolbar) rootView.findViewById(R.id.my_toolbar);
        //myToolbar.setTitleTextAppearance(getActivity(), R.style.MyTitleTextAppearance);
        //((AppCompatActivity)getActivity()).setSupportActionBar(myToolbar);
        //setHasOptionsMenu(true);

        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Gson gson = new Gson();
        String json = appSharedPrefs.getString("Devices", "");
        devices.clear();
        if (json.equals("")) {
        } else {
            devices = gson.fromJson(json, new TypeToken<ArrayList<Device>>() {
            }.getType());
        }

        //View initialization
        webView = (WebView) rootView.findViewById(R.id.webview);
        adapter = new DeviceAdapter(getActivity(), devices);
        listView = (ListView) rootView.findViewById(R.id.list_item);
        listView.setAdapter(adapter);
        instructionsTextView = (TextView) rootView.findViewById(R.id.instructions_textView);


        return rootView;
    }

    public static void dataNotify(ArrayList<Device> device){
        devices = device;
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Log.e("non notifica un pe", "moro");

    }






    @Override
    public void onPause() {
        Save();
        super.onPause();
    }

    public void Save(){
        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(devices);
        prefsEditor.putString("Devices", json);
        prefsEditor.commit();

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("asoi", " forze non va una pe");
        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Gson gson = new Gson();
        String json = appSharedPrefs.getString("Devices", "");
        devices.clear();
        if (json.equals("")) {
        } else {
            devices = gson.fromJson(json, new TypeToken<ArrayList<Device>>() {
            }.getType());
        }
        Log.w("" + devices.size(), "array size on resume");
        adapter = new DeviceAdapter(getActivity(), devices);
        ListView listView = (ListView) getActivity().findViewById(R.id.list_item);
        listView.setClickable(true);
        listView.setAdapter(adapter);
        if(isNetworkAvailable()){
            //CheckAsyncTask task = new CheckAsyncTask();
            //task.execute();
        }else{
            Toast toast = Toast.makeText(getActivity(), "Could not connect to server - Check your internet connection", Toast.LENGTH_SHORT);
            toast.show();
        }
        adapter.notifyDataSetChanged();
        listView.requestFocus();
        if(devices.size()>0){
            listView.setVisibility(View.VISIBLE);
            instructionsTextView.setVisibility(View.GONE);
        }else{
            listView.setVisibility(View.GONE);
            instructionsTextView.setVisibility(View.VISIBLE);
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}
