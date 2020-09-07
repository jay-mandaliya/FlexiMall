package com.jack.fleximall.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.jack.fleximall.Global;
import com.jack.fleximall.R;
import com.jack.fleximall.Validation;

public class PasswordResetActivity extends AppCompatActivity{

    private Global global;
    private ProgressDialog progress;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        global = (Global)getApplicationContext();

        progress = new ProgressDialog(this);
        progress.setMessage("Please wait...");
        progress.setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("We have sent password-reset link to your email").setTitle("Sent");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setCancelable(false);

        alertDialog = builder.create();
    }

    public void onSend(View view){
        Validation validation = new Validation();
        final EditText email = (EditText)findViewById(R.id.password_reset_edtEmail);

        if (validation.checkEmail(email)){
            progress.show();
            global.getFirebaseAuth().sendPasswordResetEmail(email.getText().toString()).
                    addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    progress.dismiss();
                    if (task.isSuccessful()){
                        alertDialog.show();
                    }
                    else {
                        if (task.getException() instanceof FirebaseAuthInvalidUserException){
                            email.setError("No account found");
                            email.requestFocus();
                        }
                        task.getException().printStackTrace();
                    }
                }
            });
        }
    }
}
