package com.akash.exiomstask.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.akash.exiomstask.Constants.Constant;
import com.akash.exiomstask.R;
import com.akash.exiomstask.Services.Notifyservice;
import com.akash.exiomstask.Utilities.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import static com.akash.exiomstask.Constants.Constant.FASTEST_INTERVAL;
import static com.akash.exiomstask.Constants.Constant.UPDATE_INTERVAL;

public class MainActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private String TAG = MainActivity.class.getSimpleName();
    TextView textViewCurrent;
    TextView textViewDestination;
    Button buttonStart;
    Button buttonStop;
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleApiClient mGoogleApiClient;
    private Location mStartLocation;
    private Location mDestinationLocation;
    Boolean isNotified = false;
    private AlertDialog permissionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewCurrent = (TextView) findViewById(R.id.tv_current);
        textViewDestination = (TextView) findViewById(R.id.tv_destination);
        buttonStart = (Button) findViewById(R.id.btn_start);
        buttonStop = (Button) findViewById(R.id.btn_stop);
        mDestinationLocation = new Location("destination location");

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationUpdates();
                if (mStartLocation != null && mDestinationLocation != null) {
                    checkDistance();
                }
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNotified = false;
                stopLocationUpdates();
                buttonStart.setVisibility(View.VISIBLE);
                buttonStop.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isNetworkAvailable()) {
            mGoogleApiClient.connect();
        }
    }

    public void checkDistance() {
        // for km multiplied by 0.001
        double distanceInKm = mStartLocation.distanceTo(mDestinationLocation) * 0.001;
        Log.i(TAG, "distance between " + distanceInKm);
        if (distanceInKm <= 1 && !isNotified) {
            isNotified = true;
            Intent intent = new Intent(this, Notifyservice.class);
            startService(intent);
            updateStopButtonState();
        }
    }

    public void updateStopButtonState() {
        buttonStart.setVisibility(View.GONE);
        buttonStop.setVisibility(View.VISIBLE);
    }

    public void updateStartButtonState() {
        if (textViewDestination.getText().length() != 0) {
            buttonStart.setVisibility(View.VISIBLE);
        } else {
            buttonStart.setVisibility(View.GONE);
        }
    }

    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i("location", "start location update permission granted");
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(UPDATE_INTERVAL);
            locationRequest.setFastestInterval(FASTEST_INTERVAL);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        } else {
            showLocationDialog();
        }
    }

    /* launch autocomplete activity for locations*/
    public void onClickSpinner(View v) {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(this);
            startActivityForResult(intent, Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    public void getLastLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Log.i(TAG, "latti" + location.getLatitude());
                    }
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                isNotified = false;
                Place place = PlaceAutocomplete.getPlace(this, data);
                LatLng placeLatLng = place.getLatLng();
                mDestinationLocation.setLatitude(placeLatLng.latitude);
                mDestinationLocation.setLongitude(placeLatLng.longitude);
                textViewDestination.setText(place.getName());
                stopLocationUpdates();
                updateStartButtonState();
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Utils.showLocationAccessDialog(this);
        } else {
            mStartLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            startLocationUpdates();
            if (mStartLocation != null) {
                Log.i(TAG, "onConnected: location " + mStartLocation.toString());
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mStartLocation = location;
        checkDistance();
    }

    public void showLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Denied!");
        builder.setMessage(Constant.LOCATION_PERMISSION_DENIED);
        builder.setCancelable(false);
        builder.setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent settingsOptionsIntent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                startActivityForResult(settingsOptionsIntent, Constant.SETTINGS_REQUEST);
            }
        }).setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                permissionDialog.dismiss();
            }
        });
        permissionDialog = builder.create();
        permissionDialog.show();
    }
}
