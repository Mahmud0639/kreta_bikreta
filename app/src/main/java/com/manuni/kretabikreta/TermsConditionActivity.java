package com.manuni.kretabikreta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.manuni.kretabikreta.databinding.ActivityTermsConditionBinding;

public class TermsConditionActivity extends AppCompatActivity {
    ActivityTermsConditionBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTermsConditionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());




        binding.gotoNextBtn.setOnClickListener(view -> {

            try {
                startActivity(new Intent(TermsConditionActivity.this,RegisterSellerActivity.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
            // checkConnection();
        });


        binding.termsCheck.setOnCheckedChangeListener((compoundButton, b) -> binding.gotoNextBtn.setEnabled(b));




    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}