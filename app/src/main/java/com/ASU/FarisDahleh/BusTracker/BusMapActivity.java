package com.ASU.FarisDahleh.BusTracker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import android.widget.Toast;

public class BusMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    ImageButton imgBtn_search_bus;
    List<BusObject> busesList = null;
    BusObject checkBus;
    String[] busesListName;
    LocationMap map;
    TextView txtDistance;
    String busNumber = null;
    Context ctx;
    Thread task = new Thread();
    String select_bus;


    private SharedPreferences Pref;

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    Location userLocation;
    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1;
    private static final float LOCATION_DISTANCE = 1f;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_map);
        Pref = getSharedPreferences("appData", MODE_PRIVATE);
        ctx = this;
        busNumber = AppCashe.getInstance().getPreferences().getString("busNumber", null);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        txtDistance = (TextView) findViewById(R.id.txtDistance);
        imgBtn_search_bus = (ImageButton) findViewById(R.id.imgBtn_search_bus);
        imgBtn_search_bus.setVisibility(View.VISIBLE);
        txtDistance.setVisibility(View.VISIBLE);
        initLocationListener();

        imgBtn_search_bus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocationServiceEnabled()) {
                    showDialogWithChoices();
                } else {
                    // notify user
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
                    dialog.setMessage(ctx.getResources().getString(R.string.gps_network_not_enabled));
                    dialog.setPositiveButton(ctx.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            ctx.startActivity(myIntent);
                            //get gps
                        }
                    });
                    dialog.show();

                }

            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = new LocationMap(googleMap);

    }


    public boolean isLocationServiceEnabled() {
        LocationManager locationManager = null;
        boolean gps_enabled = false, network_enabled = false;

        if (locationManager == null)
            locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            //do nothing...
        }

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            //do nothing...
        }

        return gps_enabled || network_enabled;

    }

    public String getAPIToken(boolean expried) {
        if (Pref.getInt("expires_in", 0) == 0 || expried) {
            final ProgressDialog progressDialog = new ProgressDialog(ctx);
            progressDialog.setTitle("Fetching data");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "http://81.28.112.42/MobileService/token";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject Res = null;

                            try {
                                Res = new JSONObject(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (Res != null) {
                                if (Res.optString("access_token") != null) {
                                    SharedPreferences.Editor e = Pref.edit();
                                    e.putString("apiToken", Res.optString("access_token"));
                                    int time = (int) ((new Date()).getTime() / 1000);
                                    e.putInt("expires_in", time + Res.optInt("expires_in"));
                                    e.apply();
                                    progressDialog.hide();
                                    Toast.makeText(ctx, "Please try again.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ctx, Res.optString("error"), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ctx, "Invalid response from server", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Toast.makeText(ctx, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("username", "asu2");
                    params.put("password", "asu009988");
                    params.put("server", "2");
                    params.put("grant_type", "password");
                    return params;
                }

            };
            queue.add(stringRequest);
            return "-1";
        } else {
            int ExpireIn = Pref.getInt("expires_in", 0);
            int CurrentTime = (int) ((new Date()).getTime() / 1000);
            if (CurrentTime <= ExpireIn) {
                return Pref.getString("apiToken", "");
            } else {
                return getAPIToken(true);
            }
        }
    }

    private void showDialogWithChoices() {

        final String AccessToken = getAPIToken(false);
        if (!AccessToken.equals("-1")) {
            final ProgressDialog progressDialog = new ProgressDialog(ctx);
            progressDialog.setTitle("Loading busses");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "http://81.28.112.42/MobileService/api/Vehicles";
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONArray Res = null;
                            progressDialog.hide();
                            try {
                                Res = new JSONArray(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (Res != null) {
                                busesList = new ArrayList<>();

                                for (int i = 0; i < Res.length(); i++) {
                                    JSONObject busObj = Res.optJSONObject(i);
                                    if (busObj.optString("strCarNum").matches("(.*)\\-(.*) \\((.*)\\)")) {
                                        BusObject bus = new BusObject();
                                        bus.setBusNumber(busObj.optString("strCarNum"));
                                        bus.setId(busObj.optString("nID"));
                                        bus.setLatitude(busObj.optDouble("Latitude"));
                                        bus.setLongitude(busObj.optDouble("Longitude"));
                                        busesList.add(bus);
                                    }
                                }
                                ShowBusesDialog();
                            } else {
                                JSONObject Resp = null;
                                try {
                                    Resp = new JSONObject(response);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if (Resp == null) {
                                    Toast.makeText(ctx, "Invalid response from server.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ctx, Resp.optString("Message"), Toast.LENGTH_SHORT).show();
                                }
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            }) {

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "bearer " + AccessToken);
                    return headers;
                }

            };
            queue.add(stringRequest);
        }
    }

    public void ShowBusesDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search Bus");
        map.clearMap();

        String busNumber = AppCashe.getInstance().getPreferences().getString("busNumberRem", "0");
        if (busesList != null) {
            int prevSelected = busesList.size() - 1;

            busesListName = new String[busesList.size()];
            for (int i = 0; i < busesList.size(); i++) {
                busesListName[i] = busesList.get(i).getBusNumber();
                busesListName[i] = "Bus no: " + busesListName[i].substring(busesListName[i].indexOf("("));

                if (busNumber.equals(busesListName[i])) {
                    prevSelected = i;
                }

            }
            builder.setSingleChoiceItems(busesListName, prevSelected, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    select_bus = busesListName[which];
                    String busIDCheck = busesList.get(which).getId();
                    AppCashe.getInstance().getPreferences().edit().putString("busNumber", busIDCheck).commit();
                    try {
                        AppCashe.getInstance().getPreferences().edit().putString("busNumber", busIDCheck).commit();
                        AppCashe.getInstance().getPreferences().edit().putString("busNumberRem", busesListName[which]).commit();
                        runRefreshThread();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dialog.cancel();
                }
            });
            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

        } else {
            builder.setMessage("No Buses");
            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void GetBusLocation(String busID) {
        final String AccessToken = getAPIToken(false);
        if (!AccessToken.equals("-1")) {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "http://81.28.112.42/MobileService/api/Vehicles/" + busID + "/Location";
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject Res = null;
                            try {
                                Res = new JSONObject(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (Res != null) {
                                if (Res.optString("Message") == null || Res.optString("TerminalId") != null) {
                                    checkBus = new BusObject();
                                    checkBus.setLongitude(Res.optDouble("Lon"));
                                    checkBus.setLatitude(Res.optDouble("Lat"));
                                    checkBus.setEngineOn(Res.optBoolean("EngineOn"));
                                    checkBus.setSpeed(Res.optInt("Speed"));
                                    checkBus.setSOS(Res.optBoolean("SOS"));
                                    checkBus.setBusNumber(Res.optString("TerminalName"));
                                    checkBus.setDoorOpen(Res.optBoolean("DoorOpen"));
                                    map.addBusesMarkers(checkBus, txtDistance, (new LatLng(userLocation.getLatitude(), userLocation.getLongitude())));
                                } else {
                                    Toast.makeText(ctx, Res.optString("Message"), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ctx, "Invalid response from server", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "bearer " + AccessToken);
                    return headers;
                }
            };
            queue.add(stringRequest);
        }
    }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("BusMap Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            userLocation = location;

        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }


    private void initLocationListener() {
        busNumber = AppCashe.getInstance().getPreferences().getString("busNumber", null);
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {

            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void runRefreshThread() {

        task = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Log.e("BusMapActivity", "repeatedtest");
                                String IDCheck = AppCashe.getInstance().getPreferences().getString("busNumber", "0");
                                GetBusLocation(IDCheck);
                            }
                        });
                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        task.start();
    }
}
