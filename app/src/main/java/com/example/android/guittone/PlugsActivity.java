package com.example.android.guittone;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Giacomo on 14/02/2017.
 */

public class PlugsActivity extends AppCompatActivity{

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_category);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new MainFragment())
                    .commit();
        }
}
