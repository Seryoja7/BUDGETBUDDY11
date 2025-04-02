package com.example.budgetbuddy;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {

    private static final String TAG = "Register";
    private TextInputEditText editTextEmail, editTextPassword;
    private Button buttonReg;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private TextView textView;
    private Handler handler;
    private Runnable verificationChecker;
    private FirebaseUser tempUser;
    private boolean isRegistrationInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonReg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);

        // Navigate to Login screen
        textView.setOnClickListener(v -> {
            if (tempUser != null && !tempUser.isEmailVerified()) {
                deleteTempUserAndNavigateToLogin();
            } else {
                navigateToLogin();
            }
        });

        // Handle registration
        buttonReg.setOnClickListener(v -> {
            if (isRegistrationInProgress) {
                return; // Prevent multiple registration attempts
            }

            // Check network connectivity
            if (!isNetworkAvailable()) {
                Toast.makeText(Register.this, "No internet connection. Please check your network.", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // Validate inputs
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Register.this, "Մուտքագրեք էլ. փոստը", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(Register.this, "Անվավեր էլ. փոստ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(Register.this, "Գաղտնաբառը պետք է լինի առնվազն 6 նիշ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Start registration process
            isRegistrationInProgress = true;
            buttonReg.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            // Create user with email and password
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            tempUser = mAuth.getCurrentUser();
                            if (tempUser != null) {
                                sendVerificationEmail(tempUser);
                            } else {
                                Log.e(TAG, "User is null after registration");
                                Toast.makeText(Register.this, "Սխալ: Օգտատերը ստեղծված չէ", Toast.LENGTH_SHORT).show();
                                resetRegistrationState();
                            }
                        } else {
                            Log.e(TAG, "Registration failed: " + task.getException());
                            Toast.makeText(Register.this, "Գրանցում ձախողվեց: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            resetRegistrationState();
                        }
                    });
        });
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(sendTask -> {
                    if (sendTask.isSuccessful()) {
                        Log.d(TAG, "Verification email sent to " + user.getEmail());
                        Toast.makeText(Register.this, "Հաստատման նամակը ուղարկված է " + user.getEmail(), Toast.LENGTH_LONG).show();
                        startVerificationCheck(user);
                    } else {
                        Log.e(TAG, "Failed to send verification email", sendTask.getException());
                        Toast.makeText(Register.this, "Նամակի ուղարկումը ձախողվեց: " + sendTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        resetRegistrationState();
                    }
                });
    }

    private void startVerificationCheck(FirebaseUser user) {
        handler = new Handler();
        verificationChecker = new Runnable() {
            @Override
            public void run() {
                if (!isNetworkAvailable()) {
                    Log.e(TAG, "No network connection during verification check");
                    Toast.makeText(Register.this, "No internet connection. Please check your network.", Toast.LENGTH_SHORT).show();
                    resetRegistrationState();
                    return;
                }

                user.reload()
                        .addOnCompleteListener(reloadTask -> {
                            if (reloadTask.isSuccessful()) {
                                if (user.isEmailVerified()) {
                                    Log.d(TAG, "Email verified successfully");
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(Register.this, "Էլ. փոստը հաստատված է", Toast.LENGTH_SHORT).show();
                                    navigateToLogin();
                                } else {
                                    Log.d(TAG, "Email not yet verified, checking again in 5 seconds");
                                    handler.postDelayed(this, 5000); // Check every 5 seconds
                                }
                            } else {
                                Log.e(TAG, "Failed to reload user", reloadTask.getException());
                                Toast.makeText(Register.this, "Սխալ: " + reloadTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                resetRegistrationState();
                            }
                        });
            }
        };
        handler.post(verificationChecker);
    }

    private void deleteTempUserAndNavigateToLogin() {
        if (tempUser != null) {
            tempUser.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Temporary user deleted");
                            Toast.makeText(Register.this, "Օգտատերը ջնջվեց", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Failed to delete temporary user", task.getException());
                            Toast.makeText(Register.this, "Ջնջումը ձախողվեց", Toast.LENGTH_SHORT).show();
                        }
                        navigateToLogin();
                    });
        } else {
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(Register.this, Login.class);
        startActivity(intent);
        finish();
    }

    private void resetRegistrationState() {
        isRegistrationInProgress = false;
        buttonReg.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && verificationChecker != null) {
            handler.removeCallbacks(verificationChecker); // Stop the verification checker
        }
    }
}