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

public class AllRidesRecyclerViewAdapter extends RecyclerView.Adapter<AllRidesRecyclerViewAdapter.ViewHolder> {
        ArrayList<String> Details,ImageUrls;
        Context mContext;

    public AllRidesRecyclerViewAdapter(Context mContext,ArrayList<String> Details,ArrayList<String> ImageUrls) {
        this.mContext = mContext;
        this.Details=Details;
        this.ImageUrls=ImageUrls;
    }

@NonNull
@Override
public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.allrides_item,parent,false);
        return new AllRidesRecyclerViewAdapter.ViewHolder(view);
        }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(Details.get(position));
        if(ImageUrls.get(position).length()>2)
            Glide.with(mContext).load(ImageUrls.get(position)).into(holder.imageView);
        else
            Glide.with(mContext).load(R.drawable.ic_person).into(holder.imageView);
    }

@Override
public int getItemCount() {
        return Details.size();
        }

class ViewHolder extends RecyclerView.ViewHolder{
    CircleImageView imageView;
    TextView textView;
    ViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView=itemView.findViewById(R.id.circleImageView2);
        textView=itemView.findViewById(R.id.textView29);
    }
}
}

