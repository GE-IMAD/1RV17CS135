package com.example.care_share;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.imperiumlabs.geofirestore.GeoFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CustomerMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    HashMap<String,GeoPoint> map=new HashMap<>();
    Location lastLocation;
    LocationRequest locationRequest;
    String phno= FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(),driverToken;
    LatLng PickupLocation,destination;
    CollectionReference collectionReference= FirebaseFirestore.getInstance().collection("Customer_Requests");
    GeoFirestore geoFirestore =new GeoFirestore(collectionReference);
    CollectionReference reqRef=FirebaseFirestore.getInstance().collection("Requests");
    static CollectionReference DriverAvailRef= FirebaseFirestore.getInstance().collection("Driver_Availability");
    int f=0,AUTOCOMPLETE_REQUEST_CODE = 1;
    Boolean DestSet=false;
    RecyclerViewAdapter adapter;RecyclerView recyclerView;
    ArrayList<String> NearbyList=new ArrayList<>(),Regnos=new ArrayList<>(),Seats=new ArrayList<>(),VehicleDetails=new ArrayList<>(),PhoneNumbers=new ArrayList<>(),VehicleURLs=new ArrayList<>();
    HashMap<String,String> mapDetail=new HashMap<>();
    Button button;
    LatLng latLng;
    TextView addressField,fareTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        button=findViewById(R.id.button4);
        addressField=findViewById(R.id.editText);
        fareTV=findViewById(R.id.textView18);
        String apiKey = getString(R.string.api_key);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }
        initRecyclerView();
        addressField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Place.Field> fields = Arrays.asList(Place.Field.NAME,Place.Field.LAT_LNG);
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fields)
                        .setCountry("IN")
                        .setLocationBias(RectangularBounds.newInstance(
                                new LatLng(12.400000, 77.000000),
                                new LatLng(13.400000, 78.000000)))
                        .build(getApplicationContext());
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            }
        });
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.i("SajjanList",PhoneNumbers.toString());
                final String driverph=PhoneNumbers.get(position);
                DriverAvailRef.document(driverph).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                       GeoPoint g=documentSnapshot.getGeoPoint("l");
                        LatLng l=new LatLng(g.getLatitude(),g.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(l).title("Ride Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.carsilr)));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(l));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                        Location loc1 = new Location("");
                        loc1.setLatitude(latLng.latitude);
                        loc1.setLongitude(latLng.longitude);

                        Location loc2 = new Location("");
                        loc2.setLatitude(l.latitude);
                        loc2.setLongitude(l.longitude);

                        float distanceInMeters = loc1.distanceTo(loc2)/1000;
                        int estCost= (int) (distanceInMeters*2.5+15);
                        fareTV.setText("Estimated Fare: Rs"+estCost);
                        fareTV.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onLongClick(View view, int position) {
                final String driverph=PhoneNumbers.get(position);
                new AlertDialog.Builder(CustomerMapsActivity.this)
                        .setTitle("Send Request")
                        .setMessage("Do you want to send a request!?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mapDetail.clear();map.clear();
                                FirebaseFirestore.getInstance().collection("Users").document(driverph).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        driverToken=documentSnapshot.get("MyToken").toString();
                                        Log.i("SajjanDriver",driverph);
                                         String chars="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
                                        String autoId = "";
                                        for (int i = 0; i < 5; i++) {
                                            autoId += chars.charAt((int)Math.floor(Math.random() * chars.length()));
                                        }
                                        mapDetail.put("New_Request",phno);mapDetail.put("Driver_Token",driverToken);mapDetail.put("Ride_ID",autoId);
                                        reqRef.document(driverph).set(mapDetail);
                                        map.put("Pickup",new GeoPoint(lastLocation.getLatitude(),lastLocation.getLongitude()));
                                        map.put("Destination",new GeoPoint(destination.latitude,destination.longitude));
                                        reqRef.document(driverph).collection("Customer_Details").document(phno).set(map);
                                        mapDetail.clear();
                                        mapDetail.put("My_Token",driverToken);mapDetail.put("Response","You have a new Ride Request!!");
                                        FirebaseFirestore.getInstance().collection("Responses").document(phno).set(mapDetail);
                                        Toast.makeText(CustomerMapsActivity.this, "A request has been sent!\nPlease wait", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }));
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        recyclerView=findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter=new RecyclerViewAdapter(NearbyList,VehicleDetails,VehicleURLs,this);
        recyclerView.setAdapter(adapter);
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
        latLng=new LatLng(location.getLatitude(),location.getLongitude());
        if(f==0){
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        f=1;}
    }
    protected synchronized void buildGoogleApiClient(){
        googleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    public void nearbyfun(View view){

        geoFirestore.setLocation(phno, new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude()));
        PickupLocation=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(PickupLocation).title("Pickup location"));
        Button button=findViewById(R.id.button4);button.setText("Searching Nearby Drivers...");
        FindNearbyDrivers();
    }

    private void FindNearbyDrivers() {
        if(!DestSet){
            Toast.makeText(this, "First enter the destination!", Toast.LENGTH_LONG).show();button.setText("view nearby ride offers");return;
        }
        PhoneNumbers.clear();NearbyList.clear();Seats.clear();Regnos.clear();VehicleDetails.clear();
        adapter.notifyDataSetChanged();
        GeoFirestore DriversGeo=new GeoFirestore(DriverAvailRef);
        DriversGeo.getAtLocation((new GeoPoint(PickupLocation.latitude, PickupLocation.longitude)), 5000.0, new GeoFirestore.SingleGeoQueryDataEventCallback() {
            @Override
            public void onComplete(List<? extends DocumentSnapshot> list, Exception e) {
                if(list == null || list.size() == 0){
                    Toast.makeText(CustomerMapsActivity.this, "No Drivers Available!", Toast.LENGTH_LONG).show();button.setText("view nearby ride offers");return;
                }
                    for(DocumentSnapshot documentSnapshot:list) {
                        Regnos.add(documentSnapshot.get("Regno").toString());
                        Seats.add(documentSnapshot.get("Seats").toString());
                        PhoneNumbers.add(documentSnapshot.getId());
                    }
                    ParseQuery<ParseObject> phquery = ParseQuery.getQuery("Vehicles");
                    for (int i=0;i<PhoneNumbers.size();i++){
                        final int j=i;
                        phquery.whereEqualTo("PhoneNo",PhoneNumbers.get(i));
                        phquery.whereEqualTo("RegNo",Regnos.get(i));
                        phquery.setLimit(1);
                        phquery.findInBackground(new FindCallback<ParseObject>() {
                            public void done(List<ParseObject> results, ParseException e) {
                                for(ParseObject result:results){
                                    VehicleDetails.add(result.getString("ModelName")+"\n"+
                                                        result.getString("RegNo")+"\n"+
                                                        result.getString("ManYr")+"\n"+
                                                        "Seats: "+Seats.get(j));
                                    NearbyList.add(result.getString("Owner"));
                                    VehicleURLs.add(result.getString("VehicleURL"));
                                }
                                adapter.notifyDataSetChanged();
                                recyclerView.setVisibility(View.VISIBLE);
                                button.setText("view nearby ride offers");
                            }
                        });
                    }
            }
        });
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                addressField.setText("\n    "+place.getName());
                destination=place.getLatLng();
                mMap.addMarker(new MarkerOptions().position(destination).title(place.getName()));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(destination));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                DestSet=true;
            }
        }
    }
}