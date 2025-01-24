package com.example.pulseguardwear;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PulseGuardWear";

    private ActivityResultLauncher<Intent> resultLauncher;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.server_client_id))
                .requestScopes(
                        new com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/fitness.activity.read"),
                        new com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/fitness.body.read"))
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(result.getData()));
                    } else {
                        Log.e(TAG, "Sign-in failed or cancelled");
                        Toast.makeText(this, "Sign-in failed or cancelled", Toast.LENGTH_SHORT).show();
                    }
                });

        // Check if user is already signed in
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            signIn();
        } else {
            Log.i(TAG, "Already signed in. Accessing Google Fit data.");
            accessGoogleFitData(account);
        }
    }

    /**
     * Launch the Google Sign-In flow.
     */
    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        resultLauncher.launch(signInIntent);
    }

    /**
     * Handle the result of Google Sign-In.
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                Log.i(TAG, "Sign-in successful! Account: " + account.getEmail());
                Toast.makeText(this, "Welcome, " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
                accessGoogleFitData(account);
            }
        } catch (ApiException e) {
            Log.e(TAG, "Sign-in failed with status code: " + e.getStatusCode(), e);
            handleSignInError(e);
        }
    }

    /**
     * Handle specific sign-in errors.
     */
    private void handleSignInError(ApiException e) {
        String userMessage = switch (e.getStatusCode()) {
            case CommonStatusCodes.NETWORK_ERROR -> "Network error. Please check your internet connection.";
            case CommonStatusCodes.SIGN_IN_REQUIRED -> "Sign-in required. Please try again.";
            case CommonStatusCodes.TIMEOUT -> "Request timed out. Please try again.";
            default -> "An unknown error occurred. Code: " + e.getStatusCode();
        };
        Toast.makeText(this, userMessage, Toast.LENGTH_SHORT).show();
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
                    if (stepsData.isEmpty()) {
                        Log.i(TAG, "No steps data available.");
                        Toast.makeText(this, "No steps data available.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (DataPoint dp : stepsData.getDataPoints()) {
                        int steps = dp.getValue(Field.FIELD_STEPS).asInt();
                        long timestamp = dp.getStartTime(TimeUnit.MILLISECONDS);
                        Log.i(TAG, "Steps: " + steps + ", Time: " + timestamp);
                        Toast.makeText(this, "Steps: " + steps, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error accessing Google Fit data: ", e);
                    Toast.makeText(this, "Error accessing Google Fit data", Toast.LENGTH_SHORT).show();
                });
    }
}
