package com.sunshineapp.cuong.sunshine;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sunshineapp.cuong.sunshine.data.WeatherContract;
import com.sunshineapp.cuong.sunshine.sync.SunshineSyncAdapter;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private static final int FORECAST_LOADER = 0;
    private ForecastAdapter adapters;

    private String mLocation;
    private String temperatureUnit;
    private int mListPosition = 0;

    public void setAdapterIsTwoPane(boolean isTwoPane) {
        if (adapters != null)
            adapters.setIsTwoPane(isTwoPane);
    }

    public static final String PreviousListPosition = "PreviousListPosition";
    private static final String metricSymbol = "\u2103";
    private static final String imperialSymbol = "\u2109";

    private ListView mListView;


    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        mLocation = sharedPref.getString(getString(R.string.location_key), "75074");

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragement, menu);
        //super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_test) {
            firtItemClick();

        }
        return super.onOptionsItemSelected(item);
    }

    private void firtItemClick(){
        if (adapters.isTwoPane()) {
            //set first item activated by default
            if (mListView.performItemClick(null, 0, 0))
                Log.d(LOG_TAG, "item clicked");
            else
                Log.d(LOG_TAG, "item not clicked");
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        adapters = new ForecastAdapter(getActivity(), null, 0);

        mListView = (ListView) rootView.findViewById(R.id.listview_forcast);

        if(savedInstanceState != null && savedInstanceState.containsKey(PreviousListPosition)) {
            mListPosition = savedInstanceState.getInt(PreviousListPosition);
            mListView.smoothScrollToPosition(mListPosition);
        }
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                mListPosition = position;
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(mListPosition);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity()).onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            locationSetting, cursor.getLong(COL_WEATHER_DATE)
                    ));
//                    Intent intent = new Intent(getActivity(), DetailActivity.class)
//                            .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
//                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
//                            ));
//                    startActivity(intent);
                }
            }
        });
        mListView.setAdapter(adapters);

        return rootView;

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        return new CursorLoader(
                getActivity()
                , weatherForLocationUri
                , FORECAST_COLUMNS
                , null
                , null
                , sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapters.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapters.swapCursor(null);
    }

    void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    private void updateWeather() {

        SunshineSyncAdapter.syncImmediately(getActivity());

        //calling service
        //Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
        //alarmIntent.putExtra(SunshineService.LOCATION_STRING_EXTRA, location);

        //PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
        //AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        //am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pi);


        //Intent intent = new Intent(getActivity(), SunshineService.class);
        //intent.putExtra(SunshineService.LOCATION_STRING_EXTRA, location);
        //getActivity().startService(intent);

        //FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        //String location = Utility.getPreferredLocation(getActivity());
        //weatherTask.execute(location);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(PreviousListPosition, mListPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        firtItemClick();//TODO: still not working as expected
    }
}