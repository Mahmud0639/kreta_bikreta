package com.manuni.kretabikreta;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.manuni.kretabikreta.databinding.OrderedItemsRowBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterOrderedItemsSeller extends RecyclerView.Adapter<AdapterOrderedItemsSeller.AdapterOrderedItemsSellerViewHolder> {
    private Context context;
    private ArrayList<ModelOrderedItems> list;

    public AdapterOrderedItemsSeller(Context context, ArrayList<ModelOrderedItems> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public AdapterOrderedItemsSellerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.ordered_items_row,parent,false);
        return new AdapterOrderedItemsSellerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdapterOrderedItemsSellerViewHolder holder, int position) {
        ModelOrderedItems data = list.get(position);
        String pId = data.getpId();
        String name = data.getName();
        String cost = data.getCost();
        String price = data.getPrice();
        String quantity = data.getQuantity();
        String productQuantity = data.getProQuantity();
        String proImage = data.getPrImage();


        try {
            Picasso.get().load(proImage).placeholder(R.drawable.impl1).into(holder.binding.productImage);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.impl1).into(holder.binding.productImage);
        }
        holder.binding.productTotalDesc.setText(productQuantity+" এর "+quantity+" টি অর্ডার");
        holder.binding.itemTitleTV.setText(name);
        holder.binding.itemPriceEachTV.setText("৳"+price);
        holder.binding.itemPriceTV.setText("৳"+cost);
        holder.binding.itemQuantityTV.setText("["+quantity+"]");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class AdapterOrderedItemsSellerViewHolder extends RecyclerView.ViewHolder{

        OrderedItemsRowBinding binding;
        public AdapterOrderedItemsSellerViewHolder(View itemView) {
            super(itemView);

            binding = OrderedItemsRowBinding.bind(itemView);
        }
    }
}
