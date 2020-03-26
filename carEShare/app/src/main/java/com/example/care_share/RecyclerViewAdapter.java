package com.example.care_share;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
ArrayList<String> Names,Details,URLs;
Context mContext;

    public RecyclerViewAdapter(ArrayList<String> Names,ArrayList<String> Details,ArrayList<String> URLs, Context mContext) {
        this.Names = Names;this.Details=Details;this.URLs=URLs;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.PhNoTV.setText(Names.get(position));
        holder.DetailsTV.setText(Details.get(position));
        if(URLs.get(position).length()>0)
            Glide.with(mContext).load(URLs.get(position)).into(holder.image);
        else
            Glide.with(mContext).load(R.drawable.care_share).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return Names.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView image;
        TextView PhNoTV,DetailsTV;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.circleimageID);
            PhNoTV=itemView.findViewById(R.id.phnoTV);
            DetailsTV=itemView.findViewById(R.id.textView17);
        }
    }
}