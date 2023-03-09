package com.manuni.kretabikreta;

import static com.manuni.kretabikreta.Constants.TOPICS;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.kretabikreta.databinding.ActivityShopDetailsBinding;
import com.manuni.kretabikreta.databinding.DialogCartBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopDetailsActivity extends AppCompatActivity {
    ActivityShopDetailsBinding binding;
    private String shopUid;
    private FirebaseAuth auth;
    private String myLatitude, myLongitude, phoneNumber;
    private String shopName, shopEmail, shopPhone, shopAddress, shopLatitude, shopLongitude;
    private ArrayList<ModelProduct> modelProducts;
    private ProductUserAdapter productUserAdapter;
    public String deliveryFee;
    String productQuantity;
    String quantity;

    String[] data;
    ArrayList<String> dataList;

    private String selected;


    AlertDialog dialog;
    int count;

    private EasyDB easyDB;

    private ArrayList<ModelCartItem> modelCartItemsList;
    private AdapterCartItem adapterCartItem;

    private ProgressDialog progressDialog,cateProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            shopUid = getIntent().getStringExtra("shopUid");
        } catch (Exception e) {
            e.printStackTrace();
        }

        auth = FirebaseAuth.getInstance();


        try {
            loadMyInfo();
            loadShopDetails();
            loadShopProducts();
            loadRatings();

            loadToSpinner();
        } catch (Exception e) {
            e.printStackTrace();
        }

        progressDialog = new ProgressDialog(ShopDetailsActivity.this);
        progressDialog.setTitle("Placing an order");
        progressDialog.setCanceledOnTouchOutside(false);


        cateProgressDialog = new ProgressDialog(ShopDetailsActivity.this);
        cateProgressDialog.setMessage("Please wait...");
        cateProgressDialog.setCanceledOnTouchOutside(false);
        cateProgressDialog.setCancelable(false);
        cateProgressDialog.show();




        easyDB = EasyDB.init(ShopDetailsActivity.this,"ITEM_DB_NEW_TWO")
                .setTableName("ITEM_TABLE_NEW_TWO")
                .addColumn(new Column("Items_Id_Two",new String[]{"text","unique"}))
                .addColumn(new Column("Items_PID_Two",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Name_Two",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Each_Price_Two",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Price_Two",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Quantity_Two",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Pro_Quantity_Two",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Pro_Image_Two",new String[]{"text","not null"}))
                .doneTableColumn();

        //each shop have its own products and orders
        try {
            deleteCartData();
            cartCount();
        } catch (Exception e) {
            e.printStackTrace();
        }

        binding.backArrowBtn.setOnClickListener(view -> onBackPressed());

        binding.cartBtn.setOnClickListener(view -> {
            try {
                showCartDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }
            binding.cartCountTV.setVisibility(View.GONE);
        });
        //commit korar new ekta upay paoya gece...seta holo alt+(1 key er purber tilda key mane ~ key)

        binding.callBtn.setOnClickListener(view -> dialPhone());
        binding.mapBtn.setOnClickListener(view -> openMap());

        binding.searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    // productUserAdapter.getFilter().filter(charSequence);
                    if (charSequence.equals("")){
                        loadFilteredProducts(selected);
                    }else {
                        productUserAdapter.getFilter().filter(charSequence);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.filterProductBtn.setOnClickListener(view -> {
            if (dataList.size()==0){
                Toast.makeText(ShopDetailsActivity.this, "No items available!", Toast.LENGTH_SHORT).show();
            }else {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Choose Category").setItems(data, (dialogInterface, i) -> {
                    selected = data[i];
                    binding.filterProductTV.setText(selected);
                    if (selected.equals("All")) {
                        try {
                            loadShopProducts();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //productUserAdapter.getFilter().filter(selected);
                        try {
                            loadFilteredProducts(selected);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).show();
            }

        });

        binding.reviewBtn.setOnClickListener(view -> {
            Intent intent = new Intent(ShopDetailsActivity.this, ShopReviewActivity.class);
            intent.putExtra("shopUid", shopUid);
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    private float ratingSum = 0;

    private void loadRatings() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(shopUid).child("Ratings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ratingSum = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    float rating = 0;//e.g 4.5
                    try {
                        rating = Float.parseFloat("" + dataSnapshot.child("ratings").getValue());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    ratingSum = ratingSum + rating;


                }


                long numberOfReviews = snapshot.getChildrenCount();
                float avgOfReviews = ratingSum / numberOfReviews;

                binding.ratingBar.setRating(avgOfReviews);
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

    }

    private void deleteCartData() {


        try {
            easyDB.deleteAllDataFromTable();//it will delete all data from the cart
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("SetTextI18n")
    public void cartCount() {
        //make it public so we can access it in the adapter
        count = easyDB.getAllData().getCount();
        if (count <= 0) {
            //no items in the db so hide
            binding.cartCountTV.setVisibility(View.GONE);
        } else {
            binding.cartCountTV.setVisibility(View.VISIBLE);
            binding.cartCountTV.setText("" + count);
        }
    }

    public double allTotalPrice = 0.00;
    public TextView subTotalPriceTV, deliFeeTV, allTotalPriceTV;

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void showCartDialog() {

        //init list
        modelCartItemsList = new ArrayList<>();


        DialogCartBinding binding;
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);
        binding = DialogCartBinding.bind(view);

        subTotalPriceTV = view.findViewById(R.id.subTotalTV);
        deliFeeTV = view.findViewById(R.id.deliveryFeeTV);
        allTotalPriceTV = view.findViewById(R.id.totalPriceTV);
        binding.shopNameTV.setText(shopName);

        AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
        builder.setView(view);


        EasyDB easyDB = EasyDB.init(ShopDetailsActivity.this,"ITEM_DB_NEW_TWO")
                .setTableName("ITEM_TABLE_NEW_TWO")
                .addColumn(new Column("Items_Id_Two",new String[]{"text","unique"}))
                .addColumn(new Column("Items_PID_Two",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Name_Two",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Each_Price_Two",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Price_Two",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Quantity_Two",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Pro_Quantity_Two",new String[]{"text","not null"}))
                .addColumn(new Column("Items_Pro_Image_Two",new String[]{"text","not null"}))
                .doneTableColumn();

        //get all data from db
        Cursor result = easyDB.getAllData();
        while (result.moveToNext()) {
            String id = null;
            String pId = null;
            String name = null;
            String price = null;
            String cost = null;
            String proQuantity = null;
            String prImage = null;
            try {
                id = result.getString(1);
                pId = result.getString(2);
                name = result.getString(3);
                price = result.getString(4);
                cost = result.getString(5);
                quantity = result.getString(6);
                proQuantity = result.getString(7);
                prImage = result.getString(8);
            } catch (Exception e) {
                e.printStackTrace();
            }

            assert cost != null;
            allTotalPrice = allTotalPrice + Double.parseDouble(cost);

            ModelCartItem modelCartItem = new ModelCartItem("" + id, "" + pId, "" + name, "" + price, "" + cost, "" + quantity,""+proQuantity,""+prImage);
            try {
                modelCartItemsList.add(modelCartItem);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        adapterCartItem = new AdapterCartItem(ShopDetailsActivity.this, modelCartItemsList);
        binding.cartItemRV.setAdapter(adapterCartItem);


        //binding.productTotalDesc.setText(productQuantity+" এর "+quantity+" টি");
        binding.deliveryFeeTV.setText("৳" + deliveryFee);
        binding.subTotalTV.setText("৳" + String.format("%.2f", allTotalPrice));
        binding.totalPriceTV.setText("৳" + String.format("%.2f",allTotalPrice + Double.parseDouble(deliveryFee.replaceAll("৳", ""))));

        dialog = builder.create();
        dialog.show();

        //reset total price on dialog dismiss
        dialog.setOnCancelListener(dialogInterface -> allTotalPrice = 0.00);

        binding.checkoutBtn.setOnClickListener(view1 -> {

            allTotalPrice = 0.00;


            //first validate location address
            if (myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null")) {
                Toast.makeText(ShopDetailsActivity.this, "Please set your location in your profile.", Toast.LENGTH_SHORT).show();
                return;//don't proceed further
            }
            if (phoneNumber.equals("") || phoneNumber.equals("null")) {
                Toast.makeText(ShopDetailsActivity.this, "Please set your phone in your profile.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (modelCartItemsList.size() == 0) {
                Toast.makeText(ShopDetailsActivity.this, "No items available to place an order", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                submitOrder();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    private void submitOrder() {
        progressDialog.setMessage("Please wait...");
        progressDialog.show();



        String timestamp = "" + System.currentTimeMillis();
        String cost = allTotalPriceTV.getText().toString().replace("৳", "");//if contains $ then replace with ""

        HashMap<String, String> hashMap = new HashMap<>();
        try {
            hashMap.put("orderId", "" + timestamp);
            hashMap.put("orderTime", "" + timestamp);
            hashMap.put("orderStatus", "In Progress");
            hashMap.put("orderCost", "" + cost);
            hashMap.put("orderBy", "" + auth.getUid());
            hashMap.put("orderTo", "" + shopUid);
            hashMap.put("latitude", "" + myLatitude);
            hashMap.put("longitude", "" + myLongitude);
            hashMap.put("deliveryFee", "" + deliveryFee);
            hashMap.put("shopName",""+shopName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(shopUid).child("Orders");
        dbRef.child(timestamp).setValue(hashMap).addOnSuccessListener(unused -> {
            //order info added now add order items
            for (int i = 0; i < modelCartItemsList.size(); i++) {
                String pId = null;
                String cost1 = null;
                String price = null;
                String quantity = null;
                String name = null;
                String productQuantity = null;
                String proImage = null;
                try {
                    pId = modelCartItemsList.get(i).getpId();
                    String id = modelCartItemsList.get(i).getId();
                    cost1 = modelCartItemsList.get(i).getCost();
                    price = modelCartItemsList.get(i).getPrice();
                    quantity = modelCartItemsList.get(i).getQuantity();
                    name = modelCartItemsList.get(i).getName();
                    productQuantity = modelCartItemsList.get(i).getProQuantity();
                    proImage = modelCartItemsList.get(i).getPrImage();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                HashMap<String, String> hashMap1 = new HashMap<>();
                hashMap1.put("pId", pId);
                hashMap1.put("name", name);
                hashMap1.put("cost", cost1);
                hashMap1.put("price", price);
                hashMap1.put("quantity", quantity);
                hashMap1.put("proQuantity",productQuantity);
                hashMap1.put("prImage",proImage);

                assert pId != null;
                dbRef.child(timestamp).child("Items").child(pId).setValue(hashMap1);
                //ei pId ta holo ekta timestamp jeta product ^add korar somoy neya hoyeche (AddProductActivity)--->addProductTodb-->177 no. lines
            }
            progressDialog.dismiss();
            dialog.dismiss();
            //binding.cartCountTV.setVisibility(View.GONE);

            Toast.makeText(ShopDetailsActivity.this, "Product order placed to " + shopName + " successfully!", Toast.LENGTH_SHORT).show();
            try {
                deleteCartData();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Toast.makeText(ShopDetailsActivity.this, ""+modelCartItemsList.size(), Toast.LENGTH_SHORT).show();
            //sending notification after submitting order successfully.
            try {
                prepareNotification(timestamp);
            } catch (Exception e) {
                e.printStackTrace();
            }


            //nicher ei data gulo notification set er pore ekhan theke cut kore neya hoyeche...


        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(ShopDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(shopPhone))));
        Toast.makeText(this, "" + shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void openMap() {
        //saddr means source address
        //daddr means destination address
        String address = "https://maps.google.com/maps?saddr=" + myLatitude + "," + myLongitude + "&daddr=" + shopLatitude + "," + shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMyInfo() {
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference().child("Users");
        dR.orderByChild("uid").equalTo(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String name = "" + dataSnapshot.child("fullName").getValue();
                    String accountType = "" + dataSnapshot.child("accountType").getValue();
                    String email = "" + dataSnapshot.child("email").getValue();
                    phoneNumber = "" + dataSnapshot.child("phoneNumber").getValue();
                    String profileImage = "" + dataSnapshot.child("profileImage").getValue();
                    String city = "" + dataSnapshot.child("city").getValue();
                    myLatitude = "" + dataSnapshot.child("latitude").getValue();
                    myLongitude = "" + dataSnapshot.child("longitude").getValue();

                }
            }

            @Override
            public void onCancelled( DatabaseError error) {

            }
        });
    }

    private void loadShopDetails() {
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().child("Users");
        dbr.child(shopUid).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String fullName = "" + snapshot.child("fullName").getValue();
                shopName = "" + snapshot.child("shopName").getValue();
                shopEmail = "" + snapshot.child("email").getValue();
                shopAddress = "" + snapshot.child("address").getValue();
                shopPhone = "" + snapshot.child("phoneNumber").getValue();
                shopLatitude = "" + snapshot.child("latitude").getValue();
                shopLongitude = "" + snapshot.child("longitude").getValue();
                deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                String profileImage = "" + snapshot.child("profileImage").getValue();
                String shopOpen = "" + snapshot.child("shopOpen").getValue();


                binding.shopNameTV.setText(shopName);
                binding.emailTV.setText(shopEmail);
                binding.addressTV.setText(shopAddress);
                binding.phoneTV.setText(shopPhone);
                binding.deliveryFeeTV.setText("Delivery Fee ৳" + deliveryFee);

                if (shopOpen.equals("true")) {
                    binding.openCloseTV.setText("Open");
                } else {
                    binding.openCloseTV.setText("Closed");
                }

                try {
                    Picasso.get().load(profileImage).into(binding.shopIV);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void loadShopProducts() {

        binding.itemsFoundTV.setVisibility(View.GONE);
        modelProducts = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(shopUid).child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                modelProducts.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    productQuantity = ""+dataSnapshot.child("productQuantity").getValue();
                    ModelProduct data = null;
                    try {
                        data = dataSnapshot.getValue(ModelProduct.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        modelProducts.add(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                productUserAdapter = new ProductUserAdapter(ShopDetailsActivity.this, modelProducts);
                try {
                    binding.productRV.setAdapter(productUserAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void prepareNotification(String orderId) {
        //when user places order, send notification to seller

        //prepare data for notification
        // String NOTIFICATION_TOPIC = "/topics/"+Constants.FCM_TOPIC; //must be same as subscribed by user
        String NOTIFICATION_TITLE = "Order ID " + orderId;
        String NOTIFICATION_MESSAGE = "অভিনন্দন! আপনার একটি নতুন অর্ডার আসছে।";
        String NOTIFICATION_TYPE = "NewOrder";

        String buyerUid = auth.getUid();
        String sellerUid = shopUid;


        try {
            sendFcmNotification(NOTIFICATION_TYPE, buyerUid, sellerUid, orderId, NOTIFICATION_TITLE, NOTIFICATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendFcmNotification(String notificationType, String buyerUid, String sellerUid, String orderId, String notificationTitle, String notificationMessage) {
        PushNotification notification = new PushNotification(new NotificationData(notificationType, buyerUid, sellerUid, orderId, notificationTitle, notificationMessage), TOPICS);
        try {
            sendNotification(notification, orderId);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void sendNotification(PushNotification notification, String orderId) {
        ApiUtilities.getClient().sendNotification(notification).enqueue(new Callback<PushNotification>() {
            @Override
            public void onResponse(Call<PushNotification> call,Response<PushNotification> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                    intent.putExtra("orderTo", shopUid);
                    intent.putExtra("orderId", orderId);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(ShopDetailsActivity.this, "Success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShopDetailsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure( Call<PushNotification> call,Throwable t) {
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", orderId);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(ShopDetailsActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadToSpinner() {


        DatabaseReference myDbRef = FirebaseDatabase.getInstance().getReference().child("Categories");
        myDbRef.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                dataList = new ArrayList<>();
                if (snapshot.exists()){
                    dataList.clear();
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        String categories = null;
                        try {
                            categories = ""+dataSnapshot.child("category").getValue();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            dataList.add(categories);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        dataList.add(0,"All");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    data = dataList.toArray(new String[dataList.size()]);



                }
                cateProgressDialog.dismiss();


                //adapter.notifyDataSetChanged();

            }



            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }
    private void loadFilteredProducts(String selected) {
        binding.itemsFoundTV.setVisibility(View.VISIBLE);
        modelProducts = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.child(shopUid).child("Products").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //before getting data clear the list data
                modelProducts.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){

                    String productCategory = ""+dataSnapshot.child("productCategory").getValue();
                    if (selected.equals(productCategory)){
                        ModelProduct data = null;
                        try {
                            data = dataSnapshot.getValue(ModelProduct.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            modelProducts.add(data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // binding.filterProductTV.setText(list.size()+" items found");
                    }

                }
                binding.itemsFoundTV.setText("("+modelProducts.size()+" items found)");
                productUserAdapter = new ProductUserAdapter(ShopDetailsActivity.this,modelProducts);
                try {
                    binding.productRV.setAdapter(productUserAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }


}