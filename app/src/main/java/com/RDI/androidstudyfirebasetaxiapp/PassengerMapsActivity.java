package com.RDI.androidstudyfirebasetaxiapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class PassengerMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final int CHECK_SETTINGS_CODE = 111;
    private static final int REQUEST_LOCATION_PERMISSION = 222;

    private FusedLocationProviderClient fusedLocationClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;

    private boolean isLocationUpdatesActive;

    private Button settingsButton;
    private Button signOutButton;
    private Button bookTaxiButton;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    private int seachRadius = 1;
    private boolean isDriverFound = false;
    private String nearestDriverID;
    private DatabaseReference driversGeoFire;
    private DatabaseReference nearestDriverLocation;
    private Marker driverMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_maps);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        settingsButton = findViewById(R.id.settingsButton);
        signOutButton = findViewById(R.id.signOutButton);
        bookTaxiButton = findViewById(R.id.bookTaxiButton);
        driversGeoFire = FirebaseDatabase.getInstance().getReference().child("driversGeoFire");

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                signOutPassenger();
            }
        });

        bookTaxiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookTaxiButton.setText("Поиск такси...");
                gettingNearestTexi();
            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices
                .getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);

        buildLocationRequest();
        buildLocationCallBack();
        buildLocationSettingsRequest();

        startLocationUpdates();

    }

    private void gettingNearestTexi() {


        GeoFire geoFire = new GeoFire(driversGeoFire);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(
                currentLocation.getLatitude(),
                currentLocation.getLongitude()
        ), seachRadius);

        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if (!isDriverFound) {
                    isDriverFound = true;
                    nearestDriverID = key;

                    getNearestDriverLocation();
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if (!isDriverFound) {
                    seachRadius++;
                    gettingNearestTexi();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void getNearestDriverLocation() {

        bookTaxiButton.setText("Полученеи местоположения водителя...");

        nearestDriverLocation = FirebaseDatabase.getInstance().getReference()
                .child("driversGeoFire")
                .child(nearestDriverID)
                .child("l");

        nearestDriverLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> driverLocationParametrs = (List<Object>) dataSnapshot.getValue();
                    double latitude = 0;
                    double longtitude = 0;

                    if (driverLocationParametrs.get(0) != null) {
                        latitude = Double.parseDouble(
                                driverLocationParametrs.get(0).toString()
                        );
                    }

                    if (driverLocationParametrs.get(1) != null) {
                        longtitude = Double.parseDouble(
                                driverLocationParametrs.get(1).toString()
                        );
                    }

                    LatLng driverLatlng = new LatLng(latitude, longtitude);

                    if (driverMarker != null) {
                        driverMarker.remove();
                    }

                    Location driverLocation = new Location("");
                    driverLocation.setLatitude(latitude);
                    driverLocation.setLongitude(longtitude);
                    float distanceToDriver = driverLocation.distanceTo(currentLocation);

                    bookTaxiButton.setText("Расстояние до водителя: " + distanceToDriver);
                    driverMarker = mMap.addMarker(
                            new MarkerOptions().position(driverLatlng)
                                    .title("Ваш водитель")
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void signOutPassenger() {

        String passengerUserId = currentUser.getUid();
        DatabaseReference passenger = FirebaseDatabase.getInstance()
                .getReference()
                .child("passenger");

        GeoFire geoFire = new GeoFire(passenger);
        geoFire.removeLocation(passengerUserId);

        Intent intent = new Intent(PassengerMapsActivity.this,
                ChoseModeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (currentLocation != null) {

            // Add a marker in Sydney and move the camera
            LatLng passengerLocation = new LatLng(currentLocation.getLatitude(),
                    currentLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(passengerLocation).title("passenger location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(passengerLocation));
        }
    }

    private void stopLocationUpdates() {

        if (!isLocationUpdatesActive) {
            return;
        }

        Task<Void> voidTask = fusedLocationClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        isLocationUpdatesActive = false;

                    }
                });

    }

    private void startLocationUpdates() {

        isLocationUpdatesActive = true;

        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(this,
                        new OnSuccessListener<LocationSettingsResponse>() {
                            @Override
                            public void onSuccess(
                                    LocationSettingsResponse locationSettingsResponse) {

                                if (ActivityCompat.checkSelfPermission(
                                        PassengerMapsActivity.this,
                                        Manifest.permission.ACCESS_FINE_LOCATION) !=
                                        PackageManager.PERMISSION_GRANTED &&
                                        ActivityCompat
                                                .checkSelfPermission(
                                                        PassengerMapsActivity.this,
                                                        Manifest.permission
                                                                .ACCESS_COARSE_LOCATION) !=
                                                PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }
                                fusedLocationClient.requestLocationUpdates(
                                        locationRequest,
                                        locationCallback,
                                        Looper.myLooper()
                                );

                                updateLocationUi();

                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        int statusCode = ((ApiException) e).getStatusCode();

                        switch (statusCode) {

                            case LocationSettingsStatusCodes
                                    .RESOLUTION_REQUIRED:
                                try {
                                    ResolvableApiException resolvableApiException =
                                            (ResolvableApiException) e;
                                    resolvableApiException.startResolutionForResult(
                                            PassengerMapsActivity.this,
                                            CHECK_SETTINGS_CODE
                                    );
                                } catch (IntentSender.SendIntentException sie) {
                                    sie.printStackTrace();
                                }
                                break;

                            case LocationSettingsStatusCodes
                                    .SETTINGS_CHANGE_UNAVAILABLE:
                                String message =
                                        "Adjust location settings on your device";
                                Toast.makeText(PassengerMapsActivity.this, message,
                                        Toast.LENGTH_LONG).show();

                                isLocationUpdatesActive = false;

                        }
                        updateLocationUi();
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case CHECK_SETTINGS_CODE:

                switch (resultCode) {

                    case Activity.RESULT_OK:
                        Log.d("MainActivity", "User has agreed to change location" +
                                "settings");
                        startLocationUpdates();
                        break;

                    case Activity.RESULT_CANCELED:
                        Log.d("MainActivity", "User has not agreed to change location" +
                                "settings");
                        isLocationUpdatesActive = false;
                        updateLocationUi();
                        break;
                }

                break;
        }

    }

    private void buildLocationSettingsRequest() {

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();

    }

    private void buildLocationCallBack() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                currentLocation = locationResult.getLastLocation();

                updateLocationUi();

            }
        };

    }

    private void updateLocationUi() {

        if (currentLocation != null) {

            LatLng passengerLocation = new LatLng(currentLocation.getLatitude(),
                    currentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(passengerLocation));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
            mMap.addMarker(new MarkerOptions().position(passengerLocation).title("passenger location"));

            String dpassengerUserId = currentUser.getUid();
            DatabaseReference passengerGeoFire = FirebaseDatabase.getInstance().getReference()
                    .child("passengersGeoFire");
            DatabaseReference passenger = FirebaseDatabase.getInstance().getReference()
                    .child("passenger");
            passenger.setValue(true);

            GeoFire geoFire = new GeoFire(passengerGeoFire);
            geoFire.setLocation(dpassengerUserId, new GeoLocation(currentLocation.getLatitude(),
                    currentLocation.getLongitude()));
        }

    }

    private void buildLocationRequest() {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isLocationUpdatesActive && checkLocationPermission()) {

            startLocationUpdates();

        } else if (!checkLocationPermission()) {
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {

        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        );

        if (shouldProvideRationale) {
            showSnackBar(
                    "Location permission is needed for " +
                            "app functionality",
                    "OK",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(
                                    PassengerMapsActivity.this,
                                    new String[]{
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                    },
                                    REQUEST_LOCATION_PERMISSION
                            );
                        }
                    }
            );
        } else {

            ActivityCompat.requestPermissions(
                    PassengerMapsActivity.this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_LOCATION_PERMISSION
            );

        }

    }

    private void showSnackBar(
            final String mainText,
            final String action,
            View.OnClickListener listener) {

        Snackbar.make(
                findViewById(android.R.id.content),
                mainText,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(
                        action,
                        listener
                )
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_LOCATION_PERMISSION) {

            if (grantResults.length <= 0) {
                Log.d("onRequestPermissions",
                        "Request was cancelled");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isLocationUpdatesActive) {
                    startLocationUpdates();
                }
            } else {
                showSnackBar(
                        "Turn on location on settings",
                        "Settings",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts(
                                        "package",
                                        BuildConfig.APPLICATION_ID,
                                        null
                                );
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                );
            }

        }

    }

    private boolean checkLocationPermission() {

        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;

    }
}
