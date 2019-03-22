package com.example.simpleweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import interfaces.heweather.com.interfacesmodule.view.HeConfig;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HeConfig.init("HE1903211533381332", "ebc39ed1620a423bb57350ac566cb047");
        HeConfig.switchToFreeServerNode();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherJson = preferences.getString("weather", null);
        if (weatherJson != null){
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
