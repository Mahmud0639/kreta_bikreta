package com.manuni.kretabikreta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.manuni.kretabikreta.databinding.ActivityGoogleSignInBinding;

import java.util.HashMap;
import java.util.Objects;

public class GoogleSignInActivity extends AppCompatActivity {
    ActivityGoogleSignInBinding binding;

    private FirebaseAuth auth;
    String email;


    public static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;

    private DatabaseReference reference;

    private String userId,userLatitude,userLongitude,userAddress,userCity,userState,userCountry,userPhone,userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGoogleSignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(GoogleSignInActivity.this,googleSignInOptions);


        auth = FirebaseAuth.getInstance();




        userId = getIntent().getStringExtra("uid");
        userLatitude = getIntent().getStringExtra("latitude");
        userLongitude = getIntent().getStringExtra("longitude");
        userAddress = getIntent().getStringExtra("address");
        userCity = getIntent().getStringExtra("city");
        userState = getIntent().getStringExtra("state");
        userCountry = getIntent().getStringExtra("countryName");
        userPhone = getIntent().getStringExtra("phoneNumber");
        userProfile = getIntent().getStringExtra("profileImage");


        reference = FirebaseDatabase.getInstance().getReference().child("Users");

        binding.googleSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent,RC_SIGN_IN);

            }
        });

        binding.backArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount  account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acc){
        AuthCredential credential = GoogleAuthProvider.getCredential(acc.getIdToken(),null);
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser user = auth.getCurrentUser();
                    assert user != null;
                    email = user.getEmail();
                    String name = user.getDisplayName();

                    if (task.getResult().getAdditionalUserInfo().isNewUser()){
                        // email = user.getEmail();
                        String uid = user.getUid();

                        // String uid = auth.getUid();


                        HashMap<Object,String> hashMap = new HashMap<>();
                        hashMap.put("email",email);
                        hashMap.put("fullName",""+name);
                        hashMap.put("phoneNumber",""+userPhone);
                        hashMap.put("countryName",""+userCountry);
                        hashMap.put("state",""+userState);
                        hashMap.put("city",""+userCity);
                        hashMap.put("address",""+userAddress);
                        hashMap.put("uid",""+uid);
                        hashMap.put("latitude",""+userLatitude);
                        hashMap.put("longitude",""+userLongitude);
                        hashMap.put("accountType","User");
                        hashMap.put("online","true");
                        hashMap.put("timestamp",""+System.currentTimeMillis());
                        hashMap.put("profileImage",""+userProfile);


                        reference.child(Objects.requireNonNull(uid)).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                startActivity(new Intent(GoogleSignInActivity.this, MainUserActivity.class));
                                finish();
                            }
                        });


                    }else {
                        HashMap<Object,String> hashMap = new HashMap<>();
                        hashMap.put("email",email);
                        hashMap.put("fullName",""+name);
                        hashMap.put("phoneNumber",""+userPhone);
                        hashMap.put("countryName",""+userCountry);
                        hashMap.put("state",""+userState);
                        hashMap.put("city",""+userCity);
                        hashMap.put("address",""+userAddress);
                        hashMap.put("uid",""+auth.getUid());
                        hashMap.put("latitude",""+userLatitude);
                        hashMap.put("longitude",""+userLongitude);
                        hashMap.put("accountType","User");
                        hashMap.put("online","true");
                        hashMap.put("timestamp",""+System.currentTimeMillis());
                        hashMap.put("profileImage",""+userProfile);


                        reference.child(Objects.requireNonNull(auth.getUid())).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                startActivity(new Intent(GoogleSignInActivity.this, MainUserActivity.class));
                                finish();
                            }
                        });

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