package com.manuni.kretabikreta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.kretabikreta.databinding.ActivityHelpThemBinding;

public class HelpThemActivity extends AppCompatActivity {
    ActivityHelpThemBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHelpThemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        loadAllNumber();


        binding.copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String numberMahmud = binding.mahmudNumber.getText().toString().trim();

                ClipboardManager clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(CLIPBOARD_SERVICE);
                ClipData copyData = ClipData.newPlainText("text", numberMahmud);
                clipboardManager.setPrimaryClip(copyData);
                Toast.makeText(HelpThemActivity.this, "Text Copied!", Toast.LENGTH_SHORT).show();
            }
        });

        binding.copyBtnTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String numberMasud = binding.masudNumber.getText().toString().trim();

                ClipboardManager clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(CLIPBOARD_SERVICE);
                ClipData copyData = ClipData.newPlainText("text", numberMasud);
                clipboardManager.setPrimaryClip(copyData);
                Toast.makeText(HelpThemActivity.this, "Text Copied!", Toast.LENGTH_SHORT).show();
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadAllNumber() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("HelpNumber");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        String mahmudNumb = ""+dataSnapshot.child("mahmudNumber").getValue();
                        String masudNumb = ""+dataSnapshot.child("masudNumber").getValue();

                        binding.mahmudNumber.setText(mahmudNumb);
                        binding.masudNumber.setText(masudNumb);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }
}