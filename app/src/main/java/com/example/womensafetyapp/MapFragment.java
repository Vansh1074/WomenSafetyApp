package com.example.womensafetyapp;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;

    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private TextInputEditText liveCoordinates;
    private TextView addressText;
    private Geocoder geocoder;
    private LatLng lastKnownLatLng;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        liveCoordinates = view.findViewById(R.id.live_coordinates);
        addressText = view.findViewById(R.id.address_text);

        geocoder = new Geocoder(requireContext(), Locale.getDefault());

        checkLocationSettings(); // Check and prompt for location settings
        return view;
    }

    private void checkLocationSettings() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(requireActivity())
                .checkLocationSettings(builder.build());

        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ApiException.class); // Location settings are satisfied
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        googleMap.setMyLocationEnabled(true);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                    }
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                resolvable.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are inadequate and cannot be fixed here
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        googleMap.setMyLocationEnabled(true);

        // Get the last known location and move the camera to it
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                lastKnownLatLng = currentLocation;

                // Update coordinates and address immediately
                updateLiveCoordinates(currentLocation);
                updateAddress(currentLocation);
            } else {
                // Handle case where location is null
                addressText.setText("Unable to get current location");
            }
        });

        // Set up listener to update address when the camera stops moving
        googleMap.setOnCameraIdleListener(() -> {
            LatLng center = googleMap.getCameraPosition().target;
            updateLiveCoordinates(center);
            updateAddress(center);
        });
    }

    private void updateLiveCoordinates(LatLng latLng) {
        String coordinatesText = String.format(Locale.getDefault(), "Lat: %.5f, Lng: %.5f", latLng.latitude, latLng.longitude);
        liveCoordinates.setText(coordinatesText);
    }


    private void updateAddress(LatLng latLng) {
        // Attempt to retrieve the address multiple times if needed
        final int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String addressText = address.getAddressLine(0);
                    this.addressText.setText(addressText);
                    return; // Successfully retrieved address, exit function
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            retryCount++;
            try {
                Thread.sleep(1); // Briefly wait before retrying
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

//        Toast.makeText(getContext(),""+retryCount,Toast.LENGTH_SHORT).show();
        // If all attempts fail, display a fallback message
        addressText.setText("Address not found");
    }
//    private void updateAddress(LatLng latLng) {
//        if (lastKnownLatLng == null || distanceBetween(lastKnownLatLng, latLng) > 10) {
//            lastKnownLatLng = latLng;
//            try {
//                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
//                if (addresses != null && !addresses.isEmpty()) {
//                    Address address = addresses.get(0);
//                    String addressText = address.getAddressLine(0);
//                    this.addressText.setText(addressText);
//                } else {
//                    addressText.setText("Address not found");
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                addressText.setText("Unable to get address");
//            }
//        }
//    }

    private float distanceBetween(LatLng start, LatLng end) {
        float[] results = new float[1];
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);
        return results[0];
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                googleMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
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
}
