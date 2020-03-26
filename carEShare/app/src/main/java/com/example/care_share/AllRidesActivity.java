package com.example.care_share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class AllRidesActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    AllRidesRecyclerViewAdapter adapter;
    ConstraintLayout c;
    TextView offeredRides,takenRides;
    ArrayList<String> Details=new ArrayList<>(),ImageURLS=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_rides);
        recyclerView=findViewById(R.id.allRidesRecyclerView);
        c=findViewById(R.id.filter_constraint_layout);
        offeredRides=findViewById(R.id.textView27);
        takenRides=findViewById(R.id.textView22);
        adapter=new AllRidesRecyclerViewAdapter(getApplicationContext(),Details,ImageURLS);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        ParseQuery<ParseObject> query=new ParseQuery<ParseObject>("AllRides");
        query.whereEqualTo("MyNumber", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        query.orderByDescending("Time");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for(ParseObject object:objects){
                ImageURLS.add(object.getString("ImageURL"));
                Details.add("RideID: "+object.getString("RideNo")
                                //+"\nTime: "+object.getString("Time")
                                +"\nDetails: "+object.getString("Details")
                                +"\nType: "+object.getString("Type")
                                +"\nRating Given: "+object.getString("Rating")
                        );
            }
                adapter.notifyDataSetChanged();
            }
        });
    }
    public void filterButton(View view){
        if(c.getVisibility()==View.VISIBLE) {
            c.setVisibility(View.INVISIBLE);
            recyclerView.setAlpha((float) 1.0);
        }
        else {
            c.setVisibility(View.VISIBLE);
            recyclerView.setAlpha((float) 0.25);
        }
    }
    public void onlyOffered(View view){
        ImageURLS.clear();Details.clear();
        ParseQuery<ParseObject> query=new ParseQuery<>("AllRides");
        query.whereEqualTo("MyNumber", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        query.whereEqualTo("Type","Driver");
        query.orderByDescending("Time");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for(ParseObject object:objects){
                    ImageURLS.add(object.getString("ImageURL"));
                    Details.add("RideID: "+object.getString("RideNo")
                            //+"\nTime: "+object.getString("Time")
                            +"\nDetails: "+object.getString("Details")
                            +"\nType: "+object.getString("Type")
                            +"\nRating Given: "+object.getString("Rating")
                    );
                }
                adapter.notifyDataSetChanged();
            }
        });

        c.setVisibility(View.INVISIBLE);
        recyclerView.setAlpha((float) 1.0);
    }
    public void onlyTaken(View view){
        ImageURLS.clear();Details.clear();
        ParseQuery<ParseObject> query=new ParseQuery<>("AllRides");
        query.whereEqualTo("MyNumber", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        query.whereEqualTo("Type","Customer");
        query.orderByDescending("Time");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for(ParseObject object:objects){
                    ImageURLS.add(object.getString("ImageURL"));
                    Details.add("RideID: "+object.getString("RideNo")
                            //+"\nTime: "+object.getString("Time")
                            +"\nDetails: "+object.getString("Details")
                            +"\nType: "+object.getString("Type")
                            +"\nRating Given: "+object.getString("Rating")
                    );
                }
                adapter.notifyDataSetChanged();
            }
        });

        c.setVisibility(View.INVISIBLE);
        recyclerView.setAlpha((float) 1.0);
    }

    public void allRides(View view){
        ImageURLS.clear();Details.clear();
        ParseQuery<ParseObject> query=new ParseQuery<>("AllRides");
        query.whereEqualTo("MyNumber", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        query.orderByDescending("Time");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for(ParseObject object:objects){
                    ImageURLS.add(object.getString("ImageURL"));
                    Details.add("RideID: "+object.getString("RideNo")
                            //+"\nTime: "+object.getString("Time")
                            +"\nDetails: "+object.getString("Details")
                            +"\nType: "+object.getString("Type")
                            +"\nRating Given: "+object.getString("Rating")
                    );
                }
                adapter.notifyDataSetChanged();
            }
        });

        c.setVisibility(View.INVISIBLE);
        recyclerView.setAlpha((float) 1.0);
    }
}
