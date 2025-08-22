package com.example.customer_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText etId, etPassword;
    private Button btnLogin;

    private SharedPreferences prefs;
    private DatabaseReference buyersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean loggedIn = prefs.getBoolean("isLoggedIn", false);

        // If already logged in → jump to MainActivity
        if (loggedIn) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etId = findViewById(R.id.et_EnterName);
        etPassword = findViewById(R.id.et_EnterPass);
        btnLogin = findViewById(R.id.btn_login);

        // SubAdmin id hardcoded OR pass dynamically
        buyersRef = FirebaseDatabase.getInstance().getReference("SubAdmins")
                .child("abc@123")
                .child("buyers");

        btnLogin.setOnClickListener(v -> {
            String enteredId = etId.getText().toString().trim();
            String enteredPass = etPassword.getText().toString().trim();

            if (enteredId.isEmpty() || enteredPass.isEmpty()) {
                Toast.makeText(this, "Enter ID and Password", Toast.LENGTH_SHORT).show();
                return;
            }

            verifyCredentials(enteredId, enteredPass);
        });
    }

    private void verifyCredentials(String enteredId, String enteredPass) {
        buyersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;

                for (DataSnapshot buyerSnap : snapshot.getChildren()) {
                    String dbBuild = buyerSnap.child("phoneBuild").getValue(String.class);
                    String dbImei1 = buyerSnap.child("imei1").getValue(String.class);

                    if (dbBuild != null && dbImei1 != null &&
                            dbBuild.equals(enteredId) &&
                            dbImei1.equals(enteredPass)) {

                        found = true;

                        // ✅ Save login session
                        prefs.edit()
                                .putBoolean("isLoggedIn", true)
                                .putString("buyerId", buyerSnap.getKey())
                                .apply();

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                        break;
                    }
                }

                if (!found) {
                    Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
