package com.esgipa.smartplayer.ui.authentication;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.esgipa.smartplayer.R;
import com.esgipa.smartplayer.server.Callback;
import com.esgipa.smartplayer.server.NetworkFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SignupActivity extends AppCompatActivity implements Callback<JSONObject> {
    private static final String signUpPath = "auth/signup";
    private EditText name;
    private EditText username;
    private EditText email;
    private EditText password;
    private Button signUpButton;
    private Switch roleSwitch;
    private ProgressBar progressBar;
    private NetworkFragment networkFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        String signUpUrl = getResources().getString(R.string.server_url)+signUpPath;
        Log.i("Sign in", "onCreate: url " + signUpUrl);

        name = findViewById(R.id.name);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        roleSwitch = findViewById(R.id.switch1);
        progressBar = findViewById(R.id.progressBarSignup);
        signUpButton = findViewById(R.id.sign_up_button);
        networkFragment = NetworkFragment.getInstance(getSupportFragmentManager(), signUpUrl);
        // to change the network fragment url
        networkFragment.changeUrl(signUpUrl);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                sendSignUpRequest();
            }
        });
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null) {
            return connectivityManager.getActiveNetworkInfo();
        } else {
            return null;
        }
    }

    @Override
    public void updateUi(JSONObject requestResult) {
        try {
            if(requestResult.has("Error")) {
                Toast.makeText(this, requestResult.getString("Error"), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
            if(requestResult.has("message")) {
                String message = requestResult.getString("message");
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                if (message.equals(getString(R.string.register_success))) {
                    startActivity(new Intent(SignupActivity.this, SigninActivity.class));
                    finish();
                }
                progressBar.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        // we don't need to use this on sign up
    }

    @Override
    public void finishOperation() {
    }

    private void sendSignUpRequest() {
        String nameText = name.getText().toString();
        String usernameText = username.getText().toString();
        String emailText = email.getText().toString();
        String passwordText = password.getText().toString();
        ArrayList<String> role = new ArrayList<>();
        if(roleSwitch.isChecked()) {
            role.add("artist");
        } else {
            role.add("user");
        }
        if(!validName(nameText) || !validUsername(usernameText) || !validEmail(emailText) || !validPassword(passwordText)) {
            return;
        }
        if(networkFragment != null) {
            networkFragment.sendSignupRequest(nameText, usernameText, emailText, passwordText, role);
        }
    }

    private boolean validName(String nameText) {
        if(nameText.isEmpty()) {
            name.setError("This field is required.");
            return false;
        } else {
            return true;
        }
    }

    private boolean validUsername(String usernameText) {
        if(usernameText.isEmpty()) {
            username.setError("This field is required.");
            return false;
        } else {
            return true;
        }
    }

    private boolean validPassword(String passwordText) {
        if(passwordText.isEmpty()) {
            password.setError("This field is required.");
            return false;
        } else if(passwordText.length() < 6) {
            password.setError("The password should be > 6 caracteres.");
            return false;
        } else {
            return true;
        }
    }

    private boolean validEmail(String emailText) {
        if(emailText.isEmpty()) {
            email.setError("This field is required.");
            return false;
        } else if(!emailText.contains("@")) {
            email.setError("This is not an email address.");
            return false;
        } else {
            return true;
        }
    }
}
