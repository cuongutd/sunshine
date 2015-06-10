package com.sunshineapp.cuong.sunshine;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
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
import java.util.Locale;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();

    private ArrayAdapter<String> adapters;

    private ArrayList<String> items;

    private String zipCode;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        zipCode = "94043";
        //setMenuVisibility (true);

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragement, menu);
        //super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_refresh:
                new FetchWeatherTask().execute(zipCode);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //create a blank list to initiate the adapters, this adapters will then be refreshed by the AsyncTask
        items = new ArrayList<String>();

        adapters= new ArrayAdapter(getActivity(), R.layout.list_item_forcast, R.id.list_item_forcast_textview, items);

        ListView v = (ListView)rootView.findViewById(R.id.listview_forcast);

        v.setAdapter(adapters);

        //calling asyncTask to refresh the adapters list
        AsyncTask task = new FetchWeatherTask().execute(zipCode);

        v.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Context context = getActivity().getApplicationContext();
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, "Hello! " + position + ". id: " + id, duration);
                toast.show();
            }
        });

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Integer, String[]>{

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        private String getResponseJSON(String zipCode){
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority("api.openweathermap.org")
                        .appendPath("data")
                        .appendPath("2.5")
                        .appendPath("forecast")
                        .appendPath("daily")
                        .appendQueryParameter("mode", "json")
                        .appendQueryParameter("units", "metric")
                        .appendQueryParameter("cnt", "7")
                        .appendQueryParameter("q", zipCode)
                        .fragment("section-name");

                // Create the request to OpenWeatherMap, and open the connection
                URL url = new URL(builder.build().toString());

                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("GET");

                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                return forecastJsonStr;

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
        }

        protected String[] doInBackground(String... zipCodes){
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            // Will contain the raw JSON response as a string.
            String forecastJsonStr = getResponseJSON(zipCodes[0]);
            return parseJSON(forecastJsonStr);

        }

        @Override
        protected void onPostExecute(String[] result) {
            adapters.clear();
            adapters.addAll(new ArrayList < String > (Arrays.asList(result)));

        }

        private String[] parseJSON(String json){
            String[] returnList = new String[7];
            try{

                SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd", Locale.US);
                Time dayTime = new Time();
                dayTime.setToNow();

                // we start at the day returned by local time. Otherwise this is a mess.
                int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

                // now we work exclusively in UTC
                dayTime = new Time();
                JSONObject jObj = new JSONObject(json);

                JSONArray list = jObj.getJSONArray("list");

                if (list.length() > 0)
                    for (int i = 0; i < list.length(); i++){
                        JSONObject day = list.getJSONObject(i);
                        JSONObject temp = day.getJSONObject("temp");
                        double min = temp.getDouble("min");
                        double max = temp.getDouble("max");
                        JSONArray weather = day.getJSONArray("weather");
                        String main = weather.getJSONObject(0).getString("main");

                        long dateTime;
                        // Cheating to convert this to UTC time, which is what we want anyhow
                        dateTime = dayTime.setJulianDay(julianStartDay + i);
                        String date = sdf.format(dateTime);

                        returnList[i]=date + " - " + main + " - " + Math.round(max) + "/" + Math.round(min);
                        Log.d(LOG_TAG, returnList[i]);
                    }

            }catch(JSONException e){
                System.out.println(e.getStackTrace());
            }



            return returnList;
        }
    }
}