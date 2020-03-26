package com.example.care_share;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class RequestsRecyclerViewAdapter extends RecyclerView.Adapter<RequestsRecyclerViewAdapter.RequestViewHolder> {
    ArrayList<String> PhoneNumbers,ImageURLs,Details,ActualNos;
    Context mContext;
    CollectionReference cRef= FirebaseFirestore.getInstance().collection("Responses");
    String myToken;

    public RequestsRecyclerViewAdapter(ArrayList<String> phoneNumbers,ArrayList<String> ImageURLs,ArrayList<String> Details, Context mContext,ArrayList<String> ActualNos) {
        PhoneNumbers = phoneNumbers;
        this.mContext = mContext;
        this.ImageURLs=ImageURLs;
        this.Details=Details;
        this.ActualNos=ActualNos;
    }

    @NonNull
    @Override
    public RequestsRecyclerViewAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.request_item,parent,false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestsRecyclerViewAdapter.RequestViewHolder holder, final int position) {
         final String p=PhoneNumbers.get(position);
        if(ImageURLs.get(position).length()>2)
            Glide.with(mContext).load(ImageURLs.get(position)).into(holder.image);
        else
            Glide.with(mContext).load(R.drawable.ic_person).into(holder.image);
        holder.PhNoTV.setText(p);
        holder.textView.setText(Details.get(position));
        holder.callImage.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Uri u = Uri.parse("tel:" + p);
                 Intent i = new Intent(Intent.ACTION_DIAL, u);
                 i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 try
                 {
                     mContext.startActivity(i);
                 }
                 catch (SecurityException s)
                 {
                     Toast.makeText(mContext, s.getMessage(), Toast.LENGTH_SHORT)
                             .show();
                 }

             }
         });
        holder.AcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseFirestore.getInstance().collection("Users").document(ActualNos.get(position)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        myToken = documentSnapshot.get("MyToken").toString();
                        HashMap<String, String> map = new HashMap<>();
                        map.put("Response", "Your Ride has been Accepted!!");
                        map.put("My_Token", myToken);
                        cRef.document(ActualNos.get(position)).set(map);
                        RequestsActivity.recyclerView.setVisibility(View.GONE);
                        RequestsActivity.cdTV.setText(PhoneNumbers.get(position));
                        RequestsActivity.cardView.setVisibility(View.VISIBLE);
                        RequestsActivity.customerPh=ActualNos.get(position);
                        RequestsActivity.rideGoingOn=true;
                        HashMap<String,String> nmap=new HashMap<>();
                        nmap.put("PhoneNo",ActualNos.get(position));
                        nmap.put("Name",PhoneNumbers.get(position));
                        FirebaseFirestore.getInstance().collection("CurrentRides").document(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).set(nmap);
                        FirebaseFirestore.getInstance().collection("Driver_Availability").document(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                final String reg=documentSnapshot.getString("Regno");
                                ParseQuery<ParseObject> query = ParseQuery.getQuery("Vehicles");
                                query.whereEqualTo("PhoneNo",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                                query.whereEqualTo("RegNo",reg);
                                query.getFirstInBackground(new GetCallback<ParseObject>() {
                                    @Override
                                    public void done(ParseObject object, ParseException e) {
                                            HashMap<String,String> fmap=new HashMap<>();
                                            fmap.put("RegNo",reg);
                                            fmap.put("ImageURL",object.getString("VehicleURL"));
                                            fmap.put("Details",Details.get(position));
                                            fmap.put("DriverPh",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                                            FirebaseFirestore.getInstance().collection("OnGoingRideLocation").document(ActualNos.get(position)).set(fmap);
                                            RequestsActivity.setLocation();
                                            FirebaseFirestore.getInstance().collection("Requests").document(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).collection("Customer_Details").document(ActualNos.get(position)).delete();

                                    }
                                });
                            }
                        });
                    }});
            }
        });
        holder.CancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseFirestore.getInstance().collection("Users").document(ActualNos.get(position)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                         myToken= documentSnapshot.get("MyToken").toString();
                        HashMap<String,String> map=new HashMap<>();
                        map.put("Response","Your Ride has been Canceled :(");
                        map.put("My_Token",myToken);
                        cRef.document(ActualNos.get(position)).set(map);
                        ActualNos.remove(position);Details.remove(position);ImageURLs.remove(position);PhoneNumbers.remove(position);
                        RequestsActivity.adapter.notifyDataSetChanged();
                    }});
            }
        });
    }
    @Override
    public int getItemCount() {
        return PhoneNumbers.size();
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder{
        CircleImageView image;
        TextView PhNoTV,textView;
        ImageView callImage;
        Button AcceptButton,CancelButton;
        RecyclerView r;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.request_circleimageID);
            PhNoTV=itemView.findViewById(R.id.RequestphnoTV);
            callImage=itemView.findViewById(R.id.CallimageView);
            textView=itemView.findViewById(R.id.editText2);
            AcceptButton=itemView.findViewById(R.id.AcceptButton);
            CancelButton=itemView.findViewById(R.id.cancelButton);
        }
    }
}
