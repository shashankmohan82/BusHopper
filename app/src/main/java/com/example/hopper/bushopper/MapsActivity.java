package com.example.hopper.bushopper;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.system.ErrnoException;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpRetryException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


import static com.example.hopper.bushopper.R.color.colorPrimary;

public class MapsActivity extends AppCompatActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout dlayout;
    private LinearLayoutManager linearLayoutManager;
    private TextView locationInfo;
    private TextView routeInfo;
    private static final String TAG = "MapsActivity";
    private JSONObject jsonObject;
    private Double busStoplatitude;
    private Double busStoplongitude;
    private Double busLatitude;
    private Double busLongitude;
    private String json;
    private String routeID;
    private int seqNo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);



        //set up navigationDrawer RecyclerView
        dlayout = (DrawerLayout) findViewById(R.id.drawer);
        setupDrawer();
        ArrayList<String> dataset = new ArrayList<>();
        Integer imageResource = R.drawable.logo;
        dataset.add("ABOUT US");
        dataset.add("HELP");
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerViewAdapter1 recyclerViewAdapter = new RecyclerViewAdapter1(dataset,imageResource);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        // location update View
        setUpMapIfNeeded();
        locationInfo = (TextView)findViewById(R.id.locationText);
        routeInfo = (TextView)findViewById(R.id.RouteText);

        Timer refreshTask = new Timer();
        refreshTask.scheduleAtFixedRate(new TimerTask(){
            public void run()
            {
                if(isNetworkConnected()) {
                    onTimerTick_TimerThread();
                }
            }

        }, 10000L, 10000L);






        //guardian info json accesed from LoginActivity
         json =null;
        Intent intent = getIntent();
        if(intent!=null&&intent.hasExtra("overallJson")){
            json = intent.getStringExtra("overallJson");
            Log.d(TAG,json);
            try {
                jsonObject = new JSONObject(json);
                JsonRetrieval jsonRetrieval = new JsonRetrieval();
                String lat= jsonRetrieval.getMbusstopLat(jsonObject);
                routeID = jsonRetrieval.getRouteId(jsonObject);
                routeInfo.setText(routeID);
                busStoplatitude =Double.parseDouble(jsonRetrieval.getMbusstopLat(jsonObject));
                busStoplongitude =Double.parseDouble(jsonRetrieval.getMbusstopLong(jsonObject));
                seqNo = Integer.parseInt(jsonRetrieval.getmseqNo(jsonObject));
                Log.d(TAG,lat);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        if(isNetworkConnected()) {
            //show predicted time of bus arrival
            showStatus(jsonObject);
        }
        else
        {
            Toast.makeText(MapsActivity.this,"Network not found",Toast.LENGTH_SHORT).show();
        }


        //refresh button
        registerFAB();



    }

    private void refetchJson(){

        String url = (APIConfiguration.curLocURL+routeID).trim();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        new refreshLocation(restTemplate,headers,url) {

            @Override
            protected void onPostExecute(ResponseEntity<String> response) {
                super.onPostExecute(response);
                JSONObject driverJson = null;
                try {
                    driverJson= new JSONObject(response.getBody());
                    Log.d(TAG,response.getStatusCode()+"");
                    if(isNetworkConnected() && response.getStatusCode() == HttpStatus.OK) {
                        //show predicted time of bus arrival
                        showStatus(driverJson);
                    }
                    else
                    {
                        Toast.makeText(MapsActivity.this,"Network not found/Server Error",Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    public void onTimerTick_TimerThread()
    {   if(isNetworkConnected()) {
        Thread refreshThread = new Thread(new Runnable() {
            public void run() {
                refetchJson();
            }
        });

        refreshThread.start();
    }
    }

    //set up navDrawer
    private void setupDrawer() {

        toggle = new ActionBarDrawerToggle(this, dlayout,
                R.string.drawer_open, R.string.drawer_close) {


            /**
             * Called when a drawer has settled in a completely open state.
             */


            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                invalidateOptionsMenu();
                drawerView.setClickable(true);


                // creates call to onPrepareOptionsMenu()
            }

            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

                invalidateOptionsMenu();
            }


        };
        dlayout.setDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);



    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        Intent intent = getIntent();
        if(intent!=null&&intent.hasExtra("overallJson")){
            json = intent.getStringExtra("overallJson");
            Log.d(TAG,json);
            try {
                jsonObject = new JSONObject(json);
                JsonRetrieval jsonRetrieval = new JsonRetrieval();
                String lat= jsonRetrieval.getMbusstopLat(jsonObject);
                Log.d(TAG,lat);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        if(isNetworkConnected()) {
            //show predicted time of bus arrival
            showStatus(jsonObject);
        }
        else
        {
            Toast.makeText(MapsActivity.this,"Network not found",Toast.LENGTH_SHORT).show();
        }

    }


    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();

            }
        }
    }

    //register FAB
    private void registerFAB() {
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.floatingbutton);
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        fab.setRippleColor(getResources().getColor(colorPrimary));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(isNetworkConnected()) {
                    refetchJson();
                }
            }
        });
    }




    //check Network Connection
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }


    //method to show current status
    private void showStatus(final JSONObject jsonObject){
        JsonRetrieval jsonRetrieval = new JsonRetrieval();
        try {
             busLatitude =Double.parseDouble(jsonRetrieval.getCurLat(jsonObject));
             busLongitude =Double.parseDouble(jsonRetrieval.getCurLong(jsonObject));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG,busLatitude+""+busLongitude+""+busStoplatitude+"");
        String origin = busLatitude + "," + busLongitude;

        String URL = APIConfiguration.GoogleDistanceMatrixURL + "origins=" + origin +
                "&destinations=" + busStoplatitude + "%2C" + busStoplongitude + "%7C&key=" +
                APIConfiguration.GoogleDistanceMatrix_API;

        LatLng busstop = new LatLng(busStoplatitude,busStoplongitude);
        LatLng buslocation = new LatLng(busLatitude,busLongitude);
        refreshMarker(busstop, buslocation);
        //calculate predicted Time using DistanceMatrix Api
        new MakeConnection(URL){
            @Override
            protected void onPostExecute(JSONObject distanceMatrixJson) {

                String res = calcTime(distanceMatrixJson,jsonObject);
                locationInfo.setVisibility(View.VISIBLE);
                locationInfo.setText( res+"*");
            };}.execute();


    }

    private void refreshMarker(final LatLng busstop, final LatLng buslocation){
        mMap.clear();
        BitmapDescriptor stop = BitmapDescriptorFactory.fromResource(R.drawable.busstop);
        BitmapDescriptor vehicle  = BitmapDescriptorFactory.fromResource(R.drawable.transport);
        Log.d("RefreshMarker","Indise");
        mMap.addMarker(new MarkerOptions().position(buslocation).title("Bus").icon(vehicle));
        mMap.addMarker(new MarkerOptions().position(busstop).title("BusStop").icon(stop));
        final LatLngBounds bounds = new LatLngBounds.Builder()
                .include(getMyLocation())
                .include(busstop)
                .include(buslocation)
                .build();

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                Log.d("RefreshMarker","Indise1");

                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 60));
                mMap.animateCamera(CameraUpdateFactory.zoomOut(),2000,null);



            }
        });

    }


    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        try {
            mMap.setMyLocationEnabled(true);
        }
           catch (SecurityException se){
               se.printStackTrace();
           }


        LatLng myLocation = getMyLocation();
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.bus);
        mMap.addMarker(new MarkerOptions().position(myLocation).title("Marker").icon(icon));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(myLocation)
                .zoom(17)
                .tilt(30)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private LatLng getMyLocation() {
        GPSTracker gpsTracker = new GPSTracker(this);
        LatLng myLocation = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
        Log.d(TAG,"my location :"+myLocation.longitude);
        return myLocation;
    }


    public void onClick(View view) {
        dlayout.openDrawer(Gravity.LEFT);



    }



    private String calcTime(JSONObject distanceMatrixjson,JSONObject jsonObject) {
        if (distanceMatrixjson != null) {

            String status = distanceMatrixjson.optString("status");
            Log.d(TAG+":status",status);
            if (status.equals("OK")) {
                JSONArray row = distanceMatrixjson.optJSONArray("rows");
                try {
                    JSONObject object = row.getJSONObject(0);
                    JSONArray element = object.optJSONArray("elements");
                    JSONObject object2 = element.getJSONObject(0);
                    //JSONObject distanceObject = object2.optJSONObject("distance");
                    JSONObject durationObject = object2.optJSONObject("duration");

                    String durationValue = durationObject.optString("value");


                    JsonRetrieval jsonRetrieval = new JsonRetrieval();


                    Integer lastSeqNo = Integer.parseInt(jsonRetrieval.getSeqNo(jsonObject));

                    if(seqNo<lastSeqNo){
                        Log.d(TAG,true+" Seq no is "+seqNo);
                        locationInfo.setBackgroundColor(getResources().getColor(R.color.statusRed));
                    }

                    if(seqNo>lastSeqNo) {
                        if (Integer.parseInt(durationValue) < 900) {
                            locationInfo.setBackgroundColor(getResources().getColor(R.color.statusOrange));
                            Log.d(TAG,true+" LastSeq no is "+lastSeqNo);
                        }
                        else{
                            locationInfo.setBackgroundColor(getResources().getColor(R.color.statusGreen));
                        }
                    }

                    if(seqNo>lastSeqNo){
                    Long time = System.currentTimeMillis();
                    Long predictedTime =Long.parseLong(durationValue)*1000 + time;
                    DateFormat formatter = new SimpleDateFormat("HH:mm");
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(predictedTime);
                    formatter.setCalendar(cal);
                    String res = formatter.format(cal.getTime());

                        return res;


                    }
                    else{
                        Long time = System.currentTimeMillis();
                        Long predictedTime =  time -Long.parseLong(durationValue)*1000;
                        DateFormat formatter = new SimpleDateFormat("HH:mm");
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(predictedTime);
                        formatter.setCalendar(cal);
                        String res = formatter.format(cal.getTime());
                        return res;
                    }




                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        return null;
    }

    private class refreshLocation extends AsyncTask<Void,Void,ResponseEntity<String>> {

        RestTemplate restTemplate;
        HttpHeaders headers;
        String url;
        public refreshLocation() {
        }

        public refreshLocation(RestTemplate restTemplate, HttpHeaders headers, String url) {
            this.restTemplate = restTemplate;
            this.headers = headers;
            this.url = url;
        }

        @Override
        protected ResponseEntity<String> doInBackground(Void... voids) {
            try {
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
                return response;
            }catch (RuntimeException re){

                re.printStackTrace();

            }
            return new ResponseEntity<String>("{}",HttpStatus.SERVICE_UNAVAILABLE);

        }

    }

}

