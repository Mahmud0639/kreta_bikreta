package com.manuni.kretabikreta;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.kretabikreta.databinding.RowReviewBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterReview extends RecyclerView.Adapter<AdapterReview.AdapterReviewViewHolder>{
    private Context context;
    private ArrayList<ModelReview> list;

    public AdapterReview(Context context,ArrayList<ModelReview> list){
        this.context = context;
        this.list = list;
    }


    @Override
    public AdapterReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_review,parent,false);
        return new AdapterReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder( AdapterReviewViewHolder holder, int position) {
        ModelReview data = list.get(position);

        String uid = data.getUid();
        String reviews = data.getReviews();
        String ratings = data.getRatings();
        String timestamp = data.getTimestamp();

        loadUserDetails(data,holder);

        //convert timestamp to proper date
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String date = DateFormat.format("dd/MM/yyyy",calendar).toString();

        holder.binding.reviewMsgTV.setText(reviews);
        //set ratings as float
        float ratingsValue = 0;
        try {
            ratingsValue = Float.parseFloat(ratings);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        holder.binding.ratingBar.setRating(ratingsValue);

        holder.binding.dateTV.setText(date);


    }

    private void loadUserDetails(ModelReview data, AdapterReviewViewHolder holder) {
        String uidOfUser = data.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.child(uidOfUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String fullName = null;
                String profile = null;
                try {
                    fullName = ""+snapshot.child("fullName").getValue();
                    profile = ""+snapshot.child("profileImage").getValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                holder.binding.nameTV.setText(fullName);
                try {
                    Picasso.get().load(profile).placeholder(R.drawable.ic_person_gray).into(holder.binding.profileIV);
                } catch (Exception e) {
                    Picasso.get().load(R.drawable.ic_person_gray).into(holder.binding.profileIV);
                    e.printStackTrace();
                }
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

    public class AdapterReviewViewHolder extends RecyclerView.ViewHolder{

        RowReviewBinding binding;
        public AdapterReviewViewHolder(View itemView){
            super(itemView);
            binding = RowReviewBinding.bind(itemView);
        }
    }
}
