package com.jack.fleximall;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jack.fleximall.activity.DatabaseHelper;

public class Global extends Application {

    int scanRequestCode = 3030;

    private DatabaseHelper databaseHelper;

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public void setDatabaseHelper(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public FirebaseAuth getFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }


    public FirebaseUser getFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }


    public DatabaseReference getProfileRef() {
        return FirebaseDatabase.getInstance().getReference(getString(R.string.profile_table_name));
    }

    public DatabaseReference getProductRef(){
        return FirebaseDatabase.getInstance().getReference(getString(R.string.product_table_name));
    }

    public int getScanRequestCode() {
        return scanRequestCode;
    }
}
