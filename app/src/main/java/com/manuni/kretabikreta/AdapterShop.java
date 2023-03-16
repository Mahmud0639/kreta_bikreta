package com.manuni.kretabikreta;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.kretabikreta.databinding.RowShopBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.AdapterShopViewHolder> implements Filterable {
    private final Context context;
    public ArrayList<ModelShop> list,filterList;
    private FilterShop filter;

    public AdapterShop(Context context, ArrayList<ModelShop> list){
        this.context = context;
        this.list = list;
        this.filterList = list;
    }


    @Override
    public AdapterShopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_shop,parent,false);
        return new AdapterShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdapterShopViewHolder holder, int position) {
        ModelShop data = list.get(position);
        String accountType = data.getAccountType();
        String address = data.getAddress();
        String city = data.getCity();
        String country = data.getCountryName();
        String deliveryFee = data.getDeliveryFee();
        String email = data.getEmail();
        String latitude = data.getLatitude();
        String longitude = data.getLongitude();
        String online = data.getOnline();
        String name = data.getFullName();
        String phone = data.getPhoneNumber();
        String uid = data.getUid();
        String timestamp = data.getTimestamp();
        String shopOpen = data.getShopOpen();
        String state = data.getState();
        String profileImage = data.getProfileImage();
        String shopName = data.getShopName();

        loadRatings(data,holder);

        try {
            holder.binding.shopNameTV.setText(shopName);
            holder.binding.addressTV.setText(address);
            holder.binding.phoneTV.setText(phone);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (online.equals("true")){
            try {
                holder.binding.onlineIV.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            try {
                holder.binding.onlineIV.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (shopOpen.equals("true")){
            try {
                holder.binding.closedTV.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            try {
                holder.binding.closedTV.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(holder.binding.shopIV);
        }catch (Exception e){
            holder.binding.shopIV.setImageResource(R.drawable.ic_store_gray);
        }

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context,ShopDetailsActivity.class);
            intent.putExtra("shopUid",uid);
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }
    private float ratingSum = 0;
    private void loadRatings(ModelShop data, AdapterShopViewHolder holder) {

        String shopUid = data.getUid();


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(shopUid).child("Ratings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ratingSum = 0;
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    float rating = 0;//e.g 4.5
                    try {
                        rating = Float.parseFloat(""+dataSnapshot.child("ratings").getValue());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    ratingSum = ratingSum+rating;


                }


                float avgOfReviews = 0;
                try {
                    long numberOfReviews = snapshot.getChildrenCount();
                    avgOfReviews = ratingSum/numberOfReviews;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                holder.binding.ratingBar.setRating(avgOfReviews);
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterShop(this,filterList);
        }
        return filter;
    }

    public class AdapterShopViewHolder extends RecyclerView.ViewHolder{
        RowShopBinding binding;

        public AdapterShopViewHolder(View itemView) {
            super(itemView);

            binding = RowShopBinding.bind(itemView);
        }
    }
}
