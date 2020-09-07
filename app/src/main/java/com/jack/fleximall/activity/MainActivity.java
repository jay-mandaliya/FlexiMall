package com.jack.fleximall.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.jack.fleximall.Global;
import com.jack.fleximall.R;
import com.jack.fleximall.Validation;

public class MainActivity extends AppCompatActivity {

    private Global global;
    private ProgressDialog dialog;
    private ConnectivityManager cm;
    private TextView status;

    @Override
    protected void onStart() {
        super.onStart();

        if (global.getFirebaseUser()!=null){
            Intent intent = new Intent(this,PanelActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        global = (Global)getApplicationContext();

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        global.setDatabaseHelper(databaseHelper);

        cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        status = (TextView)findViewById(R.id.main_txtNetStatus);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
    }

    public void onSignIn(View view){

        Validation validation = new Validation();

        final EditText email = (EditText)findViewById(R.id.main_edtEmail);
        final EditText pass = (EditText)findViewById(R.id.main_edtPass);

        if (validation.checkEmail(email) && validation.checkPassword(pass)){

            if (checkNetwork()){
                dialog.show();

                try {
                    global.getFirebaseAuth().signInWithEmailAndPassword(email.getText().toString(),
                            pass.getText().toString()).addOnCompleteListener(this,
                            new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    dialog.dismiss();

                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(getApplicationContext(), PanelActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        if (task.getException() instanceof FirebaseNetworkException){
                                            Toast.makeText(getApplicationContext(),"Server not reachable",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                        else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                            email.setError("No account found");
                                            email.requestFocus();
                                        }
                                        else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                            pass.setError("Wrong password");
                                            pass.requestFocus();
                                        }
                                        task.getException().printStackTrace();
                                    }
                                }
                            });
                }
                catch (Exception e){
                    dialog.dismiss();
                    Toast.makeText(this,"Someting went wrong!\nTry re-installing application",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }
    }

    public void onReset(View view){
        Intent intent = new Intent(this,PasswordResetActivity.class);
        startActivity(intent);
    }

    public void onRegister(View view){
        Intent intent = new Intent(this,RegisterActivity.class);
        startActivity(intent);
    }

    private boolean checkNetwork(){
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        if (isConnected){

            if (status.getVisibility() == View.VISIBLE){
                Animation animation;
                animation = AnimationUtils.loadAnimation(this,R.anim.fadeout);
                status.startAnimation(animation);
                status.setVisibility(View.INVISIBLE);
            }
        }
        else {
            Animation animation;
            animation = AnimationUtils.loadAnimation(this,R.anim.fadein);
            status.startAnimation(animation);
            status.setVisibility(View.VISIBLE);
        }
        return isConnected;
    }
}
