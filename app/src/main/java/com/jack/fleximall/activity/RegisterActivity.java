package com.jack.fleximall.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.jack.fleximall.Global;
import com.jack.fleximall.R;
import com.jack.fleximall.UserProfile;
import com.jack.fleximall.Validation;

public class RegisterActivity extends AppCompatActivity {

    private Global global;
    ProgressDialog dialog;
    private ConnectivityManager cm;
    private TextView status;
    private AlertDialog alertDialog;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        global = (Global)getApplicationContext();
        cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        status = (TextView)findViewById(R.id.register_txtNetStatus);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);

        builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setCancelable(false);
    }

    public void onSignUp(View view){

        Validation validation = new Validation();

        final EditText name = (EditText)findViewById(R.id.register_edtName);
        final EditText email = (EditText)findViewById(R.id.register_edtEmail);
        final EditText phone = (EditText)findViewById(R.id.register_edtMobile);
        EditText pass = (EditText)findViewById(R.id.register_edtPass);
        EditText cnfPass = (EditText)findViewById(R.id.register_edtCnfPass);

        if(validation.checkName(name) && validation.checkEmail(email) && validation.checkPhone(phone)
                && validation.checkPassword(pass) && validation.checkConfirmPassword(pass,cnfPass) &&
                checkNetwork()){

            dialog.show();
            global.getFirebaseAuth().createUserWithEmailAndPassword(email.getText().toString(),
                    pass.getText().toString()).
                    addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){
                                UserProfile userProfile = new UserProfile(name.getText().toString(),
                                        phone.getText().toString());
                                createUser(task.getResult().getUser(),userProfile);
                            }
                            else{
                                dialog.dismiss();

                                if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                    email.setError("Email already in use");
                                    email.requestFocus();
                                }
                                else {
                                    Log.w("create user: ",task.getException());
                                    Toast.makeText(getApplicationContext(),"Something went Wrong" +
                                            "..Try Again!",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        }
    }

    private void createUser(FirebaseUser user, UserProfile userProfile){

        global.getProfileRef().child(user.getUid()).setValue(userProfile);

        user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                dialog.dismiss();

                if (task.isSuccessful()){
                    builder.setMessage("We have sent you email verification link to your email, please verify your account")
                            .setTitle("Sent");
                    alertDialog = builder.create();
                    alertDialog.show();
                }
                else {
                    builder.setMessage("There seems some problem while sending email verification link " +
                            "but you can try again from user profile section after login to our app")
                            .setTitle("We are Sorry");
                    alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });
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
