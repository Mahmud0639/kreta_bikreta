package com.manuni.kretabikreta;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

import com.manuni.kretabikreta.databinding.ActivityProductInfoBinding;
import com.squareup.picasso.Picasso;

public class ProductInfoActivity extends AppCompatActivity {
    ActivityProductInfoBinding binding;
    private String productIcon,originalPrice,discountPrice,discountNote,productTitle,productDescription,productQuantity,productBrand;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            productIcon = getIntent().getStringExtra("productIcon");
            originalPrice = getIntent().getStringExtra("originalPrice");
            discountPrice = getIntent().getStringExtra("discountPrice");
            discountNote = getIntent().getStringExtra("discountNote");
            productTitle = getIntent().getStringExtra("productTitle");
            productDescription = getIntent().getStringExtra("productDes");
            productQuantity = getIntent().getStringExtra("productQuantity");
            productBrand = getIntent().getStringExtra("productBrand");
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            Picasso.get().load(productIcon).placeholder(R.drawable.impl1).into(binding.productIV);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.impl1).into(binding.productIV);
        }
        if (discountPrice.equals("0.0")){
            try {
                binding.textView.setPaintFlags(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            try {
                binding.textView.setPaintFlags(binding.textView.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            binding.pDiscount.setText("৳"+discountPrice);
            binding.productName.setText(productTitle);
            binding.textView.setText("৳"+originalPrice);
            binding.descriptionTV.setText(productDescription);
            binding.quantity.setText(productQuantity);
            binding.proBrandName.setText(productBrand);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (discountNote.equals("0")){
            try {
                binding.discountNoteTV.setVisibility(View.GONE);
                binding.pDiscount.setVisibility(View.GONE);
                binding.textView.setTextColor(getResources().getColor(R.color.blue));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            try {
                binding.pDiscount.setVisibility(View.VISIBLE);
                binding.discountNoteTV.setVisibility(View.VISIBLE);
                binding.discountNoteTV.setText(discountNote+"% OFF");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        binding.backBtn.setOnClickListener(view -> onBackPressed());


    }
}