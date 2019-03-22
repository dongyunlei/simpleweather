package com.example.simpleweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.weather.Weather;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

import static org.litepal.LitePalApplication.getContext;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, 0);
        manager.cancel(pendingIntent);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);

        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherJson = preferences.getString("weather", null);
        if (weatherJson != null){
            Weather weather = new Gson().fromJson(weatherJson, Weather.class);
            String weatherId = weather.getBasic().getCid();

            HeWeather.getWeather(getContext(), weatherId, new HeWeather.OnResultWeatherDataListBeansListener() {
                @Override
                public void onError(Throwable throwable) {
                    Log.e("WeatherActivity", throwable.getMessage());

                }

                @Override
                public void onSuccess(List<Weather> list) {
                    if (list.size() > 0) {
                        Weather weather = list.get(0);
                        if (weather != null && "ok".equals(weather.getStatus())){
                            SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                            edit.putString("weather", new Gson().toJson(weather));
                            edit.apply();
                        }
                    }
                }
            });
        }
    }

}
