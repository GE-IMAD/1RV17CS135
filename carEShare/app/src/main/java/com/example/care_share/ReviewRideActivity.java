package com.example.care_share;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.imperiumlabs.geofirestore.GeoFirestore;

public class ReviewRideActivity extends AppCompatActivity {
    TextView textView;
    NumberPicker numberPicker;String ObjectId;
    Switch sw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_ride);
        sw =findViewById(R.id.switch2);
        numberPicker=findViewById(R.id.numberPicker);
        textView=findViewById(R.id.textView24);
        ParseQuery<ParseObject> query=new ParseQuery<>("AllRides");
        query.whereEqualTo("MyNumber", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        query.orderByDescending("Time");
        query.setLimit(1);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                String rating=object.getString("Rating");
                if(rating!=null && rating.length()>0){
                    Toast.makeText(ReviewRideActivity.this, "Rating already submitted", Toast.LENGTH_SHORT).show();
                    finish();
                }
                if(object.getString("Type").equalsIgnoreCase("Customer"))
                    textView.setText("Did the driver drive safely and follow all the traffic rules?");
                ObjectId=object.getObjectId();
            }
        });
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(10);
    }
    public void submit(View view){
        int value=numberPicker.getValue();
        if(!sw.isChecked())
            value-=2;
        final int v=value;
        Log.i("SajjanID",ObjectId);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("AllRides");
        query.whereEqualTo("objectId",ObjectId);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    object.put("Rating", String.valueOf(v));
                    object.saveInBackground();
                    finish();
                }
            }
        });
    }
}
