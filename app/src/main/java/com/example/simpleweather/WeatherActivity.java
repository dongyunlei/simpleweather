package com.example.simpleweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.FitWindowsViewGroup;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.simpleweather.service.AutoUpdateService;
import com.google.gson.Gson;

import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.weather.Weather;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.Forecast;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.ForecastBase;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

import static org.litepal.LitePalApplication.getContext;

public class WeatherActivity extends AppCompatActivity {

    private Weather weather;
    private String weatherId;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView windDirectionText;
    private TextView windPowerText;
    private TextView rainText;
    private TextView comfortText;
    private TextView carwashText;
    private TextView sportText;
    private ImageView bingPic;
    public SwipeRefreshLayout swipeRefreshLayout;
    public DrawerLayout drawerLayout;
    private Button navButton;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_weather);

        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        windDirectionText = findViewById(R.id.wind_direction);
        windPowerText = findViewById(R.id.wind_power_text);
        rainText = findViewById(R.id.rain_text);
        comfortText = findViewById(R.id.comfort_text);
        carwashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);
        bingPic = findViewById(R.id.bing_pic);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);

        //加载每日一图
        Glide.with(this).load("https://open.saintic.com/api/bingPic/").into(bingPic);

        //根据是否缓存加载界面
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherJson = preferences.getString("weather", null);
        if (weatherJson == null){
            weatherId = getIntent().getStringExtra("weatherId");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        } else {
            weather = new Gson().fromJson(weatherJson, Weather.class);
            weatherId = weather.getBasic().getCid();
            showWeatherInfo(weather);
        }

        //下拉刷新
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });

        //切换城市
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.START);
            }
        });
    }

    public void requestWeather(final String weatherId) {
        HeWeather.getWeather(getContext(), weatherId, new HeWeather.OnResultWeatherDataListBeansListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.e("WeatherActivity", throwable.getMessage());
                Toast.makeText(getContext(), "网络异常，请稍后再试", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onSuccess(List<Weather> list) {
                if (list.size() > 0){
                    weather = list.get(0);
                    if (weather != null && "ok".equals(weather.getStatus())){
                        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                        edit.putString("weather", new Gson().toJson(weather));
                        edit.apply();
                        showWeatherInfo(weather);
                    } else {
                        Toast.makeText(getContext(), "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "获取天气信息失败", Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.getBasic().getLocation();
        String updateTime = weather.getUpdate().getLoc().split(" ")[1];
        String degree = weather.getNow().getTmp() + "℃";
        String weatherInfo = weather.getNow().getCond_txt();

        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);

        forecastLayout.removeAllViews();
        for (ForecastBase forecast : weather.getDaily_forecast()){
            View view = LayoutInflater.from(getContext()).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView InfoText = view.findViewById(R.id.info_text);
            //TextView maxText = view.findViewById(R.id.max_text) ;
            //TextView minText = view.findViewById(R.id.min_text);
            TextView maxMinText = view.findViewById(R.id.max_min_text);

            dateText.setText(forecast.getDate());
            InfoText.setText(forecast.getCond_txt_d());
            //maxText.setText(forecast.getTmp_max()+" ℃");
            //minText.setText(forecast.getTmp_min()+" ℃");
            maxMinText.setText(forecast.getTmp_max()+" ℃ ～ " + forecast.getTmp_min()+" ℃");

            forecastLayout.addView(view);
        }

        windDirectionText.setText(weather.getNow().getWind_dir());
        windPowerText.setText(weather.getNow().getWind_sc());
        rainText.setText(weather.getNow().getPcpn());

        comfortText.setText("舒适指数：" + weather.getLifestyle().get(0).getBrf() + "\n\n" + weather.getLifestyle().get(0).getTxt());
        carwashText.setText("洗车指数：" + weather.getLifestyle().get(6).getBrf() + "\n\n" + weather.getLifestyle().get(6).getTxt());
        sportText.setText("运动指数：" + weather.getLifestyle().get(3).getBrf() + "\n\n" + weather.getLifestyle().get(3).getTxt());

        weatherLayout.setVisibility(View.VISIBLE);

        //开启后台自动更新服务
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
}
