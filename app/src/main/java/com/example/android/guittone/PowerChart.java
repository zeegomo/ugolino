package com.example.android.guittone;

/**
 * Created by Giacomo on 14/02/2017.
 */

public class PowerChart {

    private double power;
    private  long time;

    public PowerChart(double pow, long date) {
        power = pow;
        time = date;
    }

    public double getPower() {
        return power;
    }

    public long getTime() {
        return time;
    }
}
