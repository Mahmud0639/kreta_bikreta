package com.manuni.kretabikreta;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.manuni.kretabikreta.databinding.BottomSheetProductDetailsSellerBinding;
import com.manuni.kretabikreta.databinding.SampleProductSellerBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class ProductSellerAdapter extends RecyclerView.Adapter<ProductSellerAdapter.ProductSellerViewHolder> implements Filterable {
    private Context context;
    public ArrayList<ModelProduct> list,filterList;
    private FilterProduct filter;

    public ProductSellerAdapter(Context context,ArrayList<ModelProduct> list){
        this.context = context;
        this.list = list;
        this.filterList = list;
    }

    @Override
    public ProductSellerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_product_seller,parent,false);
        return new ProductSellerViewHolder(view);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(ProductSellerViewHolder holder, int position) {
        ModelProduct data = list.get(position);
        String id = data.getProductId();
        String uid = data.getUid();
        String discountAvailable = data.getProductDiscountAvailable();
        String discountNote = data.getProductDiscountNote();
        String discountPrice = data.getProductDiscountPrice();
        String productCategory = data.getProductCategory();
        String productDescription = data.getProductDesc();
        String icon = data.getProductIcon();
        String quantity = data.getProductQuantity();
        String title = data.getProductTitle();
        String timestamp = data.getTimestamp();
        String productOriginalPrice = data.getProductOriginalPrice();
        String productAvailable = data.getProductAvailable();

        holder.binding.titleTV.setText(title);
        holder.binding.quantityTV.setText(quantity);


        double discountPriceDouble = 0;
        double productOriginalPriceDouble = 0;
        try {
            discountPriceDouble = Double.parseDouble(discountPrice);
            productOriginalPriceDouble = Double.parseDouble(productOriginalPrice);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        try {
            holder.binding.discountPriceTV.setText(String.format("৳%.2f",discountPriceDouble));
            holder.binding.originalPriceTV.setText(String.format("৳%.2f",productOriginalPriceDouble));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (productAvailable.equals("false")&&(discountAvailable.equals("true")||discountAvailable.equals("false"))){
            try {
                holder.binding.productAvailableTV.setText("Not Available");
                holder.binding.productAvailableTV.setTextColor(context.getResources().getColor(R.color.colorRed));
                // holder.binding.discountNoteTV.setVisibility(View.GONE);
                holder.binding.productAvailableTV.setVisibility(View.VISIBLE);
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }else {
            try {
                holder.binding.discountNoteTV.setText(discountNote+"% OFF");
                holder.binding.productAvailableTV.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (discountAvailable.equals("true")){
            //product is on discount

            try {
                holder.binding.discountPriceTV.setVisibility(View.VISIBLE);
                holder.binding.discountNoteTV.setText(discountNote+"% OFF");
                // holder.binding.discountNoteTV.setVisibility(View.VISIBLE);
                //to make strike original price when it is on discount state
                holder.binding.originalPriceTV.setPaintFlags(holder.binding.originalPriceTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }else {
            //product is not on discount
            try {
                holder.binding.discountPriceTV.setVisibility(View.GONE);
                //holder.binding.discountNoteTV.setText("0% OFF");
                holder.binding.discountNoteTV.setVisibility(View.GONE);
                holder.binding.originalPriceTV.setPaintFlags(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            Picasso.get().load(icon).placeholder(R.drawable.impl1).into(holder.binding.productIconIV);

        }catch (Exception e){
            holder.binding.productIconIV.setImageResource(R.drawable.impl1);

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detailsBottomSheet(data);
            }
        });


    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void detailsBottomSheet(ModelProduct data) {
        BottomSheetProductDetailsSellerBinding binding;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_product_details_seller,null);
        bottomSheetDialog.setContentView(view);


        binding = BottomSheetProductDetailsSellerBinding.bind(view);

        String id = data.getProductId();
        String uid = data.getUid();
        String discountAvailable = data.getProductDiscountAvailable();
        String discountNote = data.getProductDiscountNote();
        String discountPrice = data.getProductDiscountPrice();
        String productCategory = data.getProductCategory();
        String productDescription = data.getProductDesc();
        String icon = data.getProductIcon();
        String quantity = data.getProductQuantity();
        String title = data.getProductTitle();
        String timestamp = data.getTimestamp();
        String productOriginalPrice = data.getProductOriginalPrice();

        double discountPriceDouble = 0;
        double productOriginalPriceDouble = 0;
        try {
            discountPriceDouble = Double.parseDouble(discountPrice);
            productOriginalPriceDouble = Double.parseDouble(productOriginalPrice);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        try {
            binding.titleTV.setText(title);
            binding.descriptionTV.setText(productDescription);
            binding.categoryTV.setText("Category: "+productCategory);
            binding.quantityTV.setText(quantity);
            binding.discountNote.setText(discountNote+"% OFF");
            binding.discountPriceTV.setText(String.format("৳%.2f",discountPriceDouble));
            binding.originalPriceTV.setText(String.format("৳%.2f",productOriginalPriceDouble));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (discountAvailable.equals("true")){
            //product is on discount

            try {
                binding.discountPriceTV.setVisibility(View.VISIBLE);
                binding.discountNote.setVisibility(View.VISIBLE);
                //to make strike original price when it is on discount state
                binding.originalPriceTV.setPaintFlags(binding.originalPriceTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }else {
            //product is not on discount
            try {
                binding.discountPriceTV.setVisibility(View.GONE);
                binding.discountNote.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            Picasso.get().load(icon).placeholder(R.drawable.impl1).into(binding.productIconIV);

        }catch (Exception e){
            binding.productIconIV.setImageResource(R.drawable.impl1);

        }

        bottomSheetDialog.show();

        binding.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(context,EditProductActivity.class);
                intent.putExtra("productId",id);
                try {
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        binding.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete").setMessage("Are you sure to delete "+title+"?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteProduct(id);
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
            }
        });
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });


    }

    private void deleteProduct(String id) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.child(Objects.requireNonNull(auth.getUid())).child("Products").child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "Product deleted successfully!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure( Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
            filter = new FilterProduct(this,filterList);
        }
        return filter;
    }

    public class ProductSellerViewHolder extends RecyclerView.ViewHolder{

        SampleProductSellerBinding binding;

        public ProductSellerViewHolder(View itemView) {
            super(itemView);

            binding = SampleProductSellerBinding.bind(itemView);
        }
    }
}
