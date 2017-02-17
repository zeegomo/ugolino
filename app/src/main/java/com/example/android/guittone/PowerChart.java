package com.example.android.guittone;

/**
 * Created by Giacomo on 14/02/2017.
 */

public class PowerChart {

    private double power;
    private int day;
    private int month;

    public PowerChart(double pow, int mday, int mmonth) {
        power = pow;
        day = mday;
        month = mmonth;
    }

    public double getPower() {
        return power;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }
}
