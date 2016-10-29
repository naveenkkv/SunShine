package com.example.naveenkumark.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    public static final String TAG = ForecastFragment.class.getSimpleName();
    public static final String DAILY_FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";

    ArrayAdapter<String> mForecastAdapter;
    ListView mForecastList;
    public ForecastFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);
        String[] data = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };

        ArrayList<String> weekForecast = new ArrayList<>();
        weekForecast.addAll(Arrays.asList(data));
        mForecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weekForecast);
        mForecastList = (ListView)rootView.findViewById(R.id.listview_forecast);
        mForecastList.setAdapter(mForecastAdapter);
        mForecastList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Toast.makeText(getActivity(), mForecastAdapter.getItem(i), Toast.LENGTH_SHORT).show();
            }
        });
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:
                Toast.makeText(getActivity(), "Refreshing...", Toast.LENGTH_SHORT).show();
                Map<String, String> weatherReq = new HashMap<>();
                weatherReq.put("q", "94043");
                weatherReq.put("mode", "json");
                weatherReq.put("units", "metric");
                weatherReq.put("cnt", "7");
                weatherReq.put("appid", "6d2e4b49c5121e44de38aa79ae483ba6");
                new FetchWeatherTask().execute(weatherReq);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class FetchWeatherTask extends AsyncTask<Map<String, String>, Void, String[]> {
        private String mResponseFormat;
        private String mUnits;
        private int mDayCount;

        private final String TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(Map<String, String>... maps) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;


            if(maps.length < 1) return  null;

            try {

                //TBD: try using buidupon()

                Uri builtUri = Uri.parse(DAILY_FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter("q", maps[0].get("q"))
                        .appendQueryParameter("mode", maps[0].get("mode"))
                        .appendQueryParameter("units", maps[0].get("units"))
                        .appendQueryParameter("cnt", maps[0].get("cnt"))
                        .appendQueryParameter("appid", maps[0].get("appid")).build();
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                Uri.Builder builder = new Uri.Builder();


                builder.scheme("http");
                builder.authority("api.openweathermap.org");
                builder.appendPath("data");
                builder.appendPath("2.5");
                builder.appendPath("forecast");
                builder.appendPath("daily");
                builder.appendQueryParameter("q", maps[0].get("q"));
                builder.appendQueryParameter("mode", maps[0].get("mode"));
                builder.appendQueryParameter("units", maps[0].get("units"));
                builder.appendQueryParameter("cnt", maps[0].get("cnt"));
                builder.appendQueryParameter("appid", maps[0].get("appid"));

                String urlString = builtUri.toString();
                Log.v(TAG, "Request URL: " + urlString);
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection)url.openConnection();
                //urlConnection.connect();
                InputStream is = urlConnection.getInputStream();
                if(null == is){
                    Log.e(TAG, "Input stream is empty");
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer buffer = new StringBuffer();
                while( (line = reader.readLine()) != null){
                    buffer.append(line);
                    buffer.append("\n");//just for readability
                }
                if(buffer.length() == 0){
                    // Stream was empty.  No point in parsing.
                    Log.i(TAG, "Empty buffer");
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.v(TAG, "doInBackground: " + "\n" + forecastJsonStr);

                try {
                    return getWeatherDataFromJson(forecastJsonStr, Integer.parseInt(maps[0].get("cnt")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error ", e);
                // If the code didn't successfully get the Weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } finally {
                if(urlConnection != null){
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }

            return null;
        }


        @Override
        protected void onPostExecute(String[] strings) {
            //super.onPostExecute(strings);
            /*for (String s : strings) {
                Log.v(TAG, "onPostExecute - Forecast entry: " + s);
            }*/
            mForecastAdapter.clear();
            mForecastAdapter.addAll(strings);
            //mForecastAdapter.notifyDataSetChanged();
        }

        /* The date/time conversion code is going to be moved outside the asynctask later,
                 * so for convenience we're breaking it out into its own method now.
                 */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            Date date = new Date(time * 1000L);
            return shortenedDateFormat.format(date);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {
            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

           /* GregorianCalendar calendar = new GregorianCalendar();
            //calendar.
            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();*/

            String[] resultStrs = new String[numDays];
            for (int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime = dayForecast.getLong("dt");
                // Cheating to convert this to UTC time, which is what we want anyhow
                //dateTime = dayTime.setJulianDay(julianStartDay + i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }


            return resultStrs;
        }

    }
}
