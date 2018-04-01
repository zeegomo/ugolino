package com.example.android.ugolino;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class InteractFragment extends Fragment {

    public InteractFragment() {
        // Required empty public constructor
    }

    public static ArrayList<Device> interact_devices = /*MainActivity.interact_devices*/new ArrayList<>();

    //Device visualizer
    public static InteractAdapter adapter;
    public static WebView webView;
    public static ListView listView;
    public static TextView instructionsTextView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_main, container, false);

        //View initialization
        webView = (WebView) rootView.findViewById(R.id.webview);
        adapter = new InteractAdapter(getActivity(), interact_devices);
        listView = (ListView) rootView.findViewById(R.id.list_item);
        listView.setAdapter(adapter);
        instructionsTextView = (TextView) rootView.findViewById(R.id.instructions_textView);

        return rootView;
    }

    public static void dataNotify(ArrayList<Device> device) {
        interact_devices = device;
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Log.e("notify", "InteractFrament");
        Log.e("interact_device" + interact_devices, "InteractFrament");
        Log.e("interact_devices size: " + interact_devices.size(), "InteractFrament");
    }

    @Override
    public void onResume() {
        super.onResume();
        //dataNotify(interact_devices);
        interact_devices = MainActivity.interact_devices;
        adapter = new InteractAdapter(getActivity(), interact_devices);
        //ListView listView = (ListView) getActivity().findViewById(R.id.list_item);
        listView.setClickable(true);
        listView.setAdapter(adapter);

        adapter.notifyDataSetChanged();
        listView.requestFocus();
        if (interact_devices.size() > 0) {
            listView.setVisibility(View.VISIBLE);
            instructionsTextView.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.GONE);
            instructionsTextView.setVisibility(View.VISIBLE);
        }
    }
}
