package com.example.weteams;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.Fade;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weteams.logic.Callbacks;
import com.example.weteams.logic.UserSignIn;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignInActivity extends AppCompatActivity {
    private final int SIGN_UP_ID = 1, SIGN_IN_ID = 0;
    private int activeScene;


    private EditText editEmail;
    private EditText editPassword;
    private EditText editUsername;
    private EditText editRePassword;
    private Button btnLeft;
    private Button btnRight;

    private Scene sceneSignUp, sceneSignIn;
    private ViewGroup container;
    private Transition transition;
    private TextWatcher textWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        container = findViewById(R.id.signInLayout);

        sceneSignIn = Scene.getSceneForLayout(container, R.layout.activity_signin, this);
        sceneSignUp = Scene.getSceneForLayout(container, R.layout.activity_signup, this);

        transition = new Fade();

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtons(activeScene);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        prepareView(SIGN_IN_ID);
    }

    private void prepareView(int id) {
        switch (id) {
            case SIGN_IN_ID:
                //Set up scene for Sign In
                editEmail = sceneSignIn.getSceneRoot().findViewById(R.id.editEmail);
                editPassword = sceneSignIn.getSceneRoot().findViewById(R.id.editPassword);
                btnLeft = sceneSignIn.getSceneRoot().findViewById(R.id.btnLeft);
                btnRight = sceneSignIn.getSceneRoot().findViewById(R.id.btnRight);

                editEmail.addTextChangedListener(textWatcher);
                editPassword.addTextChangedListener(textWatcher);

                btnLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String email = editEmail.getText().toString();
                        String password = editPassword.getText().toString();
                        UserSignIn.processSignIn(email, password, new Callbacks<FirebaseUser>() {
                            @Override
                            public void onSuccess(FirebaseUser value) {
                                Log.wtf("SIGN IN", "Sign in success");
                                startMainActivity();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                if (e instanceof FirebaseAuthInvalidUserException)
                                    Toast.makeText(SignInActivity.this, "Username is incorrect\nPlease try again", Toast.LENGTH_LONG).show();
                                else if (e instanceof FirebaseAuthInvalidCredentialsException)
                                    Toast.makeText(SignInActivity.this, "Password is incorrect\nPlease try again", Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(SignInActivity.this, "Failed to sign in\nPlease check your internet connection", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });

                btnRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TransitionManager.go(sceneSignUp, transition);
                        prepareView(SIGN_UP_ID);
                    }
                });

                updateButtons(SIGN_IN_ID);
                activeScene = SIGN_IN_ID;
                break;

            case SIGN_UP_ID:
                //Set up scene for Sign Up
                editEmail = sceneSignUp.getSceneRoot().findViewById(R.id.editEmail);
                editPassword = sceneSignUp.getSceneRoot().findViewById(R.id.editPassword);
                editUsername = sceneSignUp.getSceneRoot().findViewById(R.id.editUsername);
                editRePassword = sceneSignUp.getSceneRoot().findViewById(R.id.editRePassword);
                btnLeft = sceneSignUp.getSceneRoot().findViewById(R.id.btnLeft);
                btnRight = sceneSignUp.getSceneRoot().findViewById(R.id.btnRight);

                editEmail.addTextChangedListener(textWatcher);
                editPassword.addTextChangedListener(textWatcher);
                editUsername.addTextChangedListener(textWatcher);
                editRePassword.addTextChangedListener(textWatcher);

                btnLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TransitionManager.go(sceneSignIn, transition);
                        prepareView(SIGN_IN_ID);
                    }
                });

                btnRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String email = editEmail.getText().toString();
                        String password = editPassword.getText().toString();
                        String username = editUsername.getText().toString();
                        String rePassword = editRePassword.getText().toString();

                        if (password.equals(rePassword))
                            UserSignIn.processSignUp(email, password, username, new Callbacks<FirebaseUser>() {
                                @Override
                                public void onSuccess(FirebaseUser value) {
                                    startMainActivity();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    if (e instanceof FirebaseAuthUserCollisionException)
                                        Toast.makeText(SignInActivity.this, "User already exists!", Toast.LENGTH_LONG).show();
                                    else if (e instanceof FirebaseAuthWeakPasswordException)
                                        Toast.makeText(SignInActivity.this, "Password needs to be at least 6 characters!", Toast.LENGTH_LONG).show();
                                    else if (e instanceof FirebaseAuthInvalidCredentialsException)
                                        Toast.makeText(SignInActivity.this, "Wrong email address format!", Toast.LENGTH_LONG).show();
                                    else
                                        Toast.makeText(SignInActivity.this, "Failed to sign up\nPlease check your internet connection", Toast.LENGTH_LONG).show();
                                }
                            });
                        else
                            Toast.makeText(SignInActivity.this, "The passwords do not match!", Toast.LENGTH_LONG).show();
                    }
                });

                updateButtons(SIGN_UP_ID);
                activeScene = SIGN_UP_ID;
                break;
        }
    }

    public void updateButtons(int id) {
        boolean hasEmail, hasPassword, hasUsername;
        switch (id) {
            case SIGN_UP_ID:
                hasEmail = !TextUtils.isEmpty(editEmail.getText());
                hasPassword = !TextUtils.isEmpty(editPassword.getText()) && !TextUtils.isEmpty(editRePassword.getText());
                hasUsername = !TextUtils.isEmpty(editUsername.getText());
                btnRight.setEnabled(hasEmail && hasPassword && hasUsername);
                break;
            case SIGN_IN_ID:
                hasEmail = !TextUtils.isEmpty(editEmail.getText());
                hasPassword = !TextUtils.isEmpty(editPassword.getText());
                btnLeft.setEnabled(hasEmail && hasPassword);
                break;
        }
    }


    public void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
