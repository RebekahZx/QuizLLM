package com.example.quizllm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizllm.activities.SigninActivity;
import com.example.quizllm.util.PasswordHasher;

public class MainActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnGoToSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Ensure this layout exists

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToSignup = findViewById(R.id.btnSignup);

        btnLogin.setOnClickListener(v -> {
            String inputEmail = etEmail.getText().toString().trim();
            String inputPassword = etPassword.getText().toString();

            if (inputEmail.isEmpty() || inputPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String savedEmail = sharedPreferences.getString("email", "");
            String savedHash = sharedPreferences.getString("password_hash", null);
            String legacyPassword = sharedPreferences.getString("password", null);

            boolean ok = inputEmail.equals(savedEmail)
                    && (PasswordHasher.verify(inputPassword, savedHash)
                        || (savedHash == null && inputPassword.equals(legacyPassword)));

            if (ok && savedHash == null) {
                // Account created by an older version: replace the plain-text password with a hash.
                sharedPreferences.edit()
                        .putString("password_hash", PasswordHasher.hash(inputPassword))
                        .remove("password")
                        .apply();
            }

            if (ok) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, SelectTopicActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        btnGoToSignup.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SigninActivity.class));
        });
    }
}