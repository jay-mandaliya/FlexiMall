package com.jack.fleximall.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jack.fleximall.Item;
import com.jack.fleximall.R;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("bill_info/"+FirebaseAuth.getInstance().getUid());

        Date currentTime = Calendar.getInstance().getTime();
        DatabaseReference usersRef = ref.child(currentTime.toString());

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        Cursor cursor = databaseHelper.getAllItems();

        Map<String, Item> items = new HashMap<>();
        int flag = 1;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    items.put("Item "+ String.valueOf(flag), new Item(
                            cursor.getString(cursor.getColumnIndex(getResources().getString(R.string.column_barcode))),
                            cursor.getString(cursor.getColumnIndex(getResources().getString(R.string.column_name))),
                            cursor.getString(cursor.getColumnIndex(getResources().getString(R.string.column_price))),
                            cursor.getString(cursor.getColumnIndex(getResources().getString(R.string.column_quantity)))));

                    flag++;
                } while (cursor.moveToNext());
            }
        }

        usersRef.setValue(items);

        TextView status_txt = (TextView)findViewById(R.id.checkout_status);
        status_txt.setText("Your order has been placed\n you will be notified soon");
    }
}
