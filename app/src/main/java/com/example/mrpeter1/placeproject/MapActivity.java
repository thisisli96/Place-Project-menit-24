package com.example.mrpeter1.placeproject;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mancj.materialsearchbar.MaterialSearchBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient; //fetching the location of the device
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictions;

    private Location mLastKnownLocation; // be storing current last known location
    private LocationCallback locationCallback; // untuk update atau memperbarui location terakhir

    private MaterialSearchBar materialSearchBar;
    private View mapView;
    private Button btnFind;

    private final float DEFAULT_ZOOM = 18;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        materialSearchBar = findViewById(R.id.searchBar);
        btnFind = findViewById(R.id.btnFind); // btn find

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map); // id fragment

        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this); // inisiasi varibel atau ini untuk enable fused location
        Places.initialize(MapActivity.this,"AIzaSyAvnr2u_HcPVVhK-aC4Q1XBDMjKeeRzavY"); //
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString(),true,null,true);

            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_BACK){
                    //if click opening or closing a navigation drawer

                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK){
                    materialSearchBar.disableSearch();
                }

            }
        });

        materialSearchBar.addTextChangeListener(new TextWatcher() { // ketika search barnya di isi tulisan
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
 // logic in here
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setCountry("id")
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if (task.isSuccessful()){

                        }else   {
                            Log.i("mytag", "prediction fetching task unseccessful");
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) { // langka pertama memasang map ke variabel mMap

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);


        if (mapView != null && mapView.findViewById(Integer.parseInt("1"))!= null){

            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0,0,40,180);
        } // dari atas sampai bawa ini, membuat ui untuk mengubah peta

        // mengganti lokasi user tapi sebelum itu kita pastikan dulu apakah gps aktif atay tidak
        // check if gps is enable or  not and ther request user to enable it

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
// this function will check for location setting ithink the location setting will be sufficient that the location will be on and you can perform a tack or the permission the location setting will  not be on
        SettingsClient settingsClient = LocationServices.getSettingsClient(MapActivity.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(MapActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            } // jika gps is enable this function is called
        });

        task.addOnFailureListener(MapActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException){ //user can enable the location can not accept
                    ResolvableApiException resolvable = (ResolvableApiException) e ;
                    try {
                        resolvable.startResolutionForResult(MapActivity.this,51 ); // jika location dienable nilai yang di passing adalah 51

                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                } // if gps is not enable this function is called

            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 51){
            if(resultCode == RESULT_OK) { // mean that user accepted the request and enabled the GPS option on their phone

                // current location and move the map to that particular location
                getDeviceLocation();
            }

        }
    }

    private void getDeviceLocation(){ // function untuk current location

        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()){
                    mLastKnownLocation = task.getResult();
                    if(mLastKnownLocation != null){ // jika gps di enable
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom
                                (new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                    } else { // jika di tolak
                        final LocationRequest locationRequest = LocationRequest.create();
                        locationRequest.setInterval(10000);
                        locationRequest.setFastestInterval(5000);
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                             if(locationResult == null){
                                 return;
                             }
                             mLastKnownLocation = locationResult.getLastLocation();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()),DEFAULT_ZOOM));
                            mFusedLocationProviderClient.removeLocationUpdates(locationCallback);

                            }

                        };
                        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

                    }
                } else  {
                    Toast.makeText(MapActivity.this,"unable to get last location" , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // next step would be to use the places API switch suggestions as the user tyoes in the search bar
    // user search in search bar

}
