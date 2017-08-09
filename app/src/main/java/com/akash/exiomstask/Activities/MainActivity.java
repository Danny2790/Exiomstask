package com.akash.exiomstask.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.akash.exiomstask.Constants.Constant;
import com.akash.exiomstask.R;
import com.akash.exiomstask.Services.Notifyservice;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.akash.exiomstask.Constants.Constant.FASTEST_INTERVAL;
import static com.akash.exiomstask.Constants.Constant.UPDATE_INTERVAL;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private String TAG = MainActivity.class.getSimpleName();
    EditText editTextCurrent;
    TextView textViewDestination;
    Button buttonStart;
    Button buttonStop;
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleApiClient mGoogleApiClient;
    private Location mStartLocation;
    private Location mDestinationLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextCurrent = (EditText) findViewById(R.id.et_current);
        textViewDestination = (TextView) findViewById(R.id.tv_destination);
        buttonStart = (Button) findViewById(R.id.btn_start);
        buttonStop = (Button) findViewById(R.id.btn_stop);
        mDestinationLocation = new Location("destination location");

        editTextCurrent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateStartButtonState();
            }
        });

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

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
                stopLocationUpdates();
                buttonStart.setVisibility(View.VISIBLE);
                buttonStop.setVisibility(View.GONE);
            }
        });
    }

    public void checkDistance() {
        // for km multiplied by 0.001
        double distanceInKm = mStartLocation.distanceTo(mDestinationLocation) * 0.001;
        Log.i(TAG, "distance between " + distanceInKm);
        if (distanceInKm <= 1) {
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
        if (editTextCurrent.getText().length() != 0 && textViewDestination.getText().length() != 0) {
            buttonStart.setVisibility(View.VISIBLE);
        } else {
            buttonStart.setVisibility(View.GONE);
        }
    }

    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public void startLocationUpdates() {
        Log.i("location", "start location update is called");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i("location", "start location update permission granted");
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(UPDATE_INTERVAL);
            locationRequest.setFastestInterval(FASTEST_INTERVAL);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        }
    }

    public void onClickSpinner(View v) {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(this);
            startActivityForResult(intent, Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    public void getLastLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        getAddress(location.getLatitude(), location.getLongitude());
                        Log.i(TAG, "latti" + location.getLatitude());
                    }
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void getAddress(double lat, double lng) {
        Log.i(TAG, "Address called");
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses.size() > 0) {
                Address obj = addresses.get(0);
                String add = obj.getAddressLine(0);
                String street = obj.getAddressLine(0);
                add = add + "\n" + obj.getCountryName();
                add = add + "\n" + obj.getCountryCode();
                add = add + "\n" + obj.getAdminArea();
                add = add + "\n" + obj.getPostalCode();
                add = add + "\n" + obj.getSubAdminArea();
                add = add + "\n" + obj.getLocality();
                add = add + "\n" + obj.getSubThoroughfare();
                Log.i(TAG, "Address" + add);
                editTextCurrent.setText(street);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
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
        Log.i(TAG, "onConnected: location called");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        } else {
            mStartLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.i(TAG, "onConnected: location " + mStartLocation.toString());
            getAddress(mStartLocation.getLatitude(), mStartLocation.getLongitude());
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
        Log.i(TAG, "onlocationchanged : " + location);
        mStartLocation = location;
        checkDistance();
    }
}
