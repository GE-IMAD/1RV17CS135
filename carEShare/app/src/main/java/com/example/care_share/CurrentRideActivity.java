package com.example.care_share;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;

import de.hdodenhof.circleimageview.CircleImageView;

public class CurrentRideActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    TextView textView;
    CircleImageView imageView;
    GoogleMap mMap;String driverph;
    GoogleApiClient googleApiClient;GeoPoint geoPoint;
    DocumentReference documentReference= FirebaseFirestore.getInstance().collection("OnGoingRideLocation").document(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_ride2);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.current_ride);
        mapFragment.getMapAsync(this);
        textView=findViewById(R.id.textView21);
        imageView=findViewById(R.id.currentImageView);
        FirebaseFirestore.getInstance().collection("OnGoingRideLocation").document(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(!documentSnapshot.exists()){
                    Toast.makeText(CurrentRideActivity.this, "No Ongoing ride ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                String url=documentSnapshot.getString("ImageURL");
                if(url!=null && url.length()>2)
                    Glide.with(getApplicationContext()).load(url).into(imageView);
                else
                    Glide.with(getApplicationContext()).load(R.drawable.ic_person).into(imageView);
                textView.setText(documentSnapshot.getString("RegNo")+"\n"+documentSnapshot.getString("Details"));
                driverph=documentSnapshot.getString("DriverPh");
            }
        });
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Double cLatitude,cLongitude;
                        mMap.clear();
                        Log.i("SajjanGeoPoint",documentSnapshot.toString());
                        cLatitude=documentSnapshot.getDouble("Latitude");
                        cLongitude=documentSnapshot.getDouble("Longitude");
                        LatLng latLng = new LatLng(cLatitude, cLongitude);
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title("Driver Location"))
                                .setIcon(BitmapDescriptorFactory.fromResource(R.drawable.carsilr));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }
    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) { }
    @Override
    public void onConnectionSuspended(int i) { }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }
    public void giveCall(View view){
        Uri u = Uri.parse("tel:" + driverph);
        Intent i = new Intent(Intent.ACTION_DIAL, u);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try
        {
            getApplicationContext().startActivity(i);
        }
        catch (SecurityException s)
        {
            Toast.makeText(getApplicationContext(), s.getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
