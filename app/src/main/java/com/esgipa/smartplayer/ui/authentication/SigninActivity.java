package com.esgipa.smartplayer.ui.authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.android.jwt.JWT;
import com.esgipa.smartplayer.MainActivity;
import com.esgipa.smartplayer.R;
import com.esgipa.smartplayer.data.model.User;
import com.esgipa.smartplayer.utils.UserProfileManager;
import com.esgipa.smartplayer.server.Callback;
import com.esgipa.smartplayer.server.NetworkFragment;

import org.json.JSONException;
import org.json.JSONObject;


public class SigninActivity extends AppCompatActivity implements Callback<JSONObject> {
    private static final String signInUrl = "http://192.168.0.14:8082/auth/signin";

    private EditText usernameOrEmail;
    private EditText password;
    private TextView signUpText;
    private Button signInButton;

    private NetworkFragment networkFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        usernameOrEmail = findViewById(R.id.username);
        password = findViewById(R.id.password);
        signUpText = findViewById(R.id.sign_up_link);
        signInButton = findViewById(R.id.sign_in_button);
        networkFragment = NetworkFragment.getInstance(getSupportFragmentManager(), signInUrl);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSignInRequest();
            }
        });

        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSignUpForm();
            }
        });
    }

    @Override
    public void updateUi(JSONObject requestResult) {
        try {
            if(requestResult.has("Error")) {
                Toast.makeText(this, requestResult.getString("Error"), Toast.LENGTH_SHORT).show();
            }
            if (requestResult.has("token")) {
                JWT jwt = new JWT(requestResult.getString("token"));
                User user = new User(requestResult.getString("token"),
                        jwt.getClaim("name").asString(),
                        jwt.getClaim("username").asString(),
                        jwt.getClaim("email").asString(),
                        true);
                UserProfileManager.saveUserInfo(SigninActivity.this, user);
                startActivity(new Intent(SigninActivity.this, MainActivity.class));
                finish();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        // we don't need to use this on sign in
    }

    @Override
    public void finishOperation() {

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

    private void openSignUpForm() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    private void sendSignInRequest() {
        String username = usernameOrEmail.getText().toString();
        String userPassword = password.getText().toString();
        if(username.isEmpty()) {
            usernameOrEmail.setError("This field is required.");
            return;
        }
        if(userPassword.isEmpty()) {
            password.setError("This field is required.");
            return;
        }
        if(networkFragment != null) {
            networkFragment.sendSigninRequest(username, userPassword);
        }
    }


}