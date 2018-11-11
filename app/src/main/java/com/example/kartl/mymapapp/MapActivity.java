package com.example.kartl.mymapapp;

import android.Manifest;
import android.app.ActionBar;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {
    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final String MY_LOCATION_TITLE = "აქ ვარ მე";

    // widgets
    private EditText mInputText;
    private Button mSearchBtn;
    private ImageView mGps;

    // vars
    private boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mfusedLocationProviderClient;
    private LocationCallback mLocationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mInputText = findViewById(R.id.input_search);
        mSearchBtn = findViewById(R.id.btnSearch);
        mGps = findViewById(R.id.img_gps);

        getLocationPermision();
    }

    private void init(){
        Log.d(TAG, "init");

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, " vedzebt ");

//                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                hideKeyboard();

                geoLocate();
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, " Gps Icon click : gvachvene chveni adgili");
                getDeviceLocation();
            }
        });

//        mInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
//                Log.d(TAG, " key = WW_xxxxxxxxxxxxxxxxxxxxx");
//                Log.d(TAG, " keyevent = "+ keyEvent.getAction());
//                if(actionId == EditorInfo.IME_ACTION_SEARCH
//                        || actionId == EditorInfo.IME_ACTION_DONE
//                        || actionId == EditorInfo.IME_ACTION_GO
//                        || actionId == EditorInfo.IME_ACTION_SEND
//                        || keyEvent.getAction() == KeyEvent.KEYCODE_SEARCH
//                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
//                        || keyEvent.getAction() == KeyEvent.KEYCODE_VOLUME_DOWN
//                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){
//
//                    // dzebnis funqcionali geolocation
//                    geoLocate();
//                }
//
//                return true;
//            }
//        });
    }

    private void hideKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void geoLocate() {
        Log.d(TAG, " geoLocate");

        String searchString = mInputText.getText().toString();

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();

        try {
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.d(TAG, "IO Error: " + e.getMessage());
        }

        Float zoom = DEFAULT_ZOOM;

        if(list.size() > 0){
            Address address = list.get(0);
            Log.d(TAG, " ai naxe: " + address.toString());

            if (address.getLocality() == null){
                zoom -= 7;
            }
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), zoom, address.getAddressLine(0));
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
//        stopLocationUpdates();
    }

    private void getDeviceLocation() {
        Log.d(TAG, "get Dev. Location");
        mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {

                mfusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    // Logic to handle location object
                                    moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM, MY_LOCATION_TITLE);

//                                    startLocationUpdates();

                                } else {
                                    Toast.makeText(getApplicationContext(), "null location", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        } catch (SecurityException e) {
            Log.e(TAG, " EXP: " + e.getMessage());
        }

    }

    private void stopLocationUpdates() {
        mfusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    private void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(6000);
        mLocationRequest.setFastestInterval(4000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    Log.d(TAG, " I'm HERE : " + location.getLatitude() + " / " + location.getLongitude());
                }
            }

            ;
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mfusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback,null);
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "move Cam to: " + latLng.latitude + " / " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals(MY_LOCATION_TITLE)){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }
        hideKeyboard();
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                Toast.makeText(getApplicationContext(), "map is ready", Toast.LENGTH_SHORT).show();

                if (mLocationPermissionGranted) {
                    getDeviceLocation();

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
//                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    mMap.getUiSettings().setZoomControlsEnabled(true);

                    init();
                }
            }
        });
    }

    private void getLocationPermision(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted = true;
                initMap();
            }
        }

        if (!mLocationPermissionGranted){
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE : {
                if (grantResults.length > 0){
                    for (int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    // yvelaferi kargadaa da gadavdivart map ze
                    initMap();
                }
            }
        }

    }
}
