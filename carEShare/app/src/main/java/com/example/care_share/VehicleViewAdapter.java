package com.example.care_share;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;


public class VehicleViewAdapter extends RecyclerView.Adapter<VehicleViewAdapter.ViewHolder> {
    ArrayList<String> Details,Pics;
    Context mContext;

    public VehicleViewAdapter(ArrayList<String> Details,ArrayList<String>Photos, Context mContext) {
        this.Details = Details;
        this.mContext = mContext;
        Pics=Photos;
    }

    @NonNull
    @Override
    public VehicleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.vehicle_item,parent,false);
        return new VehicleViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewAdapter.ViewHolder holder, int position) {
        String p=Details.get(position);
        holder.VehicleDetails.setText(p);
        String cURL=Pics.get(position);
        if(cURL.length()>0)
            Glide.with(mContext).load(cURL).into(holder.image);
        else
            Glide.with(mContext).load(R.drawable.care_share).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return Details.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView VehicleDetails;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.VehicleImageView);
            VehicleDetails=itemView.findViewById(R.id.VehicleDetails);
        }
    }
}
