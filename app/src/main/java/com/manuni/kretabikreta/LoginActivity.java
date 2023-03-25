package com.manuni.kretabikreta;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.kretabikreta.databinding.ActivityLoginBinding;
import com.manuni.kretabikreta.databinding.DialogSigninBinding;

import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private ProgressDialog dialog;

    private DatabaseReference dbRef;

    public static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this,googleSignInOptions);



        dbRef = FirebaseDatabase.getInstance().getReference().child("Users");


        dialog = new ProgressDialog(LoginActivity.this);
        auth = FirebaseAuth.getInstance();


        binding.googleSignInBtn.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {

                DialogSigninBinding dialogSigninBinding = DialogSigninBinding.inflate(LayoutInflater.from(getApplicationContext()));
                AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this)
                        .setView(dialogSigninBinding.getRoot())
                        .create();

               // alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
                //alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

                dialogSigninBinding.userLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.setMessage("Please wait...");
                        dialog.setCancelable(false);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                        startActivityForResult(signInIntent,RC_SIGN_IN);
                    }
                });

                dialogSigninBinding.sellerLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(LoginActivity.this,SellerLoginActivity.class));
                    }
                });

                if (alertDialog.getWindow() != null){
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }

                try {
                    alertDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

    }


    private String email,password;
    private void login(){
        dialog.setTitle("Please wait");
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setMessage("Logging in...");

        dialog.show();

        auth.signInWithEmailAndPassword(email,password).addOnSuccessListener(authResult -> {
            dialog.dismiss();
            makeMeOnline();
        }).addOnFailureListener(e -> {
            dialog.dismiss();

        });






    }

    private void makeMeOnline() {
        dialog.setMessage("Checking user...");
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("online","true");

        dbRef.child(Objects.requireNonNull(auth.getUid())).updateChildren(hashMap).addOnSuccessListener(unused -> checkUserType()).addOnFailureListener(e -> {

        });

    }
    private void checkUserType(){
        dbRef.orderByChild("uid").equalTo(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String accountType = null;
                    try {
                        accountType = ""+dataSnapshot.child("accountType").getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    assert accountType != null;
                    if (accountType.equals("Seller")){
                        DatabaseReference myReference = FirebaseDatabase.getInstance().getReference().child("Users");

                        myReference.child(Objects.requireNonNull(auth.getUid())).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    String status = null;
                                    try {
                                        status = ""+snapshot.child("accountStatus").getValue();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    assert status != null;
                                    if (status.equals("blocked")){
                                        HashMap<String,Object> hashMap = new HashMap<>();
                                        hashMap.put("online","false");

                                        dbRef.child(auth.getUid()).updateChildren(hashMap).addOnSuccessListener(unused -> {
                                            //checkUserType();
                                            Toast.makeText(LoginActivity.this, "You are now in offline", Toast.LENGTH_SHORT).show();

                                        }).addOnFailureListener(e -> {

                                        });
                                        Toast.makeText(LoginActivity.this, "You are blocked.Please contact with your admin.", Toast.LENGTH_SHORT).show();
                                        try {
                                            startActivity(new Intent(LoginActivity.this,TermsConditionActivity.class));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        finish();

                                    }else {
                                        String shopCat = ""+snapshot.child("shopCategory").getValue();

                                        if (shopCat.equals("false")){
                                            HashMap<String,Object> hashMap = new HashMap<>();
                                            hashMap.put("online","true");
                                            DatabaseReference myRR = FirebaseDatabase.getInstance().getReference().child("Users");
                                            myRR.child(Objects.requireNonNull(auth.getUid())).updateChildren(hashMap);
                                            try {
                                                startActivity(new Intent(LoginActivity.this,TermsConditionActivity.class));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }


                                        }else {

                                            try {
                                                startActivity(new Intent(LoginActivity.this,MainSellerActivity.class));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            finish();


                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {

                            }
                        });

                    }else {
                        dialog.dismiss();
                        try {
                            startActivity(new Intent(LoginActivity.this,MainUserActivity.class));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                dialog.dismiss();
                Toast.makeText(this, "Not possible to reach your destination. It can be connection issue.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }


    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acc){
        dialog.dismiss();
        AuthCredential credential = GoogleAuthProvider.getCredential(acc.getIdToken(),null);
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser user = auth.getCurrentUser();


                    if (task.getResult().getAdditionalUserInfo().isNewUser()){
                        String email = user.getEmail();
                        String uid = user.getUid();


                        startActivity(new Intent(LoginActivity.this,RegisterUserActivity.class));

                    }else {
                        makeMeOnline();
                        startActivity(new Intent(LoginActivity.this, MainUserActivity.class));
                        finish();
                    }



                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {

            }
        });
    }


}