package com.manuni.kretabikreta;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
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
import com.manuni.kretabikreta.databinding.RowOrderSellerBinding;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderShop extends RecyclerView.Adapter<AdapterOrderShop.AdapterOrderShopViewHolder> implements Filterable {
    private Context context;
    public ArrayList<ModelOrderShop> list,filterList;
    private FilterOrderShop filter;

    public AdapterOrderShop(Context context, ArrayList<ModelOrderShop> list){
        this.context = context;
        this.list = list;
        this.filterList = list;
    }


    @Override
    public AdapterOrderShopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_seller,parent,false);
        return new AdapterOrderShopViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(AdapterOrderShopViewHolder holder, int position) {
        ModelOrderShop data = list.get(position);
        String orderId = data.getOrderId();
        String orderBy = data.getOrderBy();
        String orderCost = data.getOrderCost();
        String orderTo = data.getOrderTo();
        String orderStatus = data.getOrderStatus();
        String orderTime = data.getOrderTime();
        String orderDeliveryFee = data.getDeliveryFee();
        String latitude = data.getLatitude();
        String longitude = data.getLongitude();

        loadBuyerInfos(data,holder);


        holder.binding.orderIdTV.setText("Order ID: "+orderId);
        holder.binding.orderAmountTV.setText("Total Amount: ৳"+orderCost);
        holder.binding.statusTV.setText(orderStatus);


        if (orderStatus.equals("In Progress")){
            holder.binding.statusTV.setTextColor(context.getResources().getColor(R.color.background_theme));
        }else if (orderStatus.equals("Completed")){
            holder.binding.statusTV.setTextColor(context.getResources().getColor(R.color.colorGreen));
        }else if (orderStatus.equals("Cancelled")){
            holder.binding.statusTV.setTextColor(context.getResources().getColor(R.color.colorRed));
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String date = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        holder.binding.dateTV.setText(date);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context,OrderDetailsSellerActivity.class);
            intent.putExtra("orderId",orderId);//to load order info
            intent.putExtra("orderBy",orderBy);//to load user info
            intent.putExtra("orderTo",orderTo);
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void loadBuyerInfos(ModelOrderShop data, AdapterOrderShopViewHolder holder) {

        String buyerUid = data.getOrderBy();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.child(buyerUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String email = ""+snapshot.child("email").getValue();
                holder.binding.emailTV.setText(email);
            }

            @Override
            public void onCancelled( DatabaseError error) {

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
            filter = new FilterOrderShop(this,filterList);
        }
        return filter;
    }

    public class AdapterOrderShopViewHolder extends RecyclerView.ViewHolder{

        RowOrderSellerBinding binding;
        public AdapterOrderShopViewHolder(View itemView){
            super(itemView);
            binding = RowOrderSellerBinding.bind(itemView);
        }

    }
}
