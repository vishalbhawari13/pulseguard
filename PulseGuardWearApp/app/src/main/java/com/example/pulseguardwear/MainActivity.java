package com.example.pulseguardwear;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PulseGuardWear";
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> resultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configure Google Sign-In for Fit API
        configureGoogleSignIn();

        // Handle sign-in activity result
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                    } else {
                        Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
                    }
                });

        // Launch the sign-in process if the user is not already signed in
        if (GoogleSignIn.getLastSignedInAccount(this) == null) {
            resultLauncher.launch(googleSignInClient.getSignInIntent());
        } else {
            Toast.makeText(this, "Already signed in", Toast.LENGTH_SHORT).show();
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (account != null) {
                accessGoogleFitData(account);
            }
        }
    }

    /**
     * Configure Google Sign-In options for accessing Fit API data.
     */
    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/fitness.activity.read"))
                .requestScopes(new com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/fitness.body.read"))
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    /**
     * Handle the Google Sign-In result.
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                accessGoogleFitData(account);
            }
        } catch (ApiException e) {
            Log.e(TAG, "Sign-in failed: " + e.getStatusCode());
            Toast.makeText(this, "Sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Access Google Fit data for step counts.
     */
    private void accessGoogleFitData(@NonNull GoogleSignInAccount account) {
        Fitness.getHistoryClient(this, account)
                .readData(new DataReadRequest.Builder()
                        .read(DataType.TYPE_STEP_COUNT_DELTA)
                        .setTimeRange(1, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .build())
                .addOnSuccessListener(dataReadResponse -> {
                    DataSet stepsData = dataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA);
                    if (stepsData != null && !stepsData.isEmpty()) {
                        for (DataPoint dp : stepsData.getDataPoints()) {
                            int steps = dp.getValue(Field.FIELD_STEPS).asInt();
                            long timestamp = dp.getStartTime(TimeUnit.MILLISECONDS);
                            Log.i(TAG, "Steps: " + steps + ", Time: " + timestamp);
                            Toast.makeText(this, "Steps: " + steps + ", Time: " + timestamp, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.i(TAG, "No step data available.");
                        Toast.makeText(this, "No step data available.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error accessing Google Fit data: ", e);
                    Toast.makeText(this, "Error accessing Google Fit data", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Optional: Sign out when activity is destroyed (depends on app requirement)
        if (googleSignInClient != null) {
            googleSignInClient.signOut().addOnCompleteListener(task -> Log.i(TAG, "Signed out of Google Fit"));
        }
    }
}
