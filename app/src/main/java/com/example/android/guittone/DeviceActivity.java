package com.example.android.guittone;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import static com.example.android.guittone.MainActivity.adapter;
import static com.example.android.guittone.MainActivity.devices;

/**
 * Created by Giacomo on 04/01/2017.
 */

public class DeviceActivity extends AppCompatActivity {


    int position = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        position=getIntent().getIntExtra("extras",0);


        final TextView deviceName = (TextView) findViewById(R.id.device_name_activity);
        deviceName.setText(devices.get(position).getmName());


        Button deleteButton = (Button) findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the numbers View is clicked on.
            @Override
            public void onClick(View view) {
                android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(DeviceActivity.this);
                alert.setMessage("Are you sure?");
                alert.setTitle("Delete Device");

                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        devices.remove(position);
                        Save();
                        Intent mainIntent = new Intent(DeviceActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                    }
                });

                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });
                alert.show();
            }
        });

        Button modifyButton = (Button) findViewById(R.id.modify_button);
        modifyButton.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the numbers View is clicked on.
            @Override
            public void onClick(View view) {
                android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(DeviceActivity.this);
                final EditText edittext = new EditText(DeviceActivity.this);
                alert.setMessage("Enter Name");
                alert.setTitle("New Device");
                alert.setView(edittext);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String name = edittext.getText().toString();
                        devices.get(position).setmName(name);
                        deviceName.setText(devices.get(position).getmName());
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
        });

    }

    public void Save(){
        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(devices);
        Log.w(""+devices.size(),"array size on save");
        prefsEditor.putString("Devices", json);
        prefsEditor.commit();

    }
}
