package com.example.naveenkumark.sunshine;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by naveenkumark on 10/27/16.
 */

public class WeatherDataParser {
    public static double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex)
            throws JSONException {



        // TODO: add parsing code here
        JSONObject weather = new JSONObject(weatherJsonStr);
        JSONArray dayList = weather.getJSONArray("list");
        if(dayIndex < dayList.length()){
            JSONObject dayData = dayList.getJSONObject(dayIndex);
            JSONObject temp = dayData.getJSONObject("temp");
            return temp.getDouble("max");
        }
        return -1;
    }

}
