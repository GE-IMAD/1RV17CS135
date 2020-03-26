package com.example.care_share;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.maps.android.SphericalUtil;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class RequestsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    public static RecyclerView recyclerView;public static boolean rideGoingOn=false;
    public static RequestsRecyclerViewAdapter adapter;public static String customerPh;
    public static CardView cardView;public static TextView cdTV;
    LocationRequest locationRequest;
    ArrayList<String> RequestList = new ArrayList<>(),Details=new ArrayList<>(),ImageURLs=new ArrayList<>(),ActualNos=new ArrayList<>();ArrayList<GeoPoint>Destination=new ArrayList<>(),Pickup=new ArrayList<>();
    CollectionReference CRef;
    GoogleApiClient googleApiClient;boolean route2=false;
    String phno = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(),currentURL,url,url1;
    public static double mylatitude, mylongitude, fromlatitude, fromlongitude, tolatitude, tolongitude;
    int SelectedPosition,i1,i2,j=0;
    private GoogleMap mMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.reqmap);
        mapFragment.getMapAsync(this);
        cardView=findViewById(R.id.cardView);cdTV=findViewById(R.id.textView19);
        adapter = new RequestsRecyclerViewAdapter(RequestList,ImageURLs,Details, this,ActualNos);
        FirebaseFirestore.getInstance().collection("CurrentRides").document(phno).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    recyclerView.setVisibility(View.GONE);customerPh=documentSnapshot.getString("PhoneNo");cdTV.setText(documentSnapshot.getString("Name"));
                    cardView.setVisibility(View.VISIBLE);rideGoingOn=true;
                }
            }
        });
        CRef = FirebaseFirestore.getInstance().collection("Requests").document(phno).collection("Customer_Details");
        initRecyclerView();
        CRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Users");
                j=0;
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    final  DocumentSnapshot ds=documentSnapshot;
                    query.whereEqualTo("PhoneNo",documentSnapshot.getId());
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            ParseObject result=objects.get(0);
                                String s=result.getString("Username");
                                RequestList.add(s);
                                s=result.getString("ImageURL");
                                if(s!=null && s.length()>2)
                                ImageURLs.add(s);
                                else
                                    ImageURLs.add(" ");
                                Details.add("  ");
                                ActualNos.add(ds.getId());
                                adapter.notifyDataSetChanged();
                        }
                    });
                    Destination.add(j, (GeoPoint) documentSnapshot.get("Destination"));Pickup.add(j,(GeoPoint) documentSnapshot.get("Pickup"));j++;
                }
            }
        });
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
            }
            @Override
            public void onLongClick(View view, int position) {
                SelectedPosition=position;
                mMap.clear();
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(mylatitude,mylongitude))
                        .title("My Location"))
                        .setIcon(BitmapDescriptorFactory.fromResource(R.drawable.carsilr));
                fromlatitude=Pickup.get(position).getLatitude();fromlongitude=Pickup.get(position).getLongitude();
                tolatitude=Destination.get(position).getLatitude();tolongitude=Destination.get(position).getLongitude();
                LatLng from = new LatLng(fromlatitude,fromlongitude);
                LatLng to = new LatLng(tolatitude,tolongitude);
                mMap.addMarker(new MarkerOptions().position(from).title("Pickup location"));
                mMap.addMarker(new MarkerOptions().position(to).title("Drop location"));
                getDirection();}
        }));
    }
    private void initRecyclerView() {
        recyclerView = findViewById(R.id.requests_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
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
        getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {}
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location) {
        HashMap<String,Double> map=new HashMap<>();
        if(rideGoingOn){
            map.put("Longitude",location.getLongitude());
            map.put("Latitude",location.getLatitude());
            FirebaseFirestore.getInstance().collection("OnGoingRideLocation").document(customerPh).set(map, SetOptions.merge());
            Toast.makeText(this, "Location Updated", Toast.LENGTH_SHORT).show();
        }
    }
    public static void setLocation(){
        HashMap<String,Double> map=new HashMap<>();
        map.put("Longitude",mylongitude);
        map.put("Latitude",mylatitude);
        FirebaseFirestore.getInstance().collection("OnGoingRideLocation").document(customerPh).set(map, SetOptions.merge());
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

    private void getCurrentLocation() {
        mMap.clear();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            mylongitude = location.getLongitude();
            mylatitude = location.getLatitude();
            moveMap();
        }
    }

    private void moveMap() {
        LatLng latLng = new LatLng(mylatitude, mylongitude);
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("My Location"))
                .setIcon(BitmapDescriptorFactory.fromResource(R.drawable.carsilr));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
    }
    public String makeURL (double sourcelat, double sourcelog, double destlat, double destlog ){
        String urlString = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=" +// from
                Double.toString(sourcelat) +
                "," +
                Double.toString(sourcelog) +
                "&destination=" +// to
                Double.toString(destlat) +
                "," +
                Double.toString(destlog) +
                "&sensor=false&mode=driving&alternatives=true" +
                "&key="+getString(R.string.api_key);
        return urlString;
    }

    private void getDirection(){
        //Getting the URL
        url = makeURL(mylatitude,mylongitude,fromlatitude, fromlongitude);
        //Showing a dialog till we get the route
        final ProgressDialog loading = ProgressDialog.show(this, "Getting Route", "Please wait...", false, false);

        //Creating a string request
        StringRequest stringRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //loading.dismiss();
                        route2=false;
                        //Calling the method drawPath to draw the path
                        currentURL=url;
                        drawPath(response);
                        url1 = makeURL(fromlatitude, fromlongitude, tolatitude, tolongitude);

                        //Creating a string request
                        StringRequest stringRequest1 = new StringRequest(url1,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        //loading1.dismiss();
                                        //Calling the method drawPath to draw the path
                                        currentURL=url1;
                                        route2=true;
                                        drawPath(response);
                                        double cost=i1*2+i2;
                                        Details.remove(SelectedPosition);
                                        Details.add(SelectedPosition,"Distance: "+i1+"km + "+i2+"km\nFare: Rs"+cost);
                                        Log.i("SajjanDetails",Details.toString());
                                        adapter.notifyDataSetChanged();
                                        loading.dismiss();
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                       // loading1.dismiss();
                                    }
                                });
                        RequestQueue requestQueue2=Volley.newRequestQueue(getApplicationContext());
                        requestQueue2.add(stringRequest1);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                    }
                });

        //Adding the request to request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    //The parameter is the server response
    public void drawPath(String  result) {
        try {
            //Parsing json
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            if(!route2){
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(5)
                    .color(Color.LTGRAY)
                    .geodesic(true)

            );
            line.setVisible(true); }
            else {
                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .addAll(list)
                        .width(12)
                        .color(Color.BLUE)
                        .geodesic(true)
                );
                line.setVisible(true);
            }
        }
        catch (JSONException e) {
            Log.i("Exception",e.toString());
        }
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }
            if(currentURL.equalsIgnoreCase(url)){
                i1= (int) (SphericalUtil.computeLength(poly)/1000);
            }
        if(currentURL.equalsIgnoreCase(url1)){
            i2= (int) (SphericalUtil.computeLength(poly)/1000);
        }
        return poly;
    }

    public void finishRideButton(View view){
        rideGoingOn=false;
        FirebaseFirestore.getInstance().collection("Users").document(customerPh).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String myToken=documentSnapshot.getString("MyToken");
                HashMap<String,String> myMap=new HashMap<>();
                myMap.put("My_Token",myToken);myMap.put("Response","Your Ride has ended.\nThank you for using carE-Share!");
            }
        });
        final ParseObject objectd=new ParseObject("AllRides"),objectc=new ParseObject("AllRides");
        ParseQuery<ParseObject> query=new ParseQuery<ParseObject>("Users");
        query.whereEqualTo("PhoneNo",customerPh);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                String myURL=object.getString("ImageURL");
                if(myURL!=null && myURL.length()>2)
                objectd.put("ImageURL",myURL);
                else
                    objectd.put("ImageURL","");
                String chars="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
                String autoId = "";
                for (int i = 0; i < 5; i++) {
                    autoId += chars.charAt((int)Math.floor(Math.random() * chars.length()));
                }
                objectd.put("RideNo",autoId);objectc.put("RideNo",autoId);
                Date currentTime= Calendar.getInstance().getTime();
                objectc.put("Time",currentTime);objectd.put("Time",currentTime);
                objectc.put("Type","Customer");objectd.put("Type","Driver");
                objectc.put("MyNumber",customerPh);objectd.put("MyNumber",phno);
                FirebaseFirestore.getInstance().collection("OnGoingRideLocation").document(customerPh).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String rideDetails=documentSnapshot.getString("Details");
                        String Vehicleurl=documentSnapshot.getString("ImageURL");
                        if(Vehicleurl!=null && Vehicleurl.length()>2)
                        objectc.put("ImageURL",Vehicleurl);
                        else
                            objectc.put("ImageURL","");
                        objectc.put("Details",rideDetails);objectd.put("Details",rideDetails);
                        objectc.saveInBackground();objectd.saveInBackground();
                        FirebaseFirestore.getInstance().collection("CurrentRides").document(phno).delete();
                        FirebaseFirestore.getInstance().collection("OnGoingRideLocation").document(customerPh).delete();
                        Intent intent=new Intent(getApplicationContext(),ReviewRideActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            }
        });
    }
    public void callNUm(View view){
        Uri u = Uri.parse("tel:" + customerPh);
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