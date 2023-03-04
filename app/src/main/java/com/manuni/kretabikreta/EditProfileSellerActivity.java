package com.manuni.kretabikreta;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.manuni.kretabikreta.databinding.ActivityEditProfileSellerBinding;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class EditProfileSellerActivity extends AppCompatActivity implements LocationListener {
    ActivityEditProfileSellerBinding binding;
    private static final int LOCATION_REQUEST_CODE = 100;


    private String[] locationPermissions;
    private String[] cameraPermissions;
    private String[] storagePermissions;

    private ProgressDialog progressDialog;

    private FirebaseAuth auth;
    private LocationManager locationManager;
    private double latitude = 0.0;
    private double longitude = 0.0;

    private Uri imageUri;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileSellerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        auth = FirebaseAuth.getInstance();


        try {
            checkUser();
        } catch (Exception e) {
            e.printStackTrace();
        }


        dialog = new ProgressDialog(this);
        dialog.setTitle("Please wait");
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backArrowBtn.setOnClickListener(view -> onBackPressed());
        binding.gpsBtn.setOnClickListener(view -> {
            if (checkLocationPermission()){
                try {
                    detectLocation();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                try {
                    requestLocationPermission();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        binding.updateBtn.setOnClickListener(view -> inputData());
        binding.addPhoto.setOnClickListener(view -> showImagePicDialog());
    }


    private void showImagePicDialog(){
        ImagePicker.with(this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)//Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK){
            assert data != null;
            imageUri = data.getData();

            try {
                binding.profileIVShow.setImageURI(imageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(this, ""+ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        }
    }

    private String name,shopName,deliveryFee,phone,country,state,city,address;
    private boolean shopOpen;
    private void inputData() {
        name = binding.fullNameET.getText().toString().trim();
        shopName = binding.shopET.getText().toString().trim();
        deliveryFee = binding.deliveryET.getText().toString().trim();
        phone = binding.phoneET.getText().toString().trim();
        country = binding.countryET.getText().toString().trim();
        state = binding.stateET.getText().toString().trim();
        city = binding.cityET.getText().toString().trim();
        address = binding.completeAddressET.getText().toString().trim();

        shopOpen = binding.shopOpenSwitch.isChecked();//switch open ache kina? eta check kora jabe ei method er maddhome
        try {
            updateProfile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void updateProfile(){
        progressDialog.setMessage("Updating info...");
        progressDialog.show();

        if (imageUri==null){
            //update without image

            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("fullName",""+name);
            hashMap.put("shopName",""+shopName);
            hashMap.put("phoneNumber",""+phone);
            hashMap.put("deliveryFee",""+deliveryFee);
            hashMap.put("countryName",""+country);
            hashMap.put("state",""+state);
            hashMap.put("city",""+city);
            hashMap.put("address",""+address);
            hashMap.put("latitude",""+latitude);
            hashMap.put("longitude",""+longitude);
            hashMap.put("shopOpen",""+shopOpen);

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
            databaseReference.child(Objects.requireNonNull(auth.getUid())).updateChildren(hashMap).addOnSuccessListener(unused -> {
                progressDialog.dismiss();
                Toast.makeText(EditProfileSellerActivity.this, "Info Updated!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(EditProfileSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        }else {
            //update with image
            String filePathAndName = "profile_images/"+""+auth.getUid();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloadUri = uriTask.getResult();

                if (uriTask.isSuccessful()){
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("fullName",""+name);
                    hashMap.put("shopName",""+shopName);
                    hashMap.put("phoneNumber",""+phone);
                    hashMap.put("deliveryFee",""+deliveryFee);
                    hashMap.put("countryName",""+country);
                    hashMap.put("state",""+state);
                    hashMap.put("city",""+city);
                    hashMap.put("address",""+address);
                    hashMap.put("latitude",""+latitude);
                    hashMap.put("longitude",""+longitude);
                    hashMap.put("shopOpen",""+shopOpen);
                    hashMap.put("profileImage",""+downloadUri);

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
                    databaseReference.child(Objects.requireNonNull(auth.getUid())).updateChildren(hashMap).addOnSuccessListener(unused -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileSellerActivity.this, "Info Updated!", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(EditProfileSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void checkUser() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null){
            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            finish();
        }else {
            try {
                loadMyInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadMyInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.orderByChild("uid").equalTo(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String accountType = ""+dataSnapshot.child("accountType").getValue();
                    String address = ""+dataSnapshot.child("address").getValue();
                    String city = ""+dataSnapshot.child("city").getValue();
                    String state = ""+dataSnapshot.child("state").getValue();
                    String country = ""+dataSnapshot.child("countryName").getValue();
                    String deliveryFee = ""+dataSnapshot.child("deliveryFee").getValue();
                    String email = ""+dataSnapshot.child("email").getValue();
                    latitude = Double.parseDouble(""+dataSnapshot.child("latitude").getValue());
                    longitude = Double.parseDouble(""+dataSnapshot.child("longitude").getValue());
                    String name = ""+dataSnapshot.child("fullName").getValue();
                    String online = ""+dataSnapshot.child("online").getValue();
                    String phone = ""+dataSnapshot.child("phoneNumber").getValue();
                    String profileImage = ""+dataSnapshot.child("profileImage").getValue();
                    String timestamp = ""+dataSnapshot.child("timestamp").getValue();
                    String shopName = ""+dataSnapshot.child("shopName").getValue();
                    String shopOpen = ""+dataSnapshot.child("shopOpen").getValue();
                    String uid = ""+dataSnapshot.child("uid").getValue();

                    binding.fullNameET.setText(name);
                    binding.phoneET.setText(phone);
                    binding.countryET.setText(country);
                    binding.stateET.setText(state);
                    binding.cityET.setText(city);
                    binding.shopET.setText(shopName);
                    binding.completeAddressET.setText(address);
                    binding.deliveryET.setText(deliveryFee);

                    if (shopOpen.equals("true")){
                        try {
                            binding.shopOpenSwitch.setChecked(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else {
                        try {
                            binding.shopOpenSwitch.setChecked(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray).into(binding.profileIVShow);
                    } catch (Exception e) {
                        binding.profileIVShow.setImageResource(R.drawable.ic_person_gray);
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private boolean checkLocationPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    @SuppressLint("MissingPermission")
    private void detectLocation(){
        dialog.show();
        Toast.makeText(this, "Please wait for a while...", Toast.LENGTH_SHORT).show();
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void requestLocationPermission(){
        try {
            ActivityCompat.requestPermissions(this,locationPermissions,LOCATION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            findAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void findAddress(){
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses =  geocoder.getFromLocation(latitude,longitude,1);
            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            binding.completeAddressET.setText(address);
            binding.cityET.setText(city);
            binding.stateET.setText(state);
            binding.countryET.setText(country);

            dialog.dismiss();

        } catch (IOException e) {
            dialog.dismiss();
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        dialog.dismiss();
        Toast.makeText(this, "Location is disabled.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean locationAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted){
                        try {
                            detectLocation();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else {
                        Toast.makeText(this, "Location Permission is required!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }


}