package com.manuni.kretabikreta;

import static com.manuni.kretabikreta.Constants.TOPICS;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.manuni.kretabikreta.databinding.ActivityOrderDetailsSellerBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;

public class OrderDetailsSellerActivity extends AppCompatActivity {
    ActivityOrderDetailsSellerBinding binding;
    private String orderId,orderBy,orderTo;

    private  String orderCost,orderStatus,deliveryFee,latitude,longitude,orderTime;

    private FirebaseAuth auth;
    private String sourceLatitude,sourceLongitude;
    private String sourceBuyerLatitude, sourceBuyerLongitude;

    private ArrayList<ModelOrderedItems> modelOrderedItems;
    private AdapterOrderedItems adapterOrderedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailsSellerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            orderId = getIntent().getStringExtra("orderId");
            orderBy = getIntent().getStringExtra("orderBy");
            orderTo = getIntent().getStringExtra("orderTo");
        } catch (Exception e) {
            e.printStackTrace();
        }

        auth = FirebaseAuth.getInstance();

        FirebaseMessaging.getInstance().subscribeToTopic(TOPICS);

        try {
            loadMyInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }


        binding.backArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMap();
            }
        });

        binding.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editOrderedStatusDialog();
            }
        });

        binding.buyerPhoneTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String bPhone = binding.buyerPhoneTV.getText().toString().trim();
                try {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(bPhone))));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(OrderDetailsSellerActivity.this, "" + bPhone, Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void editOrderedStatusDialog() {
        final String[] options = {"In Progress","Completed","Cancelled"};
        AlertDialog.Builder builder = new AlertDialog.Builder(OrderDetailsSellerActivity.this);
        builder.setTitle("Select to change status").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String selectedOption = options[i];
                changeOrderStatus(selectedOption);
            }
        }).show();

    }

    private void changeOrderStatus(String setSelectedOption) {
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus",""+setSelectedOption);

        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dRef.child(Objects.requireNonNull(auth.getUid())).child("Orders").child(orderId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                String message = "Order is now in "+setSelectedOption;
                Toast.makeText(OrderDetailsSellerActivity.this, message, Toast.LENGTH_SHORT).show();

                try {
                    prepareNotification(orderId,""+message);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(OrderDetailsSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrderDetails() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dbRef.child(Objects.requireNonNull(auth.getUid())).child("Orders").child(orderId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange( DataSnapshot snapshot) {
                if (snapshot.exists()){
                    try {
                        deliveryFee = ""+snapshot.child("deliveryFee").getValue();
                        latitude = ""+snapshot.child("latitude").getValue();
                        longitude = ""+snapshot.child("longitude").getValue();
                        orderBy = ""+snapshot.child("orderBy").getValue();
                        orderCost = ""+snapshot.child("orderCost").getValue();
                        orderId = ""+snapshot.child("orderId").getValue();
                        orderStatus = ""+snapshot.child("orderStatus").getValue();
                        orderTime = ""+snapshot.child("orderTime").getValue();
                        orderTo = ""+snapshot.child("orderTo").getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //covert timestamp time to proper time
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(Long.parseLong(orderTime));
                    String dateTime = DateFormat.format("dd/MM/yy hh:mm aa",calendar).toString();

                    binding.orderDateTV.setText(dateTime);

                    switch (orderStatus) {
                        case "In Progress":
                            binding.orderStatusTV.setTextColor(getResources().getColor(R.color.background_theme));
                            break;
                        case "Completed":
                            binding.orderStatusTV.setTextColor(getResources().getColor(R.color.colorGreen));
                            break;
                        case "Cancelled":
                            binding.orderStatusTV.setTextColor(getResources().getColor(R.color.colorRed));
                            break;
                    }

                    binding.orderIdTV.setText(orderId);
                    binding.orderStatusTV.setText(orderStatus);
                    binding.amountTV.setText("৳"+orderCost+"[Including delivery fee ৳"+deliveryFee+"]");



                }


                //set status color





                findAddress(latitude,longitude);
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void findAddress(String myLatitude, String myLongitude) {
        double lat = 0;
        double lon = 0;
        try {
            lat = Double.parseDouble(myLatitude);
            lon = Double.parseDouble(myLongitude);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        Geocoder geocoder;

        List<Address> addressList;

        geocoder = new Geocoder(OrderDetailsSellerActivity.this, Locale.getDefault());

        try {
            addressList = geocoder.getFromLocation(lat,lon,1);

            String address = addressList.get(0).getAddressLine(0);

            binding.deliveryAddressTV.setText(address);

        } catch (IOException e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void loadOrderedItems(){
        modelOrderedItems = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.child(Objects.requireNonNull(auth.getUid())).child("Orders").child(orderId).child("Items").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()){
                    modelOrderedItems.clear();
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        ModelOrderedItems items = null;
                        try {
                            items = dataSnapshot.getValue(ModelOrderedItems.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            modelOrderedItems.add(items);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    adapterOrderedItems = new AdapterOrderedItems(OrderDetailsSellerActivity.this,modelOrderedItems);
                    try {
                        binding.orderedItemsRV.setAdapter(adapterOrderedItems);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    binding.itemsTV.setText(""+snapshot.getChildrenCount());
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void openMap(){
        //saddr means source address
        //daddr means destination address
        String address = "https://maps.google.com/maps?saddr=" +sourceLatitude+","+sourceLongitude+"&daddr=" + sourceBuyerLatitude+","+sourceBuyerLongitude;//ekhane bola hocce kotha hote kothay map dekhabe
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMyInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.child(Objects.requireNonNull(auth.getUid())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    sourceLatitude = ""+snapshot.child("latitude").getValue();
                    sourceLongitude = ""+snapshot.child("longitude").getValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    loadOrderDetails();
                    loadOrderedItems();
                    loadBuyerInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

    } private void loadBuyerInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.child(orderBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()){
                    try {
                        sourceBuyerLatitude = ""+snapshot.child("latitude").getValue();
                        sourceBuyerLongitude = ""+snapshot.child("longitude").getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String email = null;
                    String phone = null;
                    try {
                        email = ""+snapshot.child("email").getValue();
                        phone = ""+snapshot.child("phoneNumber").getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    binding.buyerEmailTV.setText(email);
                    binding.buyerPhoneTV.setText(phone);
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

    }
    private void prepareNotification(String orderId,String message){
        //when user seller changes order status In Progress/Completed/Cancelled, send notification to buyer

        //prepare data for notification
        //String NOTIFICATION_TOPIC = "/topics/"+Constants.FCM_TOPIC; //must be same as subscribed by user
        String NOTIFICATION_TITLE = "Your Order "+orderId;
        String NOTIFICATION_MESSAGE = ""+message;
        String NOTIFICATION_TYPE = "OrderStatusChanged";

//        //prepare json(what to send and where to send)
//        JSONObject notificationJO = new JSONObject();
//        JSONObject notificationBodyJO = new JSONObject();
//
//        try {
//            //what to send
//            notificationBodyJO.put("notificationType",NOTIFICATION_TYPE);
//            notificationBodyJO.put("buyerUid",orderBy);
//            notificationBodyJO.put("sellerUid",auth.getUid());//we are logged in as seller so current id is seller user id that is auth.getUid();
//            notificationBodyJO.put("orderId",orderId);
//            notificationBodyJO.put("notificationTitle",NOTIFICATION_TITLE);
//            notificationBodyJO.put("notificationMessage",NOTIFICATION_MESSAGE);
//            //where to send
//            //notificationJO.put("to",NOTIFICATION_TOPIC);//to all who subscribe this topic
//            notificationJO.put("data",notificationBodyJO);
//
//        }catch (Exception e){
//            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//
//        }


        try {
            sendFcmNotification(NOTIFICATION_TYPE, orderBy, auth.getUid(), orderId, NOTIFICATION_TITLE, NOTIFICATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    private void sendFcmNotification(JSONObject notificationJO) {
//
////        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJO, new Response.Listener<JSONObject>() {
////            @Override
////            public void onResponse(JSONObject response) {
////                //sent notification
////                Log.e("TAG", ""+response );
////                Toast.makeText(OrderDetailsSellerActivity.this, "Notification sent successfully!", Toast.LENGTH_SHORT).show();
////
////            }
////        }, new Response.ErrorListener() {
////            @Override
////            public void onErrorResponse(VolleyError error) {
////                //failed to send notification
////                Log.e("TAG", ""+error.getMessage() );
////                Toast.makeText(OrderDetailsSellerActivity.this, "Failed to send notification", Toast.LENGTH_SHORT).show();
////
////            }
////        }){
////            @Override
////            public Map<String, String> getHeaders() throws AuthFailureError {
////                Map<String,String> headers = new HashMap<>();
////                headers.put("Content-Type","application/json");
////               // headers.put("Authorization","key="+Constants.FCM_KEY);
////                return headers;
////            }
////        };
////        Volley.newRequestQueue(this).add(jsonObjectRequest);
//    }
    private void sendFcmNotification(String notificationType, String buyerUid, String sellerUid, String orderId, String notificationTitle, String notificationMessage) {
        PushNotification notification = new PushNotification(new NotificationData(notificationType, buyerUid, sellerUid, orderId, notificationTitle, notificationMessage), TOPICS);
        sendNotification(notification, orderId);
    }

    private void sendNotification(PushNotification notification, String orderId) {
        ApiUtilities.getClient().sendNotification(notification).enqueue(new Callback<PushNotification>() {
            @Override
            public void onResponse(Call<PushNotification> call,retrofit2.Response<PushNotification> response) {
                if (response.isSuccessful()) {

                    Toast.makeText(OrderDetailsSellerActivity.this, "Success", Toast.LENGTH_SHORT).show();

//                    Intent totalCostIntent = new Intent(OrderDetailsSellerActivity.this,TotalCostActivity.class);
//                    totalCostIntent.putExtra("orderToSeller",orderTo);
//                    try {
//                        startActivity(totalCostIntent);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }


                } else {
                    Toast.makeText(OrderDetailsSellerActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PushNotification> call,Throwable t) {

                Toast.makeText(OrderDetailsSellerActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}