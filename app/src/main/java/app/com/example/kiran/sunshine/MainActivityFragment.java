package app.com.example.kiran.sunshine;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import app.com.example.kiran.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private final String LOG_TAG                = MainActivityFragment.class.getSimpleName();
    String LocSettings                          = null; // To store the Location shared preference value
    ForecastAdapter mForcastAdapter             = null;

    //private String mLocation                    = null;

//    private String mLocation                    = null;
//    private final String FORECASTFRAGMENT_TAG   = "FFTAG";

    private static final int FORECAST_LOADER    = 0;

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
    static final int COL_WEATHER_ID             = 0;
    static final int COL_WEATHER_DATE           = 1;
    static final int COL_WEATHER_DESC           = 2;
    static final int COL_WEATHER_MAX_TEMP       = 3;
    static final int COL_WEATHER_MIN_TEMP       = 4;
    static final int COL_LOCATION_SETTING       = 5;
    static final int COL_WEATHER_CONDITION_ID   = 6;
    static final int COL_COORD_LAT              = 7;
    static final int COL_COORD_LONG             = 8;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mLocation = Utility.getPreferredLocation(getActivity());
        setHasOptionsMenu(true);
//        if (savedInstanceState == null) {
//            getFragmentManager().beginTransaction()
//                    .add(R.id.container, new MainActivityFragment(), FORECASTFRAGMENT_TAG)
//                    .commit();
//       }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forcastfragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            //Toast.makeText(getActivity(),"Hi",Toast.LENGTH_SHORT).show();
            updateWeather();
            return true;
        }
        if(id == R.id.action_map){
            openPreferedLocationInMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.fragment_main, container, false);
        View rootview = inflater.inflate(R.layout.fragment_main, container, false);

        String locationSetting = Utility.getPreferredLocation(getActivity());
        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        final Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
                null, null, null, sortOrder);
        mForcastAdapter = new ForecastAdapter(getActivity(), cur, 0);

        ListView listView = (ListView) rootview.findViewById(R.id.List_view_Forcast);

        listView.setAdapter(mForcastAdapter);

        //!-- List view Click Listner -- >
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                long l =  cursor.getLong(COL_WEATHER_DATE);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            ));
                    startActivity(intent);
                }

            }
        });
        return rootview;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
//    private String getReadableDateString(long time){
//        // Because the API returns a unix timestamp (measured in seconds),
//        // it must be converted to milliseconds in order to be converted to valid date.
//        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
//        return shortenedDateFormat.format(time);
//    }


    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    // since we read the location when we create the loader, all we need to do is restart things
    void onLocationChanged( ) {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }


    public void updateWeather()
    {
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(getString(R.string.pref_Location_key),
                getString(R.string.pref_Location_default));
        weatherTask.execute(location);
    }

    private void openPreferedLocationInMap(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String Location  = sharedPref.getString(getString(R.string.pref_Location_key), getString(R.string.pref_Location_default));

        Uri geoLocation =Uri.parse("geo:0,0?").buildUpon()
                            .appendQueryParameter("q",Location)
                            .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        PackageManager pm = getActivity().getPackageManager();
        final ResolveInfo mInfo = pm.resolveActivity(intent, 0);

        if(mInfo!=null)
        {
            startActivity(intent);
        }
        else
        {
            Log.d(LOG_TAG, "openPreferedLocationInMap : Map app not found");
        }
    }
//        @Override
//        public void onResume()
//        {
//            super.onResume();
//            String location = Utility.getPreferredLocation(getActivity() );
//            // update the location in our second pane using the fragment manager
//            if (location != null && !location.equals(mLocation))
//            {
//            //MainActivityFragment ff = (MainActivityFragment)getFragmentManager().findFragmentByTag(FORECASTFRAGMENT_TAG);
//            //if ( null != ff ) {
//                    onLocationChanged();
//           //}
//             mLocation = location;
//            }
//    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String LocationSetting = Utility.getPreferredLocation(getActivity());

        //Sort order: Ascending, by date
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate
                (LocationSetting,
                        System.currentTimeMillis());

        return new CursorLoader(getActivity(),
                weatherLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        mForcastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        mForcastAdapter.swapCursor(null);
    }
}
