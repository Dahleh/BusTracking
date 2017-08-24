package com.ASU.FarisDahleh.BusTracker;

import android.location.Location;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;


public class LocationMap implements OnMapReadyCallback {

    private GoogleMap mMap;
    LatLng busLocation;


    public LocationMap(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    }

    public void clearMap() {
        mMap.clear();
    }

    public String addBusesMarkers(BusObject busObject, TextView txtDistance, LatLng userLoc) {
        clearMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        String busNumber = AppCashe.getInstance().getPreferences().getString("busNumber", "0");
        if (busNumber.equals("0")) {
            LatLng location = new LatLng(busObject.getLatitude(), busObject.getLongitude());
            mMap.addMarker(new MarkerOptions().position(location).title("Bus .No " + busObject.getBusNumber()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_75)));
        } else {
            int speedCheck = busObject.getSpeed();
            boolean engineCheck = busObject.getEngineOn();
            boolean SOSCheck = busObject.getSOS();
            boolean doorOpen = busObject.getdoorOpen();
            LatLng location = new LatLng(busObject.getLatitude(), busObject.getLongitude());
            mMap.addMarker(new MarkerOptions().position(location).title("Bus .No " + busObject.getBusNumber().substring(busObject.getBusNumber().indexOf("("))).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_75)));
            busLocation = location;
            txtDistance.setText(addUserMarker(userLoc, speedCheck, engineCheck, SOSCheck, doorOpen, busObject));

        }
        return "refreshed";
    }


    public String addUserMarker(LatLng location, int speed, boolean engine, boolean SOS, boolean doorOpen, BusObject busObject) {
        //Log.e("User", "User Added");
        LatLng locationStudent = new LatLng(location.latitude, location.longitude);
        float[] results = new float[1];
        String textViewText;
        mMap.setTrafficEnabled(true);
        if (busLocation != null) {
            Location.distanceBetween(locationStudent.latitude, locationStudent.longitude,
                    busLocation.latitude, busLocation.longitude,
                    results);
            int estimatedDriveTimeInMinutes;

            try {
                estimatedDriveTimeInMinutes = (int) results[0] / speed;
                estimatedDriveTimeInMinutes = estimatedDriveTimeInMinutes / 10;
            } catch (Exception e) {
                estimatedDriveTimeInMinutes = 0;
            }
            results[0] = results[0] / 1000;
            if (SOS != false) {
                textViewText = "Distance: " + String.format("%.2f", results[0]) + " Km \n" + "The Bus have emergency try another bus\n Bus no." + busObject.getBusNumber().substring(busObject.getBusNumber().indexOf("("));
            } else if (speed != 0) {
                textViewText = "Distance: " + String.format("%.2f", results[0]) + " Km \n" + "Estimated time: " + estimatedDriveTimeInMinutes + " min\n Bus no."+busObject.getBusNumber().substring(busObject.getBusNumber().indexOf("("));
            } else if (speed == 0 && engine == true) {
                textViewText = "Distance: " + String.format("%.2f", results[0]) + " Km \n" + "Estimated time: the bus on a traffic light\ntry again\n Bus no." + busObject.getBusNumber().substring(busObject.getBusNumber().indexOf("("));
            } else if (engine == false && speed == 0) {
                textViewText = "Distance: " + String.format("%.2f", results[0]) + " Km \n" + "The Bus is turned off\n Bus no." + busObject.getBusNumber().substring(busObject.getBusNumber().indexOf("("));
            } else if (speed == 0 && engine == true && doorOpen == true) {
                textViewText = "Distance: " + String.format("%.2f", results[0]) + " Km \n" + "Estimated time: the bus is loading students\n Bus no." + busObject.getBusNumber().substring(busObject.getBusNumber().indexOf("("));
            } else {
                textViewText = "Choose bus to see the distance";
            }

        } else {
            busLocation = null;
            textViewText = "Choose bus to see the distance";
        }
        zoomToLocationGPS(locationStudent);
        mMap.addMarker(new MarkerOptions().position(locationStudent).title("Student").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_boy_75)));

        return textViewText;
    }

    public void zoomToLocationGPS(LatLng Location) {
        String numberBus = AppCashe.getInstance().getPreferences().getString("busNumber", "0");
        if (numberBus.equals("0")) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Location, 13));
        } else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(busLocation, 16));
        }

    }


}
