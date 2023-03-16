package com.manuni.kretabikreta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.manuni.kretabikreta.databinding.ActivityUpdateBinding;

public class UpdateActivity extends AppCompatActivity {
    ActivityUpdateBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);


                if (mobile.isConnected()){
                    updateApp();
                }else if (wifi.isConnected()){
                    updateApp();
                }else {
                    Toast.makeText(UpdateActivity.this, "No connection", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }
    private void updateApp(){
        try {
            Intent updateIntent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse("market://details?id="+getPackageName());
            updateIntent.setData(uri);
            updateIntent.setPackage("com.android.vending");
            startActivity(updateIntent);
        }catch (ActivityNotFoundException e){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName());
            intent.setData(uri);
            startActivity(intent);
        }
    }
}