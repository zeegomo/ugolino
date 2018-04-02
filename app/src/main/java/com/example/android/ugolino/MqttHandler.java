package com.example.android.ugolino;

import android.content.Context;

import java.util.ArrayList;


class MqttHandler {
    ArrayList<MqttThread> connections = new ArrayList<>();
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

    void addConnection(String broker, String mask) {
        boolean found = false;
        int size = connections.size();
        for (int i = 0; i < size && !found; i++) {
            if (broker.equals(connections.get(i).getBroker()) && mask.equals(connections.get(i).getMask()))
                found = true;
        }
        if (!found) {
            connections.add(new MqttThread(broker, context, mask));
            connections.get(size).connect();
            //if(connections.get(size).isConnected())
            //    updateReadDeviceStatus(broker, mask, true);
        }
    }

    /*
    void removeConnection(String broker, String mask) {
        int size = connections.size();
        for (int i = 0; i < size && !found; i++) {
            if (broker.equals(connections.get(i).getBroker()) && mask.equals(connections.get(i).getMask())) {
                connections.get(i).close();
                connections.remove(i);
            }
        }
    }*/

    private boolean search(MqttThread mqtt, ArrayList<String> broker, ArrayList<String> mask) {
        for (int i = 0; i < broker.size(); i++)
            if (mqtt.getBroker().equals(broker.get(i)) && mqtt.getMask().equals(mask.get(i)))
                return true;

        return false;
    }

    private ArrayList<String> getBrokers() {
        ArrayList<Device> devices = MainActivity.read_devices;
        ArrayList<String> broker = new ArrayList<>();
        for (int i = 0; i < devices.size(); i++) {
            broker.add(devices.get(i).getmBroker());
        }
        return broker;
    }


    private ArrayList<String> getMasks() {
        ArrayList<Device> devices = MainActivity.read_devices;
        ArrayList<String> masks = new ArrayList<>();
        for (int i = 0; i < devices.size(); i++) {
            masks.add(devices.get(i).getmMask());
        }
        return masks;
    }

    void updateConnections() {
        ArrayList<String> broker = getBrokers();
        ArrayList<String> mask = getMasks();

        for (int i = 0; i < broker.size(); i++) {
            addConnection(broker.get(i), mask.get(i));
        }

        for (int i = 0; i < connections.size(); i++) {
            if (!search(connections.get(i), broker, mask)) {
                connections.get(i).close();
                connections.remove(i);
            }
        }

    }

}
