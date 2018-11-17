package com.angel.gooded;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.angel.gooded.moon.Moon;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;
import org.shredzone.commons.suncalc.MoonPosition;
import org.shredzone.commons.suncalc.SunPosition;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static com.angel.gooded.LocalDb.LAT;
import static com.angel.gooded.LocalDb.LONG;
import static com.angel.gooded.LocalDb.NAME;

public class MainMapHandler extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, DatePickerDialog.OnDateSetListener {
    TextView date, moon_up, moon_down, sun_up, sun_down;
    ImageButton prev, today, next;
    String currentDate = "";
    MapView mapView;
    GoogleMap map;
    Geocoder geocoder;
    List<Address> addresses;
    String current_city;
    SimpleDateFormat format1 = new SimpleDateFormat("EEEE MMMM dd,yyyy");
    MaterialSearchView searchView;
    SearchAdapter listAdapter;
    ArrayList<LatLonitem> latLonitems;
    private FusedLocationProviderClient mFusedLocationClient;

    Calendar cal, goldensunrise, goldensunset;

    LatLonitem newPlace = new LatLonitem("Mumbai", "19.07283", "72.88261");
    LinearLayout moon_layout;
    boolean isGpsLocated = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        cal = Calendar.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_parent_map_holder);
        settingUpIds(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 23) {

            Log.d("newstate", "sdkversion");
            try {
                Log.d("newstate", "acccessfinelocation");
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(MainMapHandler.this, new OnSuccessListener<android.location.Location>() {
                            @Override
                            public void onSuccess(android.location.Location location) {
                                Log.d("newstate", "onsucess" + location.getProvider());
                                newPlace = new LatLonitem("Current Location", location.getLatitude() + "", location.getLongitude() + "");
                                isGpsLocated = true;
                                mapItAgain();

                            }

                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("newstate", "failurelistener");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setSearchViewListeners();
    }

    private void settingUpIds(Bundle savedInstanceState) {
        latLonitems = new ArrayList<>();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mapView = (MapView) findViewById(R.id.mapview);
        moon_layout = (LinearLayout) findViewById(R.id.moon_layout);


        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView.getMapAsync(this);


        date = (TextView) findViewById(R.id.the_date);
        date.setOnClickListener(this);

        prev = (ImageButton) findViewById(R.id.prev_bt);

        sun_down = (TextView) findViewById(R.id.sun_down_tv);
        sun_up = (TextView) findViewById(R.id.sun_up_tv);
        moon_up = (TextView) findViewById(R.id.moon_up_tv);
        moon_down = (TextView) findViewById(R.id.moon_down_tv);


        today = (ImageButton) findViewById(R.id.today_bt);
        next = (ImageButton) findViewById(R.id.next_tv);

        prev.setOnClickListener(this);
        today.setOnClickListener(this);
        next.setOnClickListener(this);

        listAdapter = new SearchAdapter(this, R.layout.list_row_dialog, latLonitems);
        AndroidNetworking.initialize(getApplicationContext());
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
    }


    private void setSearchViewListeners() {

        searchView.setVoiceSearch(false);
        searchView.setCursorDrawable(R.drawable.custome_cursor);
        searchView.setEllipsize(true);
        searchView.setAdapter(listAdapter);

        //  searchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));


        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Do some magic

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic

                // searchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));
                AndroidNetworking.get("https://maps.googleapis.com/maps/api/place/textsearch/json?query={query}&key=" + getString(R.string.maps_api_key))
                        .addPathParameter("query", newText)

                        .setTag("test")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {

                                    if (response.has("error_message")) {
                                        Toast.makeText(MainMapHandler.this, "" + response.getString("error_message"), Toast.LENGTH_SHORT).show();
                                    }
                                    int q = response.getJSONArray("results").length();

                                    for (int i = 0; i < q; i++) {
                                        JSONObject a = response.getJSONArray("results").getJSONObject(i).getJSONObject("geometry").getJSONObject("location");
                                        String add = response.getJSONArray("results").getJSONObject(i).getString("formatted_address");
                                        LatLonitem aa = new LatLonitem(add, a.getString("lat"), a.getString("lng"));
                                        latLonitems.add(aa);
                                    }


                                    Log.d("obvious", latLonitems.size() + " ");
                                    listAdapter.notifyDataSetChanged();
                                    searchView.showSuggestions();


                                } catch (JSONException e) {
                                    e.printStackTrace();


                                }


                                Log.d("obvious", response.toString());

                            }

                            @Override
                            public void onError(ANError anError) {

                            }
                        });


                return true;
            }
        });
        searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                newPlace = latLonitems.get(position);
                isGpsLocated = false;
                calculateDate();
                searchView.closeSearch();
                // Toast.makeText(MainMapHandler.this, "" + latLonitems.get(position).name, Toast.LENGTH_SHORT).show();
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);

        isGpsLocated = false;
        mapItAgain();

        // Updates the location and zoom of the MapView
       /* CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(chinawall, 10);
        map.animateCamera(cameraUpdate);
        map.moveCamera(CameraUpdateFactory.newLatLng(chinawall));*/


        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                newPlace = new LatLonitem("Click Location", latLng.latitude + "", latLng.longitude + "");
                mapItAgain();
            }
        });
    }

    private void mapItAgain() {
        try {
            geocoder = new Geocoder(this, Locale.getDefault());

            addresses = geocoder.getFromLocation(Double.parseDouble(newPlace.getLat()), Double.parseDouble(newPlace.getLng()), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            current_city = addresses.get(0).getLocality();
            newPlace.setName(current_city);
            calculateDate();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void newCalculation(LatLonitem place) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(place.name);

        }
        LatLng o = new LatLng(Double.parseDouble(place.getLat()), Double.parseDouble(place.getLng()));
        map.clear();
        map.addMarker(new MarkerOptions().position(o)
                .title(place.name).icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(o, 8));

       /* Polyline northLine = map.addPolyline(new PolylineOptions()
                .clickable(true)
                .color(getResources().getColor(R.color.moon_blue_dark))

                .add(
                        new LatLng(o.latitude, o.longitude),
                        new LatLng(o.latitude + 1, o.longitude + 0),
                        new LatLng(o.latitude + 2, o.longitude + 0)
                ));
*/

        Location location = new Location(place.getLat(), place.getLng());
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, TimeZone.getDefault());

        String officialSunrise = calculator.getOfficialSunriseForDate(cal);


        goldensunrise = calculator.getOfficialSunriseCalendarForDate(cal);
        Calendar tiff = cal;
        tiff.add(Calendar.HOUR, 1);
        goldensunrise = calculator.getOfficialSunriseCalendarForDate(tiff);


        String officialSunset = calculator.getOfficialSunsetForDate(cal);
        Calendar diff = cal;
        diff.add(Calendar.HOUR, -1);
        goldensunset = calculator.getOfficialSunsetCalendarForDate(diff);
        if (isGpsLocated) {
            thisIsTheDefaultLocation();
            isGpsLocated = false;
        }
        sun_up.setText(officialSunrise);
        sun_down.setText(officialSunset);

        SunPosition position = SunPosition.compute()
                .on(cal.getTime())       // set a date
                .at(Double.parseDouble(place.lat), Double.parseDouble(place.lng))  // set a location
                .execute();     // get the results

        MoonPosition moonPosition = MoonPosition.compute()
                .on(cal.getTime())
                .at(Double.parseDouble(place.lat), Double.parseDouble(place.lng))  // set a location
                .execute();


        Log.d("nwlib", position.getAltitude() + "----" + position.getAzimuth() + "");
        Log.d("nwlib", moonPosition.getAltitude() + "----" + moonPosition.getAzimuth() + "");
        //    Log.d ("nwlib", times.getSet()+"");


        long shit = TimeZone.getDefault().getOffset(cal.getTimeInMillis());

        JSONObject j = Moon.riseSetString(cal.getTime(), shit, Double.parseDouble(newPlace.getLat()), Double.parseDouble(newPlace.getLng()));
        try {
            moon_layout.setVisibility(View.VISIBLE);
            moon_up.setText(j.getString("moonrise"));
            moon_down.setText(j.getString("moonset"));
        } catch (JSONException e) {
            e.printStackTrace();
            moon_layout.setVisibility(View.GONE);

        }

    }

    private void calculateDate() {


        currentDate = format1.format(cal.getTime());
        setDate(currentDate);
    }

    private void setDate(String dater) {
        date.setText(dater);
        newCalculation(newPlace);
        //customMapFragment.setLatLong(new LatLng(25.189610, 55.275940));
    }

    void thisIsTheDefaultLocation() {
        Intent notifyIntent = new Intent(this, MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (this, 3, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, goldensunset.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);


        AlarmManager newManger = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        newManger.setRepeating(AlarmManager.RTC_WAKEUP, goldensunrise.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
        Log.d("alrama", "alarms have beens set");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);


        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_pin_this_loc:
                pinThisLocation(newPlace);
                break;
            case R.id.action_show_bookmarks:
                showBookmarks();
                break;
            case R.id.action_search:
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    private void pinThisLocation(LatLonitem np) {
        LocalDb.getInstance(this).addAPlace(np.getName(),
                np.getLat(),
                np.getLng());

        Toast.makeText(this, "Location pinned for further use", Toast.LENGTH_SHORT).show();
    }

    private void showBookmarks() {
        ArrayList<LatLonitem> latLonitems = new ArrayList<>();
        Cursor c = LocalDb.getInstance(this).getAllEntries();


        while (c.moveToNext()) {
            LatLonitem a = new LatLonitem(c.getString(c.getColumnIndex(NAME)),
                    c.getString(c.getColumnIndex(LAT)),
                    c.getString(c.getColumnIndex(LONG)));
            latLonitems.add(a);
            Log.d("latlongcursor", a.toString());
        }
        final Dialog dialog = new Dialog(this);
        View view = getLayoutInflater().inflate(R.layout.root_dialog_layout, null);
        ListView lv = (ListView) view.findViewById(R.id.custom_list);
        CustomListAdapterDialog clad = new CustomListAdapterDialog(this, latLonitems);

        lv.setAdapter(clad);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                newPlace = latLonitems.get(position);
                isGpsLocated = false;
                calculateDate();
            }
        });

        dialog.setContentView(view);

        dialog.show();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.prev_bt:
                reduceADay();

                break;
            case R.id.today_bt:
                CurrentDate();
                break;
            case R.id.next_tv:
                AddADay();
                break;
            case R.id.the_date:
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        this,
                        cal.get(Calendar.YEAR), // Initial year selection
                        cal.get(Calendar.MONTH), // Initial month selection
                        cal.get(Calendar.DAY_OF_MONTH) // Inital day selection
                );

                dpd.show(getSupportFragmentManager(), "Datepickerdialog");
                break;
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        cal.set(Calendar.DATE, dayOfMonth);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthOfYear);

        currentDate = format1.format(cal.getTime());
        setDate(currentDate);
    }

    private void CurrentDate() {

        cal = Calendar.getInstance();

        currentDate = format1.format(cal.getTime());
        setDate(currentDate);


    }

    private void reduceADay() {


        cal.add(Calendar.DATE, -1);
        currentDate = format1.format(cal.getTime());
        setDate(currentDate);


    }

    private void AddADay() {


        cal.add(Calendar.DATE, 1);
        currentDate = format1.format(cal.getTime());
        setDate(currentDate);


    }


    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            finish();
        }
    }
}
