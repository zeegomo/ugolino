package com.example.android.ugolino;

import android.content.Context;

import java.util.ArrayList;


class MqttHandler {
    static ArrayList<MqttThread> connections = new ArrayList<>();
    private Context context;

    MqttHandler(Context mContext) {
        this.context = mContext;
    }

    int getSize() {
        return connections.size();
    }

    void addConnection(Device device) {
        boolean found = false;
        int size = connections.size();
        for (int i = 0; i < size && !found; i++) {
            if (connections.get(i).getId().equals(device.getId()))
                found = true;
        }
        if (!found) {
                connections.add(new MqttThread(context, device.getId(), device.getmBroker(), device.isSecure()));
                connections.get(size).connect(device);
        }
    }

    private boolean search(MqttThread mqtt, ArrayList<Device> devices) {
        for (int i = 0; i < devices.size(); i++)
            if (mqtt.getId().equals(devices.get(i).getId()))
                return true;

        return false;
    }

    void updateConnections() {
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

    void closeAll(){
        for(int i = 0; i < connections.size(); i++){
            connections.get(i).close();
        }
    }

}
