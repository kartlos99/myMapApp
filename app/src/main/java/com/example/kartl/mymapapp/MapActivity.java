package com.example.kartl.mymapapp;

import android.Manifest;
import android.app.ActionBar;
import android.app.PendingIntent;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kartl.mymapapp.models.PlaceInfo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final String MY_LOCATION_TITLE = "აქ ვარ მე";
    private LatLng latLng = new LatLng(-41.2, -44.23);
    private LatLng latLng2 = new LatLng(37.2, 132.23);
    private LatLngBounds LAT_LNG_BOUND = new LatLngBounds(latLng, latLng2);
    private static final String GEOFANCE_ID = "geofanceID";

    // widgets
    private AutoCompleteTextView mInputText;
    private Button mSearchBtn;
    private ImageView mGps, mInfo;

    // vars
    private boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mfusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient googleApiClient;
    protected GeoDataClient mGeoDataClient;
    private PlaceInfo mPlace;
    private Marker marker;
    private GeofencingClient mGeofencingClient;
    private Geofence geofence;
    private PendingIntent mGeofencePendingIntent;
    private GeofencingClient geofencingClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mInputText = findViewById(R.id.input_search);
        mSearchBtn = findViewById(R.id.btnSearch);
        mGps = findViewById(R.id.img_gps);
        mInfo = findViewById(R.id.img_info);

        getLocationPermision();


    }

    private void init() {
        Log.d(TAG, "init");

        googleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mGeoDataClient = Places.getGeoDataClient(this);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGeoDataClient, LAT_LNG_BOUND, null);

        mInputText.setAdapter(mPlaceAutocompleteAdapter);
        mInputText.setOnItemClickListener(mAutocompliteClickListener);
        mInputText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mInputText.isSelected()) {
                    mInputText.selectAll();
                }
            }
        });

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, " vedzebt ");

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

        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "clicked info img");
                try {
                    if (marker.isInfoWindowShown()) {
                        marker.hideInfoWindow();
                    } else {
                        marker.showInfoWindow();
                    }
                } catch (NullPointerException e) {
                    Log.d(TAG, "info img: Null exep " + e.getMessage());
                }
                startGeofenceMonitoring();
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
//        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mInputText.getWindowToken(), 0);
    }

    private void geoLocate() {
        Log.d(TAG, " geoLocate");

        String searchString = mInputText.getText().toString();

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();

        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.d(TAG, "IO Error: " + e.getMessage());
        }

        Float zoom = DEFAULT_ZOOM;

        if (list.size() > 0) {
            Address address = list.get(0);
            Log.d(TAG, " ai naxe: " + address.toString());

            if (address.getLocality() == null) {
                zoom -= 7;
            }
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), zoom, address.getAddressLine(0));
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
//        startLocationUpdates();
//        googleApiClient.reconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        stopLocationUpdates();
//        googleApiClient.disconnect();
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
                // my work latlng = 41.727475 / 44.7635947
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

        mfusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    private void moveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo) {
        Log.d(TAG, "move Cam to: " + latLng.latitude + " / " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        mMap.clear();

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));

        if (placeInfo != null) {
            try {
                String snippet = "მისამართი: " + placeInfo.getAddress() + "\n"
                        + "ტელ: " + placeInfo.getPhoneNumber() + "\n"
                        + "ვებ: " + placeInfo.getWebsiteUri() + "\n"
                        + "რეიტინგი: " + placeInfo.getRating();

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(placeInfo.getName())
                        .snippet(snippet);
                Log.d(TAG, "SNIPPET: " + snippet);

                marker = mMap.addMarker(options);
            } catch (NullPointerException e) {
                Log.d(TAG, "movCam: " + e.getMessage());
            }
        } else {
            mMap.addMarker(new MarkerOptions().position(latLng));
        }

        hideKeyboard();
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "move Cam to: " + latLng.latitude + " / " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals(MY_LOCATION_TITLE)) {
            mMap.clear();
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

    private void getLocationPermision() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            }
        }

        if (!mLocationPermissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //  ******************  lesson 9 on youtub

    private AdapterView.OnItemClickListener mAutocompliteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // aq unda miviRot Prace obieqti rasac davachiret imis Sesabamisad *******************
            hideKeyboard();

            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(position);
            final String placeID = item.getPlaceId();
            Log.d(TAG, "PlaceID: " + placeID);
            Toast.makeText(getApplicationContext(), placeID, Toast.LENGTH_SHORT).show();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(googleApiClient, placeID);

            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.d(TAG, "Place query not succesful: " + places.getStatus().toString());
                places.release();
                return;
            }

            final Place place = places.get(0);
            mPlace = new PlaceInfo();

            try {
                mPlace.setLatLng(place.getLatLng());
                mPlace.setName(place.getName().toString());
                mPlace.setAddress(place.getAddress().toString());
                mPlace.setId(place.getId());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                mPlace.setWebsiteUri(place.getWebsiteUri().toString());
                mPlace.setRating(place.getRating());
            } catch (NullPointerException e) {
                Log.d(TAG, "PlaceResCallback: " + e.getMessage());
            }

            Log.d(TAG, "PlaceResCallback: " + mPlace.toString());
            Toast.makeText(getApplicationContext(), mPlace.toString(), Toast.LENGTH_SHORT).show();


            try {
                if (mPlace.getLatLng() != null) {
                    moveCamera(mPlace.getLatLng(), DEFAULT_ZOOM, mPlace);
                } else {
                    moveCamera(place.getViewport().getCenter(), DEFAULT_ZOOM, mPlace);
                }
            } catch (NullPointerException e) {
                Log.d(TAG, "can't Move Cam: " + e.getMessage());
            }

            places.release();
        }
    };


    private void startGeofenceMonitoring() {
        Log.d(TAG, "start geofence monitoring");
        mGeofencingClient = LocationServices.getGeofencingClient(this);

        geofence = new Geofence.Builder()
                .setRequestId(GEOFANCE_ID)
                .setCircularRegion(41.73, 44.7625, 200f)
                .setExpirationDuration(60 * 60 * 1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();


        geofencingClient = LocationServices.getGeofencingClient(this);
        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(MapActivity.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "GepFence daemata");
                    }
                })
                .addOnFailureListener(MapActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "GeoFence ver daemata"+ e.getMessage());
                    }
                });

//                addOnSuccessListener(this, new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d(TAG, "GepFence daemata");
//                    }
//                })
//                .addOnFailureListener(this, new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d(TAG, "GeoFence ver daemata"+ e.getMessage());
//                    }
//                });

//        GeofencingRequest geofencingRequest = new GeofencingRequest();

    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(geofence);
        Log.d(TAG, "GeoFence Request done");
        return builder.build();
    }


    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        Log.d(TAG, "GeoFence PandingIntent done");
        return mGeofencePendingIntent;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "can't Stop fance Monitoring", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Stop fance Monitoring", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
