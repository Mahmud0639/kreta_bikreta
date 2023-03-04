package com.manuni.kretabikreta;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.manuni.kretabikreta.databinding.ActivityEditProductBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class EditProductActivity extends AppCompatActivity {
    ActivityEditProductBinding binding;
    private String productId;


    String[] data;
    ArrayList<String> dataList;


    private String[] cameraPermissions;
    private String[] storagePermission;

    private Uri imageUri;
    String productIcon;


    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());




        auth = FirebaseAuth.getInstance();

        try {
            loadToSpinner();

            productId = getIntent().getStringExtra("productId");
            loadProductDetails();
        } catch (Exception e) {
            e.printStackTrace();
        }


        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);


        binding.discountSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                // binding.discountPriceET.setVisibility(View.VISIBLE);
                try {
                    binding.discountNoteET.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // binding.discountPriceET.setVisibility(View.GONE);
                try {
                    binding.discountNoteET.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });



        binding.addPhoto.setOnClickListener(view -> showImagePickDialog());
        binding.categoryTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryDialog();
            }
        });

        binding.updateProductBtn.setOnClickListener(view -> inputData());
        binding.backArrowBtn.setOnClickListener(view -> onBackPressed());

    }

    private void loadProductDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(Objects.requireNonNull(auth.getUid())).child("Products").child(productId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String productId = "" + snapshot.child("productId").getValue();
                String productTitle = "" + snapshot.child("productTitle").getValue();
                String productDesc = "" + snapshot.child("productDesc").getValue();
                String productCategory = "" + snapshot.child("productCategory").getValue();
                String productQuantity = "" + snapshot.child("productQuantity").getValue();
                productIcon = "" + snapshot.child("productIcon").getValue();
                String productOriginalPrice = "" + snapshot.child("productOriginalPrice").getValue();
                String productDiscountPrice = "" + snapshot.child("productDiscountPrice").getValue();
                String productDiscountNote = "" + snapshot.child("productDiscountNote").getValue();
                String productDiscountAvailable = "" + snapshot.child("productDiscountAvailable").getValue();
                String timestamp = "" + snapshot.child("timestamp").getValue();
                String uid = "" + snapshot.child("uid").getValue();
                String productAvailability = ""+snapshot.child("productAvailable").getValue();


                if (productDiscountAvailable.equals("true")) {
                    try {
                        binding.discountSwitch.setChecked(true);
                        // binding.discountPriceET.setVisibility(View.VISIBLE);
                        binding.discountNoteET.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        binding.discountSwitch.setChecked(false);
                        // binding.discountPriceET.setVisibility(View.GONE);
                        binding.discountNoteET.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (productAvailability.equals("true")){
                    try {
                        binding.productAvailableSwitch.setChecked(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    try {
                        binding.productAvailableSwitch.setChecked(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


                binding.titleET.setText(productTitle);
                binding.descriptionET.setText(productDesc);
                // binding.discountPriceET.setText(productDiscountPrice);
                binding.discountNoteET.setText(productDiscountNote);
                binding.categoryTV.setText(productCategory);
                binding.priceET.setText(productOriginalPrice);
                binding.quantityET.setText(productQuantity);



                try {
                    Picasso.get().load(productIcon).placeholder(R.drawable.impl2).into(binding.productIconIVShow);
                } catch (Exception e) {
                    Picasso.get().load(R.drawable.impl2).into(binding.productIconIVShow);
                }


            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EditProductActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private String productTitle, productDescription, productCategory, productQuantity, originalPrice, discountPrice, discountNote;
    private boolean discountAvailable = false;
    private double discountNoteSum=0.0;
    private boolean productAvailable = false;
    private String productAvailableSwitch;

    private void inputData() {
        productTitle = binding.titleET.getText().toString().trim();
        productDescription = binding.descriptionET.getText().toString().trim();
        productCategory = binding.categoryTV.getText().toString().trim();
        productQuantity = binding.quantityET.getText().toString().trim();
        originalPrice = binding.priceET.getText().toString().trim();

        discountAvailable = binding.discountSwitch.isChecked();//true or false...jodi check thake tahole discountAvailable false theke true te update hoye jabe

        productAvailable = binding.productAvailableSwitch.isChecked();


        if (discountAvailable) {


            //ekhane discountAvailable hocche true



            //  discountPrice = binding.discountPriceET.getText().toString().trim();
            discountNote = binding.discountNoteET.getText().toString().trim();

            double myDiscountNote = 0.0;
            // int myDiscountNote = Integer.parseInt(discountNote);
            if (!discountNote.equals("")){
                myDiscountNote = Double.parseDouble(discountNote);
            }

            if (TextUtils.isEmpty(discountNote)) {
                Toast.makeText(this, "Discount Note required!", Toast.LENGTH_SHORT).show();
                return;
            }
            else if (myDiscountNote==0 || myDiscountNote<0 || myDiscountNote >100){
                Toast.makeText(this, "Invalid Discount", Toast.LENGTH_SHORT).show();
                return;
            }
//
            else{
                double disNote = 0;
                double oriPrice = 0;
                try {
                    disNote = Double.parseDouble(discountNote);
                    oriPrice = Double.parseDouble(originalPrice);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                double afterDiscount = disNote * oriPrice/100;

                discountNoteSum = oriPrice - afterDiscount;
            }




//            if (TextUtils.isEmpty(discountPrice)) {
//                Toast.makeText(this, "Discount price required!", Toast.LENGTH_SHORT).show();
//                return;
//            }



        } else {
            //switchAvailable = true;
            // discountPrice = "0";
            discountNote = "";
            discountNoteSum = 0.0;


        }

        if (productAvailable){
            productAvailableSwitch = "true";
        }else {
            productAvailableSwitch = "false";
        }




        if (TextUtils.isEmpty(productTitle)) {
            Toast.makeText(this, "Title required!", Toast.LENGTH_SHORT).show();
            return;//don't proceed further
        } else if (TextUtils.isEmpty(productDescription)) {
            Toast.makeText(this, "Description required!", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(productCategory)) {
            Toast.makeText(this, "Select a category!", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(productQuantity)) {
            Toast.makeText(this, "Put quantity!", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(originalPrice)) {
            Toast.makeText(this, "Original Price required!", Toast.LENGTH_SHORT).show();
            return;
        }  else {
            updateProductToDb();
        }



    }

    private void updateProductToDb() {
        progressDialog.setMessage("Updating product...");
        progressDialog.show();

        if (imageUri == null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("productTitle", "" + productTitle);
            hashMap.put("productDesc", "" + productDescription);
            hashMap.put("productCategory", "" + productCategory);
            hashMap.put("productQuantity", "" + productQuantity);
            hashMap.put("productOriginalPrice", "" + originalPrice);
            hashMap.put("productDiscountPrice", "" + discountNoteSum);
            hashMap.put("productDiscountNote",""+discountNote);
            hashMap.put("productDiscountAvailable", "" + discountAvailable);
            hashMap.put("timestamp",""+productId);
            hashMap.put("uid",""+auth.getUid());
            hashMap.put("productIcon",""+productIcon);
            hashMap.put("productId",""+productId);
            hashMap.put("productAvailable",""+productAvailableSwitch);


            //update to database
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
            databaseReference.child(Objects.requireNonNull(auth.getUid())).child("Products").child(productId).updateChildren(hashMap).addOnSuccessListener(unused -> {
                progressDialog.dismiss();
                Toast.makeText(EditProductActivity.this, "Updated Successfully!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(EditProductActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

            });
        } else {
            String pathAndName = "Product_Images/" + "" + productId;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(pathAndName);
            storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                Uri downloadUri = uriTask.getResult();
                if (uriTask.isSuccessful()) {

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("productTitle", "" + productTitle);
                    hashMap.put("productDesc", "" + productDescription);
                    hashMap.put("productCategory", "" + productCategory);
                    hashMap.put("productQuantity", "" + productQuantity);
                    hashMap.put("productOriginalPrice", "" + originalPrice);
                    hashMap.put("productDiscountPrice", "" + discountNoteSum);
                    hashMap.put("productIcon", "" + downloadUri);
                    hashMap.put("productDiscountNote",""+discountNote);
                    hashMap.put("productDiscountAvailable", "" + discountAvailable);
                    hashMap.put("timestamp",""+productId);
                    hashMap.put("uid",""+auth.getUid());
                    hashMap.put("productId",""+productId);
                    hashMap.put("productAvailable",""+productAvailableSwitch);

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
                    reference.child(Objects.requireNonNull(auth.getUid())).child("Products").child(productId).setValue(hashMap).addOnSuccessListener(unused -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditProductActivity.this, "Product added successfully!", Toast.LENGTH_SHORT).show();
                        clearData();
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditProductActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }


            });
        }
    }

    private void clearData() {
        binding.titleET.setText("");
        binding.descriptionET.setText("");
        binding.categoryTV.setText("");
        binding.quantityET.setText("");
        binding.priceET.setText("");
        //binding.discountPriceET.setText("");
        binding.discountNoteET.setText("");
        binding.productIconIVShow.setImageResource(R.drawable.impl2);
        imageUri = null;
    }


    private void categoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProductActivity.this);
        builder.setTitle("Product Category").setItems(data, (dialogInterface, i) -> {
            String category = data[i];//ekhane kono category select kora hole seta ei variable er moddhe chole ashbe
            binding.categoryTV.setText(category);
        }).show();
    }

    private void showImagePickDialog(){
        ImagePicker.with(this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)//Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            assert data != null;
            imageUri = data.getData();

            try {
                binding.productIconIVShow.setImageURI(imageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(this, ""+ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadToSpinner() {


        DatabaseReference myDbRef = FirebaseDatabase.getInstance().getReference().child("Categories");
        myDbRef.child(Objects.requireNonNull(auth.getUid())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                dataList = new ArrayList<>();
                if (snapshot.exists()){
                    dataList.clear();
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        String categories = ""+dataSnapshot.child("category").getValue();
                        dataList.add(categories);
                    }
                    dataList.add(0,"All");
                    data = dataList.toArray(new String[dataList.size()]);



                }
                progressDialog.dismiss();


                //adapter.notifyDataSetChanged();

            }



            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }


}