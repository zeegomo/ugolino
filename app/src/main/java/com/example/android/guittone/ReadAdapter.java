package com.example.android.guittone;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.os.AsyncTask;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import static android.view.View.GONE;

/**
 * Created by ${Giacomo} on ${26/12/2016}
 */

public class ReadAdapter extends ArrayAdapter<Device> {

    Device currentDevice;

    public ReadAdapter(Activity context, ArrayList<Device> devices) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.

        super(context, 0, devices);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }


        // Get the {@link AndroidFlavor} object located ate this position in the list
        currentDevice = getItem(position);
        //final String OnUrl = currentDevice.getOnUrl();
        //final String OffUrl = currentDevice.getOffUrl();
        Switch statusSwitch = (Switch) listItemView.findViewById(R.id.on_switch);
        TextView readTextView = (TextView) listItemView.findViewById(R.id.read_value);
        if (currentDevice.getmType()) {
            readTextView.setVisibility(GONE);

            boolean response = currentDevice.getmStatus();
            if (response) {
                statusSwitch.setChecked(true);
            }
            if (!response) {
                statusSwitch.setChecked(false);
            }

            statusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        currentDevice.on();
                        Log.d("on", "MainActivity");
                        //Log.e(OnUrl, "onurl");
                    } else {
                        currentDevice.off();
                        Log.d("off", "MainActivity");
                        //Log.e(OffUrl, "onurl");
                    }
                }
            });
        } else {
            statusSwitch.setVisibility(GONE);
            readTextView.setText(currentDevice.getmRead());
        }

        ImageView imageView = (ImageView) listItemView.findViewById(R.id.image_id);
        imageView.setClickable(true);
        imageView.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the numbers View is clicked on.
            @Override
            public void onClick(View view) {
                Intent deviceIntent = new Intent(getContext(), DeviceActivity.class);
                deviceIntent.putExtra("position", position);
                deviceIntent.putExtra("type", currentDevice.getmType());
                Log.e("type: " + currentDevice.getmType(), "DeviceActivity");
                view.getContext().startActivity(deviceIntent);
            }
        });
        // Find the TextView in the list_item.xml layout with the ID version_name
        TextView nameTextView = (TextView) listItemView.findViewById(R.id.device_name);
        // Get the version name from the current AndroidFlavor object and
        // set this text on the name TextView
        nameTextView.setText(currentDevice.getmName());
        //}
        return listItemView;

    }
}

