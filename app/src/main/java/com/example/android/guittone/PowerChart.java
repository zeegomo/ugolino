package com.example.android.guittone;

/**
 * Created by Giacomo on 14/02/2017.
 */

public class PowerChart {

    private int power;
    private int day;
    private int month;

    public PowerChart(int pow, int mday, int mmonth) {
        power = pow;
        day = mday;
        month = mmonth;
    }

    public int getPower() {
        return power;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }
}
