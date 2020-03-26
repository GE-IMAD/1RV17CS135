package com.example.care_share;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import org.imperiumlabs.geofirestore.GeoFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DriverMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    boolean SwitchOn=false;
    String phno= FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
    CollectionReference collectionReference= FirebaseFirestore.getInstance().collection("Driver_Availability");
    GeoFirestore geoFirestore =new GeoFirestore(collectionReference);
    ArrayList<String> allReg=new ArrayList<>();
    ArrayAdapter<String> autoAdapter;
    Switch sw;
    AutoCompleteTextView myReg;
    EditText seats;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        myReg=findViewById(R.id.autoCompleteTextView);seats=findViewById(R.id.editText8);
        ParseQuery<ParseObject> query=ParseQuery.getQuery("Vehicles");
        query.whereEqualTo("PhoneNo",phno);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject object : objects)
                        allReg.add(object.getString("RegNo"));
                    autoAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, allReg);
                    myReg.setAdapter(autoAdapter);
                    myReg.setThreshold(1);
                }
            }
        });
        sw =findViewById(R.id.switch1);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SwitchOn=true;
                } else {
                    SwitchOn=false;
                    geoFirestore.removeLocation(phno, new GeoFirestore.CompletionCallback() {
                        @Override
                        public void onComplete(Exception e) {
                            Toast.makeText(getApplicationContext(), "You are removed from the list of available drivers", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        buildGoogleApiClient();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }
    @Override
    public void onConnectionSuspended(int i) { }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation=location;
        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        if(SwitchOn){
        String phno= FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        CollectionReference collectionReference= FirebaseFirestore.getInstance().collection("Driver_Availability");
        GeoFirestore geoFirestore =new GeoFirestore(collectionReference);
        geoFirestore.setLocation(phno,new GeoPoint(location.getLatitude(),location.getLongitude()));
            HashMap<String,String> map=new HashMap<>();
            map.put("Regno",myReg.getText().toString());
            map.put("Seats",seats.getText().toString());
            collectionReference.document(phno).set(map, SetOptions.merge());
        }
    }

    protected synchronized void buildGoogleApiClient(){
        googleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
        }
        public void ViewRequestButton(View view){
        Intent intent=new Intent(getApplicationContext(),RequestsActivity.class);startActivity(intent);
        }

    @Override
    protected void onStart() {
        super.onStart();
        collectionReference.document(phno).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                try {
                    String reg=documentSnapshot.get("Regno").toString(),seat=documentSnapshot.get("Seats").toString();
                    if(reg.length()>0 && seat.length()>0 ){
                        myReg.setText(reg);seats.setText(seat);
                        SwitchOn=true;
                        sw.setChecked(true);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        collectionReference.document(phno).get().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("SajjanError",e.toString());
            }
        });
    }
}