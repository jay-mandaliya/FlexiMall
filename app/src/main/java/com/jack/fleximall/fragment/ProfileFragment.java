package com.jack.fleximall.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.jack.fleximall.activity.MainActivity;
import com.jack.fleximall.Global;
import com.jack.fleximall.R;
import com.jack.fleximall.UserProfile;
import com.jack.fleximall.Validation;

import org.w3c.dom.Text;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private Global global;
    private UserProfile currentUser;

    private EditText name;
    private EditText phone;
    private TextView email;

    private ProgressDialog progress;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        global = (Global)getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        progress = new ProgressDialog(getContext());
        progress.setMessage("Please wait...");
        progress.setCancelable(false);

        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        name = (EditText)view.findViewById(R.id.profile_frag_edtName);
        phone = (EditText)view.findViewById(R.id.profile_frag_edtMobile);
        email = (TextView)view.findViewById(R.id.profile_frag_txtEmail);

        email.setTextSize(name.getTextSize()/getContext().getResources().getDisplayMetrics().scaledDensity);

        global.getProfileRef().child(global.getFirebaseUser().getUid()).addListenerForSingleValueEvent
                (new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(UserProfile.class);
                if (currentUser!=null) {
                    name.setText(currentUser.getName());
                    email.setText(global.getFirebaseUser().getEmail());
                    phone.setText(currentUser.getMobile());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        TextView delete = (TextView)view.findViewById(R.id.profile_frag_txtDelete);
        delete.setOnClickListener(this);
        TextView reset = (TextView)view.findViewById(R.id.profile_frag_txtReset);
        reset.setOnClickListener(this);
        Button button = (Button)view.findViewById(R.id.profile_frag_btnSave);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.profile_frag_txtDelete:
                onDelete();
                break;

            case  R.id.profile_frag_txtReset:
                onReset();
                break;

            case R.id.profile_frag_btnSave:
                onSave();
                break;
        }
    }

    private void onSave(){
        Validation validation = new Validation();

        if (validation.checkName(name) && validation.checkPhone(phone)){
            currentUser.setName(name.getText().toString());
            currentUser.setMobile(phone.getText().toString());

            global.getProfileRef().child(global.getFirebaseUser().getUid()).setValue(currentUser);
        }
    }

    private void onReset(){
        progress.show();

        global.getFirebaseAuth().sendPasswordResetEmail(global.getFirebaseUser().getEmail()).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        AlertDialog alertDialog;
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });

                        progress.dismiss();

                        if (task.isSuccessful()){
                            builder.setMessage("We have sent password-reset link to your email").setTitle("Sent");
                            alertDialog = builder.create();
                            alertDialog.show();
                        }
                        else {
                            task.getException().printStackTrace();
                            builder.setMessage("Something went wrong, try after some time").setTitle("Error");
                            alertDialog = builder.create();
                            alertDialog.show();
                        }
                    }
                });
    }

    private void onDelete(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Are you sure to delete your Account?").setTitle("Delete Account");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                progress.show();

                global.getFirebaseUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progress.dismiss();

                        if (task.isSuccessful()){
                            global.getDatabaseHelper().dropAll();
                            Intent intent = new Intent(getActivity(),MainActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        }
                        else {
                            Log.w("delete user ",task.getException());
                            Toast.makeText(getContext(),"Something went wrong",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Context context = getContext();
                Window view = ((AlertDialog)dialogInterface).getWindow();

//                view.setBackgroundDrawableResource(R.color.colorPrompt);
                Button negButton = ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_NEGATIVE);
//                negButton.setBackgroundColor(context.getResources().getColor(R.color.colorPromptButton));
//                negButton.setTextColor(context.getResources().getColor(R.color.colorPromptButtonText));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0,0,30,0);
                negButton.setLayoutParams(params);
            }
        });

        alertDialog.show();
    }
}
