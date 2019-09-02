package com.RDI.androidstudyfirebasetaxiapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DriverSignInActivity extends AppCompatActivity {

    private static final String TAG = "DriverSignInActivity";
    private TextInputLayout textInputEmail;
    private TextInputLayout textInputName;
    private TextInputLayout textInputPassword;
    private TextInputLayout textInputConfirmPassword;

    private Button loginSignUnButton;
    private TextView toggleLoginSignUpTextView;

    private boolean isLoginModeActive;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivger_sign_in);
        auth = FirebaseAuth.getInstance();
        if (auth != null) {
            startActivity(new Intent(DriverSignInActivity.this, DriverMapsActivity.class));
        }
        textInputEmail = findViewById(R.id.textInputEmail);
        textInputName = findViewById(R.id.textInputName);
        textInputPassword = findViewById(R.id.textInputPassword);
        textInputConfirmPassword = findViewById(R.id.textInputConfirmPassword);
        loginSignUnButton = findViewById(R.id.loginSignUnButton);
        toggleLoginSignUpTextView = findViewById(R.id.toggleLoginSignUpTextView);
    }

    private boolean validateEmail() {
        String emailInput = textInputEmail.getEditText().getText().toString().trim();
        if (emailInput.isEmpty()) {
            textInputEmail.setError("Пожалуйста введите ваш email");
            return false;
        } else {
            textInputEmail.setError("");
            return true;
        }
    }

    private boolean validateName() {
        String nameInput = textInputName.getEditText().getText().toString().trim();
        if (nameInput.isEmpty()) {
            textInputName.setError("Пожалуйста введите ваше имя");
            return false;
        } else if (nameInput.length() > 15) {
            textInputName.setError("Длинна имени не должна превышать 15 символов");
            return false;
        } else {
            textInputName.setError("");
            return true;
        }
    }


    private boolean validatePassword() {
        String passwordInput = textInputPassword.getEditText().getText().toString().trim();
        if (passwordInput.isEmpty()) {
            textInputPassword.setError("Пожалуйста введите пароль");
            return false;
        } else if (passwordInput.length() < 7) {
            textInputPassword.setError("Пароль должен быть больше 6 символов");
            return false;
        } else {
            textInputPassword.setError("");
            return true;
        }
    }

    private boolean validateConfirmPassword() {
        String passwordInput = textInputPassword.getEditText().getText().toString().trim();
        String confermPasswordInput = textInputConfirmPassword.getEditText().getText().toString().trim();

        if (!passwordInput.equals(confermPasswordInput)) {
            textInputConfirmPassword.setError("Пароли должны совпвдать");
            return false;
        } else {
            textInputPassword.setError("");
            return true;
        }
    }

    public void loginSignUpUser(View view) {
        String email = textInputEmail.getEditText().getText().toString().trim();
        String password = textInputPassword.getEditText().getText().toString().trim();

        if (isLoginModeActive) {
            if (!validateEmail() | !validateName() | !validatePassword()) {
                return;
            }
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                startActivity(new Intent(DriverSignInActivity.this, DriverMapsActivity.class));

                                FirebaseUser user = auth.getCurrentUser();
                                // updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(DriverSignInActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                //  updateUI(null);
                            }

                            // ...
                        }
                    });
        } else {
            if (!validateEmail() | !validateName() | !validatePassword() | !validateConfirmPassword()) {
                return;
            }
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                startActivity(new Intent(DriverSignInActivity.this, DriverMapsActivity.class));
                                FirebaseUser user = auth.getCurrentUser();
                                //  updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(DriverSignInActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                // updateUI(null);
                            }

                            // ...
                        }
                    });
        }
    }

    public void togglLoginSignUpUser(View view) {
        if (isLoginModeActive) {
            isLoginModeActive = false;
            loginSignUnButton.setText("Зарегестрироваться");
            toggleLoginSignUpTextView.setText("Войти?");
            textInputConfirmPassword.setVisibility(View.VISIBLE);
        } else {
            isLoginModeActive = true;
            loginSignUnButton.setText("Войти");
            toggleLoginSignUpTextView.setText("Зарагестрироваться");
            textInputConfirmPassword.setVisibility(View.GONE);
        }
    }
}
