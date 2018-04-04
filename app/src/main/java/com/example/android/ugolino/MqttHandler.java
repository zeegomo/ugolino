package com.example.android.ugolino;

import android.content.Context;

import java.util.ArrayList;


class MqttHandler {
    static ArrayList<MqttThread> connections = new ArrayList<>();
    private Context context;

    MqttHandler(Context mContext) {
        this.context = mContext;
    }

    /*
    void init(){
        for(int i = 0; i < connections.size(); i++)
            if(!connections.get(i).isConnected())
                connections.get(i).connect();
    }*/
    int getSize() {
        return connections.size();
    }

    void addConnection(Device device) {
        boolean found = false;
        int size = connections.size();
        for (int i = 0; i < size && !found; i++) {
            if (device.getmBroker().equals(connections.get(i).getBroker()) && device.getmMask().equals(connections.get(i).getMask()) && device.isSecure() == connections.get(i).isSecure())
                found = true;
        }
        if (!found) {
                connections.add(new MqttThread(device.getmBroker(), context, device.getmMask(), device.getPassword(), device.getUser(),device.isSecure()));
                connections.get(size).connect();
        }
    }

    private boolean search(MqttThread mqtt, ArrayList<Device> devices) {
        for (int i = 0; i < devices.size(); i++)
            if (mqtt.getBroker().equals(devices.get(i).getmBroker()) && mqtt.getMask().equals(devices.get(i).getmMask()) && mqtt.isSecure() == devices.get(i).isSecure())
                return true;

        return false;
    }

    void updateConnections() {
        //ArrayList<String> broker = getBrokers();
        //ArrayList<String> mask = getMasks();
        ArrayList<Device> devices = MainActivity.read_devices;

        for (int i = 0; i < devices.size(); i++) {
            addConnection(devices.get(i));
        }

        for (int i = 0; i < connections.size(); i++) {
            if (!search(connections.get(i), devices)) {
                connections.get(i).close();
                connections.remove(i);
            }
        }

    }

}
