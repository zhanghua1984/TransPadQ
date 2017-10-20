package cn.transpad.transpadui.http;

import android.util.Log;

import cn.transpad.transpadui.entity.Aqi;
import cn.transpad.transpadui.entity.Weather;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Kongxiaojun on 2015/3/23.
 */
public class WeatherApi {

    private static final String TAG = "WeatherApi";

    private static final String endpoint = "http://apistore.baidu.com/microservice";

    private static WeatherApi ourInstance = new WeatherApi();

    public static WeatherApi getInstance() {
        return ourInstance;
    }

    private RestAdapter restAdapter;

    private WeatherApiService service;

    private WeatherApi() {
        restAdapter = new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        service = restAdapter.create(WeatherApiService.class);
    }

    interface WeatherApiService {
        @GET("/weather")
        void getWeatherByCityName(@Query(value = "cityname",encodeValue = false) String cityName, Callback<Weather> cb);

        @GET("/aqi")
        void getAqiByCityName(@Query(value = "city",encodeValue = false) String cityName, Callback<Aqi> cb);
    }

    public void getWeatherByCityName(String cityName, Callback<Weather> cb) {
        Log.v(TAG,"getWeatherByCityName");
        service.getWeatherByCityName(cityName, cb);
    }

    public void getAqiByCityName(String cityName, Callback<Aqi> cb) {
        service.getAqiByCityName(cityName, cb);
    }

}
